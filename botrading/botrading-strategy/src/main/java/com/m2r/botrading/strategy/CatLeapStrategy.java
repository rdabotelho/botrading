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

public class CatLeapStrategy extends StrategyBase {

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
				CatLeap catLeap = sorted.get(i);
				CurrencyPairIds currencyPairIds = session.getCurrencyFactory().getCurrencyPairConverter().stringToCurrencyPair(catLeap.getCurrencyPair());
				if (!ignoredCoins.contains(currencyPairIds.getCurrencyId()) && filter(session, traderJob, catLeap.getCurrencyPair())) {
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
	
	@Override
	public String getUuid() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}
	
	@Override
	public String getInfo() {
		return "";
	}
	
}