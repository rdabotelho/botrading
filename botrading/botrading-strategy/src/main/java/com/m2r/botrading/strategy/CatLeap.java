package com.m2r.botrading.strategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.util.CalcUtil;

public class CatLeap implements Comparable<CatLeap> {

	private static int CAT_LEAP_SIZE = 6;
	
	private static Map<String, CatLeapMarketCoin> catLeapMarketCoinMap = new HashMap<>();
	
	private String currencyPair;
	private BigDecimal[] percents; 
	private int index;

	synchronized public static void foodCatLeap(IExchangeSession session, int count) throws Exception {
		MarketCoin marketCoin = session.getMarketCoin();
		CatLeapMarketCoin catLeapMarketCoin = getCatLeapMarketCoin(marketCoin.getId());
		ITickerList tl = session.getTikers();
		List<ITicker> sorted = tl.getTickers(marketCoin.getId()).stream().sorted((c1,c2) -> c2.getBaseVolume().compareTo(c1.getBaseVolume())).collect(Collectors.toList());
		int limit = count;
		for (int i=0; i<sorted.size(); i++) {
			addTicker(catLeapMarketCoin, sorted.get(i));
			limit--;
			if (limit == 0) {
				break;
			}				
		}
		catLeapMarketCoin.sort();
	}
	
	synchronized public static List<CatLeap> getList(MarketCoin marketCoin) {
		return catLeapMarketCoinMap.get(marketCoin.getId()).getCatLeapList();
	}
	
	private static CatLeapMarketCoin getCatLeapMarketCoin(String marketCoinId) {
		CatLeapMarketCoin leapMarketCoin = catLeapMarketCoinMap.get(marketCoinId);
		if (leapMarketCoin == null) {
			leapMarketCoin = new CatLeapMarketCoin();
			catLeapMarketCoinMap.put(marketCoinId, leapMarketCoin);
		}
		return leapMarketCoin;
	}
	
	private static void addTicker(CatLeapMarketCoin catLeapMarketCoin  , ITicker ticker) {
		CatLeap catLeap = catLeapMarketCoin.get(ticker.getCurrencyPair());
		if (catLeap == null) {
			catLeap = new CatLeap(ticker.getCurrencyPair());
			catLeapMarketCoin.put(ticker.getCurrencyPair(), catLeap);
		}
		catLeap.enqueue(ticker);
	}
	
	public CatLeap(String currencyPair) {
		this.percents = new BigDecimal[CAT_LEAP_SIZE];
		this.currencyPair = currencyPair;
		this.index = 0;
		for (int i=0; i<percents.length; i++) {
			percents[i] = BigDecimal.ZERO;
		}
	}
	
	public void enqueue(ITicker ticker) {
		if (isComplete()) {
			shift();
		}
		percents[index++] = ticker.getPercentChange();
	}
	
	private void shift() {
		for (int i=1; i<percents.length; i++) {
			percents[i-1] = percents[i]; 
		}
		index = CAT_LEAP_SIZE - 1;
	}
	
	public boolean isComplete() {
		return index == CAT_LEAP_SIZE;
	}

	public String getCurrencyPair() {
		return currencyPair;
	}

	public BigDecimal[] getPercents() {
		return percents;
	}
	
	public BigDecimal getFirst() {
		return percents[CAT_LEAP_SIZE - 1];
	}
	
	public BigDecimal getLast() {
		return percents[0];
	}
	
	public BigDecimal getVariation() {
		return CalcUtil.subtract(getLast(), getFirst());
	}

	@Override
	public int compareTo(CatLeap o) {
		return o.getVariation().compareTo(this.getVariation());
	}
	
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append("[currencyPair: ").append(currencyPair).append(", ")
		.append("variation: ").append(getVariation()).append(", ")
		.append("percents: [");
		for (int i=0; i<percents.length; i++) {
			str.append(percents[i]).append(",");
		}
		str.append("]]");
		return str.toString();
	}
	
}
