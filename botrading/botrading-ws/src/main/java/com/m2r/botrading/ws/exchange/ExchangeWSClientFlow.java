package com.m2r.botrading.ws.exchange;

import com.m2r.botrading.api.service.IExchangeBasic;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder.Builder;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder.WithCanceledAction;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder.WithChartdata30Action;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder.WithLiquidedAction;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder.WithTickerAction;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder.WithUrl;

import rx.functions.Action1;
import rx.functions.Action2;

public class ExchangeWSClientFlow implements WithUrl, WithTickerAction, WithLiquidedAction, WithCanceledAction, WithChartdata30Action, Builder {

	private IExchangeBasic service;
	private String url;
	private Action1<String> tickerAction; 
	private Action1<String> liquidedAction; 
	private Action1<String> canceledAction;
	private Action2<String,String> chartdata30Action; 
	
	@Override
	public ExchangeWSClient build() {
		return new ExchangeWSClient(service, url, chartdata30Action, tickerAction, liquidedAction, canceledAction);
	}

	@Override
	public WithChartdata30Action withCanceledAction(Action1<String> canceledAction) {
		this.canceledAction = canceledAction;
		return this;
	}
	
	@Override
	public Builder withChartdata30Action(Action2<String,String> chartdata30Action) {
		this.chartdata30Action = chartdata30Action;
		return this;
	}

	@Override
	public WithCanceledAction withLiquidedAction(Action1<String> liquidedAction) {
		this.liquidedAction = liquidedAction;
		return this;
	}

	@Override
	public WithLiquidedAction withTickerAction(Action1<String> tickerAction) {
		this.tickerAction = tickerAction;
		return this;
	}

	@Override
	public WithTickerAction withUrl(String url) {
		this.url = url;
		return this;
	}

	public ExchangeWSClientFlow(IExchangeBasic service) {
		this.service = service;
	}

}
