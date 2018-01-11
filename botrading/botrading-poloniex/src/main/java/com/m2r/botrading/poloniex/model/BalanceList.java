package com.m2r.botrading.poloniex.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.m2r.botrading.api.model.IBalance;
import com.m2r.botrading.api.model.IBalanceList;

public class BalanceList<T extends IBalance> implements IBalanceList {

	private List<IBalance> balances;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends IBalance> IBalanceList of(Map<String, T> balancesMap) {
		IBalanceList bl = new BalanceList(balancesMap);
		return bl;
	}
	
	public BalanceList(Map<String, T> balancesMap) {
		this.balances = new ArrayList<>();
		balancesMap.forEach((k,v)->{
			v.setCoin(k);
			balances.add(v);
		});
	}
	
	@Override
	public List<IBalance> getBalances() {
		return this.balances;
	}
	
	@Override
	public IBalance getBalance(String coin) {
		return balances.stream().filter(b -> b.getCoin().equals(coin)).findFirst().orElse(null);
	}

}
