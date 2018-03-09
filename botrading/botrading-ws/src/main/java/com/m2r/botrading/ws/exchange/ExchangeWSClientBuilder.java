package com.m2r.botrading.ws.exchange;

import com.m2r.botrading.api.service.IExchangeBasic;

import rx.functions.Action1;
import rx.functions.Action2;

public class ExchangeWSClientBuilder {

	public static WithUrl withExchangeService(IExchangeBasic service) {
		return new ExchangeWSClientFlow(service);
	}
	
	// Interfaces
	
	public static interface WithUrl {
		public WithTickerAction withUrl(String url);
	}
	
	public static interface WithTickerAction {
		public WithLiquidedAction withTickerAction(Action1<String> tickerAction);
	}
	
	public static interface WithLiquidedAction {
		public WithCanceledAction withLiquidedAction(Action1<String> liquidedAction);
	}
	
	public static interface WithCanceledAction {
		public WithChartdata30Action withCanceledAction(Action1<String> canceledAction);
	}
	
	public static interface WithChartdata30Action {
		public Builder withChartdata30Action(Action2<String,String> chartdata30Action);
	}
	
	public static interface Builder {
		public ExchangeWSClient build();
	}
	
}
