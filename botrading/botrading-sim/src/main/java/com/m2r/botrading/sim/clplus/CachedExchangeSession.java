package com.m2r.botrading.sim.clplus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.IDataChartPeriod;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.ExchangeService;
import com.m2r.botrading.api.service.ExchangeSession;
import com.m2r.botrading.api.service.IExchangeService;

public class CachedExchangeSession extends ExchangeSession {

	private IChartDataList chartDataListCached; 
	private LocalDateTime localStart;
	private LocalDateTime localEnd;
	
	public CachedExchangeSession(IExchangeService service, MarketCoin marketCoin, LocalDateTime start, LocalDateTime end) {
		super((ExchangeService)service, marketCoin);
		this.localStart = start;
		this.localEnd = end;
	}
	
	@Override
	public IChartDataList getChartDatas(String currencyPair, IDataChartPeriod period, LocalDateTime start, LocalDateTime end) throws Exception {
		if (chartDataListCached == null) {
			chartDataListCached = super.getChartDatas(currencyPair, period, this.localStart.minusMonths(1), this.localEnd.plusMonths(1));
		}
		
		List<IChartData> subList = chartDataListCached.getChartDatas().stream().filter(it -> {
			LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(it.getDate())), ZoneId.systemDefault());
			return !date.isBefore(start) && !date.isAfter(end);
		})
		.collect(Collectors.toList());
		
		return new CachedChartDataList<>(subList);
	}
	
	
}
