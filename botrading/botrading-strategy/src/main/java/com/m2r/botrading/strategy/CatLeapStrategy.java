package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.model.OrderIntent;
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
	public List<IOrderIntent> selectOrderIntent(IExchangeSession session, int count, List<String> ignoredCoins) {
		List<IOrderIntent> list = new ArrayList<>();
		try {
			List<CatLeap> sorted = CatLeap.getList(session.getMarketCoin());
			if (sorted == null) {
				return list;
			}
			int limit = count;
			for (int i=0; i<sorted.size(); i++) {
				String coin = MarketCoin.currencyPairToCurrencyId(sorted.get(i).getCurrencyPair());
				if (!ignoredCoins.contains(coin)) {
					list.add(OrderIntent.of(new Currency(coin, coin)));					
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
	
	@Override
	public boolean isReplacePrice() {
		return false;
	}
	
}