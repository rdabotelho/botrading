package com.m2r.botrading.api.model;

import java.util.List;

public interface IOrderList {
	
	public List<IExchangeOrder> getOrders(String currencyPair);
	public List<IExchangeOrder> getOrders();

}
