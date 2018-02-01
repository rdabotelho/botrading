package com.m2r.botrading.api.service;

import java.util.List;

import com.m2r.botrading.api.strategy.IStrategy;

public interface IStrategyManager {

	public List<IStrategy> getStrategies();	
	public IStrategy getStrategyByName(String name);
	public <T> T getEnviromentObject(Class<T> classOfT, String id);

}
