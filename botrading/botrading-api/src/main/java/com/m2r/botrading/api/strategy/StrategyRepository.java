package com.m2r.botrading.api.strategy;

import java.util.ArrayList;
import java.util.List;

public abstract class StrategyRepository {

	private List<IStrategy> strategies;
	
	public StrategyRepository() {
		this.strategies = new ArrayList<>();
		this.init();
	}
	
	protected abstract void init();
	
	protected void registerStrategy(IStrategy strategy) {
		this.strategies.add(strategy);
	}
	
	public List<IStrategy> getStrategies() {
		return this.strategies;
	}
	
	public IStrategy getStrategy(String name) {
		return this.strategies.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
	}
	
}
