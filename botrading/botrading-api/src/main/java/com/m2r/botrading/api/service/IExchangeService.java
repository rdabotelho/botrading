package com.m2r.botrading.api.service;

import java.math.BigDecimal;
import java.util.Map;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.CurrencyFactory;
import com.m2r.botrading.api.model.IOrder;
import com.m2r.botrading.api.model.MarketCoin;

public interface IExchangeService extends IExchangeBasic {
	
	
	/**
	 * Method which get the exchange id
	 * @return String
	 */
	public String getId();
	
	/**
	 * Method to initialize the exchange context.
	 * @return IExchangeService
	 */
	public IExchangeService init();
	
	/**
	 * Method which get the currency through market coin id and currency id.
	 * @param marketCoinId: Market coin id
	 * @param id: Currency id
	 * @return Currency
	 */
	public Currency getCurrency(String marketCoinId, String id);
	
	/**
	 * Method which get the market coin through market coin id.
	 * @param id
	 * @return IMarketCoin
	 */
	public MarketCoin getMarketCoin(String id);
	
	/**
	 * Method which get the exchange session through market coin.
	 * @param marketCoin: Market coin
	 * @param resetPublic: Flag if the public context must be reseted.
	 * @param resetPrivate: Flag if the private context must be reseted.
	 * @return IExchangeSession
	 */
	public IExchangeSession getSession(MarketCoin marketCoin, boolean resetPublic, boolean resetPrivate);
	
	/**
	 * Method Method which get the default market coin.
	 * @return IMarketCoin
	 */
	public MarketCoin getDefaultMarketCoin();

	/**
	 * Method which get all market coins.
	 * @return Map<String, IMarketCoin>
	 */
	public Map<String, MarketCoin> getMarketCoins();
	
	/**
	 * Method which get the exchange fee to do trading.
	 * @return BigDecimal
	 */
	public BigDecimal getFee();
	
	/**
	 * Method which get the fee to immediate buying and immediate selling.
	 * @return BigDecimal
	 */
	public BigDecimal getImmediateFee();
	
	/**
	 * Method which get the currency factory
	 * @return CurrencyFactory
	 */
	public CurrencyFactory getCurrencyFactory();
	
	/**
	 * Event before save order
	 */
	public void onBeforeSaveOrder(IOrder order);
	
}
