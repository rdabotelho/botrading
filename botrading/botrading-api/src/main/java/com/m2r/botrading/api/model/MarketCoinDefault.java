package com.m2r.botrading.api.model;

import java.util.HashMap;
import java.util.Map;

public class MarketCoinDefault extends CurrencyDefault implements IMarketCoin {

	private Map<String, ICurrency> currencies;
	
	public static MarketCoinDefault of(String id) {
		return new MarketCoinDefault(id);
	}
	
	public MarketCoinDefault(String id) {
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
