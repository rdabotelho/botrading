package com.m2r.botrading.admin.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.m2r.botrading.admin.model.Account;
import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.util.AccountExchangeInfo;
import com.m2r.botrading.admin.util.OrderExchangeInfo;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.strategy.IStrategy;

public interface AdminService {
	
	public Account findAccount(String apiKey);	
    public List<TraderJob> findAllTraderJobs(Account account);
    public Page<Trader> findAllTraderByTraderJob(TraderJob traderJob, Pageable pageable);
    public TraderJob newTraderJob(Account account);
	public void saveTraderJob(TraderJob traderJob);
	public void finishTraderJob(Long id);
	public void deleteTraderJob(Long id);
    public TraderJob findTraderJob(Long id, Account account);
    public List<Order> findAllOrders(Trader trader);	
    public Order findOrder(Long id);
    public Trader findTrader(Long id);
    public List<IStrategy> getStrategies();
    public void startTraderJob(TraderJob traderJob) throws Exception;
    public void cancelOrder(Order order);
	public void retryOrder(Order order);
	public void immediateSell(Order order);
	public AccountExchangeInfo getAccountExchangeInfo(Account account);
	public OrderExchangeInfo getOrderExchangeInfo(Trader trader);
	public IMarketCoin getDefaultMarketCoin();
	public List<IMarketCoin> getMarketCoins();
	public IMarketCoin getMarketCoin(String id);
	
}
