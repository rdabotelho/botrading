package com.m2r.botrading.api.service;

import java.time.LocalDateTime;
import java.util.Map;

import com.m2r.botrading.api.enums.DataChartPeriod;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.IAccount;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITickerList;

public abstract class ExchangeService implements IExchangeService {

	private Map<String, IMarketCoin> marketCoinsMap;
	
	@Override
	public IExchangeService init() {
		this.marketCoinsMap = loadMarketCoins();
		return this;
	}
	
	public ICurrency getCurrency(String marketCoinId, String id) {
		return getMarkeyCoin(marketCoinId).getCurrency(id);
	}
	
	public IMarketCoin getMarkeyCoin(String id) {
		return getMarketCoins().get(id);
	}
	
	@Override
	public Map<String, IMarketCoin> getMarketCoins() {
		return this.marketCoinsMap;
	}

	protected abstract Map<String, IMarketCoin> loadMarketCoins();
	public abstract IExchangeSession getSession(IMarketCoin marketCoin, boolean resetPublic, boolean resetPrivate);
	protected abstract ITickerList getTikers(IExchangeSession session) throws ExchangeException;
	protected abstract IChartDataList getChartDatas(String currencyPair, DataChartPeriod period, LocalDateTime start, LocalDateTime end, IExchangeSession session) throws ExchangeException;
	protected abstract IBalanceList getBanlances(IAccount IAccount, IExchangeSession session) throws ExchangeException;
	protected abstract IOrderList getOrders(IAccount IAccount, IExchangeSession session) throws ExchangeException;

}
