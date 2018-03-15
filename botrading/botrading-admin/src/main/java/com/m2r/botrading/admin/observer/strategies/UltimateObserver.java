package com.m2r.botrading.admin.observer.strategies;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.poloniex.model.ChartData;
import com.m2r.botrading.sim.clplus.Candle;
import com.m2r.botrading.sim.clplus.UltimateAnalyze;

@Component
@Transactional
@Scope(value="application")
public class UltimateObserver extends StrategyWSObserver {

	private static final String STRATEGY_NAME = "CATLEAP ULTIMATE";
	private static final BigDecimal SELL_FACTOR = new BigDecimal("1.0");
	private static final BigDecimal DOIS = new BigDecimal("2.0");
	
	private static final int EXPIRATION_TIME = 5;
	
	private Map<String,String> ordereds = new HashMap<>();
	
	@Override
	protected String getStrategy() {
		return STRATEGY_NAME;
	}
	
	@Override
	public void whenChartdata30(String currencyPair, String data) {
		findOpportunity(currencyPair, data);
		checkExpiration(currencyPair);
	}
	
	@Async
	protected void findOpportunity(String currencyPair, String data) {
		List<IChartData> list = new Gson().fromJson(data,  new TypeToken<List<ChartData>>(){}.getType());
		IExchangeService service = getExchangeManager().getExchangeService();
		Currency currency = service.getCurrencyFactory().currencyPairToCurrency(currencyPair, service);
		try {
			Candle candle = UltimateAnalyze.ultimateAnalyze(currency.getId(), list, COUNT_PERIODS, CalcUtil.divide(CANDLE_SIZE_FACTOR, DOIS));
			if (candle.isOpportunity()) {
				LOG.info(String.format("OPPORTUNITY: %s %s [%s / %s]", candle.getCoin(), CalcUtil.formatUS(candle.getClose()), CalcUtil.formatUS(candle.getLength()), CalcUtil.formatUS(candle.getLengthToFilter())));
				opportunityFound(service, currencyPair, candle, this::buy);
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void buy(String currencyPair, Order order) {
		super.buy(currencyPair, order);
		if (order.getOrderNumber() != null) {
			ordereds.put(currencyPair, order.getOrderNumber());
		}
	}

	private void checkExpiration(String currencyPair) {
		String orderNumber = ordereds.get(currencyPair);
		if (orderNumber != null) {
			Order order = orderRepository.findByOrderNumber(orderNumber);
			if (order != null && order.isBuy()) {
				if (!order.isOrdered()) {
					ordereds.remove(orderNumber);
				}
				else if (order.isExpired(EXPIRATION_TIME)) {
					cancel(currencyPair, order);
					orderRepository.saveOrder(order.confirmCancellation());
					LOG.info("Order (buy) " + order.getOrderNumber() + " canceled by expiration.");
				}
			}
		}
	}
	
	@Override
	protected BigDecimal calculateBuyPrice(Candle candle) {
		return CalcUtil.subtract(candle.getClose(), candle.getLengthToFilter());
	}
	
	@Override
	protected BigDecimal calculateSellPrice(Candle candle) {
		return CalcUtil.add(candle.getClose(), CalcUtil.multiply(candle.getLength(), SELL_FACTOR));
	}
	
}
