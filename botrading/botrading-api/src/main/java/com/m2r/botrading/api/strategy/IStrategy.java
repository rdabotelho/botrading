package com.m2r.botrading.api.strategy;

import java.util.List;

import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.service.IStrategyManager;

public interface IStrategy {

	public String getUuid();
	public String getName();
	public String getDescription();
	public String getInfo();
	public List<IOrderIntent> selectOrderIntent(IStrategyManager manager, IExchangeSession session, ITraderJob traderJob, int count, List<String> ignoredCoins);
	public boolean isReplacePrice();
	
}
