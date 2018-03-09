package com.m2r.botrading.sim.clplus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.CurrencyFactory;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.IDataChartPeriod;
import com.m2r.botrading.api.model.IOrder;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;

public class CachedExchangeService implements IExchangeService {

	private IExchangeService service;
	private IExchangeSession session;
	
	private LocalDateTime start;
	private LocalDateTime end;
	
	public CachedExchangeService(IExchangeService service, LocalDateTime start, LocalDateTime end) {
		this.service = service;
		this.start = start;
		this.end = end;
	}

	@Override
	public String buy(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException {
		return service.buy(apiAccess, currencyPair, price, amount);
	}

	@Override
	public String sell(IApiAccess apiAccess, String currencyPair, String price, String amount)
			throws ExchangeException {
		return service.sell(apiAccess, currencyPair, price, amount);
	}

	@Override
	public void cancel(IApiAccess apiAccess, String currencyPair, String orderNumber) throws ExchangeException {
		service.cancel(apiAccess, currencyPair, orderNumber);
	}

	@Override
	public String getId() {
		return service.getId();
	}

	@Override
	public IExchangeService init() {
		return service.init();
	}

	@Override
	public Currency getCurrency(String marketCoinId, String id) {
		return service.getCurrency(marketCoinId, id);
	}

	@Override
	public MarketCoin getMarketCoin(String id) {
		return service.getMarketCoin(id);
	}

	@Override
	public IExchangeSession getSession(MarketCoin marketCoin, boolean resetPublic, boolean resetPrivate) {
		if (session == null) {
			session = new CachedExchangeSession(service, marketCoin, start, end);
		}
		return session;
	}

	@Override
	public MarketCoin getDefaultMarketCoin() {
		return service.getDefaultMarketCoin();
	}

	@Override
	public Map<String, MarketCoin> getMarketCoins() {
		return service.getMarketCoins();
	}

	@Override
	public BigDecimal getFee() {
		return service.getFee();
	}

	@Override
	public BigDecimal getImmediateFee() {
		return service.getImmediateFee();
	}

	@Override
	public CurrencyFactory getCurrencyFactory() {
		return service.getCurrencyFactory();
	}

	@Override
	public void onBeforeSaveOrder(IOrder order) {
		service.onBeforeSaveOrder(order);
	}

	@Override
	public boolean isOrderExecuted(IApiAccess apiAccess, String orderNumber) throws ExchangeException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ITickerList getAllTikers() throws ExchangeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOrderList getAllOrders(IApiAccess apiAccess) throws ExchangeException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IChartDataList getAllChartData(String currencyPair, IDataChartPeriod period, LocalDateTime start,
			LocalDateTime end) throws ExchangeException {
		// TODO Auto-generated method stub
		return null;
	}

}
