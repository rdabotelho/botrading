package com.m2r.botrading.poloniex.model;

import com.m2r.botrading.api.model.CurrencyFactory;
import com.m2r.botrading.api.model.ICurrencyPairConverter;

public class PoloniexCurrencyFactory extends CurrencyFactory {

	private static CurrencyFactory instance;
	
	public static CurrencyFactory getInstance() {
		if (instance == null) {
			instance = new PoloniexCurrencyFactory();
		}
		return instance;
	}
	
	@Override
	public ICurrencyPairConverter getCurrencyPairConverter() {
		return PoloniexCurrencyPairConverter.getInstance();
	}

}
