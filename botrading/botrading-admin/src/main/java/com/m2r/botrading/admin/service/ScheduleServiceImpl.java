package com.m2r.botrading.admin.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
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
import com.m2r.botrading.admin.util.SMSSender;
import com.m2r.botrading.admin.util.TraderBuilder;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.service.IStrategyManager;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.api.util.CalcUtil;

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
    
    @Autowired
    private IStrategyManager strategyManager;
    
    protected IExchangeService getExchangeService() {
	    return exchangeManager.getExchangeService(); 
    }
    
    @Override
    public MarketCoin getMarketCoin(String id) {
    		return getExchangeService().getMarketCoin(id);
    }
    
    @Override
    public IExchangeSession getExchangeSession(MarketCoin marketCoin, boolean resetPublic, boolean resetPrivate) {
    		return getExchangeService().getSession(marketCoin, resetPublic, resetPrivate);
    }
    
    @Override
    public List<Order> findAllToScheduleTaskOrders() {
    		return orderRepository.findAllToScheduleTaskOrders();
    }
    
    @Override
    public List<Order> findAllToScheduleTaskSynch(Trader trader) {
    		return orderRepository.findAllByTraderAndPendingAndStateIn(trader, true, Order.STATE_ORDERED, Order.STATE_ORDERED_CANCEL);
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
    	orderRepository.saveOrder(order);
    }
    
    @Override
    public void verifyAndCreateNewTrading(Long traderJobId, IExchangeSession session) throws Exception {
    		TraderJob traderJob = traderJobRepository.findOne(traderJobId);
		if (!traderJob.isStarted()) {
			return;
		}
		
		if (!traderJob.getContinuoMode()) {
			return;
		}

		long countOfRunningCoins = 0;
		if (!traderJob.isNew()) {
			countOfRunningCoins = traderRepository.countByTraderJobAndStateIn(traderJob, Trader.STATE_NEW, Trader.STATE_STARTED);
		}
    		
		int countOfNewCoins = (int) (traderJob.getCurrencyCount() - countOfRunningCoins);
		if (countOfNewCoins > 0) {
			IStrategy strategy = strategyManager.getStrategyByName(traderJob.getStrategy());
			if (strategy == null) {
				return;
			}
			
			// Eliminate the duplicity
			List<String> ignoredCoins = new ArrayList<>();
			List<Trader> tradersToIgnoreCoin = traderRepository.findAllByTraderJobAndStateNotIn(traderJob, Order.STATE_LIQUIDED, Order.STATE_CANCELED);
			for (Trader t : tradersToIgnoreCoin) {
				ignoredCoins.add(t.getCoin());
			}

			// Get the order intent strategy
			List<IOrderIntent> orderIntents = strategy.selectOrderIntent(strategyManager, session, traderJob, countOfNewCoins, ignoredCoins);
			
			if (!orderIntents.isEmpty()) {
				
				// Get the used value
				BigDecimal used = traderRepository.sumInvestmentByTraderJobAndStateIn(traderJob, Trader.STATE_NEW, Trader.STATE_STARTED);
				if (used == null) {
					used = BigDecimal.ZERO;
				}
				
				// calculate the new investment
				BigDecimal investment = CalcUtil.divide(CalcUtil.subtract(traderJob.getBalance(), used), new BigDecimal(new Integer(countOfNewCoins).toString()));
				
				int limit = countOfNewCoins;
				for (IOrderIntent orderIntent : orderIntents) {
					
					// Create trader and its orders
					traderJob.getTraders().add(createTrader(orderIntent.getCurrency().getId(), investment, traderJob, session, orderIntent));
					
					// Exit on limit
					limit--;
					if (limit == 0) {
						break;
					}
				}
				
				traderJobRepository.save(traderJob);
			}
		}
    }
    
    private Trader createTrader(String coin, BigDecimal investment, TraderJob traderJob, IExchangeSession session, IOrderIntent intent) throws Exception {
		Trader t = TraderBuilder.create(coin, investment, session.getFee(), traderJob);
		BigDecimal lastPrice = session.getLastPrice(coin);
		if (lastPrice == null) {
			return null;
		}
		t.start();
		traderRepository.save(t);
		OrderBuilder.createAll(t, lastPrice, intent).forEach(o -> {
			getExchangeService().onBeforeSaveOrder(o);
			orderRepository.saveOrder(o);
		});
		return t;
    }
    
    @Override
	public void synchronize(Trader trader, IExchangeSession session) throws Exception {
		final List<Order> orders = this.findAllToScheduleTaskSynch(trader);
		if (orders.isEmpty()) {
			return;
		}
		
		IOrderList orderList = session.getOrders(trader.getTraderJob().getAccount());
		for (Order order : orders) {
			Currency currency = session.getCurrencyOfTrader(trader);
			String currencyPair = session.getCurrencyFactory().currencyToCurrencyPair(currency);
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
				if (order.isOrderedCancel()) {
					orderRepository.saveOrder(order.confirmCancellation());
					LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " canceled in the exchange.");
				}
				// order to liquidate (buy or sell)
				else {
					order.setFee(order.isImmediate() ? session.getImmediateFee() : session.getFee());
					orderRepository.saveOrder(order.confirmLiquidation());
					LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " liquided in the exchange.");
					if (order.isSell()) {
						this.calculateProfit(order);
					}
				}
				concludeSynchronize(order, session);					
			}
			// Automatic canceling and selling
			else if (order.isOrdered()) {
				// Cancel/Sell when expire
				if (order.isExpired()) {
					// Expired Buy (Cancel buy)
					if (order.isBuy() && order.getTrader().getTraderJob().getCancelBuyWhenExpire()) {
						order.setLog("Canceled buy expiration");
						orderRepository.saveOrder(order.preperToCancel());
					}
					// Expired Sell (Immediate sell)
					else if (order.getTrader().getTraderJob().getExecuteSellWhenExpire()) {
						order.setLog("Immediate sell buy expiration");
						orderRepository.saveOrder(order.preperToImmediateSel());
					}
				}	
				// Stop loss
				if (order.isSell() && order.isOrdered() && order.getTrader().getTraderJob().getStopLoss()) {
		    			Order buyOrder = orderRepository.findByTraderAndParcelAndKind(trader, order.getParcel(), Order.KIND_BUY);
		    			if (buyOrder != null) {
		    				BigDecimal lastValue = session.getLastPrice(trader.getCoin());
		    				BigDecimal difference = CalcUtil.subtract(buyOrder.getPrice(), lastValue);
		    				if (difference.signum() > 0) {
		    					BigDecimal percent = CalcUtil.multiply(CalcUtil.divide(difference, buyOrder.getPrice()), CalcUtil.HUNDRED);
		    					if (!CalcUtil.lessThen(percent, order.getTrader().getTraderJob().getLimitToStop())) {
		    						order.setLog("Immediate sell buy stop loss");
		    						orderRepository.saveOrder(order.preperToImmediateSel());		    						
		    					}
		    				}
		    			}
				}
			}
		};
	}	
    
    private void concludeSynchronize(Order order, IExchangeSession session) throws Exception {
    		Trader trader = order.getTrader();
    		if (trader.isCompleted()) {
    			return;
    		}

    		// For liquidad sell orders
    		if (order.isSell()) {
    			// finish the trading
    			if (hasNotOpenedOrders(trader)) {
	    			traderRepository.complete(trader);
    			}
    		}
    		// For liquided buy orders
    		else {
	    		Order sellOrder = orderRepository.findByTraderAndParcelAndKind(trader, order.getParcel(), Order.KIND_SELL);
	    		if (sellOrder != null) {
		    		if (order.isCanceled()) {
	    				orderRepository.saveOrder(sellOrder.preperToCancel());
		    		}
		    		else if (order.isLiquided()) {
	    				orderRepository.saveOrder(sellOrder.preperToSell());		    			
		    		}
	    		}
    		}
    }
    
    private void calculateProfit(Order sellOrder) {
		Order buyOrder = orderRepository.findByTraderAndParcelAndKind(sellOrder.getTrader(), sellOrder.getParcel(), Order.KIND_BUY);
	    	if (buyOrder != null) {
	    		BigDecimal profit = CalcUtil.subtract(sellOrder.getBalance(), buyOrder.getBalance());
	    		sellOrder.getTrader().addProfit(profit);
	    		sellOrder.getTrader().getTraderJob().addProfit(profit);
	    		traderRepository.save(sellOrder.getTrader());
	    		traderJobRepository.save(sellOrder.getTrader().getTraderJob());
	    		
	    		// send sell liquided SMS
	    		try {
	    			sendLiquidationSMS(sellOrder);
	    		}
	    		catch (Exception e) {
	    			LOG.log(Level.SEVERE, e.getMessage(), e);
	    		}
	    	}
    }
    
    private void sendLiquidationSMS(Order sellOrder) {
		Trader trader = sellOrder.getTrader();
    		TraderJob traderJob = trader.getTraderJob();
    		String phone = traderJob.getAccount().getPhone();
    		if (phone != null) {
    			BigDecimal percent = trader.getProfit().multiply(CalcUtil.HUNDRED, CalcUtil.DECIMAL_COIN).divide(trader.getInvestment(), CalcUtil.DECIMAL_PERCENT);
    			SMSSender.sendLiquidedSellSMS(phone, traderJob.getStrategy(), trader.getCoin(), trader.getProfit(), percent);
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
		// Order to Cancel
		if (order.isOrderedCancel() ) {
			cancel(order, session);
		}
		// Order to Buy
		else if (order.isBuy()) {
			buy(order, session);
		}
		// Order to Sell
		else if (order.isSell()) {
			sell(order, session);
		}
    }
    
    /*
     * ORDERS
     */
    
    private void buy(Order order, IExchangeSession session) {
		try {
			Currency currency = session.getCurrencyOfTrader(order.getTrader());
			String currencyPair = session.getCurrencyFactory().currencyToCurrencyPair(currency);
			String price = CalcUtil.formatUS(order.getPrice());
			String amount = CalcUtil.formatUS(order.getAmount());
			String orderNumber = session.buy(order.getTrader().getTraderJob().getAccount(), currencyPair, price, amount);
			order.setOrderNumber(orderNumber);
			order.setPending(true);
			LOG.info("Order (buy) " + order.getOrderNumber() + " created in the exchange.");					
		}
		catch (ExchangeException e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Order (buy) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());					
		}
		this.saveOrder(order);
    }
    
    private void sell(Order order, IExchangeSession session) {
		try {
			BigDecimal priceToSell = order.getPrice(); 
			BigDecimal amountToSell = session.calculateAmountToImmediateSell(order.getTrader(), order.getAmount());
			Currency currency = session.getCurrencyOfTrader(order.getTrader());
			if (order.isImmediate()) {
				if (order.isNoProfit()) {
					Order buyOrder = orderRepository.findByTraderAndParcelAndKind(order.getTrader(), order.getParcel(), Order.KIND_BUY);
					priceToSell = CalcUtil.add(buyOrder.getPrice(), CalcUtil.percent(buyOrder.getPrice(), session.getFee()));   
					BigDecimal lastPrice = session.getLastPrice(currency.getId());
					if (!CalcUtil.greaterThen(priceToSell, lastPrice)) {
						priceToSell = CalcUtil.add(buyOrder.getPrice(), CalcUtil.percent(buyOrder.getPrice(), session.getImmediateFee()));   
					}
			    	order.setNoProfit(false);
				}
				else {
					priceToSell = session.getLastPrice(order.getTrader().getCoin());
				}
			}
			String price = CalcUtil.formatUS(priceToSell);
			String amount = CalcUtil.formatUS(amountToSell);
			String currencyPair = session.getCurrencyFactory().currencyToCurrencyPair(currency);
			String orderNumber = session.sell(order.getTrader().getTraderJob().getAccount(), currencyPair, price, amount);
			order.setOrderNumber(orderNumber);
			order.setPending(true);
			order.setPrice(priceToSell);
			order.setAmount(amountToSell);
			LOG.info("Order (sell) " + order.getOrderNumber() + " created in the exchange.");						
		}
		catch (Exception e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Order (sell) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());
		}
		this.saveOrder(order);	
    }
    
    private void cancel(Order order, IExchangeSession session) {
		try {
			if (existInTheExchangeOrders(order, session)) {			
				if (order.getOrderNumber() != null && !order.getOrderNumber().equals("")) {
					Currency currency = session.getCurrencyOfTrader(order.getTrader());
					String currencyPair = session.getCurrencyFactory().currencyToCurrencyPair(currency);
					session.cancel(order.getTrader().getTraderJob().getAccount(), currencyPair, order.getOrderNumber());
					LOG.info("Cancel order " + order.getOrderNumber() + " created in the exchange.");
				}
			}
			// already liquided/canceled in the exchange
			else {
				if (order.getOrderNumber() != null && !order.getOrderNumber().equals("")) {
					order.setState(Order.STATE_ORDERED);
				}
			}
			order.setPending(true);
		}
		catch (ExchangeException e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Cancel order " + order.getOrderNumber() + " not created in the exchange due to error: " + order.getLog());					
		}
		this.saveOrder(order);    	
    }
    
    private boolean existInTheExchangeOrders(Order order, IExchangeSession session) throws ExchangeException {
		IOrderList orderList = null;
		try {
			orderList = session.getOrders(order.getTrader().getTraderJob().getAccount());
		} catch (Exception e) {
			throw new ExchangeException(e.getMessage());
		}
		Currency currency = session.getCurrencyOfTrader(order.getTrader());
		String currencyPair = session.getCurrencyFactory().currencyToCurrencyPair(currency);
		List<IExchangeOrder> jsonOrders = orderList.getOrders(currencyPair);
		boolean existInTheExchangeOrders = false;
		if (jsonOrders != null) {
			for (IExchangeOrder jsonOrder : jsonOrders) {
				if (jsonOrder.getOrderNumber().equals(order.getOrderNumber())) {
					existInTheExchangeOrders = true;
					break;
				}
			}
		}
		return existInTheExchangeOrders;
    }
    
}
