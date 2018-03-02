package com.m2r.botrading.tests.clplus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.tests.sim.SimulatorBuilder;

public class MainTest {

	public static void main(String[] args) {
		
		IExchangeService service = new PoloniexExchange().init();		
		SimulatorBuilder builder = new SimulatorBuilder();
		
		builder
			.withService(service)
			.withCoin("ETC")
			.withPeriod(LocalDateTime.now().minusDays(90), LocalDateTime.now())
			.withAmount(new BigDecimal("1000.00"))
			.withoutStopLoss()
			.build()
			.run();
	}
	
}
