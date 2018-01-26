package com.m2r.botrading.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.m2r.botrading.api.enums.DataChartPeriod;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.ITrader;

public interface IExchangeSession extends IExchangeOrder {

	/**
	 * Method which reset the public cache context.
	 */
	public void resetPublicCache();
	
	/**
	 * Method which reset the private cache context.
	 */
	public void resetPrivateCache();
	
	/**
	 * Method which get the market coin of the session context.
	 * @return {@link IMarketCoin} 
	 */
    public IMarketCoin getMarketCoin();
    
    /**
     * Method which extract the trader market coin.
     * @param trader: Trader
     * @return {@link IMarketCoin}
     */
    public IMarketCoin getMarketCoinOfTrader(ITrader trader);
    
    /**
     * Method which extract the trader currency.
     * @param trader: Trader
     * @return {@link ICurrency}
     */
    public ICurrency getCurrencyOfTrader(ITrader trader);
    
    /**
     * Method which get the last price of the coin.
     * @param coin: Coin
     * @return {@link BigDecimal} the price
     * @throws Exception
     */
    public BigDecimal getLastPrice(String coin) throws Exception;
    
    /**
     * Method which get all tickers of the exchange.
     * @return {@link ITickerList}
     * @throws Exception
     */
    public ITickerList getTikers() throws Exception;
    
    /**
     * Method which get the chart data of a particular currency pair.
     * @param currencyPair: Currency pair
     * @param period: Periodo of the query
     * @param start: Start of the query
     * @param end: End of the query
     * @return {@link IChartDataList}
     * @throws Exception
     */
    public IChartDataList getChartDatas(String currencyPair, DataChartPeriod period, LocalDateTime start, LocalDateTime end) throws Exception;
    
    /**
     * Method which get all the balances of particular exchange account.
     * @param apiAccess: API of access.
     * @return {@link IChartDataList}
     * @throws Exception
     */
    public IBalanceList getBanlances(IApiAccess apiAccess) throws Exception;
    
    /**
     * Mthod which get all orders of a particular exchange account.
     * @param apiAccess: API of access
     * @return {@link IBalanceList}
     * @throws Exception
     */
    public IOrderList getOrders(IApiAccess apiAccess) throws Exception;
    
    /**
     * Method which get the available balance of a coin of a particular exchange account.
     * @param coin: Coin
     * @param apiAccess: API of access
     * @return Balance
     * @throws Exception
     */
    public BigDecimal getAvailableBalance(String coin, IApiAccess apiAccess) throws Exception;
    
    /**
     * Method which calculate the amount for an  immediate selling.
     * @param trader: Trader
     * @param amount: Amount
     * @return {@link BigDecimal}
     */
    public BigDecimal calculateAmountToImmediateSell(ITrader trader, BigDecimal amount);
    
    /**
     * Method which get the exchange fee.
     * @return {@link BigDecimal}
     */
	public BigDecimal getFee();
	
	/**
	 * Method which get the exchange immediate fee. 
     * @return {@link BigDecimal}
	 */
	public BigDecimal getImmediateFee();
	
}
