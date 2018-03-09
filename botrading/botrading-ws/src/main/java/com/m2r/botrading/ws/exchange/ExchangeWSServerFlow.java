package com.m2r.botrading.ws.exchange;

import java.util.List;

import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IDataChartPeriod;
import com.m2r.botrading.api.service.IExchangeBasic;
import com.m2r.botrading.ws.exchange.ExchangeWSServerBuilder.Builder;
import com.m2r.botrading.ws.exchange.ExchangeWSServerBuilder.WithApiAccess;
import com.m2r.botrading.ws.exchange.ExchangeWSServerBuilder.WithChannel;
import com.m2r.botrading.ws.exchange.ExchangeWSServerBuilder.WithChartdata30Push;
import com.m2r.botrading.ws.exchange.ExchangeWSServerBuilder.WithPort;
import com.m2r.botrading.ws.exchange.ExchangeWSServerBuilder.WithTickerPush;

public class ExchangeWSServerFlow implements WithApiAccess, WithPort, WithChannel, WithTickerPush, WithChartdata30Push, Builder {

	private List<String> currencyCoinsToTickerPush;
	private List<String> currencyCoinsToCHartdata30Push;
	private IDataChartPeriod periodToCHartdata30Push;
	private String channel;
	private int port;
	private IApiAccess apiAccess;
	private IExchangeBasic service;
	
	@Override
	public ExchangeWSServer build() {
		return new ExchangeWSServer(service, apiAccess, port, channel, currencyCoinsToTickerPush, currencyCoinsToCHartdata30Push, periodToCHartdata30Push);
	}
	
	@Override
	public Builder withChartdata30Push(List<String> currencyCoins, IDataChartPeriod period) {
		this.currencyCoinsToCHartdata30Push = currencyCoins;
		this.periodToCHartdata30Push = period;
		return this;
	}
	
	@Override
	public Builder withoutChartdata30Push() {
		this.currencyCoinsToCHartdata30Push = null;
		this.periodToCHartdata30Push = null;
		return this;
	}
	
	@Override
	public WithChartdata30Push withoutTicker() {
		this.currencyCoinsToTickerPush = null;
		return this;
	}
	
	@Override
	public WithChartdata30Push withTicker(List<String> currencyCoins) {
		this.currencyCoinsToTickerPush = currencyCoins;
		return this;
	}

	@Override
	public WithTickerPush withChannel(String channel) {
		this.channel = channel;
		return this;
	}

	@Override
	public WithChannel withPort(int port) {
		this.port = port;
		return this;
	}
	
	@Override
	public WithPort withApiAccess(IApiAccess apiAccess) {
		this.apiAccess = apiAccess;
		return this;
	}
	
	public WithApiAccess withExchangeService(IExchangeBasic service) {
		this.service = service;
		return this;
	}

	protected ExchangeWSServerFlow(IExchangeBasic service) {
		this.service = service;
	}

}
