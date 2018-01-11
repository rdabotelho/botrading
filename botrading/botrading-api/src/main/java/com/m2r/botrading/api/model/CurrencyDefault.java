package com.m2r.botrading.api.model;

public class CurrencyDefault implements ICurrency {

	private IMarketCoin marketCoin;
	private String id;
	private String name;
	
	public static ICurrency of(IMarketCoin marketCoin, String id, String name) {
		return new CurrencyDefault(marketCoin, id, name);
	}
	
	public CurrencyDefault(IMarketCoin marketCoin, String id, String name) {
		this.marketCoin = marketCoin;
		this.id = id;
		this.name = name;
	}
	
	public CurrencyDefault(String id, String name) {
		this.id = id;
		this.name = name;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public IMarketCoin getMarketCoin() {
		return this.marketCoin;
	}

}
