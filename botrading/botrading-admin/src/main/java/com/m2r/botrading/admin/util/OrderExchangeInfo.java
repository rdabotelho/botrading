package com.m2r.botrading.admin.util;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.m2r.botrading.api.util.CalcUtil;

public class OrderExchangeInfo {

	@JsonIgnore
	private BigDecimal baseVolume;
	
	@JsonIgnore
	private BigDecimal last;
	
	@JsonIgnore
	private BigDecimal low;
	
	@JsonIgnore
	private BigDecimal available;
	
	@JsonIgnore
	private BigDecimal percentChange;

	public BigDecimal getBaseVolume() {
		return baseVolume;
	}

	public void setBaseVolume(BigDecimal baseVolume) {
		this.baseVolume = baseVolume;
	}

	public BigDecimal getLast() {
		return last;
	}

	public void setLast(BigDecimal last) {
		this.last = last;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getAvailable() {
		return available;
	}

	public void setAvailable(BigDecimal available) {
		this.available = available;
	}
	
	public void setPercentChange(BigDecimal percentChange) {
		this.percentChange = percentChange;
	}
	
	public BigDecimal getPercentChange() {
		return percentChange;
	}

	public String getFormatedBaseVolume() {
		return CalcUtil.formatBR(getBaseVolume());
	}

	public String getFormatedLast() {
		return CalcUtil.formatBR(getLast());
	}

	public String getFormatedLow() {
		return CalcUtil.formatBR(getLow());
	}

	public String getFormatedAvailable() {
		return CalcUtil.formatBR(getAvailable());
	}
	
	public String getFormatedPercentChange() {
		return CalcUtil.formatPercent(getPercentChange());
	}

}
