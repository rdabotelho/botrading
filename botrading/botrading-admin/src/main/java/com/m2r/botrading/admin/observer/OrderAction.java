package com.m2r.botrading.admin.observer;

import com.m2r.botrading.admin.model.Order;

@FunctionalInterface
public interface OrderAction {
	public void execute(String currencyPair, Order order);
}
