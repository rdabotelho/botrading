package com.m2r.botrading.api.enums;

public enum DataChartPeriod {

	FIVE_MINUTES(300),
	FIFTEEN_MINUTES(900),
	THIRTY_MINUTESS(1800),
	TWO_HOURS(7200),
	FOUR_HOURS(14400),
	ONE_DAY(86400);

	private Integer seconds;
	
	DataChartPeriod(Integer period) {
		this.seconds = period;
	}
	
	public Integer getSeconds() {
		return seconds;
	}	
	
}
