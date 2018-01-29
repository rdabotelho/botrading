package com.m2r.botrading.api.model;

public class CurrencyPairIds {

	public String marketCoinId;
	private String currencyId;
	
	public static CurrencyPairIds of(Currency currency) {
		return new CurrencyPairIds(currency.getMarketCoin() != null ? currency.getMarketCoin().getId() : "" , currency.getId()); 
	}
	
	public static CurrencyPairIds of(String marketCoinId, String currencyId) {
		return new CurrencyPairIds(marketCoinId, currencyId); 
	}
	
	public CurrencyPairIds(String marketCoinId, String currencyId) {
		this.marketCoinId = marketCoinId;
		this.currencyId = currencyId;
	}

	public String getMarketCoinId() {
		return marketCoinId;
	}

	public void setMarketCoinId(String marketCoinId) {
		this.marketCoinId = marketCoinId;
	}

	public String getCurrencyId() {
		return currencyId;
	}

	public void setCurrencyId(String currencyId) {
		this.currencyId = currencyId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currencyId == null) ? 0 : currencyId.hashCode());
		result = prime * result + ((marketCoinId == null) ? 0 : marketCoinId.hashCode());
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
		CurrencyPairIds other = (CurrencyPairIds) obj;
		if (currencyId == null) {
			if (other.currencyId != null)
				return false;
		} else if (!currencyId.equals(other.currencyId))
			return false;
		if (marketCoinId == null) {
			if (other.marketCoinId != null)
				return false;
		} else if (!marketCoinId.equals(other.marketCoinId))
			return false;
		return true;
	}

}
