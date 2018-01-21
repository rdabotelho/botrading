package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.model.OrderIntent;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;

public class HighVolumeCoinsStrategy implements IStrategy {

    private final static Logger LOG = Logger.getLogger(HighVolumeCoinsStrategy.class.getSimpleName());
    
	private static final String NAME = "HIGH VOLUME COINS";
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<IOrderIntent> selectOrderIntent(IExchangeSession session, int count, List<String> ignoredCoins) {
		List<IOrderIntent> list = new ArrayList<>();
		try {
			ITickerList tl = session.getTikers();
			List<ITicker> sorted = tl.getTickers(session.getMarketCoin().getId()).stream().sorted((c1,c2) -> c2.getBaseVolume().compareTo(c1.getBaseVolume())).collect(Collectors.toList());
			
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