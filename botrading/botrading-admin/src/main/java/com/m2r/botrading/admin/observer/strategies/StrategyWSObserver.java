package com.m2r.botrading.admin.observer.strategies;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.observer.BaseWSObserver;
import com.m2r.botrading.admin.observer.OrderAction;
import com.m2r.botrading.admin.repositories.OrderRepository;
import com.m2r.botrading.admin.repositories.TraderJobRepository;
import com.m2r.botrading.admin.repositories.TraderRepository;
import com.m2r.botrading.admin.util.OrderBuilder;
import com.m2r.botrading.admin.util.TraderBuilder;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.OrderIntent;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.sim.clplus.Candle;

public abstract class StrategyWSObserver extends BaseWSObserver {

    @Autowired
    TraderJobRepository traderJobRepository;
    
    @Autowired
    TraderRepository traderRepository;
    
    @Autowired
    OrderRepository orderRepository;
    
	public List<TraderJob> findStartedTradersJobByStrategy(String strategy) {
		return traderJobRepository.findAllByStrategyAndStateOrderByDateTimeAsc(strategy, TraderJob.STATE_STARTED);
	}
	
	public void opportunityFound(IExchangeService service, String currencyPair, Candle candle, OrderAction action) throws Exception {
		List<TraderJob> tradersJob = findStartedTradersJobByStrategy(getStrategy());
		for (TraderJob traderJob : tradersJob) {
			if (!traderJob.getContinuoMode()) {
				continue;
			}
			
			Currency currency = service.getCurrencyFactory().currencyPairToCurrency(currencyPair, service);
			if (!filter(traderJob, currency)) {
				continue;
			}
			
			long countOfRunningCoins = 0;
			if (!traderJob.isNew()) {
				countOfRunningCoins = traderRepository.countByTraderJobAndStateIn(traderJob, Trader.STATE_NEW, Trader.STATE_STARTED);
			}
	    		
			int countOfNewCoins = (int) (traderJob.getCurrencyCount() - countOfRunningCoins);
			if (countOfNewCoins > 0) {

				// Get the used value
				BigDecimal used = traderRepository.sumInvestmentByTraderJobAndStateIn(traderJob, Trader.STATE_NEW, Trader.STATE_STARTED);
				if (used == null) {
					used = BigDecimal.ZERO;
				}
				
				// calculate the new investment
				BigDecimal investment = CalcUtil.divide(CalcUtil.subtract(traderJob.getBalance(), used), new BigDecimal(new Integer(countOfNewCoins).toString()));
				
				BigDecimal buyPrice = calculateBuyPrice(candle);
				BigDecimal sellPrice = calculateSellPrice(candle);
				
				IOrderIntent orderIntent = new OrderIntent(currency, buyPrice, sellPrice, true);
				
				// Create trader and its orders
				
				Trader trader = createTrader(orderIntent.getCurrency().getId(), investment, traderJob, service, orderIntent);
				Order buyOrder = trader.getOrders().stream().filter(o -> o.isBuy()).findFirst().orElse(null);
				action.execute(currencyPair, buyOrder);
				orderRepository.save(buyOrder);
				
				//traderJob.getTraders().add(trader);
				//traderJobRepository.save(traderJob);
			}
		}
	}
	
    private Trader createTrader(String coin, BigDecimal investment, TraderJob traderJob, IExchangeService service, IOrderIntent intent) throws Exception {
		Trader t = TraderBuilder.create(coin, investment, BigDecimal.ZERO, traderJob);
		t.start();
		traderRepository.save(t);
		OrderBuilder.createAll(t, BigDecimal.ZERO, intent).forEach(o -> {
			service.onBeforeSaveOrder(o);
			orderRepository.saveOrder(o);
			t.getOrders().add(o);
		});
		return t;
    }
	
	protected abstract String getStrategy();
	protected abstract BigDecimal calculateBuyPrice(Candle candle);
	protected abstract BigDecimal calculateSellPrice(Candle candle);
	
	protected boolean filter(TraderJob traderJob, Currency currency) {
		List<Trader> tradersToIgnoreCoin = traderRepository.findAllByTraderJobAndStateNotIn(traderJob, Order.STATE_LIQUIDED, Order.STATE_CANCELED);
		for (Trader t : tradersToIgnoreCoin) {
			if (t.getCoin().equals(currency.getId()) && t.getTraderJob().getMarketCoin().equals(currency.getMarketCoin().getId())) {
				return false;
			}
		}
		return true;
	}
	
	public void whenTicker(String json) {
	}
	
	public void whenLiquided(String json) {
	}
	
	public void whenCanceled(String json) {
	}
	
}
