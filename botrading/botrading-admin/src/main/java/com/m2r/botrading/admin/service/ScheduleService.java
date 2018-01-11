package com.m2r.botrading.admin.service;

import java.util.List;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.service.IExchangeSession;

public interface ScheduleService {
	
	public IMarketCoin getMarketCoin(String id);
	public IExchangeSession getExchangeSession(IMarketCoin marketCoin, boolean resetPublic, boolean resetPrivate);
    public List<TraderJob> findAllTraderJobsByState(Integer state);
	public List<Trader> findAllByTraderJob(TraderJob traderJob);
	public List<Order> findAllToSchedule();
    public Order findByTraderAndParcelAndKind(Trader trader, Integer parcel, Integer kind);
    public void synchronize(Trader trader, IExchangeSession session) throws Exception;
    public void executeOrder(Order order, IExchangeSession session);
	public void saveOrder(Order order);
    public void verifyAndCreateNewTrading(TraderJob traderJob, IExchangeSession session) throws Exception;
        
}
