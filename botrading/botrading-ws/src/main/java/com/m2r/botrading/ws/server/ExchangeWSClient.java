package com.m2r.botrading.ws.server;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.util.JsonException;
import com.m2r.botrading.api.util.JsonSuccess;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class ExchangeWSClient {

    private final static Logger LOG = Logger.getLogger(ExchangeWSClient.class.getSimpleName());
    
	private WampClient client;
	private String url;
	private boolean connected;
	private Action1<String> ticker;
	private Action1<String> liquided;
	private Action1<String> canceled;
	
    private Subscription tickerSubscription;
    private Subscription boughtSubscription;
    private Subscription soldSubscription;
    private Subscription canceledSubscription;
    
	public static ExchangeWSClient build(String url, Action1<String> ticker, Action1<String> liquided, Action1<String> canceled) {
		return new ExchangeWSClient(url, ticker, liquided, canceled);
	}
	
	private ExchangeWSClient(String url, Action1<String> action, Action1<String> liquided, Action1<String> canceled) {
		this.connected = false;
		this.url = url;
		this.ticker = action;
		this.liquided = liquided;
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
                    tickerSubscription = client.makeSubscription(ExchangeTopicEnum.TICKER.getId(), String.class).subscribe((json)->{
                    	ticker.call(json);
                    });
                    boughtSubscription = client.makeSubscription(ExchangeTopicEnum.LIQUIDED.getId(), String.class).subscribe((json)->{
                    	liquided.call(json);
                    });
                    canceledSubscription = client.makeSubscription(ExchangeTopicEnum.CANCELED.getId(), String.class).subscribe((json)->{
                    	canceled.call(json);
                    });
                    connected = true;
                }
            }
        });

        client.open();        
        
		return this;
	}

	public boolean isConnected() {
		return connected;
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
		LOG.log(Level.INFO, "Client closed");
	}
	
	/**
	 * Exchange Actions
	 */
	
	public void init(Set<String> initialOrderNumbers) throws ExchangeException {
		String json = new Gson().toJson(initialOrderNumbers);
		SyncCall.of(client).call(ExchangeTopicEnum.INIT, JsonSuccess.class, json);
		LOG.log(Level.INFO, "Init success ");
	}
	
	public String buy(String currencyPair, String price, String amount) throws ExchangeException {
		JsonSuccess result = SyncCall.of(client).call(ExchangeTopicEnum.BUY, JsonSuccess.class, currencyPair, price, amount);
		LOG.log(Level.INFO, "Buy ("+result.getOrderNumber()+") ordered in the exchange");
		return result.getOrderNumber();
	}
	
	public String sell(String currencyPair, String price, String amount) throws ExchangeException {
		JsonSuccess result = SyncCall.of(client).call(ExchangeTopicEnum.SELL, JsonSuccess.class, currencyPair, price, amount);
		LOG.log(Level.INFO, "Sell ("+result.getOrderNumber()+") ordered in the exchange");
		return result.getOrderNumber();
	}
	
	public void cancel(String currencyPair, String orderNumber) throws ExchangeException {
		SyncCall.of(client).call(ExchangeTopicEnum.CANCEL, JsonSuccess.class, currencyPair, orderNumber);
		LOG.log(Level.INFO, "Cancel order ("+orderNumber+") in the exchange");
	}
	
	static class SyncCall {
		private WampClient client;
		private boolean callReady = false;
		private String callResult = null;
		private Throwable callException = null;
		public static SyncCall of(WampClient client) {
			return new SyncCall(client);
		}
		private SyncCall(WampClient client) {
			this.client = client;
		}
		@SuppressWarnings("unchecked")
		public <T> T call(ExchangeTopicEnum topic, Type typeOf, Object ... args) throws ExchangeException {
	        Observable<String> result = client.call(topic.getId(), String.class, args);
	        
	        System.out.println("CALL " + topic.name().toLowerCase());
	        
	        client.call(topic.name().toLowerCase(), String.class, args);
	        result.subscribe(
	        	new Action1<String>() {
		            @Override
		            public void call(String data) {
		            	callResult = data;
		            	callReady = true;
		            }
	        	}, 
	        	new Action1<Throwable>() {
		            @Override
		            public void call(Throwable t1) {
		            	callException = t1;
		            	callReady = true;
		            }
	        	}
	        );
	        while (!callReady) {
	        	try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	        }
	        if (callException != null) {
	        	throw new ExchangeException(callException);
	        }
	        try {
				return (T) parseReturn(callResult, typeOf);
			} 
	        catch (JsonException e) {
				throw new ExchangeException(e);
			}			
		}
		
		@SuppressWarnings("unchecked")
		private <T> T parseReturn(String data, Type typeOf) throws JsonException {
			Gson gson = new Gson();
			if (data.startsWith("{\"error\"")) {
				Map<String, Object> result = gson.fromJson(data, new TypeToken<Map<String, Object>>(){}.getType());
				throw new JsonException(result.get("error").toString());
			}
			return (T) gson.fromJson(data, typeOf);
		}
		
	}
	
}
