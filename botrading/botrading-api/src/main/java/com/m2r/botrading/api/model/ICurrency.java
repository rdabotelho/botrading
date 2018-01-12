package com.m2r.botrading.api.model;

public interface ICurrency {

	public static final String BTC = "BTC";
	public static final String ETH = "ETH";
	public static final String XMR = "XMR";
	public static final String USDT = "USDT";
	
	public String getId();
	public String getName();
	public IMarketCoin getMarketCoin();
    default public String getCurrencyPair() {
		return getMarketCoin().getId() + "_" + getId();
    }
	
}
