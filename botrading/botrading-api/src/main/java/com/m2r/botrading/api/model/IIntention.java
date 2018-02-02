package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public interface IIntention {

	default public String getId() {
		return String.format("%s-%s", getUuidStrategy(), getCurrencyPair());
	};
	public String getUuidStrategy();
	public String getCurrencyPair();
	public BigDecimal getBuyPrice();
	public BigDecimal getSalePrice();
	
}
