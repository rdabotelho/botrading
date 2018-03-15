package com.m2r.botrading.admin.observer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.repositories.TraderJobRepository;
import com.m2r.botrading.admin.service.IExchangeManager;
import com.m2r.botrading.api.model.ITrader;
import com.m2r.botrading.api.service.IExchangeBasic;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.sim.clplus.Candle;
import com.m2r.botrading.ws.exchange.ExchangeWSClient;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder;
import com.m2r.botrading.ws.exchange.ExchangeWSServer;

public abstract class BaseWSObserver {

    protected final static Logger LOG = Logger.getLogger(BaseWSObserver.class.getSimpleName());
    
	protected static final String URL = "ws://localhost:8880/wsexchange";
	protected static int COUNT_PERIODS = 30;
	protected static final BigDecimal CANDLE_SIZE_FACTOR = new BigDecimal("6");

    @Autowired
	private IExchangeManager exchangeManager;
    
    @Autowired
    private TraderJobRepository traderJobRepository;
    
	private ExchangeWSClient client;
	
	public BaseWSObserver init(ExchangeWSServer localServer) {
		IExchangeBasic service = exchangeManager.getExchangeService(); 
		client = ExchangeWSClientBuilder
				.withExchangeService(service)
				.withUrl(URL)
				.withTickerAction(this::whenTicker)
				.withLiquidedAction(this::whenLiquided)
				.withCanceledAction(this::whenCanceled)
				.withExchangeWSServer(localServer)
				.withChartdata30Action(this::whenChartdata30)
				.build();
		client.start();
		client.waitToConnect();
		return this;
	}
	
	@PreDestroy
	public void finish() {
		client.close();
	}
	
	public IExchangeManager getExchangeManager() {
		return exchangeManager;
	}
	
	public List<TraderJob> findStartedTradersJobByStrategy(String strategy) {
		return traderJobRepository.findAllByStrategyAndStateOrderByDateTimeAsc(strategy, TraderJob.STATE_STARTED);
	}
	
	public void opportunityFound(String currencyPair, Candle candle) {
	}
	
	public void update(Set<String> orderNumbers) {
		try {
			client.update(orderNumbers);
			LOG.info("Orders " + orderNumbers + " updated in the server");					
		}
		catch (Exception e) {
			LOG.warning("Orders " + orderNumbers + " not updated in the server: " + e.getMessage());					
		}
	}
	
	public void update(String ... orderNumbers) {
		update(Stream.of(orderNumbers).collect(Collectors.toSet()));
	}
	
	public void buy(String currencyPair, Order order) {
		try {
			String price = CalcUtil.formatUS(order.getPrice());
			String amount = CalcUtil.formatUS(order.getAmount());
			String orderNumber = client.buy(currencyPair, price, amount);
			order.setOrderNumber(orderNumber);
			order.setPending(true);
			LOG.info("Order (buy) " + order.getOrderNumber() + " created in the exchange.");					
			update(orderNumber);
		}
		catch (Exception e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Order (buy) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());					
		}
	}
	
	public void sell(String currencyPair, Order order) {
		try {
			BigDecimal amountToSell = calculateAmountToImmediateSell(order.getTrader(), order.getAmount());
			String price = CalcUtil.formatUS(order.getPrice());
			String amount = CalcUtil.formatUS(amountToSell);
			String orderNumber = client.sell(currencyPair, price, amount);
			order.setAmount(amountToSell);
			order.setOrderNumber(orderNumber);
			order.setPending(true);
			LOG.info("Order (sell) " + order.getOrderNumber() + " created in the exchange.");
			update(orderNumber);
		}
		catch (Exception e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Order (sell) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());
		}
	}
	
	public void cancel(String currencyPair, Order order) {		
		try {
			client.cancel(currencyPair, order.getOrderNumber());
			order.setPending(true);
		}
		catch (Exception e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Cancel order " + order.getOrderNumber() + " not created in the exchange due to error: " + order.getLog());					
		}
	}
	
	protected BigDecimal calculateAmountToImmediateSell(ITrader trader, BigDecimal amount) {
	    	try {
		    	BigDecimal availableBalance = client.getExchangeWSServer().getAvailableBalance(trader.getCoin(), trader.getTraderJob().getAccount());
		    	if (availableBalance == null) {
		    		return amount;
		    	}
		    	return CalcUtil.greaterThen(availableBalance, amount) ? amount : availableBalance;
	    	}
	    	catch (Exception e) {
	    		return amount;
	    	}
	}	
	
	public abstract void whenTicker(String json);
	
	public abstract void whenLiquided(String json);
	
	public abstract void whenCanceled(String json);
	
	public abstract void whenChartdata30(String currencyPair, String data);
	


}
