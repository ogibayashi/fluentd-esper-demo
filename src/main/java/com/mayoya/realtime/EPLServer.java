package com.mayoya.realtime;

import org.msgpack.rpc.loop.EventLoop;
import org.msgpack.rpc.Server;
import org.msgpack.rpc.Request;
import com.espertech.esper.client.EPAdministrator;
import java.util.Properties;

public class EPLServer extends Thread {

    protected EPAdministrator cepAdm;
    private Properties conf;

    public  EPLServer(EPAdministrator adm, Properties conf){
	this.cepAdm = adm;
	this.conf = conf;
    }


    public void run() {
	try {
	    EventLoop loop = EventLoop.defaultEventLoop();
	    Server svr = new Server(loop);
	    svr.serve(new EPLServerHandler(cepAdm,conf));
	    svr.listen(1985);
	    loop.join();
	}
	catch (Exception e){
	    e.printStackTrace();
	}
    }
	
}
