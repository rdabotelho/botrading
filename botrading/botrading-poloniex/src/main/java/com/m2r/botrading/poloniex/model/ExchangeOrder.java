package com.m2r.botrading.poloniex.model;

import java.math.BigDecimal;

import com.m2r.botrading.api.model.IExchangeOrder;

public class ExchangeOrder implements IExchangeOrder {

	private String orderNumber;
	private String type;
	private String rate;
	private String amount;
	private String total;

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public BigDecimal getRate() {
		return new BigDecimal(rate);
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	public BigDecimal getAmount() {
		return new BigDecimal(amount);
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public BigDecimal getTotal() {
		return new BigDecimal(total);
	}

	public void setTotal(String total) {
		this.total = total;
	}

}
