package com.m2r.botrading.admin.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.util.CalcUtil;

public class OrderBuilder {

    public static List<Order> createAll(Trader trader, BigDecimal lastPrice, IOrderIntent intent) {
    	List<Order> list = new ArrayList<>();
    	List<BigDecimal> parcels = trader.getParcels();
		BigDecimal total = CalcUtil.divide(trader.getInvestment(), new BigDecimal(parcels.size() + ".0"));
		if (parcels.isEmpty() && intent.isReplacePrice()) {
			parcels.add(BigDecimal.ZERO);
		}		
		for (int i=0; i<parcels.size(); i++) {
			BigDecimal parcel = parcels.get(i);
			Order buyOrder = createBuying(trader, i+1, lastPrice, total, parcel, trader.getFee(), intent);
			Order selOrder = createSelling(buyOrder, lastPrice, parcel, trader.getFee(), intent);
			list.add(buyOrder);
			list.add(selOrder);
		}
		return list;
    }
    
    /*
     * amount = price / total
     */
    public static Order createBuying(Trader t, Integer parcel, BigDecimal lastPrice, BigDecimal total, BigDecimal percent, BigDecimal fee, IOrderIntent intent) {
		BigDecimal priceToBuy = null;
		if (intent.isReplacePrice()) {
			priceToBuy = intent.getBuyPrice();
		}
		else {
			BigDecimal backToBuy = t.getTraderJob().getOptions().getBackToBuy();
			BigDecimal backParcent =  CalcUtil.percent(CalcUtil.add(CalcUtil.add(percent, fee), fee), backToBuy);
			priceToBuy = CalcUtil.multiply(lastPrice, CalcUtil.divide(CalcUtil.subtract(CalcUtil.HUNDRED, backParcent), CalcUtil.HUNDRED));
		}		
		Order o = new Order();
		o.setTrader(t);
		o.setDateTime(LocalDateTime.now());
		o.setParcel(parcel);
		o.setKind(Order.KIND_BUY);
		o.setState(Order.STATE_ORDERED);
		o.setPrice(priceToBuy);
		o.setAmount(CalcUtil.divide(total, priceToBuy)); // convertion of marketcoin to the destination coin
		return o;
    }
	
    /*
     * total = price * amount
     */
    public static Order createSelling(Order buyOrder, BigDecimal lastPrice, BigDecimal percent, BigDecimal fee, IOrderIntent intent) {
		BigDecimal priceToSell = null;
		if (intent.isReplacePrice()) {
			priceToSell = intent.getSellPrice();
		}
		else {
			BigDecimal percentWithFee = CalcUtil.add(CalcUtil.add(percent, fee), fee);
			priceToSell = CalcUtil.multiply(buyOrder.getPrice(), CalcUtil.add(BigDecimal.ONE, CalcUtil.divide(percentWithFee, CalcUtil.HUNDRED)));
		}
		Order o = new Order();
		o.setTrader(buyOrder.getTrader());
		o.setDateTime(LocalDateTime.now());
		o.setParcel(buyOrder.getParcel());
		o.setKind(Order.KIND_SELL);
		o.setState(Order.STATE_NEW);
		o.setPrice(priceToSell);
		o.setAmount(CalcUtil.subtract(buyOrder.getAmount(), CalcUtil.percent(buyOrder.getAmount(), fee)));
		return o;
    }
    
}
