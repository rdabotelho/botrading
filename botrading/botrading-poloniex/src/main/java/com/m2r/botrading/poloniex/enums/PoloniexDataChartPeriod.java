package com.m2r.botrading.poloniex.enums;

import com.m2r.botrading.api.model.IDataChartPeriod;

public enum PoloniexDataChartPeriod implements IDataChartPeriod {

	FIVE_MINUTES("300"),
	FIFTEEN_MINUTES("900"),
	THIRTY_MINUTESS("1800"),
	TWO_HOURS("7200"),
	FOUR_HOURS("14400"),
	ONE_DAY("86400");

	private String seconds;
	
	PoloniexDataChartPeriod(String period) {
		this.seconds = period;
	}
	
	public String getSeconds() {
		return seconds;
	}	
	
}
