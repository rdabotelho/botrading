package com.m2r.botrading.admin.util;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2r.botrading.api.util.CalcUtil;

public class AccountExchangeInfo {

	private BigDecimal amount;
    
	@JsonIgnore
	private BigDecimal coinBalance;
    
	@JsonIgnore
	private BigDecimal realBalance;

	public BigDecimal getAmount() {
		return amount;
	}
	
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public BigDecimal getCoinBalance() {
		return coinBalance;
	}

	public void setCoinBalance(BigDecimal coinBalance) {
		this.coinBalance = coinBalance;
	}

	public BigDecimal getRealBalance() {
		return realBalance;
	}

	public void setRealBalance(BigDecimal realBalance) {
		this.realBalance = realBalance;
	}
	
	public String getFormatedAmount() {
		return CalcUtil.formatBR(getAmount());
	}
	
	public String getFormatedCoinBalance() {
		return CalcUtil.formatBR(getCoinBalance());
	}
	
	public String getFormatedRealBalance() {
		return CalcUtil.formatReal(getRealBalance());
	}

}
