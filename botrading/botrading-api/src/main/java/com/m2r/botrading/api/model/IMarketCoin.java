package com.m2r.botrading.api.model;

import java.util.Map;

public interface IMarketCoin extends ICurrency {
	
	public Map<String, ICurrency> getCurrencies();
	
	default public ICurrency createAndAddCurrency(String id, String name) {
		ICurrency currency = CurrencyDefault.of(this, id, name);
		getCurrencies().put(id, currency);
		return currency;
	}
	
	default public ICurrency getCurrency(String coin) {
		return getCurrencies().get(coin);
	}
	
}
