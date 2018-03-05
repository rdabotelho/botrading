package com.m2r.botrading.tests.clplus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.tests.sim.SimulatorBuilder;
import com.m2r.botrading.tests.sim.SimulatorBuilder.Simulator;

public class MainTest {

	public static void main(String[] args) throws Exception {
		
		LocalDateTime dateRef = LocalDateTime.of(2018, 2, 1, 0, 0);
		
		LocalDateTime from = dateRef.minusDays(30); //LocalDateTime.now().minusDays(360);
		LocalDateTime to = dateRef;// LocalDateTime.now();
		List<Simulator> simmulators = new ArrayList<>();
		
		IExchangeService service = new PoloniexExchange().init();		
		SimulatorBuilder builder = new SimulatorBuilder();
		
		MarketCoin marketCoin = service.getMarketCoin("BTC");
		
		for (ITicker ticker : getTheBestCoins(service, marketCoin)) {
			
			Currency currency = service.getCurrencyFactory().currencyPairToCurrency(ticker.getCurrencyPair(), service);
			
			Simulator simmulator = builder
					.withService(service)
					.withMarketCoin(marketCoin.getId())
					.withCoin(currency.getId())
					.withPeriod(from, to)
					.withAmount(new BigDecimal("1000.00"))
					.withoutStopLoss()
					.withCandleSizeFactor(new BigDecimal("6"))
					.withSellFactor(new BigDecimal("1.0"))
					.withoutTSSL()// withTSSL(new BigDecimal("0.1"))
					.withoutDelayToBuy() // withDelayToBuy(new BigDecimal("-0.1"))
					.build();
				
			simmulator.run();
			
			log("");
			
			simmulators.add(simmulator);
		}
		
		simmulators = simmulators
			.stream()
			.sorted((it,other) -> it.getTotalProfit().compareTo(other.getTotalProfit()))
			.collect(Collectors.toList());
		logSummary(simmulators, from, to);
		
	}
	
	private static List<ITicker> getTheBestCoinsFixed(IExchangeService service, MarketCoin marketCoin) throws Exception {
		IExchangeSession session = service.getSession(marketCoin, true, true);
		final String[] COINS = new String[]{"BTC_XEM","BTC_BTS","BTC_XRP","BTC_ETC","BTC_STR"};
		List<ITicker> list = session.getTikers().getTickers()
			.stream()
			.filter(it -> {
				return Arrays.binarySearch(COINS, it.getCurrencyPair()) > -1;
			})
			.collect(Collectors.toList());
		return list;		
	}
	
	private static List<ITicker> getTheBestCoins(IExchangeService service, MarketCoin marketCoin) throws Exception {
		IExchangeSession session = service.getSession(marketCoin, true, true);
		List<ITicker> list = session.getTikers().getTickers()
			.stream()
			.filter(it -> {
				Currency currency = service.getCurrencyFactory().currencyPairToCurrency(it.getCurrencyPair(), service);
				return currency.getMarketCoin().getId().equals(marketCoin.getId()) && currency.getCurrencyPair().equals(it.getCurrencyPair()); 
			})
			.sorted((it,other) -> other.getBaseVolume().compareTo(it.getBaseVolume()))
			.limit(10)
			.collect(Collectors.toList());
		return list;
	}
	
	private static void logSummary(List<Simulator> simmulators, LocalDateTime from, LocalDateTime to) {
		String sFrom = from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		String sTo = to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		log("PERIOD: %s TO %s", sFrom, sTo);
		logLine();
		log("%-8s %11s %11s %11s %11s %11s %7s",
				"COIN", 
				"AMOUNT", 
				"TOTAL", 
				"PROFIT", 
				"FEE", 
				"BALANCE",
				"STUCKED"
		);
		
		logLine();
		
		BigDecimal amount = BigDecimal.ZERO;
		Integer total = 0;
		BigDecimal profit = BigDecimal.ZERO;
		BigDecimal fee = BigDecimal.ZERO;
		BigDecimal balance = BigDecimal.ZERO;
		
		for (Simulator simulator : simmulators) {
			log("%-8s %11s %11s %11s %11s %11s %7s",
					simulator.getCoin() , 
					CalcUtil.formatReal(simulator.getAmount()),
					simulator.getTotal().toString(),
					CalcUtil.formatPercent(simulator.getTotalProfit()),
					CalcUtil.formatReal(simulator.getTotalFee()),
					CalcUtil.formatReal(simulator.getTotalBalance()),
					(simulator.isStucked() ? "YES" : "NO")
			);
			
			amount = CalcUtil.add(amount, simulator.getAmount());
			total+= simulator.getTotal();
			profit = CalcUtil.add(profit, simulator.getTotalProfit());
			fee = CalcUtil.add(fee, simulator.getTotalFee());
			balance = CalcUtil.add(balance, simulator.getTotalBalance());
		}
		
		logLine();
		
		log("%-8s %11s %11s %11s %11s %11s %7s",
				"", 
				CalcUtil.formatReal(amount),
				total.toString(),
				CalcUtil.formatPercent(profit),
				CalcUtil.formatReal(fee),
				CalcUtil.formatReal(balance),
				""
		);
	}
	
	private static void logLine() {
		log(String.format("%76s"," ").replaceAll("\\s","="));		
	}
	
	private static void log(String frm, Object ... params) {
		log(String.format(frm, params));
	}
	
	private static void log(String msg) {
		System.out.println(msg);
	}
}
