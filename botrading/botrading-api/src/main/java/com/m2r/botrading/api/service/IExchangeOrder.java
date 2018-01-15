package com.m2r.botrading.api.service;

import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.IApiAccess;

public interface IExchangeOrder {

    public String buy(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException;
    public String sell(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException;
    public void cancel(IApiAccess apiAccess, String orderNumber) throws ExchangeException;

}
