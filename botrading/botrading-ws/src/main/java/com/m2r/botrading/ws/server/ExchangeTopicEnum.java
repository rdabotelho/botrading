package com.m2r.botrading.ws.server;

import com.google.gson.Gson;
import com.m2r.botrading.api.service.IExchangeService;

import ws.wamp.jawampa.Request;

public enum ExchangeTopicEnum {

	BUY(1){
		@Override
		public void execute(IExchangeService service, Request request) throws Exception {
			JsonApiKey apiAccess = new Gson().fromJson(request.arguments().get(0).asText(), JsonApiKey.class);
			String currencyPair = request.arguments().get(1).asText();
			String price = request.arguments().get(2).asText();
			String amount = request.arguments().get(3).asText();
			String result = service.buy(apiAccess, currencyPair, price, amount);
			request.reply(result);
		}
	}, 
	SELL(1){
		@Override
		public void execute(IExchangeService service, Request request) throws Exception {
			JsonApiKey apiAccess = new Gson().fromJson(request.arguments().get(0).asText(), JsonApiKey.class);
			String currencyPair = request.arguments().get(1).asText();
			String price = request.arguments().get(2).asText();
			String amount = request.arguments().get(3).asText();
			String result = service.sell(apiAccess, currencyPair, price, amount);
			request.reply(result);
		}
	}, 	 
	CANCEL(1){
		@Override
		public void execute(IExchangeService service, Request request) throws Exception {
			JsonApiKey apiAccess = new Gson().fromJson(request.arguments().get(0).asText(), JsonApiKey.class);
			String currencyPair = request.arguments().get(1).asText();
			String orderNumber = request.arguments().get(2).asText();
			service.cancel(apiAccess, currencyPair, orderNumber);
		}
	}, 
	BOUGHT(2), 
	SOLD(2), 	
	CANCELED(2), 	 
	TICKER(3), 
	CHARTDATA(3);
	
	private int priority;

	private ExchangeTopicEnum(int priority) {
		this.priority = priority;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void execute(IExchangeService service, Request request) throws Exception {
	}
	
}
