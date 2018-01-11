package com.m2r.botrading.strategy;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.util.CalcUtil;

public class CatLeap implements Comparable<CatLeap> {

	private static int CAT_LEAP_SIZE = 6;
	
	private static List<CatLeap> catLeapList = new LinkedList<>();
	private static Map<String, CatLeap> catLeapMap = new TreeMap<>();
	
	private String currencyPair;
	private BigDecimal[] percents; 
	private int index;

	synchronized public static void foodCatLeap(IMarketCoin marketCoin, IExchangeSession session, int count) throws Exception {
		ITickerList tl = session.getTikers();
		List<ITicker> sorted = tl.getTickers(marketCoin.getId()).stream().sorted((c1,c2) -> c2.getBaseVolume().compareTo(c1.getBaseVolume())).collect(Collectors.toList());
		int limit = count;
		for (int i=0; i<sorted.size(); i++) {
			addTicker(sorted.get(i));
			limit--;
			if (limit == 0) {
				break;
			}				
		}
		Collections.sort(catLeapList);
	}
	
	synchronized public static List<CatLeap> getList() {
		return catLeapList;
	}
	
	private static void addTicker(ITicker ticker) {
		CatLeap catLeap = catLeapMap.get(ticker.getCurrencyPair());
		if (catLeap == null) {
			catLeap = new CatLeap(ticker.getCurrencyPair());
			catLeapList.add(catLeap);
			catLeapMap.put(ticker.getCurrencyPair(), catLeap);
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
