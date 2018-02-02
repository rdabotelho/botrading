package com.m2r.botrading.strategy;

import java.util.HashMap;
import java.util.Map;

import com.m2r.botrading.api.model.IIntention;
import com.m2r.botrading.api.model.IIntentionRequest;
import com.m2r.botrading.api.strategy.IStrategy;

public class IntentionRequest implements IIntentionRequest {
	
	private Long traderJobId;
	private IStrategy strategy;
	private String marketCoin;
	private Map<String, IIntention> intentions;

	public IntentionRequest(Long traderJobId, IStrategy strategy, String marketCoin) {
		this.traderJobId = traderJobId;
		this.strategy = strategy;
		this.marketCoin = marketCoin;
		this.intentions = new HashMap<>();
	}

	public Long getTraderJobId() {
		return traderJobId;
	}

	public void setTraderJobId(Long traderJobId) {
		this.traderJobId = traderJobId;
	}

	@Override
	public IStrategy getStrategy() {
		return this.strategy;
	}
	
	public void setStrategy(IStrategy strategy) {
		this.strategy = strategy;
	}
	
	public void setMarketCoin(String marketCoin) {
		this.marketCoin = marketCoin;
	}
	
	@Override
	public String getMarketCoin() {
		return this.marketCoin;
	}

	public Map<String, IIntention> getIntentions() {
		return intentions;
	}
	
	@Override
	public boolean haveReserve() {
		return !this.intentions.isEmpty();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((strategy == null) ? 0 : strategy.hashCode());
		result = prime * result + ((traderJobId == null) ? 0 : traderJobId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IntentionRequest other = (IntentionRequest) obj;
		if (strategy == null) {
			if (other.strategy != null)
				return false;
		} else if (!strategy.equals(other.strategy))
			return false;
		if (traderJobId == null) {
			if (other.traderJobId != null)
				return false;
		} else if (!traderJobId.equals(other.traderJobId))
			return false;
		return true;
	}
	
}
