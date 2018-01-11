package com.m2r.botrading.api.strategy;

import java.util.List;

import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.service.IExchangeSession;

public interface IStrategy {

	public String getName();
	public List<ICurrency> selectCurrencies(IExchangeSession session, int count, List<String> ignoredCoins);
	
}
