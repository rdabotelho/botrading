package com.m2r.botrading.admin.observer;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.repositories.OrderRepository;
import com.m2r.botrading.admin.repositories.TraderJobRepository;
import com.m2r.botrading.admin.repositories.TraderRepository;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.ws.exchange.JsonOrderResult;

@Component
@Transactional
@Scope(value="application")
public class SynchWSObserver extends BaseWSObserver {

    @Autowired
    private TraderJobRepository traderJobRepository;
    
    @Autowired
    private TraderRepository traderRepository;
    
    @Autowired
    private OrderRepository orderRepository;    
	
	public void whenTicker(String json) {
	}
	
	public void whenLiquided(String json) {
		JsonOrderResult result = new Gson().fromJson(json,  new TypeToken<JsonOrderResult>(){}.getType());
		Order order = orderRepository.findByOrderNumber(result.getOrderNumber());
		orderRepository.saveOrder(order.confirmLiquidation());
		LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " liquided in the exchange.");
		if (order.isSell()) {
			this.calculateProfit(order);
		}
		concludeSynchronize(order);
	}
	
	public void whenCanceled(String json) {
		JsonOrderResult result = new Gson().fromJson(json,  new TypeToken<JsonOrderResult>(){}.getType());
		Order order = orderRepository.findByOrderNumber(result.getOrderNumber());
		orderRepository.saveOrder(order.confirmCancellation());
		LOG.info("Order ("+(order.isBuy()?"buy":"sell")+") " + order.getOrderNumber() + " canceled in the exchange.");
		if (order.isSell()) {
			this.calculateProfit(order);
		}
		concludeSynchronize(order);
	}

	@Override
	public void whenChartdata30(String currencyPair, String data) {
	}

    private void calculateProfit(Order sellOrder) {
		Order buyOrder = orderRepository.findByTraderAndParcelAndKind(sellOrder.getTrader(), sellOrder.getParcel(), Order.KIND_BUY);
    	if (buyOrder != null) {
    		BigDecimal profit = CalcUtil.subtract(sellOrder.getBalance(), buyOrder.getBalance());
    		sellOrder.getTrader().addProfit(profit);
    		sellOrder.getTrader().getTraderJob().addProfit(profit);
    		traderRepository.save(sellOrder.getTrader());
    		traderJobRepository.save(sellOrder.getTrader().getTraderJob());
    	}
    }
    
    private void concludeSynchronize(Order order) {
		Trader trader = order.getTrader();
		if (trader.isCompleted()) {
			return;
		}

		// For liquidad sell orders
		if (order.isSell()) {
			// finish the trading
			if (hasNotOpenedOrders(trader)) {
    			traderRepository.complete(trader);
			}
		}
		// For liquided buy orders
		else {
    		Order sellOrder = orderRepository.findByTraderAndParcelAndKind(trader, order.getParcel(), Order.KIND_SELL);
    		if (sellOrder != null) {
	    		if (order.isCanceled()) {
	    			sellOrder.setImmediate(false);
					orderRepository.saveOrder(sellOrder.confirmCancellation());
					concludeSynchronize(sellOrder);
	    		}
	    		else if (order.isLiquided()) {
	    			Currency currency = getExchangeManager().getExchangeService().getCurrency(sellOrder.getTrader().getTraderJob().getMarketCoin(), sellOrder.getTrader().getCoin());
	    			sell(currency.getCurrencyPair(), sellOrder);
	    			sellOrder.setState(Order.STATE_ORDERED);
    				orderRepository.save(sellOrder);		    			
	    		}
    		}
		}
    }
    
    private boolean hasNotOpenedOrders(Trader trader) {
		return orderRepository.countByTraderAndStateNotIn(trader, Order.STATE_LIQUIDED, Order.STATE_CANCELED) == 0;
    }

}
