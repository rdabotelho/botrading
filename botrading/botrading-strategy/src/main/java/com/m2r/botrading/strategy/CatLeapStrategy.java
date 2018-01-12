package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.m2r.botrading.api.model.CurrencyDefault;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.MarketCoinDefault;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;

public class CatLeapStrategy implements IStrategy {

    private final static Logger LOG = Logger.getLogger(CatLeapStrategy.class.getSimpleName());
    
	private static final String NAME = "CAT LEAP";
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<ICurrency> selectCurrencies(IExchangeSession session, int count, List<String> ignoredCoins) {
		List<ICurrency> list = new ArrayList<>();
		try {
			List<CatLeap> sorted = CatLeap.getList(session.getMarketCoin());
			if (sorted == null) {
				return list;
			}
			int limit = count;
			for (int i=0; i<sorted.size(); i++) {
				String coin = MarketCoinDefault.currencyPairToCurrencyId(sorted.get(i).getCurrencyPair());
				if (!ignoredCoins.contains(coin)) {
					list.add(new CurrencyDefault(coin, coin));					
	    			limit--;
	    			if (limit == 0) {
	    				break;
	    			}				
				}
			}
		}
		catch (Exception e) {
			LOG.warning(e.getMessage());
		}		
		return list;
	}
	
}