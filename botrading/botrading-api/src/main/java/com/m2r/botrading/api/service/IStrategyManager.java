package com.m2r.botrading.api.service;

import java.util.List;

import com.m2r.botrading.api.model.IIntentionRequest;
import com.m2r.botrading.api.strategy.IStrategy;

public interface IStrategyManager {

	public List<IStrategy> getStrategies();	
	public IStrategy getStrategyByName(String name);
	public void registerIntentionRequest(IIntentionRequest intentionRequest);
	public void removeIntentionRequest(IIntentionRequest intentionRequest);
	public IIntentionRequest findIntentionRequest(Long traderJobId);

}
