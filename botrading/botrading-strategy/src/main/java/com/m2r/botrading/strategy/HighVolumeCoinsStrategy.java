package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.CurrencyDefault;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.MarketCoinDefault;
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
	public List<ICurrency> selectCurrencies(IExchangeSession session, int count, List<String> ignoredCoins) {
		List<ICurrency> list = new ArrayList<>();
		try {
			ITickerList tl = session.getTikers();
			List<ITicker> sorted = tl.getTickers(session.getMarketCoin().getId()).stream().sorted((c1,c2) -> c2.getBaseVolume().compareTo(c1.getBaseVolume())).collect(Collectors.toList());
			
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