package com.m2r.botrading.admin.strategy;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.admin.service.IExchangeManager;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.service.IExchangeBasic;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.poloniex.model.ChartData;
import com.m2r.botrading.sim.clplus.Candle;
import com.m2r.botrading.sim.clplus.UltimateAnalyze;
import com.m2r.botrading.ws.exchange.ExchangeWSClient;
import com.m2r.botrading.ws.exchange.ExchangeWSClientBuilder;

@Component
@Transactional
@Scope(value="application")
public class UltimateStrategy {

	private static final String URL = "ws://localhost:8880/wsexchange";
	private static int COUNT_PERIODS = 30 * 5;
	private static final BigDecimal CANDLE_SIZE_FACTOR = new BigDecimal("6");

	
    @Autowired
	IExchangeManager exchangeManager;
    
	private static ExchangeWSClient client;
	
	public void init() {
		IExchangeBasic service = exchangeManager.getExchangeService(); 
		client = ExchangeWSClientBuilder
				.withExchangeService(service)
				.withUrl(URL)
				.withTickerAction(this::whenTicker)
				.withLiquidedAction(this::whenLiquided)
				.withCanceledAction(this::whenCanceled)
				.withChartdata30Action(this::whenChartdata30)
				.build();
		client.start();
		client.waitToConnect();
	}
	
	@PreDestroy
	void finish() {
		client.close();
	}

	public void whenTicker(String json) {
	}
	
	public void whenLiquided(String json) {
	}
	
	public void whenCanceled(String json) {
	}
	
	public void whenChartdata30(String currencyPair, String data) {
		List<IChartData> list = new Gson().fromJson(data,  new TypeToken<List<ChartData>>(){}.getType());
		IExchangeService service = exchangeManager.getExchangeService();
		Currency currency = service.getCurrencyFactory().currencyPairToCurrency(currencyPair, service);
		try {
			Candle candle = UltimateAnalyze.ultimateAnalyze(currency.getId(), list, COUNT_PERIODS, CANDLE_SIZE_FACTOR);
			if (candle.isOpportunity()) {
				String sDate = candle.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
				System.out.println(String.format("%11s %11s %16s", currencyPair, candle.getClose().toString(), sDate));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
