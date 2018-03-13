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
	
	private static boolean concluded = false;
	private static List<Simulator> simmulators = new ArrayList<>();
	
	public static void execute(IExchangeService service, String marketCoin) throws Exception {
		LocalDateTime from = LocalDateTime.now().minusDays(30);
		LocalDateTime to = LocalDateTime.now();
		
		List<Simulator> simmulatorsTemp = new ArrayList<>();
		
		SimulatorBuilder builder = new SimulatorBuilder();

		int i = 0;
		for (String currencyPair : PoloniexConst.TOP_20(Currency.BTC)) {
			i++;
			if (i > 3) {
				break;
			}
			
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
			
			simmulatorsTemp.add(simmulator);
		}
		
		simmulatorsTemp = simmulatorsTemp
			.stream()
			.sorted((it,other) -> other.getTotalProfit().compareTo(it.getTotalProfit()))
			.collect(Collectors.toList());
		
		UltimateReport.printSummary(simmulatorsTemp, from, to);		
		
		concluded = false;
		simmulators.clear();
		simmulators.addAll(simmulatorsTemp);
		concluded = true;
	}
	
	public static boolean isConcluded() {
		return concluded;
	}
	
	public static List<Simulator> getSimulators() {
		return simmulators;
	}
	
	public static void main(String[] args) throws Exception {
		IExchangeService service = new PoloniexExchange().init();		
		execute(service, Currency.BTC);
	}
	
}
