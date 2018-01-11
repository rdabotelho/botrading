package com.m2r.botrading.poloniex.model;

import java.math.BigDecimal;

import com.m2r.botrading.api.model.IBalance;

public class Balance implements IBalance {

	private String coin;
	private String available;
	private String onOrders;
	private String btcValue;
	
	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public BigDecimal getAvailable() {
		return new BigDecimal(available);
	}

	public void setAvailable(String available) {
		this.available = available;
	}

	public BigDecimal getOnOrders() {
		return new BigDecimal(onOrders);
	}

	public void setOnOrders(String onOrders) {
		this.onOrders = onOrders;
	}

	public BigDecimal getBtcValue() {
		return new BigDecimal(btcValue);
	}

	public void setBtcValue(String btcValue) {
		this.btcValue = btcValue;
	}

}
