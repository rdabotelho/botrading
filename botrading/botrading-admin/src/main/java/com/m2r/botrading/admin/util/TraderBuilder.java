package com.m2r.botrading.admin.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;

public class TraderBuilder {

    public static Trader create(String coin, BigDecimal investment, TraderJob traderJob) {
		return create(
					coin, 
					investment, 
					traderJob.getFee(), 
					traderJob.getParcel1(), 
					traderJob.getParcel2(), 
					traderJob.getParcel3(), 
					traderJob.getParcel4(),
					traderJob);    	
    }
    
    public static Trader create(String coin, BigDecimal investment, BigDecimal fee, BigDecimal parcel1, BigDecimal parcel2, BigDecimal parcel3, BigDecimal parcel4, TraderJob traderJob) {
		Trader t = new Trader();
		t.setCoin(coin);
		t.setDateTime(LocalDateTime.now());
		t.setState(Trader.STATE_NEW);
		t.setInvestment(investment);
		t.setFee(fee);
		t.setParcel1(parcel1);
		t.setParcel2(parcel2);
		t.setParcel3(parcel3);
		t.setParcel4(parcel4);
		t.setTraderJob(traderJob);
		return t;
	}
	
}
