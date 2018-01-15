package com.m2r.botrading.api.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.m2r.botrading.api.enums.DataChartPeriod;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IBalance;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.ITrader;
import com.m2r.botrading.api.util.CalcUtil;

public class ExchangeSession implements IExchangeSession {

	private IMarketCoin marketCoin;
	private ExchangeService service;
	private ITickerList tickers;
	private IBalanceList balances;
	private IOrderList orders;
	private IChartDataList chartDataList;
	
	public static ExchangeSession createSession(ExchangeService service, IMarketCoin marketCoin) {
		return new ExchangeSession(service, marketCoin);
	}
	
	private ExchangeSession(ExchangeService service, IMarketCoin marketCoin) {
		this.service = service;
		this.marketCoin = marketCoin;
	}
	
	@Override
	public void resetPublicCache() {
		tickers = null;
		chartDataList = null;
	}
	
	@Override
	public void resetPrivateCache() {
		balances = null;
		orders = null;
	}
	
	@Override
	public IMarketCoin getMarketCoin() {
		return this.marketCoin;
	}
	
	@Override
	public IMarketCoin getMarketCoinOfTrader(ITrader trader) {
		return service.getMarketCoin(trader.getCoin());
	}
	
	@Override
	public ICurrency getCurrencyOfTrader(ITrader trader) {
		return service.getCurrency(trader.getTraderJob().getMarketCoin(), trader.getCoin());
	}
	
	@Override
	public BigDecimal getLastPrice(String coin) throws Exception {
		ICurrency currency = getMarketCoin().getCurrency(coin);
		ITicker ticker = getTikers().getTicker(currency.getCurrencyPair());
		return ticker != null ? ticker.getLast() : null;
	}

	@Override
	public IBalanceList getBanlances(IApiAccess apiAccess) throws Exception {
		if (balances == null) {
			balances = service.getBanlances(apiAccess, this);
		}
		return balances;
	}

	@Override
	public ITickerList getTikers() throws Exception {
		if (tickers == null) {
			tickers = service.getTikers(this);
		}
		return tickers;
	}
	
	@Override
	public IChartDataList getChartDatas(String currencyPair, DataChartPeriod period, LocalDateTime start, LocalDateTime end) throws Exception {
		if (chartDataList == null) {
			chartDataList = service.getChartDatas(currencyPair, period, start, end, this);
		}
		return chartDataList;
	}
	
	@Override
	public IOrderList getOrders(IApiAccess apiAccess) throws Exception {
		if (orders == null) {
			orders = service.getOrders(apiAccess, this);
		}
		return orders;
	}
	
	@Override
	public BigDecimal getAvailableBalance(String coin, IApiAccess apiAccess) throws Exception {
	    	IBalance balance = this.getBanlances(apiAccess).getBalance(coin);
	    	return balance != null ? balance.getAvailable() : null;
	}

	@Override
	public BigDecimal calculateAmountToImmediateSell(ITrader trader, BigDecimal amount) {
	    	try {
		    	BigDecimal availableBalance = this.getAvailableBalance(trader.getCoin(), trader.getTraderJob().getAccount());
		    	if (availableBalance == null) {
		    		return amount;
		    	}
		    	return CalcUtil.greaterThen(availableBalance, amount) ? amount : availableBalance;
	    	}
	    	catch (Exception e) {
	    		return amount;
	    	}
    }
	
	@Override
	public BigDecimal getFee() {
		return service.getFee();
	}
	
	@Override
	public BigDecimal getImmediateFee() {
		return service.getImmediateFee();
	}
	
	/*
	 * ORDERS
	 */
	
	@Override
	public String buy(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException {
		return service.buy(apiAccess, currencyPair, price, amount);
	}

	@Override
	public String sell(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException {
		return service.sell(apiAccess, currencyPair, price, amount);
	}
	
	@Override
	public void cancel(IApiAccess apiAccess, String orderNumber) throws ExchangeException {
		service.cancel(apiAccess, orderNumber);		
	}
	
}
