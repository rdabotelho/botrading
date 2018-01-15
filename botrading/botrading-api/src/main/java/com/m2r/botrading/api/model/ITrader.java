package com.m2r.botrading.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ITrader {

	public Long getId();

	public String getCoin();

	public LocalDateTime getDateTime();

	public Integer getState();

	public BigDecimal getInvestment();

	public BigDecimal getParcel1();

	public BigDecimal getParcel2();

	public BigDecimal getParcel3();

	public BigDecimal getParcel4();
	
	public ITraderJob getTraderJob();

}
