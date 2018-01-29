package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public interface IOrderIntent {

	public Currency getCurrency();
	public BigDecimal getBuyPrice();
	public BigDecimal getSellPrice();
	public boolean isReplacePrice();
	
}
