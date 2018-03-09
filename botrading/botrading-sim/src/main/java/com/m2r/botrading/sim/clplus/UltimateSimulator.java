package com.m2r.botrading.sim.clplus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.poloniex.PoloniexConst;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.sim.SimulatorBuilder;
import com.m2r.botrading.sim.SimulatorBuilder.Simulator;

public class UltimateSimulator {
	
	public static void execute(IExchangeService service, String marketCoin) throws Exception {
		LocalDateTime from = LocalDateTime.now().minusDays(30);
		LocalDateTime to = LocalDateTime.now();
		
		List<Simulator> simmulators = new ArrayList<>();
		
		SimulatorBuilder builder = new SimulatorBuilder();
		
		for (String currencyPair : PoloniexConst.TOP_20(Currency.BTC)) {
			
			Currency currency = service.getCurrencyFactory().currencyPairToCurrency(currencyPair, service);
			
			Simulator simmulator = builder
					.withService(service)
					.withMarketCoin(marketCoin)
					.withCoin(currency.getId())
					.withPeriod(from, to)
					.withAmount(new BigDecimal("1000.00"))
					.withoutStopLoss()
					.withCandleSizeFactor(new BigDecimal("6"))
					.withSellFactor(new BigDecimal("1.0"))
					.withoutTSSL()
					.withoutDelayToBuy()
					.build();
				
			simmulator.run();
			
			UltimateReport.print("");
			
			simmulators.add(simmulator);
		}
		
		simmulators = simmulators
			.stream()
			.sorted((it,other) -> it.getTotalProfit().compareTo(other.getTotalProfit()))
			.collect(Collectors.toList());
		
		UltimateReport.printSummary(simmulators, from, to);		
	}
	
	public static void findOportunity() {
		
	}
	
	public static void main(String[] args) throws Exception {
		IExchangeService service = new PoloniexExchange().init();		
		execute(service, Currency.BTC);
	}
	
}
