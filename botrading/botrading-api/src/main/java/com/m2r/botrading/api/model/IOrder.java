package com.m2r.botrading.api.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IOrder {

	public Long getId();

	public Integer getParcel();
	
	public String getOrderNumber();

	public LocalDateTime getDateTime();

	public Integer getState();

	public Integer getKind();

	public BigDecimal getPrice();
	
	public void setPrice(BigDecimal price);

	public BigDecimal getAmount();
	
	public void setAmount(BigDecimal ammount);
	
	public BigDecimal getTotal();

	public String getLog();

	public BigDecimal getFee();
	
	public BigDecimal getBalance();
	
	public ITrader getTrader();
	
}
