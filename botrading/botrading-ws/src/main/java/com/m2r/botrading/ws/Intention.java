package com.m2r.botrading.ws;

import java.io.Serializable;
import java.math.BigDecimal;

import com.m2r.botrading.api.model.IIntention;

public class Intention implements IIntention, Serializable {

	private static final long serialVersionUID = 1L;
	
	private String uuidStrategy;
	private String currencyPair;
	private String buyPriceStr;
	private String salePriceStr;
	
	public Intention() {
	}

	public Intention(String uuidStrategy, String currencyPair, String buyPriceStr, String salePriceStr) {
		this.uuidStrategy = uuidStrategy;
		this.currencyPair = currencyPair;
		this.buyPriceStr = buyPriceStr;
		this.salePriceStr = salePriceStr;
	}

	@Override
	public String getUuidStrategy() {
		return this.uuidStrategy;
	}

	public String getCurrencyPair() {
		return this.currencyPair;
	}

	public BigDecimal getBuyPrice() {
		return new BigDecimal(this.buyPriceStr);
	}

	public BigDecimal getSalePrice() {
		return new BigDecimal(this.salePriceStr);
	}
	
	public void setBuyPriceStr(String buyPriceStr) {
		this.buyPriceStr = buyPriceStr;
	}
	
	public String getBuyPriceStr() {
		return buyPriceStr;
	}
	
	public void setSalePriceStr(String selePriceStr) {
		this.salePriceStr = selePriceStr;
	}
	
	public String getSalePriceStr() {
		return salePriceStr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((currencyPair == null) ? 0 : currencyPair.hashCode());
		result = prime * result + ((uuidStrategy == null) ? 0 : uuidStrategy.hashCode());
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
		Intention other = (Intention) obj;
		if (currencyPair == null) {
			if (other.currencyPair != null)
				return false;
		} else if (!currencyPair.equals(other.currencyPair))
			return false;
		if (uuidStrategy == null) {
			if (other.uuidStrategy != null)
				return false;
		} else if (!uuidStrategy.equals(other.uuidStrategy))
			return false;
		return true;
	}
	
}