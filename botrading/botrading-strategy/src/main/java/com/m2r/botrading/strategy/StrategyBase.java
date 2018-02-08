package com.m2r.botrading.strategy;

import java.util.Arrays;
import java.util.List;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.model.ITraderJobOptions;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.api.util.CalcUtil;

public abstract class StrategyBase implements IStrategy {

	protected boolean filter(IExchangeSession session, ITraderJob traderJob, String currencyPair) throws Exception {
		ITraderJobOptions options = traderJob.getOptions();
		if (options == null) {
			return true;
		}
		
		Currency currency = session.getCurrencyFactory().currencyPairToCurrency(currencyPair, session.getService());
				
		List<String> coins = Arrays.asList(options.getArrayCoins());
		if (!coins.isEmpty() && !coins.contains(currency.getId())) {
			return false;
		}
		
		ITicker ticker = session.getTikers().getTicker(currencyPair);
		if (CalcUtil.lessThen(ticker.getBaseVolume(), options.getMinimumVolume())) {
			return false;
		}
		
		if (CalcUtil.lessThen(ticker.getLast(), options.getMinimimPrice())) {
			return false;
		}
		
		return true;
	}	
	
	
}
