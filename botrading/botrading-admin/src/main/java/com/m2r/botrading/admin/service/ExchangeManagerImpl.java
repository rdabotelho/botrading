package com.m2r.botrading.admin.service;

import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.binance.BinanceExchange;
import com.m2r.botrading.poloniex.PoloniexExchange;

@Service("exchangeManager")
@Transactional
@Scope(value="prototype")
public class ExchangeManagerImpl implements IExchangeManager {
	
    private final static Logger LOG = Logger.getLogger(ExchangeManagerImpl.class.getSimpleName());

	private IExchangeService exchangeService;
	
	@Autowired
	private PropertiesService serverProperties; 
	
	@PostConstruct
	protected void init() {
    	LOG.info("Service working with the EXCHANGE: " + serverProperties.getExchangeId());    		
		if (serverProperties.getExchangeId().equals(PoloniexExchange.EXCHANGE_ID)) {
			exchangeService = new PoloniexExchange().init();
		}
		else if (serverProperties.getExchangeId().equals(BinanceExchange.EXCHANGE_ID)) {
			exchangeService = new BinanceExchange().init();
		}
	}
	
	@Override
	public IExchangeService getExchangeService() {
		return exchangeService;
	}

}
