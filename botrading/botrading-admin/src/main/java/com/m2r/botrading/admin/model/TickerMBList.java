package com.m2r.botrading.admin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;

public class TickerMBList implements ITickerList {

	private List<ITicker> tikers;
	
	public static ITickerList of(Map<String, TickerMB> tikersMap) {
		ITickerList eb = new TickerMBList(tikersMap);
		return eb;
	}
	
	public TickerMBList(Map<String, TickerMB> tikersMap) {
		this.tikers = new ArrayList<>();
		tikersMap.forEach((k,v)->{
			v.setCurrencyPair(k);
			tikers.add(v);
		});
	}
	
	@Override
	public List<ITicker> getTickers() {
		return this.tikers;
	}
	
	@Override
	public List<ITicker> getTickers(String coin) {
		return tikers.stream().filter(t -> t.getCurrencyPair().startsWith(coin+"_")).collect(Collectors.toList());
	}
	
	@Override
	public ITicker getTicker(String currencyPair) {
		return tikers.stream().filter(t -> t.getCurrencyPair().equals(currencyPair)).findFirst().orElse(null);
	}

}
