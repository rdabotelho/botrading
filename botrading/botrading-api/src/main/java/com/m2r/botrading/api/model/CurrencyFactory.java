package com.m2r.botrading.api.model;

import com.m2r.botrading.api.service.IExchangeService;

public abstract class CurrencyFactory {

	public MarketCoin currencyPairToMarketCoin(String currencyPair) {
		return currencyPairToMarketCoin(getCurrencyPairConverter().stringToCurrencyPair(currencyPair));
	}
	
	public MarketCoin currencyPairToMarketCoin(CurrencyPairIds currencyPairIds) {
		return createMarketCoin(currencyPairIds.getMarketCoinId() , currencyPairIds.getMarketCoinId());
	}
	
	public Currency currencyPairToCurrency(String currencyPair, IExchangeService service) {
		return currencyPairToCurrency(getCurrencyPairConverter().stringToCurrencyPair(currencyPair), service);
	}
	
	public Currency currencyPairToCurrency(CurrencyPairIds currencyPairIds, IExchangeService service) {
		MarketCoin marketCoin = service.getMarketCoin(currencyPairIds.getMarketCoinId());
		return createCurrency(marketCoin, currencyPairIds.getCurrencyId(), currencyPairIds.getCurrencyId());
	}
	
	public Currency currencyPairToCurrency(MarketCoin marketCoin, CurrencyPairIds currencyPairIds) {
		return createCurrency(marketCoin, currencyPairIds.getCurrencyId(), currencyPairIds.getCurrencyId());
	}
	
	public MarketCoin createMarketCoin(String id, String name) {
		return new MarketCoin(createCurrency(null, id, name));
	}
	
	public String currencyToCurrencyPair(Currency currency) {
		return getCurrencyPairConverter().currencyPairToString(CurrencyPairIds.of(currency));
	}
	
	protected Currency createCurrency(MarketCoin marketCoin, String id, String name) {
		return new Currency(this, marketCoin, id, name);
	}
	
	public abstract ICurrencyPairConverter getCurrencyPairConverter();
	
}
