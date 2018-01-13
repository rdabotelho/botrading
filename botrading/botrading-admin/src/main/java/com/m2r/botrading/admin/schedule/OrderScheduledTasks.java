package com.m2r.botrading.admin.schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.service.PropertiesService;
import com.m2r.botrading.admin.service.ScheduleService;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.strategy.CatLeap;

@Component
public class OrderScheduledTasks {

    private final static Logger LOG = Logger.getLogger(OrderScheduledTasks.class.getSimpleName());

    private final static int SCHEDULE_TIME = 10000; 	// milliseconds
    
    private final static int LIMIT_TO_SYNCH = 60000;	// milliseconds
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private PropertiesService propertiesService;
    
	private Map<Long, IExchangeSession> sessions = new HashMap<>();
	
	private long startTime, time = 0;
	
	@Scheduled(fixedRate = SCHEDULE_TIME)
	public void scheduleTask() {
		if (!propertiesService.getEnableScheduling()) {
			return;
		}
		time = System.currentTimeMillis() - startTime;
		executeOrdersTask();
		if (time > LIMIT_TO_SYNCH) {
			executeSynchTask();
			startTime = System.currentTimeMillis();
		}
		executeCatLeapTask();
	}
	
	private void executeOrdersTask() {
		initSessions();
		List<Order> orders = scheduleService.findAllToSchedule();
		for (Order order : orders) {
			IExchangeSession session = loadSession(order.getTrader());
			scheduleService.executeOrder(order, session);
		}		
	}
	
	private void executeCatLeapTask() {
		String[] marketCoinIds = new String[] {ICurrency.BTC, ICurrency.USDT}; 
		for (String marketCoinId : marketCoinIds) {
			IMarketCoin marketCoin = scheduleService.getMarketCoin(marketCoinId);
			IExchangeSession session = scheduleService.getExchangeSession(marketCoin, false, false);
			try {
				CatLeap.foodCatLeap(session, 20);
			} catch (Exception e) {
				LOG.warning(e.getMessage());
			}			
		}
	}
	
	private void executeSynchTask() {
		List<TraderJob> traderJobs = scheduleService.findAllTraderJobsByState(TraderJob.STATE_STARTED);
		for (TraderJob traderJob : traderJobs) {
			try {
		    	List<Trader> traders = scheduleService.findAllByTraderJobAndStateNotComplete(traderJob);
		    	if (!traders.isEmpty()) {
					for (Trader trader : traders) {
						IExchangeSession session = loadSession(trader);
						scheduleService.synchronize(trader, session);
					}    	
		    	}
			} 
			catch (Exception e) {
				LOG.warning(e.getMessage());
			}
		}
	}
	
	private void initSessions() {
		sessions.clear();
	}
	
	public IExchangeSession loadSession(Trader trader) {
		Long tjId = trader.getTraderJob().getId();
		IExchangeSession session = sessions.get(tjId);
		if (session == null) {
			IMarketCoin marketCoin = scheduleService.getMarketCoin(trader.getTraderJob().getMarketCoin());
			session = scheduleService.getExchangeSession(marketCoin, false, true);
			sessions.put(tjId, session);
		}
		return session;
	}
		
}

