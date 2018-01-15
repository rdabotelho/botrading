package com.m2r.botrading.api.service;

import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.IApiAccess;

public interface IExchangeOrder {

	/**
	 * Method which do a buying in the exchange.
	 * @param apiAccess: API access
	 * @param currencyPair: Currency pair
	 * @param price: Price
	 * @param amount: Amount
	 * @return String (Order number)
	 * @throws ExchangeException
	 */
    public String buy(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException;

	/**
	 * Method which do a selling in the exchange.
	 * @param apiAccess: API access
	 * @param currencyPair: Currency pair
	 * @param price: Price
	 * @param amount: Amount
	 * @return String (Order number)
	 * @throws ExchangeException
	 */
    public String sell(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException;
    
	/**
	 * Method which cancel an order in the exchange.
	 * @param apiAccess: API access
	 * @param orderNumber: Order number
	 * @throws ExchangeException
	 */
    public void cancel(IApiAccess apiAccess, String orderNumber) throws ExchangeException;

}
