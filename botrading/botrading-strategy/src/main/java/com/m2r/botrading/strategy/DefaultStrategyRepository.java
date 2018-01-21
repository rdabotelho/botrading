package com.m2r.botrading.strategy;

import com.m2r.botrading.api.strategy.StrategyRepository;

public class DefaultStrategyRepository extends StrategyRepository {

	@Override
	protected void init() {
		this.registerStrategy(new HighVolumeCoinsStrategy());
		this.registerStrategy(new HighCoinsStrategy());
		this.registerStrategy(new MiddleCoinsStrategy());
		this.registerStrategy(new LowCoinsStrategy());
		this.registerStrategy(new RandomCoinsStrategy());
		this.registerStrategy(new CatLeapStrategy());
		this.registerStrategy(new LowBollingerStrategy());
	}

}
