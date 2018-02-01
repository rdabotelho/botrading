package com.m2r.botrading.strategy;

import java.math.BigDecimal;

public interface IIntention {

	public String getUuidStrategy();
	public String getCurrencyPair();
	public BigDecimal getBuyPrice();
	public BigDecimal getSalePrice();
	
}
