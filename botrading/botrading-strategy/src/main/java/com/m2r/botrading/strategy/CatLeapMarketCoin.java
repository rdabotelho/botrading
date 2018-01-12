package com.m2r.botrading.strategy;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CatLeapMarketCoin {

	private List<CatLeap> catLeapList;
	private Map<String, CatLeap> catLeapMap;

	public CatLeapMarketCoin() {
		this.catLeapList = new LinkedList<>();
		this. catLeapMap = new TreeMap<>();
	}
	
	public List<CatLeap> getCatLeapList() {
		return catLeapList;
	}

	public void put(String currencyPair, CatLeap catLeap) {
		catLeapList.add(catLeap);
		catLeapMap.put(currencyPair, catLeap);
	}
	
	public CatLeap get(String currencyPair) {
		return catLeapMap.get(currencyPair);
	}
	
	public void sort() {
		Collections.sort(catLeapList);		
	}
	
}
