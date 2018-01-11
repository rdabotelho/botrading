package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public interface ITicker {

	public String getId();
	public String getCurrencyPair();
	public void setCurrencyPair(String currencyPair);
	public BigDecimal getLast();
	public BigDecimal getLowestAsk();
	public BigDecimal getHighestBid();
	public BigDecimal getPercentChange();
	public BigDecimal getBaseVolume();
	public BigDecimal getQuoteVolume();
	public String getIsFrozen();
	public BigDecimal getHigh24hr();
	public BigDecimal getLow24hr();	
	
}
