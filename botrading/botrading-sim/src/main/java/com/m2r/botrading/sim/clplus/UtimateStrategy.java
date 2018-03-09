package com.m2r.botrading.sim.clplus;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.service.IStrategyManager;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.poloniex.model.ChartData;
import com.m2r.botrading.strategy.StrategyBase;
import com.m2r.botrading.ws.exchange.ExchangeWSClient;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder;

public class UtimateStrategy extends StrategyBase {
	
	private static final String URL = "ws://localhost:8080/wsexchange";

	private static ExchangeWSClient client;

	public static void execute(IExchangeService service) throws Exception {
		client = ExchangeWSClientBuilder
				.withExchangeService(service)
				.withUrl(URL)
				.withTickerAction(UtimateStrategy::whenTicker)
				.withLiquidedAction(UtimateStrategy::whenLiquided)
				.withCanceledAction(UtimateStrategy::whenCanceled)
				.withChartdata30Action(UtimateStrategy::whenChartdata30)
				.build();
		
		client.start();

		client.waitToConnect();
	}	
	
	public static void whenTicker(String json) {
	}
	
	public static void whenLiquided(String json) {
	}
	
	public static void whenCanceled(String json) {
	}
	
	public static void whenChartdata30(String currencyPair, String data) {
		if (currencyPair.equals("BTC_LTC")) {
			List<IChartData> list = new Gson().fromJson(data,  new TypeToken<List<ChartData>>(){}.getType());
			
			IChartData cd = list.get(list.size()-1);
	
			ZonedDateTime date = ZonedDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(cd.getDate())), ZoneId.systemDefault());
			String sDate = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
			
			System.out.println(String.format("%11s %11s %16s", currencyPair, cd.getClose(), sDate));
		}
	}
	
	public static void main(String[] args) throws Exception {
		IExchangeService service = new PoloniexExchange().init();		
		execute(service);
		Thread.sleep(10*60000);
	}

	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<IOrderIntent> selectOrderIntent(IStrategyManager manager, IExchangeSession session,
			ITraderJob traderJob, int count, List<String> ignoredCoins) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isReplacePrice() {
		// TODO Auto-generated method stub
		return false;
	}

}
