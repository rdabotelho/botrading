package com.m2r.botrading.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ITraderJob {

	public Long getId();

	public LocalDateTime getDateTime();

	public String getStrategy();

	public Integer getCurrencyCount();
	
	public BigDecimal getAmount();

	public BigDecimal getTradingPercent();
	
	public BigDecimal getTradingAmount();

	public IAccount getAccount();
	
	public String getMarketCoin();

}
