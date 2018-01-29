package com.m2r.botrading.api.model;

public class Currency {

	public static final String BNB = "BNB";
	public static final String BTC = "BTC";
	public static final String ETH = "ETH";
	public static final String XMR = "XMR";
	public static final String USDT = "USDT";
	
	private CurrencyFactory factory;
	
	private MarketCoin marketCoin;
	private String id;
	private String name;

	public Currency(CurrencyFactory factory, MarketCoin marketCoin, String id, String name) {
		this.factory = factory;
		this.marketCoin = marketCoin;
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}
	
	public MarketCoin getMarketCoin() {
		return this.marketCoin;
	}
	
	public String getCurrencyPair() {
		return factory.currencyToCurrencyPair(this);
	}

}
