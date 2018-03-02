package com.m2r.botrading.tests.rsi;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.ta4j.core.Decimal;
import org.ta4j.core.TimeSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.StochasticOscillatorDIndicator;
import org.ta4j.core.indicators.StochasticOscillatorKIndicator;
import org.ta4j.core.indicators.StochasticRSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.MaxPriceIndicator;
import org.ta4j.core.indicators.helpers.MinPriceIndicator;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.poloniex.enums.PoloniexDataChartPeriod;

public class ExchangeTradesLoader {

	private static Map<String, Decimal> results = new HashMap<>();

	public static void main(String[] args) throws Exception {
		analyzeStochRSI();
	}

	public static void analyzeStochRSI() throws Exception {
		results.clear();
		LocalDateTime end = LocalDateTime.now().plusDays(1L);
		LocalDateTime start = end.minusDays(15);
		IExchangeService service = new PoloniexExchange().init();
		MarketCoin marketCoin = service.getMarketCoin("BTC");
		
		int exit = 20;
		for (Currency currency : marketCoin.getCurrencies().values()) {
			if (filter(currency)) {
				IExchangeSession session = service.getSession(marketCoin, false, false);
				IChartDataList chartDataList = session.getChartDatas(currency.getCurrencyPair(),
						PoloniexDataChartPeriod.ONE_DAY, start, end);
				TimeSeries timeSeries = new MyTimeSeries(currency, chartDataList);
				verifyStochRSISelection(currency, timeSeries);
				// RSI2Strategy.run(timeSeries);
				
				exit--;
				if (exit <= 0) {
					break;
				}
			}
		}
		printResult();
	}

	private static boolean filter(Currency currency) {
		return true;
	}

	private static void verifyStochRSISelection(Currency currency, TimeSeries series) {
		info("Analyzing " + currency.getCurrencyPair() + "...");
		
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        StochasticRSIIndicator stoRsi = new StochasticRSIIndicator(closePrice, 14);
        MaxPriceIndicator maxPrice = new MaxPriceIndicator(series);
        MinPriceIndicator minPrice = new MinPriceIndicator(series);

        StochasticOscillatorKIndicator stochK= new StochasticOscillatorKIndicator(stoRsi,3,maxPrice, minPrice);
        StochasticOscillatorDIndicator stochD = new StochasticOscillatorDIndicator(stochK);
        
        info("K: " + stochK.getValue(13));
        info("D: " + stochD.getValue(13));
        
		Decimal result = stoRsi.getValue(13);
		if (result.isGreaterThanOrEqual(Decimal.valueOf("0.80"))) {
			results.put(currency.getCurrencyPair(), result);
		}
	}

	private static void info(String log) {
		System.out.println(log);
	}

	private static void printResult() {
		info("#### RESTULS ####");
		results.forEach((k,v) -> {
			info(k + "\t" + v);
		});
	}

}
