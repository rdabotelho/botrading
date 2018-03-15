package com.m2r.botrading.ws.exchange;

public enum ExchangeTopicEnum {

	UPDATE("botrading.ws.update"),
	BUY("botrading.ws.buy"), 
	SELL("botrading.ws.sell"), 	 
	CANCEL("botrading.ws.cancel"), 
	LIQUIDED("botrading.ws.liquided"), 
	CANCELED("botrading.ws.canceled"), 	 
	TICKER("botrading.ws.ticker"),
	CHARTDATA30("botrading.ws.chartdata30");
	
	private String id;
	
	private ExchangeTopicEnum(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
}
