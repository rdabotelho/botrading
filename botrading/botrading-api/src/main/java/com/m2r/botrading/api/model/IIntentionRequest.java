package com.m2r.botrading.api.model;

import java.util.Map;

import com.m2r.botrading.api.strategy.IStrategy;

public interface IIntentionRequest {

	public Long getTraderJobId();
	public IStrategy getStrategy();
	public String getMarketCoin();
	public Map<String, IIntention> getIntentions();
	public boolean haveReserve();

}
