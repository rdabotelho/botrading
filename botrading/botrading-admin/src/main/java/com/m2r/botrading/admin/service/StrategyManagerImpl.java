package com.m2r.botrading.admin.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.persistence.PostRemove;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.service.IStrategyManager;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.strategy.CatLeapStrategy;
import com.m2r.botrading.strategy.DynamicStrategy;
import com.m2r.botrading.strategy.IIntention;
import com.m2r.botrading.strategy.JobOffer;
import com.m2r.botrading.ws.IntentionClient;
import com.m2r.botrading.ws.example.ServerTest;

import rx.functions.Action1;

@Component
@Scope(scopeName = "application")
public class StrategyManagerImpl implements Action1<IIntention>, IStrategyManager {

	private final static Logger LOG = Logger.getLogger(StrategyManagerImpl.class.getSimpleName());
	
	private static final String WS_URL = "ws://localhost:8280/ws1";
	public static final String STRATEGIES_URL = "http://localhost:8180/strategies";
	
	private IntentionClient client;
	private List<JobOffer> offersBook = new ArrayList<>();
	private List<IStrategy> strategies = new ArrayList<>();
	
	@PostConstruct	
	private void init() {
		strategies.addAll(loadDynamicStrategies());
		strategies.add(new CatLeapStrategy());
		//strategies.add(new LowBollingerStrategy());
		client = IntentionClient.build(WS_URL, ServerTest.Intention.class, this).start();
	}
	
	@PostRemove
	private void finish() {
		client.close();
	}
	
	public List<DynamicStrategy> loadDynamicStrategies() {
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet get = new HttpGet(STRATEGIES_URL);
			CloseableHttpResponse response = httpClient.execute(get);
			HttpEntity responseEntity = response.getEntity();
			String data = EntityUtils.toString(responseEntity);
			return new Gson().fromJson(data, new TypeToken<List<DynamicStrategy>>() {}.getType());
		} 
		catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return new ArrayList<>();
		}
	}
	
	@Override
	public void call(IIntention intention) {
		
		// Verify if have some job offer for this strategy
		List<JobOffer> offers = offersBook.stream().filter(o -> o.getUuidStrategy().equals(intention.getUuidStrategy())).collect(Collectors.toList());
		
		// If exist offer then save the intent 
		for (JobOffer offer : offers) {
			if (!offer.getIgnoredCoins().contains(intention.getCurrencyPair())) {
				offer.getIntentions().add(intention);
				offer.getIgnoredCoins().add(intention.getCurrencyPair());
			}
		}
	}
	
	public List<IStrategy> getStrategies() {
		return strategies;
	}
	
	@Override
	public IStrategy getStrategyByName(String name) {
		return strategies.stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getEnviromentObject(Class<T> classOfT, String id) {
		if (id.equals("offersBook")) {
			return (T) offersBook;
		}
		return null;
	}

}
