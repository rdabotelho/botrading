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

public class MiddleCoinsStrategy implements IStrategy {

    private final static Logger LOG = Logger.getLogger(MiddleCoinsStrategy.class.getSimpleName());

    private static final String NAME = "MIDDLE COINS";
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<ICurrency> selectCurrencies(IExchangeSession session, int count, List<String> ignoredCoins) {
		List<ICurrency> list = new ArrayList<>();
		try {
			ITickerList tl = session.getTikers();
			List<ITicker> sorted = tl.getTickers(session.getMarketCoin().getId()).stream().sorted((c1,c2) -> c2.getPercentChange().compareTo(c1.getPercentChange())).collect(Collectors.toList());
			int start = (sorted.size() / 2) - (count / 2);
			
			int limit = count;
			for (int i=start; i<sorted.size(); i++) {
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
