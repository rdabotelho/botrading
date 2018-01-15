package com.m2r.botrading.admin.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2r.botrading.admin.model.Account;
import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.repositories.AccountRepository;
import com.m2r.botrading.admin.repositories.OrderRepository;
import com.m2r.botrading.admin.repositories.TraderJobRepository;
import com.m2r.botrading.admin.repositories.TraderRepository;
import com.m2r.botrading.admin.util.AccountExchangeInfo;
import com.m2r.botrading.admin.util.OrderExchangeInfo;
import com.m2r.botrading.api.model.IBalance;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.api.strategy.StrategyRepository;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.strategy.DefaultStrategyRepository;

@Service("adminService")
@Transactional
public class AdminServiceImpl implements AdminService {
    
    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private TraderJobRepository traderJobRepository;
    
    @Autowired
    private TraderRepository traderRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private IExchangeManager exchangeManager;
    
    @Autowired
    private IRealExchangeService realExchangeService;
    
    private StrategyRepository strategyRepository = new DefaultStrategyRepository();
    
    protected IExchangeService getExchangeService() {
    		return exchangeManager.getExchangeService(PoloniexExchange.EXCHANGE_ID);
    }
    
    @Override
    public Account findAccount(String apiKey) {
    		return accountRepository.findByApiKey(apiKey);
    }
    
    @Override
    public List<TraderJob> findAllTraderJobs(Account account) {
    		return traderJobRepository.findAllByAccountAndMarketCoinOrderByStateAscDateTimeDesc(account, account.getSelectedMarketCoin().getId());
    }
    
    @Override
    public Page<Trader> findAllTraderByTraderJob(TraderJob traderJob, Pageable pageable) {
    		return traderRepository.findAllTraderByTraderJobOrderByStateAscDateTimeDesc(traderJob, pageable);
    }
    
    @Override
    public TraderJob newTraderJob(Account account) {
    		TraderJob traderJob = new TraderJob();
    		traderJob.setDateTime(LocalDateTime.now());
    		traderJob.setAccount(account);
		traderJob.setAmount(BigDecimal.ZERO);
    		return traderJob;
    }
    
    @Override
    public void saveTraderJob(TraderJob traderJob) {
    		traderJobRepository.save(traderJob);
    }
    
    @Override
    public void finishTraderJob(Long id) {
		TraderJob traderJob = traderJobRepository.findOne(id);
		for (Trader trader : traderJob.getTraders()) {
			trader.complete();
			traderRepository.save(trader);
		}
		traderJob.finish();
		traderJobRepository.save(traderJob);  	
    }
    
    @Override
    public void deleteTraderJob(Long id) {
    		traderJobRepository.delete(id);
    }
    
    @Override
    public TraderJob findTraderJob(Long id, Account account) {
    		TraderJob traderJob = traderJobRepository.findByIdAndAccount(id, account);
    		if (traderJob != null) {
	    		traderJob.getTraders().forEach(t -> {
	    			t.setOrderedTotal(orderRepository.countByTraderAndState(t, Order.STATE_ORDERED));
	    			t.setLiquidedTotal(orderRepository.countByTraderAndState(t, Order.STATE_LIQUIDED));
	    		});
    		}
    		return traderJob;
    }
    
    @Override
    public void startTraderJob(TraderJob traderJob) throws Exception {
    		IMarketCoin marketCoin = getExchangeService().getMarketCoin(traderJob.getMarketCoin());
	    	IExchangeSession session = getExchangeService().getSession(marketCoin, false, true);
	    	scheduleService.verifyAndCreateNewTrading(traderJob, session);
    }
    
    @Override
    public List<Order> findAllOrders(Trader trader) {
    	return orderRepository.findAllByTraderOrderByIdAsc(trader);
    }
    
    @Override
    public Order findOrder(Long id) {
    		return this.orderRepository.findOne(id);
    }
    
    @Override
    public Trader findTrader(Long id) {
    		return traderRepository.findOne(id);
    }
        
    @Override
    public List<IStrategy> getStrategies() {
    		return strategyRepository.getStrategies();
    }
    
    @Override
    public AccountExchangeInfo getAccountExchangeInfo(Account account) {
    		try {
    			IExchangeSession session = getExchangeService().getSession(account.getSelectedMarketCoin(), false, true);
    			IBalanceList balanceList = session.getBanlances(account);
    			IBalance coinBalance = balanceList.getBalance(account.getSelectedMarketCoin().getId());
    			AccountExchangeInfo tei = new AccountExchangeInfo();
    			tei.setAmount(coinBalance == null ? BigDecimal.ZERO : coinBalance.getAvailable());
    			tei.setCoinBalance(balanceList.getAmount());
    			if (account.getSelectedMarketCoin().getId().equals(ICurrency.BTC)) {
    				tei.setRealBalance(CalcUtil.multiply(getLastRealBTC(), tei.getCoinBalance()));
    			}
    			else {
    				tei.setRealBalance(BigDecimal.ZERO);
    			}
    			return tei;
    		}
    		catch (Exception e) {
    			return null;
    		}
    }
    
    @Override
    public OrderExchangeInfo getOrderExchangeInfo(Trader trader) {
    		try {
    			IExchangeSession session = getExchangeService().getSession(trader.getTraderJob().getAccount().getSelectedMarketCoin(), false, true);
    			ITicker ticker = session.getTikers().getTicker(session.getCurrencyOfTrader(trader).getCurrencyPair());
    			BigDecimal available = session.getAvailableBalance(trader.getCoin(), trader.getTraderJob().getAccount());
        		OrderExchangeInfo oei = new OrderExchangeInfo();
        		oei.setBaseVolume(ticker.getBaseVolume());
        		oei.setLast(ticker.getLast());
        		oei.setLow(ticker.getLow24hr());
        		oei.setPercentChange(ticker.getPercentChange());
        		oei.setAvailable(available);
        		return oei;
    		}
    		catch (Exception e) {
    			return null;
    		}
    }
    
    public BigDecimal getLastRealBTC() {
    		try {
    			ITickerList list = realExchangeService.getTikersWithoutCache();
    			return list.getTicker("ticker").getLast();
    		}
    		catch (Exception e) {
    			return BigDecimal.ZERO;
    		}
    }
    
    @Override
    public IMarketCoin getDefaultMarketCoin() {
    		return getExchangeService().getDefaultMarketCoin();
    }
    
    @Override
    public List<IMarketCoin> getMarketCoins() {
    		return getExchangeService().getMarketCoins().values().stream().map((t) -> t).collect(Collectors.toList());
    }
    
    @Override
    public IMarketCoin getMarketCoin(String id) {
    		return getExchangeService().getMarketCoin(id);
    }
    
    /*
     * ORDERS
     */
    
    @Override
    public void orderBuy(Order order) {
		orderRepository.save(order.preperToBuy());	
    }
    
    @Override
    public void orderSell(Order order) {
		orderRepository.save(order.preperToSell());	    	
    }
    
    @Override
    public void orderImmediateSel(Order order) {
		orderRepository.save(order.preperToImmediateSel());    		    	    	
    }
    
    @Override
    public void orderCancel(Order order) {
		orderRepository.save(order.preperToCancel());   		    	    	    	
    }
    
    @Override
    public void retryOrder(Order order) {
		order.setState(Order.STATE_ORDERED);
		order.setLog("");
		orderRepository.save(order);    	
    }
      
}
