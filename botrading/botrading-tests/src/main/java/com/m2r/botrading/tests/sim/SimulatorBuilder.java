package com.m2r.botrading.tests.sim;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.m2r.botrading.api.service.IExchangeService;

public class SimulatorBuilder {

	public SimulatorBuilder() {
	}
	
	public ISimulatorMarketCoin withService(IExchangeService service) {
		return new SimulatorFlow().withService(service);
	}
	
	public static interface ISimulatorMarketCoin {
		public ISimulatorCoin withMarketCoin(String marketCoin);
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
		public ISimulatorCandleSizeFactor withoutStopLoss();
		public ISimulatorCandleSizeFactor withStopLoss(BigDecimal stopLoss);
		public ISimulatorCandleSizeFactor withStopLossOfCandleLength();
	}
	public static interface ISimulatorCandleSizeFactor {
		public ISimulatorSellFactor withCandleSizeFactor(BigDecimal candleSizeFactor);
	}	
	public static interface ISimulatorSellFactor {
		public ISimulatorTSSL withSellFactor(BigDecimal sellFactor);
	}	
	public static interface ISimulatorTSSL {
		public ISimulatorDelayToBuy withoutTSSL();
		public ISimulatorDelayToBuy withTSSL(BigDecimal tsslPercent);
	}	
	public static interface ISimulatorDelayToBuy {
		public ISimulatorBuild withoutDelayToBuy();
		public ISimulatorBuild withDelayToBuy(BigDecimal delayToBuyPercent);
	}	
	public static interface ISimulatorBuild {
		public Simulator build();
	}
	public static interface Simulator {
		public void run();
		public String getCoin();
		public BigDecimal getAmount();
		public Integer getTotal();
		public BigDecimal getTotalProfit();
		public BigDecimal getTotalFee();
		public BigDecimal getTotalBalance();
		public boolean isStucked();
	}
	
}
