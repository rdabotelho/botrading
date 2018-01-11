package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public interface IExchangeOrder {

	public String getOrderNumber();
	public String getType();
	public BigDecimal getRate();
	public BigDecimal getAmount();
	public BigDecimal getTotal();
	
}
