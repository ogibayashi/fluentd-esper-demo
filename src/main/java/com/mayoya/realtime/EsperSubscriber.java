package com.mayoya.realtime;

import org.zeromq.ZMQ;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPRuntime;
import org.msgpack.template.Template;
import java.net.Socket;
import com.espertech.esper.client.EPServiceProviderManager;
import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;
import java.util.List;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;
import static org.msgpack.template.Templates.TString;
import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.tMap;
import static org.msgpack.template.Templates.tList;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPAdministrator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.io.IOException;
import org.msgpack.template.Templates;
import java.util.Properties;


public class EsperSubscriber {
    EPServiceProvider cep;
    EPRuntime cepRT;
    EPAdministrator cepAdm;
    MessagePack msgpack;
    Template<Map<String, String>> mapTmpl;
    Template<List<Value>> listTmpl;
    ZMQ.Context context;
    ZMQ.Socket subscriber;
    EPLServer svr;

    public EsperSubscriber(String[] filters, Properties conf){

        cep = EPServiceProviderManager.getDefaultProvider();
        cepRT = cep.getEPRuntime();
	cepAdm  = cep.getEPAdministrator();


	msgpack = new MessagePack();
	mapTmpl = tMap(TString, TString);
	listTmpl = tList(TValue);

	context = ZMQ.context(1);
	subscriber = context.socket(ZMQ.SUB);
	subscriber.connect("tcp://localhost:5556");
	for(String filter : filters){
	    subscriber.subscribe(filter.getBytes());
	}

	svr = new EPLServer(cepAdm,conf);

    }

    public void start() throws IOException{
        byte[] msgByte;

	svr.start();
	while(true){
	    if ((msgByte = subscriber.recv(ZMQ.NOBLOCK)) != null) {
		Unpacker unpacker = msgpack.createBufferUnpacker(getMessage(msgByte));
		List<Value> valueList = unpacker.read(listTmpl);
		String tag = valueList.get(0).asRawValue().getString();
		String eventName = tag.replace(".","_");
		Value record = valueList.get(2);
		if(record.isMapValue()){
		    Map<String, String> m = new Converter(record).read(mapTmpl);
		    m.put("tag",tag);
		    try{
			cepRT.sendEvent(m,eventName);
		    } catch (EPException e){
			createSchemaFromMap(m, eventName, cepAdm);
		    }
		}
	    }
	    try {
		Thread.sleep(100);
	    }catch(Exception e){
		e.printStackTrace();
	    }
	}
	
    }


    public byte[] getMessage(byte[] received){
	int i = 0;
	while (received[i] != 0x20){
	    i++;
	}
	int msglen = received.length - i - 1;
	byte[] ret = new byte[msglen];
	System.arraycopy(received, i+1, ret, 0, msglen);
	return ret;
    }

    public void createSchemaFromMap(Map m, String event, EPAdministrator cepAdm){
	Map<String, Object> typeMap = new HashMap<String, Object>();

	for(String key: (Set<String>)m.keySet()){
	    typeMap.put(key, String.class);
	}
	cepAdm.getConfiguration().addEventType(event, typeMap);
	System.out.println("New event defined: " + event);
    }

    public static void main(String[] args) throws IOException{
    }
}
