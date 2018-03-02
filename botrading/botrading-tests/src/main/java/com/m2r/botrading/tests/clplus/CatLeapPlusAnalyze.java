package com.m2r.botrading.tests.clplus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.ta4j.core.Decimal;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.poloniex.enums.PoloniexDataChartPeriod;

public class CatLeapPlusAnalyze {

	private static int DAYS = 90;
	private static PoloniexDataChartPeriod PERIOD = PoloniexDataChartPeriod.FIVE_MINUTES; 
	private static BigDecimal MAX_VOLUME = new BigDecimal("100.0");
	
	private static Map<String, Decimal> results = new HashMap<>();

	public static void main(String[] args) throws Exception {
		catLeapPlusAnalyze();
	}

	public static void catLeapPlusAnalyze() throws Exception {
		results.clear();
		LocalDateTime end = LocalDateTime.now().plusDays(1L);
		LocalDateTime start = end.minusDays(DAYS);
		IExchangeService service = new PoloniexExchange().init();
		MarketCoin marketCoin = service.getMarketCoin("BTC");
		
		List<Result> results = new ArrayList<>();
		
		for (Currency currency : marketCoin.getCurrencies().values()) {
			IExchangeSession session = service.getSession(marketCoin, false, false);
			if (filter(session, currency)) {
				IChartDataList chartDataList = session.getChartDatas(currency.getCurrencyPair(), PERIOD, start, end);
				BigDecimal min = new BigDecimal("9999999999");
				BigDecimal max = BigDecimal.ZERO;
				BigDecimal now = BigDecimal.ZERO;
				for (IChartData cd : chartDataList.getChartDatas()) {
					BigDecimal low = new BigDecimal(cd.getLow());
					BigDecimal high = new BigDecimal(cd.getHigh());
					if (CalcUtil.lessThen(low, min)) {
						min = low;
					}
					if (CalcUtil.greaterThen(high, max)) {
						max = high;
					}
					now = low;
				}
				System.out.println(currency.getId()+"...");
				double period = (double) max.doubleValue();// chartDataList.getChartDatas().size();
				double angle = Math.toDegrees(Math.atan(now.doubleValue() / period));
				results.add(new Result(currency.getId(), new BigDecimal(angle+"")));
			}
		}

		System.out.println("\n### RESULTS ###");
		results = results.stream().sorted((it,other) -> it.getPercent().compareTo(other.getPercent())).collect(Collectors.toList());
		for (Result r : results) {
			System.out.println(String.format("%-10s%10s", r.getCoin(), r.getPercent().toString()));
		}
	}

	private static boolean filter(IExchangeSession session, Currency currency) throws Exception {
		BigDecimal volume = session.getTikers().getTicker(currency.getCurrencyPair()).getBaseVolume();
		return !CalcUtil.lessThen(volume, MAX_VOLUME);
	}

	static class Result {
		private String coin;
		private BigDecimal percent;
		public Result(String coin, BigDecimal percent) {
			this.coin = coin;
			this.percent = percent;
		}
		public String getCoin() {
			return coin;
		}
		public void setCoin(String coin) {
			this.coin = coin;
		}
		public BigDecimal getPercent() {
			return percent;
		}
		public void setPercent(BigDecimal percent) {
			this.percent = percent;
		}
	}
}
