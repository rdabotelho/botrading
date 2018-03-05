package com.m2r.botrading.ws.server;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.m2r.botrading.api.service.IExchangeService;

import rx.Subscription;
import rx.functions.Action1;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.Request;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.WampRouter;
import ws.wamp.jawampa.WampRouterBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;
import ws.wamp.jawampa.transport.netty.SimpleWampWebsocketListener;

public class ExchangeWSServer {

	private static final long TIMEOUT = 1000;
	
	IExchangeService service;
	private List<ExchangeRequest> requests;
	private boolean running;
	
	private int port;
	private String channel;
	private WampRouter router;
	private SimpleWampWebsocketListener server;
	private WampClient client;
    
    private Subscription buyProcSubscription;
    private Subscription sellProcSubscription;
    private Subscription cancelProcSubscription;
    private Subscription chartdataProcSubscription;
	
	public static ExchangeWSServer build(IExchangeService service, int port, String channel) {
		return new ExchangeWSServer(service, port, channel);
	}
	
	private ExchangeWSServer(IExchangeService service, int port, String channel) {
		this.requests = new LinkedList<>();
		this.service = service;
		this.port = port;
		this.channel = channel;
	}
	
	public ExchangeWSServer start() {
        WampRouterBuilder routerBuilder = new WampRouterBuilder();
        try {
            routerBuilder.addRealm("realm1");
            router = routerBuilder.build();
        } catch (ApplicationError e1) {
            e1.printStackTrace();
        }
        
        URI serverUri = URI.create("ws://0.0.0.0:"+this.port+"/"+channel);
        IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
        WampClientBuilder builder = new WampClientBuilder();

        try {
            server = new SimpleWampWebsocketListener(router, serverUri, null);
            server.start();
            builder.withConnectorProvider(connectorProvider)
                   .withUri("ws://localhost:"+this.port+"/"+channel)
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
                    buyProcSubscription = client.registerProcedure(ExchangeTopicEnum.BUY.name().toLowerCase()).subscribe(new Action1<Request>() {
                        @Override
                        public void call(Request request) {
                        	enqueueRequest(ExchangeTopicEnum.BUY, request);
                        }
                   });
                   buyProcSubscription = client.registerProcedure(ExchangeTopicEnum.SELL.name().toLowerCase()).subscribe(new Action1<Request>() {
                        @Override
                        public void call(Request request) {
                        	enqueueRequest(ExchangeTopicEnum.SELL, request);
                        }
                   });
                   cancelProcSubscription = client.registerProcedure(ExchangeTopicEnum.CANCEL.name().toLowerCase()).subscribe(new Action1<Request>() {
                       @Override
                       public void call(Request request) {
                       	enqueueRequest(ExchangeTopicEnum.CANCEL, request);
                       }
                  });
                   chartdataProcSubscription = client.registerProcedure(ExchangeTopicEnum.CHARTDATA.name().toLowerCase()).subscribe(new Action1<Request>() {
                       @Override
                       public void call(Request request) {
                       	enqueueRequest(ExchangeTopicEnum.CHARTDATA, request);
                       }
                  });
                }
            }
        });

        client.open();    
        
        System.out.println("Server started!");
        return this;
	}
	
	public void publish(ExchangeTopicEnum topic, Object object) {
        client.publish(topic.name().toLowerCase(), new Gson().toJson(object));
	}
	
	public void close() {
		router.close().toBlocking().last();
		server.stop();
		if (buyProcSubscription != null) {
			buyProcSubscription.unsubscribe();
		}
		if (sellProcSubscription != null) {
			sellProcSubscription.unsubscribe();
		}
		if (cancelProcSubscription != null) {
			cancelProcSubscription.unsubscribe();
		}
		if (chartdataProcSubscription != null) {
			chartdataProcSubscription.unsubscribe();
		}
       	client.close().toBlocking().last();
       	running = false;
		System.out.println("Server closed");		
	}
	
	private void enqueueRequest(ExchangeTopicEnum topic, Request request) {
		requests.add(new ExchangeRequest(System.currentTimeMillis(), topic, request));
		Collections.sort(requests);
	}
	
	private ExchangeRequest dequeueRequest() {
		if (requests.isEmpty()) {
			return null;
		}
		return requests.remove(requests.size()-1);
	}
	
	public void run() {
		running = true;
		while (running) {
			ExchangeRequest er = dequeueRequest();
			if (er != null) {
				try {
					er.getTopic().execute(service, er.getRequest());
				} catch (Exception e) {
					e.printStackTrace();
					er.getRequest().reply(String.format("{\"erro\":\"%s\"}", e.getMessage()));
				}
			}
			try {
				Thread.sleep(TIMEOUT);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
}
