package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.m2r.botrading.api.model.CurrencyPairIds;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.model.OrderIntent;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.service.IStrategyManager;
import com.m2r.botrading.api.strategy.IStrategy;

public class CatLeapStrategy implements IStrategy {

    private final static Logger LOG = Logger.getLogger(CatLeapStrategy.class.getSimpleName());
    
	private static final String NAME = "CAT LEAP";
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<IOrderIntent> selectOrderIntent(IStrategyManager manager, IExchangeSession session, ITraderJob traderJob, int count, List<String> ignoredCoins) {
		List<IOrderIntent> list = new ArrayList<>();
		try {
			List<CatLeap> sorted = CatLeap.getList(session.getMarketCoin());
			if (sorted == null) {
				return list;
			}
			int limit = count;
			for (int i=0; i<sorted.size(); i++) {
				CurrencyPairIds currencyPairIds = session.getCurrencyFactory().getCurrencyPairConverter().stringToCurrencyPair(sorted.get(i).getCurrencyPair());
				if (!ignoredCoins.contains(currencyPairIds.getCurrencyId())) {
					list.add(OrderIntent.of(session.getCurrencyFactory().currencyPairToCurrency(currencyPairIds, session.getService())));					
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