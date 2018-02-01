package com.m2r.botrading.ws;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.m2r.botrading.strategy.IIntention;

import rx.Subscription;
import rx.functions.Action1;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class IntentionClient {

	private WampClient client;
	private String url;
	private Class<? extends IIntention> classI;
	private Action1<IIntention> action;
    private Subscription eventSubscription;
    
	public static IntentionClient build(String url, Class<? extends IIntention> classI, Action1<IIntention> action) {
		return new IntentionClient(url, classI, action);
	}
	
	private IntentionClient(String url, Class<? extends IIntention> classI, Action1<IIntention> action) {
		this.url = url;
		this.classI = classI;
		this.action = action;
	}
	
	public IntentionClient start() {
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        try {
            builder.withConnectorProvider(connectorProvider)
                   .withUri(this.url)
                   .withRealm("realm1")
                   .withInfiniteReconnects()
                   .withReconnectInterval(3, TimeUnit.SECONDS);
            client = builder.build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        client.statusChanged().subscribe(new Action1<WampClient.State>() {
            @Override
            public void call(WampClient.State t1) {
                System.out.println("State: " + t1);
                if (t1 instanceof WampClient.ConnectedState) {
                    eventSubscription = client.makeSubscription("test.event", String.class).subscribe((json)->{
                    	IIntention intention = new Gson().fromJson(json, classI); 
                    	action.call(intention);
                    });
                }
            }
        });

        client.open();        
        
		return this;
	}
	
	public void close() {
		if (eventSubscription != null) {
			eventSubscription.unsubscribe();
		}
       	client.close().toBlocking().last();
		System.out.println("Client closed");
	}
	
}
