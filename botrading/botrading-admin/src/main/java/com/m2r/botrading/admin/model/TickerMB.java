package com.m2r.botrading.admin.model;

import java.math.BigDecimal;

import com.m2r.botrading.api.model.ITicker;

public class TickerMB implements ITicker {

	private String currencyPair;
	private String high;
	private String low;
	private String vol;
	private String last;
	private String buy;
	private String sell;
	private String date;

	@Override
	public String getId() {
		return this.currencyPair.toString();
	}
	
	public void setCurrencyPair(String currencyPair) {
		this.currencyPair = currencyPair;
	}

	@Override
	public String getCurrencyPair() {
		return this.currencyPair;
	}

	@Override
	public BigDecimal getLast() {
		return new BigDecimal(last);
	}

	@Override
	public BigDecimal getLowestAsk() {
		return new BigDecimal(low);
	}

	@Override
	public BigDecimal getHighestBid() {
		return new BigDecimal(high);
	}

	@Override
	public BigDecimal getPercentChange() {
		return null;
	}

	@Override
	public BigDecimal getBaseVolume() {
		return null;
	}

	@Override
	public BigDecimal getQuoteVolume() {
		return new BigDecimal(vol);
	}

	@Override
	public String getIsFrozen() {
		return null;
	}
	
	@Override
	public BigDecimal getHigh24hr() {
		return null;
	}

	@Override
	public BigDecimal getLow24hr() {
		return null;
	}
	
	public String getBuy() {
		return buy;
	}
	
	public String getSell() {
		return sell;
	}
	
	public String getDate() {
		return date;
	}

}
