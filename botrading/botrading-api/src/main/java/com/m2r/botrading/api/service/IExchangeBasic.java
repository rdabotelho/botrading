package com.m2r.botrading.api.service;

import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITickerList;

public interface IExchangeBasic extends IExchangeOrder {

	/**
	 * Method which alert if the order was executed
	 * @param apiAccess: API of access
	 * @param orderNumber: Order number
	 * @return boolean
	 * @throws ExchangeException
	 */
	public boolean isOrderExecuted(IApiAccess apiAccess, String orderNumber) throws ExchangeException;
	
	/**
	 * Method which get all tikers in the exchange
	 * @return Tickers
	 * @throws Exception
	 */
	public ITickerList getAllTikers() throws ExchangeException;

	/**
	 * Method which get all orders of a account
	 * @param apiAccess: API Access
	 * @return List of orders
	 * @throws ExchangeException
	 */
	public IOrderList getAllOrders(IApiAccess apiAccess) throws ExchangeException;
	
	/**
	 * Method which get all chat data of a specific currency pair
	 * @param currencyPair: Currency Pair
	 * @param period: Periodo of the query
	 * @param start: Initial date (UNIX format)
	 * @param end: Finished date (UNIX format)
	 * @return Chart Data
	 * @throws ExchangeException
	 */
	public String getAllChartData(String currencyPair, String period, String start, String end) throws ExchangeException;

}
