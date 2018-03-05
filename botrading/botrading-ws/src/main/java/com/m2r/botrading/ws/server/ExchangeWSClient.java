package com.m2r.botrading.ws.server;

import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.m2r.botrading.ws.Intention;

import rx.Subscription;
import rx.functions.Action1;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class ExchangeWSClient {

	private WampClient client;
	private String url;
	private Action1<Object> ticker;
	private Action1<Object> bought;
	private Action1<Object> sold;
	private Action1<Object> canceled;
	
    private Subscription tickerSubscription;
    private Subscription boughtSubscription;
    private Subscription soldSubscription;
    private Subscription canceledSubscription;
    
	public static ExchangeWSClient build(String url, Action1<Object> ticker, Action1<Object> bought, Action1<Object> sold, Action1<Object> canceled) {
		return new ExchangeWSClient(url, ticker, bought, sold, canceled);
	}
	
	private ExchangeWSClient(String url, Action1<Object> action, Action1<Object> bought, Action1<Object> sold, Action1<Object> canceled) {
		this.url = url;
		this.ticker = action;
		this.bought = bought;
		this.sold = sold;
		this.canceled = canceled;
	}
	
	public ExchangeWSClient start() {
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
                if (t1 instanceof WampClient.ConnectedState) {
                    tickerSubscription = client.makeSubscription(ExchangeTopicEnum.TICKER.name().toLowerCase(), String.class).subscribe((json)->{
                    	Intention intention = new Gson().fromJson(json, Intention.class);
                    	ticker.call(intention);
                    });
                    boughtSubscription = client.makeSubscription(ExchangeTopicEnum.BOUGHT.name().toLowerCase(), String.class).subscribe((json)->{
                    	Intention intention = new Gson().fromJson(json, Intention.class);
                    	bought.call(intention);
                    });
                    soldSubscription = client.makeSubscription(ExchangeTopicEnum.SOLD.name().toLowerCase(), String.class).subscribe((json)->{
                    	Intention intention = new Gson().fromJson(json, Intention.class);
                    	sold.call(intention);
                    });
                    canceledSubscription = client.makeSubscription(ExchangeTopicEnum.CANCELED.name().toLowerCase(), String.class).subscribe((json)->{
                    	Intention intention = new Gson().fromJson(json, Intention.class);
                    	canceled.call(intention);
                    });
                }
            }
        });

        client.open();        
        
		return this;
	}
	
	public void close() {
		if (tickerSubscription != null) {
			tickerSubscription.unsubscribe();
		}
		if (boughtSubscription != null) {
			boughtSubscription.unsubscribe();
		}
		if (soldSubscription != null) {
			soldSubscription.unsubscribe();
		}
		if (canceledSubscription != null) {
			canceledSubscription.unsubscribe();
		}
       	client.close().toBlocking().last();
		System.out.println("Client closed");
	}
	
	/**
	 * Exchange Actions
	 */
	
	public void buy() {
	}
	
	public void sell() {
	}
	
	public void cancel() {
	}
	
}
