package com.m2r.botrading.ws.exchange;

public class Chardata30Pair {

	private String currencyPair;
	private String data;

	public Chardata30Pair(String currencyPair, String data) {
		this.currencyPair = currencyPair;
		this.data = data;
	}

	public String getCurrencyPair() {
		return currencyPair;
	}

	public void setCurrencyPair(String currencyPair) {
		this.currencyPair = currencyPair;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
