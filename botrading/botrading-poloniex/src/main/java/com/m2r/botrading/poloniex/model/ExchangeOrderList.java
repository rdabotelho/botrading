package com.m2r.botrading.poloniex.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IOrderList;

public class ExchangeOrderList<T extends IExchangeOrder> implements IOrderList {

	private Map<String, List<IExchangeOrder>> ordersMap;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends IExchangeOrder> IOrderList of(Map<String, List<T>> ordersMap) {
		IOrderList eb = new ExchangeOrderList(ordersMap);
		return eb;
	}
	
	public ExchangeOrderList(Map<String, List<IExchangeOrder>> ordersMap) {
		this.ordersMap = ordersMap;
	}
	
	@Override
	public List<IExchangeOrder> getOrders(String currencyPair) {
		List<IExchangeOrder> list = ordersMap.get(currencyPair);
		return list != null ? new ArrayList<>(list) : null;
	}

}
