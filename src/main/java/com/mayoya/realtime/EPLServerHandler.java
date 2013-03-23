package com.mayoya.realtime;

import com.espertech.esper.client.EPAdministrator;
import org.msgpack.rpc.Request;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventType;
import com.espertech.esper.client.StatementAwareUpdateListener;
import com.espertech.esper.client.EPServiceProvider;
import java.util.Properties;
import org.fluentd.logger.FluentLogger;
import java.util.Map;

public class EPLServerHandler {
    private EPAdministrator cepAdm;
    private Properties conf;
    private final String FLUENT_DEFAULT_TAG="view";
    private final String FLUENT_DEFAULT_HOST="localhost";
    private final String FLUENT_DEFAULT_PORT="24224";

    public class CEPListener implements StatementAwareUpdateListener {
	private FluentLogger LOG = null;
	
	public CEPListener() {
	    String fluent_host = conf.getProperty("realtime.fluent.host",FLUENT_DEFAULT_HOST);
	    String fluent_port = conf.getProperty("realtime.fluent.port",FLUENT_DEFAULT_PORT);
	    String tag_prefix = conf.getProperty("realtime.fluent.tag_prefix", FLUENT_DEFAULT_TAG);

	    if((fluent_host != null)  && (fluent_port != null) && (LOG == null)){
		LOG = FluentLogger.getLogger(tag_prefix,fluent_host, Integer.parseInt(fluent_port));
	    }
	}
	
        public void update(EventBean[] newData, EventBean[] oldData,  EPStatement statement, EPServiceProvider epServiceProvider) {
	    if(LOG == null){
		System.out.println("------------" + statement.getName() + "-------");
		for (EventBean e: newData){
		    System.out.println("Event received: " + e.getUnderlying());
		}
	    }
	    else {
		for (EventBean e: newData){
		    LOG.log(statement.getName(), (Map<String,Object>)e.getUnderlying());
		}
	    } 
		
        }
    }

    public EPLServerHandler(EPAdministrator cepAdm, Properties conf) {
	this.cepAdm = cepAdm;
	this.conf = conf;
    }

    public String getStatementList(){
	String ret = "";
	for(String st : cepAdm.getStatementNames()){
	    EPStatement statement = cepAdm.getStatement(st);
	    ret = ret + statement.getName() + ":" + statement.getText() + "\n";
	}
	return ret;
    }

    public void removestatement(Request request, String statementId){
	EPStatement statement = cepAdm.getStatement(statementId);
	System.out.println("Remove statment " + statementId + ": " + statement.getText());
	statement.destroy();
	request.sendResult("Removed");
    }

    public void liststatements(Request request){
	request.sendResult(getStatementList());
    }

    public void listschema (Request request) {
	String retStr = "";
	for(EventType e : cepAdm.getConfiguration().getEventTypes()){
	    retStr = retStr + e.getName() + "\n";
	}
	request.sendResult(retStr);
    }

    public void createschema(Request request, String eplString){
	EPStatement statement = cepAdm.createEPL(eplString);
	request.sendResult("schema created: " + statement.getName());
    }

    public void create(Request request, String query, String name){
	EPStatement statement = cepAdm.createEPL(query,name);
	statement.addListener(new CEPListener());
	request.sendResult("created EPL: " + statement.getName());
    }

}
