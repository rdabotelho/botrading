package com.m2r.botrading.ws.exchange;

public class JsonOrderResult {

	String orderNumber;

	public JsonOrderResult() {
	}

	public JsonOrderResult(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public String getOrderNumber() {
		return orderNumber;
	}

	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}
	
}
