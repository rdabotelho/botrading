package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;

public class JobOffer {
	
	private Long traderJobId;
	private String uuidStrategy;
	private List<IIntention> intentions;
	private List<String> ignoredCoins;

	public JobOffer(Long traderJobId, String uuidStrategy, Integer count) {
		this.traderJobId = traderJobId;
		this.uuidStrategy = uuidStrategy;
		this.intentions = new ArrayList<>();
		this.ignoredCoins = new ArrayList<>();
	}

	public Long getTraderJobId() {
		return traderJobId;
	}

	public void setTraderJobId(Long traderJobId) {
		this.traderJobId = traderJobId;
	}

	public String getUuidStrategy() {
		return uuidStrategy;
	}
	
	public void setUuidStrategy(String uuidStrategy) {
		this.uuidStrategy = uuidStrategy;
	}

	public List<IIntention> getIntentions() {
		return intentions;
	}
	
	public void setIgnoredCoins(List<String> ignoredCoins) {
		this.ignoredCoins = ignoredCoins;
	}
	
	public List<String> getIgnoredCoins() {
		return ignoredCoins;
	}

}
