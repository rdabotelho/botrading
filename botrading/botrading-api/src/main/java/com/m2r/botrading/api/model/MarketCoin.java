package com.m2r.botrading.api.model;

import java.util.HashMap;
import java.util.Map;

public class MarketCoin {
	
	private Currency currency;
	public Map<String, Currency> currencies;
	
	public MarketCoin(Currency currency) {
		this.currencies = new HashMap<>();
		this.currency = currency;
	}
	
	public String getId() {
		return this.currency.getId();
	}

	public Currency getCurrency() {
		return this.currency;
	}
	
	public void addCurrency(Currency currency) {
		currencies.put(currency.getId(), currency);
	}
	
	public Currency getCurrency(String coin) {
		return currencies.get(coin);
	}
	
	public Map<String, Currency> getCurrencies() {
		return currencies;
	}
	
}
