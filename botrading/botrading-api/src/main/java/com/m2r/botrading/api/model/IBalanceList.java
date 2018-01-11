package com.m2r.botrading.api.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public interface IBalanceList {
	
	public List<IBalance> getBalances();
	public IBalance getBalance(String coin);
	default public BigDecimal getAmount() {
		BigDecimal total = BigDecimal.ZERO;
		for (IBalance b : getBalances()) {
			total = total.add(b.getBtcValue());
		}
		return total.setScale(8, RoundingMode.DOWN);
	}

}
