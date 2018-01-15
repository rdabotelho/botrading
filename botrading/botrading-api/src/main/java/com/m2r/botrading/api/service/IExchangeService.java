package com.m2r.botrading.api.service;

import java.util.Map;

import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IMarketCoin;

public interface IExchangeService extends IExchangeOrder {

	public IExchangeService init();
	public ICurrency getCurrency(String marketCoinId, String id);
	public IMarketCoin getMarkeyCoin(String id);
	public IExchangeSession getSession(IMarketCoin marketCoin, boolean resetPublic, boolean resetPrivate);
	public IMarketCoin getDefaultMarketCoin();
	public Map<String, IMarketCoin> getMarketCoins();

}
