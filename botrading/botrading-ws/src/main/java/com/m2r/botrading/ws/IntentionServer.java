package com.m2r.botrading.ws;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.m2r.botrading.strategy.IIntention;

import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampRouter;
import ws.wamp.jawampa.WampRouterBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;
import ws.wamp.jawampa.transport.netty.SimpleWampWebsocketListener;

public class IntentionServer {

	private int port;
	private WampRouter router;
	private SimpleWampWebsocketListener server;
	private WampClient client;
    
	public static IntentionServer build(int port) {
		return new IntentionServer(port);
	}
	
	private IntentionServer(int port) {
		this.port = port;
	}
	
	public IntentionServer start() {
        WampRouterBuilder routerBuilder = new WampRouterBuilder();
        try {
            routerBuilder.addRealm("realm1");
            router = routerBuilder.build();
        } catch (ApplicationError e1) {
            e1.printStackTrace();
        }
        
        URI serverUri = URI.create("ws://0.0.0.0:"+this.port+"/ws1");
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        try {
            server = new SimpleWampWebsocketListener(router, serverUri, null);
            server.start();
            builder.withConnectorProvider(connectorProvider)
                   .withUri("ws://localhost:"+this.port+"/ws1")
                   .withRealm("realm1")
                   .withInfiniteReconnects()
                   .withReconnectInterval(3, TimeUnit.SECONDS);
            client = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        client.open();    
        
        System.out.println("Server started!");
        return this;
	}
	
	public void publish(IIntention intention) {
        client.publish("test.event", new Gson().toJson(intention));
	}
	
	public void close() {
		router.close().toBlocking().last();
		server.stop();
       	client.close().toBlocking().last();
		System.out.println("Server closed");		
	}
	
}
