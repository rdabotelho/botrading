package com.m2r.botrading.poloniex.model;

import com.m2r.botrading.api.model.CurrencyPairIds;
import com.m2r.botrading.api.model.ICurrencyPairConverter;

public class PoloniexCurrencyPairConverter implements ICurrencyPairConverter {

	private static ICurrencyPairConverter instance;
	
	public static ICurrencyPairConverter getInstance() {
		if (instance == null) {
			instance = new PoloniexCurrencyPairConverter();
		}
		return instance;
	}
	
	@Override
	public String currencyPairToString(CurrencyPairIds object) {
		return String.format("%s_%s", object.getMarketCoinId(), object.getCurrencyId());
	}

	@Override
	public CurrencyPairIds stringToCurrencyPair(String text) {
		String[] parts = text.split("_");
		return CurrencyPairIds.of(parts[0], parts[1]);
	}

}
