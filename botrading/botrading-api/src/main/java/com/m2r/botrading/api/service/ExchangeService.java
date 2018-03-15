package com.m2r.botrading.api.service;

import java.time.LocalDateTime;
import java.util.Map;

import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.IDataChartPeriod;
import com.m2r.botrading.api.model.IOrder;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.MarketCoin;

public abstract class ExchangeService implements IExchangeService {

	private Map<String, MarketCoin> marketCoinsMap;
	
	@Override
	public IExchangeService init() {
		this.marketCoinsMap = loadMarketCoins();
		return this;
	}
	
	public Currency getCurrency(String marketCoinId, String id) {
		return getMarketCoin(marketCoinId).getCurrency(id);
	}
	
	public MarketCoin getMarketCoin(String id) {
		return getMarketCoins().get(id);
	}
	
	@Override
	public Map<String, MarketCoin> getMarketCoins() {
		return this.marketCoinsMap;
	}
	
	@Override
	public IExchangeSession getSession(MarketCoin marketCoin, boolean resetPublic, boolean resetPrivate) {
		IExchangeSession session = ExchangeSession.createSession(this, marketCoin);
		if (resetPublic) {
			session.resetPublicCache();
		}
		if (resetPrivate) {
			session.resetPrivateCache();
		}
		return session;
	}

	@Override
	public void onBeforeSaveOrder(IOrder order) {
	}

	/**
	 * Method which load all market coins of the exchange.
	 * @return Map<String, IMarketCoin>
	 */
	protected abstract Map<String, MarketCoin> loadMarketCoins();
	
	/**
	 * Method which get all tickers of the instant.
	 * @param session: The exchange session
	 * @return ITickerList
	 * @throws ExchangeException
	 */
	protected abstract ITickerList getTikers(IExchangeSession session) throws ExchangeException;
	
	/**
	 * Method which get all chart date of a determinate period.
	 * @param currencyPair: Currency pair.
	 * @param period: Period
	 * @param start: Start date time
	 * @param end: End date time
	 * @param session: Exchange session
	 * @return IChartDataList
	 * @throws ExchangeException
	 */
	protected abstract IChartDataList getChartDatas(String currencyPair, IDataChartPeriod period, LocalDateTime start, LocalDateTime end, IExchangeSession session) throws ExchangeException;
	
	/**
	 * Method which get all balances of an account through the API access.
	 * @param apiAccess: API access
	 * @param session: Exchange session
	 * @return IBalanceList
	 * @throws ExchangeException
	 */
	public abstract IBalanceList getBanlances(IApiAccess apiAccess, IExchangeSession session) throws ExchangeException;
	
	/**
	 * Method which get all orders of an account through the API access.
	 * @param apiAccess: API access
	 * @param session: Exchange session
	 * @return IOrderList
	 * @throws ExchangeException
	 */
	protected abstract IOrderList getOrders(IApiAccess apiAccess, IExchangeSession session) throws ExchangeException;

}
