package com.m2r.botrading.admin.ws;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.m2r.botrading.admin.observer.UltimateObserver;
import com.m2r.botrading.admin.service.IExchangeManager;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.service.IExchangeBasic;
import com.m2r.botrading.poloniex.PoloniexConst;
import com.m2r.botrading.poloniex.enums.PoloniexDataChartPeriod;
import com.m2r.botrading.ws.exchange.ExchangeWSServer;
import com.m2r.botrading.ws.exchange.ExchangeWSServerBuilder;

@Component
@Transactional
@Scope(value="application")
public class ExchengeWSServer {

	private static final String CHANNEL = "wsexchange";
	
    @Autowired
	IExchangeManager exchangeManager;
    
    @Autowired
    UltimateObserver ultimateObserver;
    
	private ExchangeWSServer server;
	
	@PostConstruct
	void init() {
		IApiAccess apiAccess = new IApiAccess() {
			public String getSecretKey() {
				return "c034c84ba459f281e3c5ad43694f3f24d024316bb30bdb8a0071f38879b56424b976a5613da101ecf256ae8a43e100ad835d37040b46d607c4738402cfd828e0";
			}
			@Override
			public String getApiKey() {
				return "A6MSDX56-KPVBAZED-YJ53MWN6-GN8JWZ0X";
			}
		};
		
		IExchangeBasic service = exchangeManager.getExchangeService(); 
		server = ExchangeWSServerBuilder
				.withExchangeService(service)
				.withApiAccess(apiAccess)
				.withPort(8880)
				.withChannel(CHANNEL)
				.withoutTicker()
				.withChartdata30Push(PoloniexConst.TOP_20(Currency.BTC), PoloniexDataChartPeriod.FIVE_MINUTES)
				.build();
		server.start();
		server.waitToConnect();
		ultimateObserver.init();
	}
	
	@PreDestroy
	void finish() {
		server.close();
	}
	
}
