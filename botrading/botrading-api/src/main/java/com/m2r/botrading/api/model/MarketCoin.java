package com.m2r.botrading.api.model;

import java.util.HashMap;
import java.util.Map;

public class MarketCoin extends Currency implements IMarketCoin {

	private Map<String, ICurrency> currencies;
	
	public static MarketCoin of(String id) {
		return new MarketCoin(id);
	}
	
	public MarketCoin(String id) {
		super(id, id);
		this.currencies = new HashMap<>();
	}
	
	@Override
	public Map<String, ICurrency> getCurrencies() {
		return this.currencies;
	}
	
	public static String currencyPairToMarketCoinId(String currencyPair) {
		return currencyPair.split("_")[0];
	}
	
	public static String currencyPairToCurrencyId(String currencyPair) {
		return currencyPair.split("_")[1];
	}

}
