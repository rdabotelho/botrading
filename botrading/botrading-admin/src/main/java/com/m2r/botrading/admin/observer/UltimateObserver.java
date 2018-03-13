package com.m2r.botrading.admin.observer;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.poloniex.model.ChartData;
import com.m2r.botrading.sim.clplus.Candle;
import com.m2r.botrading.sim.clplus.UltimateAnalyze;
import com.m2r.botrading.sim.clplus.UltimateSimulator;

@Component
@Transactional
@Scope(value="application")
public class UltimateObserver extends StrategyWSObserver {

	private static final String STRATEGY_NAME = "CATLEAP ULTIMATE";
	
	@Override
	protected String getStrategy() {
		return STRATEGY_NAME;
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	@Override
	public void whenChartdata30(String currencyPair, String data) {
		if (currencyPair.equals("BTC_XRP")) {
			findOpportunity(currencyPair, data);
		}
	}
	
	@Async
	protected void findOpportunity(String currencyPair, String data) {
		List<IChartData> list = new Gson().fromJson(data,  new TypeToken<List<ChartData>>(){}.getType());
		IExchangeService service = getExchangeManager().getExchangeService();
		Currency currency = service.getCurrencyFactory().currencyPairToCurrency(currencyPair, service);
		try {
			Candle candle = UltimateAnalyze.ultimateAnalyze(currency.getId(), list, COUNT_PERIODS, CANDLE_SIZE_FACTOR);
			//if (candle.isOpportunity()) {
				opportunityFound(service, currencyPair, candle, this::buy);
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected boolean filter(Currency currency, int count) {
		if (UltimateSimulator.isConcluded()) {
			for (int i=0; i<count; i++) {
				if (UltimateSimulator.getSimulators().get(i).getCoin().equals(currency.getId())) {
					return true;
				}
			}
		}
		return false;
	}

}
