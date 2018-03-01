package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public interface ITraderJobOptions {

	public BigDecimal getMinimimPrice();
	
	public BigDecimal getMinimumVolume();
	
	public BigDecimal getMaximumChange();
	
	public String[] getArrayCoins();
	
}
