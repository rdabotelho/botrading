package com.m2r.botrading.strategy;

import com.m2r.botrading.api.strategy.StrategyRepository;

public class DefaultStrategyRepository extends StrategyRepository {

	@Override
	protected void init() {
		DynamicStrategy.loadDynamicStrategies().forEach(s -> {
			registerStrategy(s);
		});
		this.registerStrategy(new LowBollingerStrategy());
		this.registerStrategy(new CatLeapStrategy());
	}

}
