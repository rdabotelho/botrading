package com.m2r.botrading.tests.clplus;

import java.time.LocalDateTime;
import java.util.List;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.poloniex.enums.PoloniexDataChartPeriod;

public class CatLeapPlus2Analyze {

	private static PoloniexDataChartPeriod PERIOD = PoloniexDataChartPeriod.FIVE_MINUTES; 
	
	public static Candle catLeapPlusAnalyze(IExchangeService service, String marketCoinId, String currencyId, LocalDateTime start, LocalDateTime end, int count) throws Exception {
		MarketCoin marketCoin = service.getMarketCoin(marketCoinId);
		IExchangeSession session = service.getSession(marketCoin, true, false);
		Currency currency = marketCoin.getCurrency(currencyId);
		IChartDataList chartDataList = session.getChartDatas(currency.getCurrencyPair(), PERIOD, start, end);
		List<IChartData> list = chartDataList.getChartDatas();
		Candle candle = new Candle(currency.getId());
		for (int i=0; i<count; i++) {
			candle.update(list.get(i));
		}
		return candle;
	}
	
}
