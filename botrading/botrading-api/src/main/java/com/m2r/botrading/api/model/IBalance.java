package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public interface IBalance {

	public String getCoin();
	public void setCoin(String coin);
	public BigDecimal getAvailable();
	public BigDecimal getOnOrders();
	public BigDecimal getBtcValue();
	
}
