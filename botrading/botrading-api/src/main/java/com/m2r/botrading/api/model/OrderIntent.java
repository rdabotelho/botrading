package com.m2r.botrading.api.model;

import java.math.BigDecimal;

public class OrderIntent implements IOrderIntent {

	private ICurrency currency;
	private BigDecimal buyPrice;
	private BigDecimal sellPrice;
	
	public static OrderIntent of(ICurrency currency, BigDecimal buyPrice, BigDecimal sellPrice) {
		return new OrderIntent(currency, buyPrice, sellPrice);
	}
	
	public static OrderIntent of(ICurrency currency) {
		return new OrderIntent(currency);
	}
	
	public OrderIntent(ICurrency currency, BigDecimal buyPrice, BigDecimal sellPrice) {
		this.currency = currency;
		this.buyPrice = buyPrice;
		this.sellPrice = sellPrice;
	}
	
	public OrderIntent(ICurrency currency) {
		this(currency, null, null);
	}

	@Override
	public ICurrency getCurrency() {
		return this.currency;
	}

	@Override
	public BigDecimal getBuyPrice() {
		return buyPrice;
	}

	@Override
	public BigDecimal getSellPrice() {
		return sellPrice;
	}

}
