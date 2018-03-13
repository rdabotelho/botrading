package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;

import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.service.IStrategyManager;

public class UltimateStrategy extends StrategyBase {

	private static final String NAME = "CATLEAP ULTIMATE";
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<IOrderIntent> selectOrderIntent(IStrategyManager manager, IExchangeSession session, ITraderJob traderJob, int count, List<String> ignoredCoins) {
		List<IOrderIntent> list = new ArrayList<>();
		return list;
	}
	
	@Override
	public boolean isReplacePrice() {
		return false;
	}
	
	@Override
	public String getUuid() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public String getInfo() {
		return "";
	}
	
}