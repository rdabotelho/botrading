package com.m2r.botrading.admin.ws;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.observer.SynchWSObserver;
import com.m2r.botrading.admin.observer.strategies.UltimateObserver;
import com.m2r.botrading.admin.repositories.AccountRepository;
import com.m2r.botrading.admin.repositories.OrderRepository;
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
    
    @Autowired
    SynchWSObserver synchWSObserver;
    
    @Autowired
    AccountRepository accountRepository;
    
    @Autowired
    OrderRepository orderRepository;
    
	private ExchangeWSServer server;
	
	@PostConstruct
	void init() {
		IApiAccess apiAccess = accountRepository.findAll().stream().findFirst().orElse(null);
		
		IExchangeBasic service = exchangeManager.getExchangeService(); 
		server = ExchangeWSServerBuilder
				.withExchangeService(service)
				.withApiAccess(apiAccess)
				.withPort(8880)
				.withChannel(CHANNEL)
				.withoutTicker()
				.withChartdata30Push(PoloniexConst.TOP_5(Currency.BTC), PoloniexDataChartPeriod.FIVE_MINUTES)
				.build();
		server.start();
		server.waitToConnect();
		
		Set<String> orderNumbers = orderRepository.findAllByStateIn(Order.STATE_ORDERED)
				.stream()
				.filter(it->it.getOrderNumber()!=null)
				.map(it->it.getOrderNumber())
				.collect(Collectors.toSet());
		
		ultimateObserver.init(server).update(orderNumbers);
		synchWSObserver.init(server);
	}
	
	@PreDestroy
	void finish() {
		server.close();
	}
	
	public ExchangeWSServer getServer() {
		return server;
	}
	
}
