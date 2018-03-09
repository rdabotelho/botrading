package com.m2r.botrading.ws.exchange;

import java.util.List;

import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IDataChartPeriod;
import com.m2r.botrading.api.service.IExchangeBasic;

public class ExchangeWSServerBuilder {

	public static WithApiAccess withExchangeService(IExchangeBasic service) {
		return new ExchangeWSServerFlow(service);
	}	

	// Interfaces
	
	public static interface WithApiAccess {
		public WithPort withApiAccess(IApiAccess apiAccess);
	}
	
	public static interface WithPort {
		public WithChannel withPort(int port);
	}
	
	public static interface WithChannel {
		public WithTickerPush withChannel(String channel);
	}
	
	public static interface WithTickerPush {
		public WithChartdata30Push withoutTicker();
		public WithChartdata30Push withTicker(List<String> currencyCoins);
	}
	
	public static interface WithChartdata30Push {
		public Builder withoutChartdata30Push();
		public Builder withChartdata30Push(List<String> currencyCoins, IDataChartPeriod period);
	}
	
	public static interface Builder {
		public ExchangeWSServer build();
	}
	
}
