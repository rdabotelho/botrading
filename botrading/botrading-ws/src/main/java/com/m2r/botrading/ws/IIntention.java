package com.m2r.botrading.ws;

import java.math.BigDecimal;

public interface IIntention {

	public String getCurrencyPair();
	public BigDecimal getBuyPrice();
	public BigDecimal getSalePrice();
	
}
