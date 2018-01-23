package com.m2r.botrading.strategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.model.OrderIntent;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.api.util.CalcUtil;

public class LowBollingerStrategy implements IStrategy {

    private final static Logger LOG = Logger.getLogger(LowBollingerStrategy.class.getSimpleName());
    
    public static final String TARGET_URL = "http://208.113.135.137:8180/intentions";
    
	private static final String NAME = "LOW BOLLINGER";
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public List<IOrderIntent> selectOrderIntent(IExchangeSession session, int count, List<String> ignoredCoins) {
		List<IOrderIntent> list = new ArrayList<>();
		try {
			List<Intention> intentions = getIntantions(session.getMarketCoin().getId());
			if (intentions == null) {
				return list;
			}
			int limit = count;
			for (int i=0; i<intentions.size(); i++) {
				Intention intention = intentions.get(i);
				String coin = MarketCoin.currencyPairToCurrencyId(intention.getCurrencyPair());
				if (!ignoredCoins.contains(coin) && isNotLowVolume(session, intention.getCurrencyPair())) {
					list.add(OrderIntent.of(new Currency(coin, coin), intention.getBuyPrice(), intention.getSalePrice(), true));					
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
	
	private static final BigDecimal MIN_VOLUME = new BigDecimal("150.00");
	
	private boolean isNotLowVolume(IExchangeSession session, String currencyPair) throws Exception {
		ITicker ticker = session.getTikers().getTicker(currencyPair);
		return !CalcUtil.lessThen(ticker.getBaseVolume(), MIN_VOLUME);
	}
	
	private List<Intention> getIntantions(String marketCoin) throws Exception {
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    HttpGet get = new HttpGet(TARGET_URL+"?marketCoin="+marketCoin);
	    CloseableHttpResponse response = httpClient.execute(get);
	    HttpEntity responseEntity = response.getEntity();
	    String data = EntityUtils.toString(responseEntity);
	    return new Gson().fromJson(data, new TypeToken<List<LowBollingerStrategy.Intention>>(){}.getType());
	}

	public static class Intention {

		private String currencyPair;
		private BigDecimal buyPrice;
		private BigDecimal salePrice;

		public String getCurrencyPair() {
			return currencyPair;
		}

		public void setCurrencyPair(String currencyPair) {
			this.currencyPair = currencyPair;
		}

		public BigDecimal getBuyPrice() {
			return buyPrice;
		}

		public void setBuyPrice(BigDecimal buyPrice) {
			this.buyPrice = buyPrice;
		}

		public BigDecimal getSalePrice() {
			return salePrice;
		}

		public void setSalePrice(BigDecimal salePrice) {
			this.salePrice = salePrice;
		}

	}
	
	@Override
	public boolean isReplacePrice() {
		return true;
	}

}
