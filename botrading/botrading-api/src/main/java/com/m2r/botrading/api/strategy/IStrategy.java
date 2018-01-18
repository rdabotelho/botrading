package com.m2r.botrading.api.strategy;

import java.util.List;

import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.service.IExchangeSession;

public interface IStrategy {

	public String getName();
	public List<IOrderIntent> selectOrderIntent(IExchangeSession session, int count, List<String> ignoredCoins);
	
}
