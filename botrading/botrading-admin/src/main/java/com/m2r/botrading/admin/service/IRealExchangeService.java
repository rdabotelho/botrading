package com.m2r.botrading.admin.service;

import com.m2r.botrading.api.model.ITickerList;

public interface IRealExchangeService {
	
	public ITickerList getTikersWithoutCache() throws Exception;

}
