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

	public void resetPublicCache();
	public void resetPrivateCache();
    public IMarketCoin getMarketCoin();
    public IMarketCoin getMarketCoinOfTrader(ITrader trader);
    public ICurrency getCurrencyOfTrader(ITrader trader);
    public BigDecimal getLastPrice(String coin) throws Exception;
    public ITickerList getTikers() throws Exception;
    public IChartDataList getChartDatas(String currencyPair, DataChartPeriod period, LocalDateTime start, LocalDateTime end) throws Exception;
    public IBalanceList getBanlances(IApiAccess apiAccess) throws Exception;
    public IOrderList getOrders(IApiAccess apiAccess) throws Exception;
    public BigDecimal getAvailableBalance(String coin, IApiAccess apiAccess) throws Exception;
    public BigDecimal calculateAmountToImmediateSell(ITrader trader, BigDecimal amount);
	public BigDecimal getFee();
	public BigDecimal getImmediateFee();
	
}
