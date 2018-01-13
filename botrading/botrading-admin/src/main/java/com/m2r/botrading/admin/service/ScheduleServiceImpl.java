package com.m2r.botrading.admin.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.repositories.OrderRepository;
import com.m2r.botrading.admin.repositories.TraderJobRepository;
import com.m2r.botrading.admin.repositories.TraderRepository;
import com.m2r.botrading.admin.util.OrderBuilder;
import com.m2r.botrading.admin.util.TraderBuilder;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.api.strategy.StrategyRepository;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.strategy.DefaultStrategyRepository;

@Service("schudeleService")
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final static Logger LOG = Logger.getLogger(ScheduleServiceImpl.class.getSimpleName());
    
    @Autowired
    private TraderJobRepository traderJobRepository;
    
    @Autowired
    private TraderRepository traderRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
	private IExchangeManager exchangeManager;
    
    private StrategyRepository strategyRepository = new DefaultStrategyRepository();
    
    protected IExchangeService getExchangeService() {
    	return exchangeManager.getExchangeService(PoloniexExchange.EXCHANGE_ID);
    }
    
    @Override
    public IMarketCoin getMarketCoin(String id) {
    	return getExchangeService().getMarkeyCoin(id);
    }
    
    @Override
    public IExchangeSession getExchangeSession(IMarketCoin marketCoin, boolean resetPublic, boolean resetPrivate) {
    		return getExchangeService().getSession(marketCoin, resetPublic, resetPrivate);
    }
    
    @Override
    public List<Order> findAllToSchedule() {
    		return orderRepository.findAllToSchedule();
    }
    
    @Override
    public List<TraderJob> findAllTraderJobsByState(Integer state) {
    		return traderJobRepository.findAllByStateOrderByDateTimeAsc(state);
    }
    
    @Override
    public List<Trader> findAllByTraderJobAndStateNotComplete(TraderJob traderJob) {
    		return traderRepository.findAllByTraderJobAndStateNot(traderJob, Trader.STATE_COMPLETED);    
    }
    
    @Override
    public void saveOrder(Order order) {
    		orderRepository.save(order);
    }
    
    @Override
    public void verifyAndCreateNewTrading(TraderJob traderJob, IExchangeSession session) throws Exception {
		if (traderJob.isFinished()) {
			return;
		}

		long countOfRunningCoins = 0;
		if (!traderJob.isNew()) {
			countOfRunningCoins = traderRepository.countByTraderJobAndStateIn(traderJob, Trader.STATE_NEW, Trader.STATE_STARTED);
		}
    		
		int countOfNewCoins = (int) (traderJob.getCurrencyCount() - countOfRunningCoins);
    		if (countOfNewCoins > 0) {
        		IStrategy strategy = strategyRepository.getStrategy(traderJob.getStrategy());
        		if (strategy == null) {
        			return;
        		}    		
        		try {
	    			List<String> ignoredCoins = new ArrayList<>();
	    			List<Trader> tradersToIgnoreCoin = traderRepository.findAllByTraderJobAndStateNotIn(traderJob, Order.STATE_LIQUIDED, Order.STATE_CANCELED);
	    			for (Trader t : tradersToIgnoreCoin) {
	    				ignoredCoins.add(t.getCoin());
	    			}
		    		List<ICurrency> currencies = strategy.selectCurrencies(session, countOfNewCoins, ignoredCoins);
		    		BigDecimal investment = traderJob.getTradingAmount().divide(new BigDecimal(traderJob.getCurrencyCount().toString()), MathContext.DECIMAL64);
		    		int limit = countOfNewCoins;
		    		for (ICurrency currency : currencies) {
		    			traderJob.getTraders().add(createTrader(currency.getId(), investment, traderJob, session));
		    			limit--;
		    			if (limit == 0) {
		    				break;
		    			}
		    		}    		
        		}
        		finally {
	        		if (traderJob.isNew()) {
	            		traderJob.start();        			
	        		}
	        		traderJobRepository.save(traderJob); 
        		}
    		}
    }
    
    private Trader createTrader(String coin, BigDecimal investment, TraderJob traderJob, IExchangeSession session) throws Exception {
		Trader t = TraderBuilder.create(coin, investment, traderJob);
		BigDecimal lastPrice = session.getLastPrice(coin);
		if (lastPrice == null) {
			return null;
		}
		t.start();
		traderRepository.save(t);
		OrderBuilder.createAll(t, lastPrice).forEach(o -> {
			orderRepository.save(o);
		});
		return t;
    }
    
    @Override
	public void synchronize(Trader trader, IExchangeSession session) throws Exception {
		if (trader == null) {
			return;
		}
		
		final List<Order> orders = this.orderRepository.findAllByTraderAndStateIn(trader, Order.STATE_ORDERED, Order.STATE_ORDERED_TO_CANCEL);
		if (orders.isEmpty()) {
			return;
		}
		
		IOrderList orderList = session.getOrders(trader.getTraderJob().getAccount());
		for (Order order : orders) {
			String currencyPair = session.getCurrencyOfTrader(trader).getCurrencyPair();
			List<IExchangeOrder> jsonOrders = orderList.getOrders(currencyPair);
			boolean notExistInTheExchangeOrders = true;
			if (jsonOrders != null) {
				for (IExchangeOrder jsonOrder : jsonOrders) {
					if (jsonOrder.getOrderNumber().equals(order.getOrderNumber())) {
						notExistInTheExchangeOrders = false;
						break;
					}
				}
			}
			if (notExistInTheExchangeOrders) {
				// order to cancel
				if (order.isOrderedToCancel()) {
					order.cancel();
					orderRepository.save(order);
					LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " canceled in the exchange.");
				}
				// order to liquidate
				else {
					order.liquided();
					orderRepository.save(order);
					LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " liquided in the exchange.");
				}
				continueTrading(order, session);					
			}
			// Automatic canceling and selling
			else if (order.isOrdered()) {
				// Cancel/Sell when expire
				if (order.isExpired()) {
					// Expired Buy (Cancel buy)
					if (order.isBuy() && order.getTrader().getTraderJob().getCancelBuyWhenExpire()) {
						order.newCancel();
						order.setLog("Canceled buy expiration");
						orderRepository.save(order);
					}
					// Expired Sell (Immediate sell)
					else if (order.getTrader().getTraderJob().getExecuteSellWhenExpire()) {
						order.immediateSell();
						order.setLog("Immediate sell buy expiration");
						orderRepository.save(order);
					}
				}				
			}
		};
	}	
    
    private void continueTrading(Order order, IExchangeSession session) throws Exception {
    		Trader trader = order.getTrader();
    		if (trader.isCompleted()) {
    			return;
    		}

    		// For liquidad sell orders
    		if (order.isSell()) {
    			// finish the trading
    			if (hasNotOpenedOrders(trader)) {
	    			trader.complete();
	    			traderRepository.save(trader);
	    			if (trader.getTraderJob().getContinuoMode()) {
	    				verifyAndCreateNewTrading(trader.getTraderJob(), session);
	    			}
    			}
    		}
    		// For liquided buy orders
    		else {
	    		Order sellOrder = orderRepository.findByTraderAndParcelAndKind(trader, order.getParcel(), Order.KIND_SELL);
	    		if (sellOrder != null) {
	    			// canceled
	    			if (order.isCanceled()) {
	    				sellOrder.cancel();
	    				orderRepository.save(sellOrder);
	    				continueTrading(sellOrder, session);
	    			}
	    		}
    		}
    }
    
    private boolean hasNotOpenedOrders(Trader trader) {
    		return orderRepository.countByTraderAndStateNotIn(trader, Order.STATE_LIQUIDED, Order.STATE_CANCELED) == 0;
    }
        
    @Override
    public Order findByTraderAndParcelAndKind(Trader trader, Integer parcel, Integer kind) {
    		return orderRepository.findByTraderAndParcelAndKind(trader, parcel, kind);
    }
  
    @Override
    public void executeOrder(Order order, IExchangeSession session) {
		// Cancel
		if (order.isNewCancel()) {
			try {
				session.cancel(order);
				order.orderToCancel();
				LOG.info("Cancel order " + order.getOrderNumber() + " created in the exchange.");
			}
			catch (ExchangeException e) {
				order.setLog(e.getMessage());
				LOG.warning("Cancel rder " + order.getOrderNumber() + " not created in the exchange due to error: " + order.getLog());					
			}
			order.setStateDateTime(LocalDateTime.now());
			this.saveOrder(order);
		}
		// Order
		else {
			// Buy
			if (order.isBuy()) {
				try {
					String number = session.buy(order);
					order.setOrderNumber(number);
					order.ordered();
					LOG.info("Order (buy) " + order.getOrderNumber() + " created in the exchange.");					
				}
				catch (ExchangeException e) {
					order.setLog(e.getMessage());					
					LOG.warning("Order (buy) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());					
				}
				order.setStateDateTime(LocalDateTime.now());
				this.saveOrder(order);
			}
			// Immediate Sell
			else if (order.isImmediateSell()) {
				try {
					String number = session.immediateSell(order);
					order.setOrderNumber(number);
					order.ordered();
					if (!(order.getLog() != null && order.getLog().contains("expiration"))) {
						order.setLog("Immediate sell");
					}
					LOG.info("Order (immediate sell) " + order.getOrderNumber() + " created in the exchange.");
				}
				catch (ExchangeException e) {
					order.setLog(e.getMessage());					
					LOG.warning("Order (immediate sell) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());
				}
				order.setStateDateTime(LocalDateTime.now());
				this.saveOrder(order);
			}
			// Sell if was already bought
			else 
			{
				Order buyOrder = this.findByTraderAndParcelAndKind(order.getTrader(), order.getParcel(), Order.KIND_BUY);
				if (buyOrder == null || buyOrder.isLiquided()) {
					try {
						order.setAmount(session.calculateSellToAmount(order.getTrader(), order.getAmount()));
						String number = session.sell(order);
						order.setOrderNumber(number);
						order.ordered();
						LOG.info("Order (sell) " + order.getOrderNumber() + " created in the exchange.");						
					}
					catch (ExchangeException e) {
						order.setLog(e.getMessage());					
						LOG.warning("Order (sell) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());
					}
					order.setStateDateTime(LocalDateTime.now());
					this.saveOrder(order);
				}
			}
		}
    }
    
}
