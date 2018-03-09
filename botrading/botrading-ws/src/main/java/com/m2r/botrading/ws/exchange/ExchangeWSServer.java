package com.m2r.botrading.ws.exchange;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.IDataChartPeriod;
import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.service.IExchangeBasic;

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

	private boolean connected;
	
	private IExchangeBasic service;
	private IApiAccess apiAccess;
	private Set<String> orderNumbers;
	
	private Set<String> orderNumbersLiquided;
	
	private ExchangeQueryThead tikerThread;
	private ExchangeQueryThead syncThread;
	private ExchangeQueryThead chardata30Thread;
	
	private List<String> currencyCoinsToTickerPush;
	private List<String> currencyCoinsToCHartdata30Push;
	private IDataChartPeriod periodToCHartdata30Push;
	
	private int port;
	private String channel;
	private WampRouter router;
	private SimpleWampWebsocketListener server;
	private WampClient client;
    
    private Subscription initProcSubscription;
    private Subscription buyProcSubscription;
    private Subscription sellProcSubscription;
    private Subscription cancelProcSubscription;
	
	protected ExchangeWSServer(IExchangeBasic service, IApiAccess apiAccess, int port, String channel, List<String> currencyCoinsToTickerPush, List<String> currencyCoinsToCHartdata30Push, IDataChartPeriod periodToCHartdata30Push) {
		this.connected = false;
		this.orderNumbers = new HashSet<>();
		this.orderNumbersLiquided = new HashSet<>();
		this.service = service;
		this.apiAccess = apiAccess;
		this.port = port;
		this.channel = channel;
		this.currencyCoinsToTickerPush = currencyCoinsToTickerPush;
		this.currencyCoinsToCHartdata30Push = currencyCoinsToCHartdata30Push;
		this.periodToCHartdata30Push = periodToCHartdata30Push;
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
                  connected = true;
                }
            }
        });

        client.open();    
        
        this.run();
        System.out.println("Server started!");
        return this;
	}
	
	public boolean isConnected() {
		return connected;
	}
	
	public void waitToConnect() {
		while (!this.isConnected()) {
			try {
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
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
		if (tikerThread != null) {
			tikerThread.close();
		}
		if (syncThread != null) {
			syncThread.close();
		}
		if (chardata30Thread != null) {
			chardata30Thread.close();
		}
       	client.close().toBlocking().last();
		System.out.println("Server closed");		
	}
	
	private void run() {
		
		// Tikers
		if (currencyCoinsToTickerPush != null) {
		tikerThread = ExchangeQueryThead
				.build()
				.withTimeout(1000)
				.withAction(()->{
					try {
						ITickerList list = service.getAllTikers();
						List<ITicker> tickers = list.getTickers().stream().filter(it -> currencyCoinsToTickerPush.contains(it.getCurrencyPair())).collect(Collectors.toList());
						publish(ExchangeTopicEnum.TICKER, tickers);
					} catch (Exception e) {
						e.printStackTrace();
					}
				})
				.execute();
		}
		
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
		
		// chardata30Thread
		if (currencyCoinsToCHartdata30Push != null) {
			chardata30Thread = ExchangeQueryThead
					.build()
					.withTimeout(1000)
					.withAction(()->{
						LocalDateTime end = LocalDateTime.now().withSecond(0).withNano(0);
						LocalDateTime start =  end.minusMinutes((30*15));
						try {
							for (String currencyPair : currencyCoinsToCHartdata30Push) {
								IChartDataList list = service.getAllChartData(currencyPair, periodToCHartdata30Push, start, end);
								Chardata30Pair chardata30Pair = new Chardata30Pair(currencyPair, new Gson().toJson(list.getChartDatas()));
								publish(ExchangeTopicEnum.CHARTDATA30, chardata30Pair);
							}						
						} catch (Exception e) {
							e.printStackTrace();
						}
					})
					.execute();
		}
		
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
