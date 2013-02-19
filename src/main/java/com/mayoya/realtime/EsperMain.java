package com.mayoya.realtime;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class EsperMain {

    public static void main(String[] args){
	Properties conf = new Properties();

	try {
	    InputStream is = EsperMain.class.getResourceAsStream("/espersubscriber.properties");
	    conf.load(is);
	    is.close();
	}catch (IOException e){
	    System.out.println("espersubscriber.properties not found");
	    System.exit(1);
	}

	EsperSubscriber sub = new EsperSubscriber(args,conf);
	try {
	    sub.start();
	}
	catch (IOException e){
	    e.printStackTrace();
	}
    }
}