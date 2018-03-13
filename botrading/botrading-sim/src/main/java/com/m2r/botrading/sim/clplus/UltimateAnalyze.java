package com.m2r.botrading.sim.clplus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.poloniex.enums.PoloniexDataChartPeriod;

public class UltimateAnalyze {

	private static PoloniexDataChartPeriod PERIOD = PoloniexDataChartPeriod.FIVE_MINUTES; 
	
	public static Candle ultimateAnalyze(IExchangeService service, String marketCoinId, String currencyId, LocalDateTime start, LocalDateTime end, int count, BigDecimal candleSizeFactor) throws Exception {
		MarketCoin marketCoin = service.getMarketCoin(marketCoinId);
		IExchangeSession session = service.getSession(marketCoin, true, false);
		Currency currency = marketCoin.getCurrency(currencyId);
		IChartDataList chartDataList = session.getChartDatas(currency.getCurrencyPair(), PERIOD, start, end);
		return ultimateAnalyze(currency.getId(), chartDataList.getChartDatas(),  count, candleSizeFactor);
	}

	public static Candle ultimateAnalyze(String currencyId, List<IChartData> list, int count, BigDecimal candleSizeFactor) throws Exception {
		Candle candle = new Candle(currencyId, candleSizeFactor);
		for (int i=0; i<count; i++) {
			if (i < list.size()) {
				candle.update(list.get(i));
			}
		}
		return candle;
	}

}
