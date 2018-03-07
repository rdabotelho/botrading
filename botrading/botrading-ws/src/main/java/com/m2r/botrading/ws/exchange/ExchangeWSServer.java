package com.m2r.botrading.ws.exchange;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.poloniex.model.ChartData;

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

	private IExchangeService service;
	private IApiAccess apiAccess;
	private Set<String> orderNumbers;
	
	private Set<String> orderNumbersLiquided;
	
	private ExchangeQueryThead tikerThread;
	private ExchangeQueryThead syncThread;
	private ExchangeQueryThead reportThread;
	
	private int port;
	private String channel;
	private WampRouter router;
	private SimpleWampWebsocketListener server;
	private WampClient client;
    
    private Subscription initProcSubscription;
    private Subscription buyProcSubscription;
    private Subscription sellProcSubscription;
    private Subscription cancelProcSubscription;
    private Subscription chartdataProcSubscription;
	
	public static ExchangeWSServer build(IExchangeService service, int port, IApiAccess apiAccess, String channel) {
		return new ExchangeWSServer(service, port, apiAccess, channel);
	}
	
	private ExchangeWSServer(IExchangeService service, int port, IApiAccess apiAccess, String channel) {
		this.orderNumbers = new HashSet<>();
		this.orderNumbersLiquided = new HashSet<>();
		this.service = service;
		this.apiAccess = apiAccess;
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
                    initProcSubscription = client.registerProcedure(ExchangeTopicEnum.INIT.getId()).subscribe(new Action1<Request>() {
                        @Override
                        public void call(Request request) {
                        	try {
                        		init(request);
                        	}
                        	catch (Exception e) {
                        		setError(request, e);
                        	}
                        }
                   });
                   buyProcSubscription = client.registerProcedure(ExchangeTopicEnum.BUY.getId()).subscribe(new Action1<Request>() {
                        @Override
                        public void call(Request request) {
                        	try {
                            	buy(request);
                        	}
                        	catch (Exception e) {
                        		setError(request, e);
                        	}
                        }
                   });
                   sellProcSubscription = client.registerProcedure(ExchangeTopicEnum.SELL.getId()).subscribe(new Action1<Request>() {
                       @Override
                       public void call(Request request) {
	                       	try {
	                       		sell(request);
	                    	}
	                    	catch (Exception e) {
	                    		setError(request, e);
	                    	}
                       }
                  });
                   cancelProcSubscription = client.registerProcedure(ExchangeTopicEnum.CANCEL.getId()).subscribe(new Action1<Request>() {
                       @Override
                       public void call(Request request) {
	                       	try {
	                       		cancel(request);
	                    	}
	                    	catch (Exception e) {
	                    		setError(request, e);
	                    	}
                       }
                  });
                   chartdataProcSubscription = client.registerProcedure(ExchangeTopicEnum.CHARTDATA.getId()).subscribe(new Action1<Request>() {
                       @Override
                       public void call(Request request) {
	                       	try {
	                     	   chartdata(request);
	                    	}
	                    	catch (Exception e) {
	                    		setError(request, e);
	                    	}
                       }
                  });
                }
            }
        });

        client.open();    
        
        this.run();
        System.out.println("Server started!");
        return this;
	}
	
	public void close() {
		router.close().toBlocking().last();
		server.stop();
		if (initProcSubscription != null) {
			initProcSubscription.unsubscribe();
		}
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
		if (tikerThread != null) {
			tikerThread.close();
		}
		if (syncThread != null) {
			syncThread.close();
		}
		if (reportThread != null) {
			reportThread.close();
		}
       	client.close().toBlocking().last();
		System.out.println("Server closed");		
	}
	
	private void run() {
		
		// Tikers
		tikerThread = ExchangeQueryThead
				.build()
				.withTimeout(1000)
				.withAction(()->{
					try {
						ITickerList list = service.getAllTikers();
						publish(ExchangeTopicEnum.TICKER, list.getTickers());
					} catch (Exception e) {
						e.printStackTrace();
					}
				})
				.execute();
		
		// Synch
		syncThread = ExchangeQueryThead
				.build()
				.withTimeout(1000)
				.withAction(()->{
					try {
						orderNumbersLiquided.clear();
						orderNumbersLiquided.addAll(orderNumbers);
						IOrderList list = service.getAllOrders(apiAccess);
						for (IExchangeOrder order : list.getOrders()) {
							if (orderNumbers.contains(order.getOrderNumber())) {
								orderNumbersLiquided.remove(order.getOrderNumber());
							}
							else {
								orderNumbers.add(order.getOrderNumber());
							}
						}
						for (String orderNumber : orderNumbersLiquided) {
							ExchangeTopicEnum topic = ExchangeTopicEnum.CANCELED;
							if (service.isOrderExecuted(apiAccess, orderNumber)) {
								topic = ExchangeTopicEnum.LIQUIDED; 
							}
							publish(topic, String.format("{\"orderNumber\":\"%s\"}", orderNumber));
							orderNumbers.remove(orderNumber);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				})
				.execute();
		
		// Report
		reportThread = ExchangeQueryThead
				.build()
				.withTimeout(1000)
				.withAction(()->{
					
				})
				.execute();
	}
	
	private void publish(ExchangeTopicEnum topic, Object data) {
        client.publish(topic.getId(), new Gson().toJson(data));
	}
	
	/**
	 * Exchange Actions
	 */
	
	public void init(Request request) throws Exception {
		Set<String> list = new Gson().fromJson(request.arguments().get(0).asText(), new TypeToken<Set<String>>(){}.getType());
		orderNumbers.addAll(list);
		setSuccess(request);
	}
	
	public void buy(Request request) throws Exception {
		String currencyPair = request.arguments().get(0).asText();
		String price = request.arguments().get(1).asText();
		String amount = request.arguments().get(2).asText();
		String orderNumber = service.buy(apiAccess, currencyPair, price, amount);
		setOrderSuccess(request, orderNumber);
	}
	
	public void sell(Request request) throws Exception {
		String currencyPair = request.arguments().get(0).asText();
		String price = request.arguments().get(1).asText();
		String amount = request.arguments().get(2).asText();
		String orderNumber = service.sell(apiAccess, currencyPair, price, amount);
		setOrderSuccess(request, orderNumber);
	}
	
	public void cancel(Request request) throws Exception {
		String currencyPair = request.arguments().get(0).asText();
		String orderNumber = request.arguments().get(1).asText();
		service.cancel(apiAccess, currencyPair, orderNumber);		
		setSuccess(request);
	}
	
	public void chartdata(Request request) throws Exception {
		String currencyPair = request.arguments().get(0).asText();
		String period = request.arguments().get(1).asText();
		String start = request.arguments().get(2).asText();
		String end = request.arguments().get(3).asText();
		String result = service.getAllChartData(currencyPair, period, start, end);
		request.reply(result.replaceAll("\\[", "").replaceAll("\\]", ""));
	}
	
	private void setError(Request request, Exception e) {
		e.printStackTrace();
		request.reply(String.format("{\"erro\":\"%s\"}", e.getMessage()));
	}
	
	private void setSuccess(Request request) {
		request.reply("{\"success\":\"true\"}");
	}
	
	private void setOrderSuccess(Request request, String orderNumber) {
		request.reply(String.format("{\"success\":\"true\",\"orderNumber\":\"%s\"}", orderNumber));
	}
	
}
