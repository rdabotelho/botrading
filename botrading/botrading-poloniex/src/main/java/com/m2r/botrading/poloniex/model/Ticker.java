package com.m2r.botrading.poloniex.model;

import java.math.BigDecimal;

import com.m2r.botrading.api.model.ITicker;

public class Ticker implements ITicker {
	
	private String id;
	private String currencyPair;
	private String last;
	private String lowestAsk;
	private String highestBid;
	private String percentChange;
	private String baseVolume;
	private String quoteVolume;
	private String isFrozen;
	private String high24hr;
	private String low24hr;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@Override
	public String getCurrencyPair() {
		return this.currencyPair;
	}
	
	public void setCurrencyPair(String currencyPair) {
		this.currencyPair = currencyPair;
	}

	public BigDecimal getLast() {
		return new BigDecimal(last);
	}

	public void setLast(String last) {
		this.last = last;
	}

	public BigDecimal getLowestAsk() {
		return new BigDecimal(lowestAsk);
	}

	public void setLowestAsk(String lowestAsk) {
		this.lowestAsk = lowestAsk;
	}

	public BigDecimal getHighestBid() {
		return new BigDecimal(highestBid);
	}

	public void setHighestBid(String highestBid) {
		this.highestBid = highestBid;
	}

	public BigDecimal getPercentChange() {
		return new BigDecimal(percentChange);
	}

	public void setPercentChange(String percentChange) {
		this.percentChange = percentChange;
	}

	public BigDecimal getBaseVolume() {
		return new BigDecimal(baseVolume);
	}

	public void setBaseVolume(String baseVolume) {
		this.baseVolume = baseVolume;
	}

	public BigDecimal getQuoteVolume() {
		return new BigDecimal(quoteVolume);
	}

	public void setQuoteVolume(String quoteVolume) {
		this.quoteVolume = quoteVolume;
	}

	public String getIsFrozen() {
		return isFrozen;
	}

	public void setIsFrozen(String isFrozen) {
		this.isFrozen = isFrozen;
	}

	public BigDecimal getHigh24hr() {
		return new BigDecimal(high24hr);
	}

	public void setHigh24hr(String high24hr) {
		this.high24hr = high24hr;
	}

	public BigDecimal getLow24hr() {
		return new BigDecimal(low24hr);
	}

	public void setLow24hr(String low24hr) {
		this.low24hr = low24hr;
	}

}
