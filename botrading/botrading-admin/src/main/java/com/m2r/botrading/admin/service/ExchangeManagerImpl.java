package com.m2r.botrading.admin.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.poloniex.PoloniexExchange;

@Service("exchangeManager")
@Transactional
@Scope(value="prototype")
public class ExchangeManagerImpl implements IExchangeManager {

	private Map<String, IExchangeService> exchangeServicesMap = new HashMap<>();
	
	@PostConstruct
	protected void init() {
		exchangeServicesMap.clear();
		exchangeServicesMap.put(PoloniexExchange.EXCHANGE_ID, new PoloniexExchange().init());
	}
	
	@Override
	public IExchangeService getExchangeService(String id) {
		return exchangeServicesMap.get(id);
	}

}
