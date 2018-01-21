package com.m2r.botrading.api.model;

import java.math.BigDecimal;

import com.m2r.botrading.api.util.CalcUtil;

public class OrderIntent implements IOrderIntent {

	private ICurrency currency;
	private BigDecimal buyPrice;
	private BigDecimal sellPrice;
	private boolean replacePrice;
	
	public static OrderIntent of(ICurrency currency, BigDecimal buyPrice, BigDecimal sellPrice, boolean replacePrice) {
		return new OrderIntent(currency, buyPrice, sellPrice, replacePrice);
	}
	
	public static OrderIntent of(ICurrency currency) {
		return new OrderIntent(currency);
	}
	
	public OrderIntent(ICurrency currency, BigDecimal buyPrice, BigDecimal sellPrice, boolean replacePrice) {
		this.currency = currency;
		if (buyPrice != null) {
			this.buyPrice = CalcUtil.toCoinScale(buyPrice);
		}
		if (sellPrice != null) {
			this.sellPrice = CalcUtil.toCoinScale(sellPrice);
		}
		this.replacePrice = replacePrice;
	}
	
	public OrderIntent(ICurrency currency) {
		this(currency, null, null, false);
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
	
	public boolean isReplacePrice() {
		return replacePrice;
	}

}
