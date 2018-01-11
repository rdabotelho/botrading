package com.m2r.botrading.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.m2r.botrading.api.enums.DataChartPeriod;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.IAccount;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.IOrder;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.ITrader;

public interface IExchangeSession {

	public void resetPublicCache();
	public void resetPrivateCache();
    public IMarketCoin getMarketCoin();
    public IMarketCoin getMarketCoinOfTrader(ITrader trader);
    public ICurrency getCurrencyOfTrader(ITrader trader);
    public BigDecimal getLastPrice(String coin) throws Exception;
    public ITickerList getTikers() throws Exception;
    public IChartDataList getChartDatas(String currencyPair, DataChartPeriod period, LocalDateTime start, LocalDateTime end) throws Exception;
    public IBalanceList getBanlances(IAccount account) throws Exception;
    public IOrderList getOrders(IAccount account) throws Exception;
    public BigDecimal getAvailableBalance(String coin, IAccount account) throws Exception;
    public String buy(IOrder order) throws ExchangeException;
    public String sell(IOrder order) throws ExchangeException;
    public String immediateSell(IOrder order) throws ExchangeException;
    public void cancel(IOrder order) throws ExchangeException;
    public BigDecimal calculateSellToAmount(ITrader trader, BigDecimal amount);
	
}
