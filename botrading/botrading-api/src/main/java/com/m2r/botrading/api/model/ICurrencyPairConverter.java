package com.m2r.botrading.api.model;

public interface ICurrencyPairConverter {

	public String currencyPairToString(CurrencyPairIds currencyPairIds);
	public CurrencyPairIds stringToCurrencyPair(String text);
	
}
