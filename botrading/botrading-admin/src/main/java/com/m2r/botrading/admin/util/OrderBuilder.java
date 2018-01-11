package com.m2r.botrading.admin.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.api.util.CalcUtil;

public class OrderBuilder {

    public static List<Order> createAll(Trader t, BigDecimal lastPrice) {
    		List<Order> list = new ArrayList<>();
    	
		int countParcels = t.countParcels();
		BigDecimal residue = t.getInvestment();
		BigDecimal total = CalcUtil.divide(residue, new BigDecimal(countParcels+".0"));
		
		int parcelNum = 1;
		
		if (!CalcUtil.valueLessThanOne(t.getParcel1())) {
			total = countParcels == 1 ? residue : total;
			Order buyOrder = createBuying(t, parcelNum++, lastPrice, total, t.getParcel1(), t.getFee());
			Order selOrder = createSelling(buyOrder, lastPrice, t.getParcel1(), t.getFee());
			list.add(buyOrder);
			list.add(selOrder);
			residue = residue.subtract(total);
			countParcels--;
		}
		
		if (!CalcUtil.valueLessThanOne(t.getParcel2())) {
			total = countParcels == 1 ? residue : total;
			Order buyOrder = createBuying(t, parcelNum++, lastPrice, total, t.getParcel2(), t.getFee());
			Order selOrder = createSelling(buyOrder, lastPrice, t.getParcel2(), t.getFee());
			list.add(buyOrder);
			list.add(selOrder);
			residue = residue.subtract(total);
			countParcels--;
		}
		
		if (!CalcUtil.valueLessThanOne(t.getParcel3())) {
			total = countParcels == 1 ? residue : total;
			Order buyOrder = createBuying(t, parcelNum++, lastPrice, total, t.getParcel3(), t.getFee());
			Order selOrder = createSelling(buyOrder, lastPrice, t.getParcel3(), t.getFee());
			list.add(buyOrder);
			list.add(selOrder);
			residue = residue.subtract(total);
			countParcels--;
		}
		
		if (!CalcUtil.valueLessThanOne(t.getParcel4())) {
			total = countParcels == 1 ? residue : total;
			Order buyOrder = createBuying(t, parcelNum++, lastPrice, total, t.getParcel4(), t.getFee());
			Order selOrder = createSelling(buyOrder, lastPrice, t.getParcel4(), t.getFee());
			list.add(buyOrder);
			list.add(selOrder);
			residue = residue.subtract(total);
			countParcels--;
		}
		
		return list;
    }
    
    /*
     * amount = price / total
     */
    public static Order createBuying(Trader t, Integer parcel, BigDecimal lastPrice, BigDecimal total, BigDecimal percent, BigDecimal fee) {
		BigDecimal priceToBuy = lastPrice.multiply(percent.subtract(fee).divide(CalcUtil.HUNDRED));
		priceToBuy = lastPrice.add(priceToBuy.negate());
		Order o = new Order();
		o.setTrader(t);
		o.setDateTime(LocalDateTime.now());
		o.setParcel(parcel);
		o.setKind(Order.KIND_BUY);
		o.setState(Order.STATE_NEW);
		o.setPrice(priceToBuy);
		o.setAmount(CalcUtil.divide(total, priceToBuy));
		return o;
    }
	
    /*
     * total = price * amount
     */
    public static Order createSelling(Order buyOrder, BigDecimal lastPrice, BigDecimal percent, BigDecimal fee) {
		BigDecimal priceToSell = lastPrice.multiply(percent.subtract(fee).divide(CalcUtil.HUNDRED));
		priceToSell = lastPrice.add(priceToSell);
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
