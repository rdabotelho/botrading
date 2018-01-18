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
import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.api.strategy.StrategyRepository;
import com.m2r.botrading.api.util.CalcUtil;
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
    		return getExchangeService().getMarketCoin(id);
    }
    
    @Override
    public IExchangeSession getExchangeSession(IMarketCoin marketCoin, boolean resetPublic, boolean resetPrivate) {
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
				// Eliminate the duplicity
				List<String> ignoredCoins = new ArrayList<>();
				List<Trader> tradersToIgnoreCoin = traderRepository.findAllByTraderJobAndStateNotIn(traderJob, Order.STATE_LIQUIDED, Order.STATE_CANCELED);
				for (Trader t : tradersToIgnoreCoin) {
					ignoredCoins.add(t.getCoin());
				}

				// Get the order intent strategy
				List<IOrderIntent> orderIntents = strategy.selectOrderIntent(session, countOfNewCoins, ignoredCoins);
				
				BigDecimal investment = traderJob.getTradingAmount().divide(new BigDecimal(traderJob.getCurrencyCount().toString()), MathContext.DECIMAL64);
				int limit = countOfNewCoins;
				for (IOrderIntent orderIntent : orderIntents) {
					
					// Create trader and its orders
					traderJob.getTraders().add(createTrader(orderIntent.getCurrency().getId(), investment, traderJob, session));
					
					//Check if the order intent strategy change the original prices 
					if (orderIntent.isReplacePrice()) {
						traderJob.getTraders().forEach(t -> {
							t.getOrders().forEach(o -> {
								o.setPrice(o.isBuy() ? orderIntent.getBuyPrice() : orderIntent.getSellPrice());
							});
						});
					}
					
					// Exit on limit
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
		Trader t = TraderBuilder.create(coin, investment, session.getFee(), traderJob);
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
		final List<Order> orders = this.findAllToScheduleTaskSynch(trader);
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
				if (order.isOrderedCancel()) {
					orderRepository.save(order.confirmCancellation());
					LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " canceled in the exchange.");
				}
				// order to liquidate (buy or sell)
				else {
					order.setFee(order.isImmediate() ? session.getImmediateFee() : session.getFee());
					orderRepository.save(order.confirmLiquidation());
					LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " liquided in the exchange.");
					this.calculateProfit(order);
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
						orderRepository.save(order.preperToCancel());
					}
					// Expired Sell (Immediate sell)
					else if (order.getTrader().getTraderJob().getExecuteSellWhenExpire()) {
						order.setLog("Immediate sell buy expiration");
						orderRepository.save(order.preperToImmediateSel());
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
	    			if (trader.getTraderJob().getContinuoMode()) {
	    				verifyAndCreateNewTrading(trader.getTraderJob(), session);
	    			}
    			}
    		}
    		// For liquided buy orders
    		else {
	    		Order sellOrder = orderRepository.findByTraderAndParcelAndKind(trader, order.getParcel(), Order.KIND_SELL);
	    		if (sellOrder != null) {
		    		if (order.isCanceled()) {
	    				orderRepository.save(sellOrder.preperToCancel());
		    		}
		    		else if (order.isLiquided()) {
	    				orderRepository.save(sellOrder.preperToSell());		    			
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
			String currencyPair = session.getCurrencyOfTrader(order.getTrader()).getCurrencyPair();
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
		order.setStateDateTime(LocalDateTime.now());
		this.saveOrder(order);
    }
    
    private void sell(Order order, IExchangeSession session) {
		try {
			BigDecimal priceToSell = order.getPrice(); 
			BigDecimal amountToSell = session.calculateAmountToImmediateSell(order.getTrader(), order.getAmount());
			String currencyPair = session.getCurrencyOfTrader(order.getTrader()).getCurrencyPair();
			if (order.isImmediate()) {
				priceToSell = session.getLastPrice(order.getTrader().getCoin());
			}
			String price = CalcUtil.formatUS(priceToSell);
			String amount = CalcUtil.formatUS(amountToSell);
			String orderNumber = session.sell(order.getTrader().getTraderJob().getAccount(), currencyPair, price, amount);
			order.setOrderNumber(orderNumber);
			order.setPending(true);
			order.setAmount(amountToSell);
			LOG.info("Order (sell) " + order.getOrderNumber() + " created in the exchange.");						
		}
		catch (Exception e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Order (sell) " + order.getOrderNumber() + " not create in the exchange due to error: " + order.getLog());
		}
		order.setStateDateTime(LocalDateTime.now());
		this.saveOrder(order);	
    }
    
    private void cancel(Order order, IExchangeSession session) {
		try {
			if (order.getOrderNumber() != null && !order.getOrderNumber().equals("")) {
				session.cancel(order.getTrader().getTraderJob().getAccount(), order.getOrderNumber());
				LOG.info("Cancel order " + order.getOrderNumber() + " created in the exchange.");
			}
			order.setPending(true);
		}
		catch (ExchangeException e) {
			order.setLog(e.getMessage());
			order.setState(Order.STATE_ERROR);
			LOG.warning("Cancel order " + order.getOrderNumber() + " not created in the exchange due to error: " + order.getLog());					
		}
		order.setStateDateTime(LocalDateTime.now());
		this.saveOrder(order);    	
    }
    
}
