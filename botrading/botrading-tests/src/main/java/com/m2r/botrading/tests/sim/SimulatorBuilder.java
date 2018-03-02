package com.m2r.botrading.tests.sim;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.m2r.botrading.api.service.IExchangeService;

public class SimulatorBuilder {

	public SimulatorBuilder() {
	}
	
	public ISimulatorCoin withService(IExchangeService service) {
		return new SimulatorFlow().withService(service);
	}
	
	public static interface ISimulatorCoin {
		public ISimulatorWithPeriod withCoin(String coin);
	}
	public static interface ISimulatorWithPeriod {
		public ISimulatorAmount withPeriod(LocalDateTime to, LocalDateTime from);
	}
	public static interface ISimulatorAmount {
		public ISimulatorStopLoss withAmount(BigDecimal amount);
	}
	public static interface ISimulatorStopLoss {
		public ISimulatorBuild withoutStopLoss();
		public ISimulatorBuild withStopLoss(BigDecimal stopLoss);
		public ISimulatorBuild withStopLossOfCandleLength();
	}
	public static interface ISimulatorBuild {
		public Simulator build();
	}
	public static interface Simulator {
		public void run();
	}
	
}
