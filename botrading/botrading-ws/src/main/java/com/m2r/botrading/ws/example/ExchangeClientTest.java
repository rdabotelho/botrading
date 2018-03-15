package com.m2r.botrading.ws.example;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.poloniex.model.ChartData;
import com.m2r.botrading.ws.exchange.ExchangeWSClient;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder;

public class ExchangeClientTest {

	private static final String URL = "ws://localhost:8080/ws1";

	private static ExchangeWSClient client;

	public static void main(String[] args) throws Exception {
		IApiAccess apiAccess = new IApiAccess() {
			public String getSecretKey() {
				return "c034c84ba459f281e3c5ad43694f3f24d024316bb30bdb8a0071f38879b56424b976a5613da101ecf256ae8a43e100ad835d37040b46d607c4738402cfd828e0";
			}
			@Override
			public String getApiKey() {
				return "A6MSDX56-KPVBAZED-YJ53MWN6-GN8JWZ0X";
			}
		};
		
		PoloniexExchange service = new PoloniexExchange();
		service.init();
		
		client = ExchangeWSClientBuilder
				.withExchangeService(service)
				.withUrl(URL)
				.withTickerAction(ExchangeClientTest::whenTicker)
				.withLiquidedAction(ExchangeClientTest::whenLiquided)
				.withCanceledAction(ExchangeClientTest::whenCanceled)
				.withChartdata30Action(ExchangeClientTest::whenChartdata30)
				.build();
		
		client.start();

		client.waitToConnect();
		
		waitUntilKeypressed();
		client.close();
	}

	private static void waitUntilKeypressed() {
		try {
			System.in.read();
			while (System.in.available() > 0) {
				System.in.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void whenTicker(String json) {
		System.out.println("NOTIFICATION OF TICKER: " + json);
	}
	
	public static void whenLiquided(String json) {
		System.out.println("NOTIFICATION OF LIQUIDATION: " + json);
	}
	
	public static void whenCanceled(String json) {
		System.out.println("NOTIFICATION OF CANCELED: " + json);
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
}
