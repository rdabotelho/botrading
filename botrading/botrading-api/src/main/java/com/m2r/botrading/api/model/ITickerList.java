package com.m2r.botrading.api.model;

import java.util.List;

public interface ITickerList {
	
	public List<ITicker> getTickers();
	public List<ITicker> getTickers(String coin);
	public ITicker getTicker(String currencyPair);

}
