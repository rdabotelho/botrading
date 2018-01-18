package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public interface IOrderIntent {

	public ICurrency getCurrency();
	public BigDecimal getBuyPrice();
	public BigDecimal getSellPrice();
	default public boolean isReplacePrice() {
		return getBuyPrice() != null && getSellPrice() != null;
	}
	
}
