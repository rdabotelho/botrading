package com.m2r.botrading.admin.schedule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.service.IExchangeManager;
import com.m2r.botrading.admin.service.PropertiesService;
import com.m2r.botrading.admin.service.ScheduleService;
import com.m2r.botrading.admin.util.TimeCounter;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.sim.clplus.UltimateSimulator;
import com.m2r.botrading.strategy.CatLeap;

@Component
public class OrderScheduledTasks {

    private final static Logger LOG = Logger.getLogger(OrderScheduledTasks.class.getSimpleName());

    private final static int SCHEDULE_TIME = 1000; 	// milliseconds
    
    private TimeCounter orderTaskTimeCounter = TimeCounter.of(10000, this::executeOrdersTask);
    private TimeCounter synchTaskTimeCounter = TimeCounter.of(30000, this::executeSynchTask);
    private TimeCounter catLeapTaskTimeCounter = TimeCounter.of(10000, this::executeCatLeapTask);
    private TimeCounter ultimateTaskTimeCounter = TimeCounter.of(24*60*60*1000, this::executeUltimateTask);
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private PropertiesService propertiesService;
    
    @Autowired
	private IExchangeManager exchangeManager;
    
	private Map<Long, IExchangeSession> sessions = new HashMap<>();
	
	@Scheduled(fixedDelay = SCHEDULE_TIME)
	public void scheduleTask() {
		if (!propertiesService.getEnableScheduling()) {
			return;
		}
		//orderTaskTimeCounter.ifTimeoutExecute();
		//synchTaskTimeCounter.ifTimeoutExecute();
		//catLeapTaskTimeCounter.ifTimeoutExecute();
		//ultimateTaskTimeCounter.ifTimeoutExecute();
	}
	
	private void executeOrdersTask() {
		initSessions();
		List<Order> orders = scheduleService.findAllToScheduleTaskOrders();
		for (Order order : orders) {
			IExchangeSession session = loadSession(order.getTrader().getTraderJob());
			scheduleService.executeOrder(order, session);
		}		
	}
	
	private void executeSynchTask() {
		List<TraderJob> traderJobs = scheduleService.findAllTraderJobsByState(TraderJob.STATE_STARTED);
		for (TraderJob traderJob : traderJobs) {
			try {
				IExchangeSession session = loadSession(traderJob);
				List<Trader> traders = scheduleService.findAllByTraderJobAndStateNotComplete(traderJob);
				for (Trader trader : traders) {
					scheduleService.synchronize(trader, session);
				}
				scheduleService.verifyAndCreateNewTrading(traderJob.getId(), session);
			} 
			catch (Exception e) {
				LOG.warning(e.getMessage());
			}
		}
	}
	
	private void executeCatLeapTask() {
		String[] marketCoinIds = new String[] {Currency.BTC, Currency.USDT}; 
		for (String marketCoinId : marketCoinIds) {
			MarketCoin marketCoin = scheduleService.getMarketCoin(marketCoinId);
			IExchangeSession session = scheduleService.getExchangeSession(marketCoin, false, false);
			try {
				CatLeap.foodCatLeap(session, 20);
			} catch (Exception e) {
				LOG.warning(e.getMessage());
			}			
		}
	}
	
	@Async
	private void executeUltimateTask() {
		try {
			IExchangeService service = exchangeManager.getExchangeService(); 
			UltimateSimulator.execute(service, Currency.BTC, true);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initSessions() {
		sessions.clear();
	}
	
	public IExchangeSession loadSession(TraderJob traderJob) {
		IExchangeSession session = sessions.get(traderJob.getId());
		if (session == null) {
			MarketCoin marketCoin = scheduleService.getMarketCoin(traderJob.getMarketCoin());
			session = scheduleService.getExchangeSession(marketCoin, false, true);
			sessions.put(traderJob.getId(), session);
		}
		return session;
	}
		
}

