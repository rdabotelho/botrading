package com.m2r.botrading.admin.service;

import java.util.List;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.IExchangeSession;

public interface ScheduleService {
	
	public MarketCoin getMarketCoin(String id);
	public IExchangeSession getExchangeSession(MarketCoin marketCoin, boolean resetPublic, boolean resetPrivate);
    public List<TraderJob> findAllTraderJobsByState(Integer state);
	public List<Trader> findAllByTraderJobAndStateNotComplete(TraderJob traderJob);
	public List<Order> findAllToScheduleTaskOrders();
	public List<Order> findAllToScheduleTaskSynch(Trader trader);
    public Order findByTraderAndParcelAndKind(Trader trader, Integer parcel, Integer kind);
    public void synchronize(Trader trader, IExchangeSession session) throws Exception;
    public void executeOrder(Order order, IExchangeSession session);
	public void saveOrder(Order order);
    public void verifyAndCreateNewTrading(Long traderJobId, IExchangeSession session) throws Exception;
    
}
