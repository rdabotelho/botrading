package com.m2r.botrading.strategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.CurrencyPairIds;
import com.m2r.botrading.api.model.IIntention;
import com.m2r.botrading.api.model.IIntentionRequest;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.model.OrderIntent;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.service.IStrategyManager;
import com.m2r.botrading.api.strategy.IStrategy;
import com.m2r.botrading.api.util.CalcUtil;

public class DynamicStrategy implements IStrategy {

    private final static Logger LOG = Logger.getLogger(DynamicStrategy.class.getSimpleName());
    
	private IStrategyManager exchangeWSClient;
	
	private String uuid;
	private String name;
	private String description;
	private String type;
	private List<String> marketsCoin; 
	private List<Parameter> parameters;
	
	private List<IOrderIntent> emptyList = new ArrayList<>();

	public DynamicStrategy() {
		this.parameters = new ArrayList<>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<IOrderIntent> selectOrderIntent(IStrategyManager manager, IExchangeSession session, ITraderJob traderJob, int count, List<String> ignoredCoins) {
		IIntentionRequest intentionRequest = manager.findIntentionRequest(traderJob.getId());
		if (intentionRequest == null) {
			// the first time
			IStrategy strategy = manager.getStrategyByName(name);
			if (strategy != null) {
				intentionRequest = new IntentionRequest(traderJob.getId(), strategy, session.getMarketCoin().getId());
				manager.registerIntentionRequest(intentionRequest);
			}
		}
		else if (intentionRequest.haveReserve()) {
			List<IOrderIntent> orderIntents = this.selectOrderIntent(session, intentionRequest, count, ignoredCoins);
			if (orderIntents.size() == count) {
				manager.removeIntentionRequest(intentionRequest);
			}
			return orderIntents;
		}
		return emptyList;
	}
	
	public List<IOrderIntent> selectOrderIntent(IExchangeSession session, IIntentionRequest intentionRequest, int count, List<String> ignoredCoins) {
		List<IOrderIntent> list = new ArrayList<>();
		try {
			List<IIntention> intentions = intentionRequest.getIntentions().values().stream().map(it -> it).collect(Collectors.toList());
			int limit = count;
			for (int i=0; i<intentions.size(); i++) {
				IIntention intention = intentions.get(i);
				CurrencyPairIds currencyPairIds = session.getCurrencyFactory().getCurrencyPairConverter().stringToCurrencyPair(intention.getCurrencyPair());
				if (!ignoredCoins.contains(currencyPairIds.getCurrencyId()) && isNotLowVolume(session, intention.getCurrencyPair())) {
					list.add(OrderIntent.of(session.getCurrencyFactory().currencyPairToCurrency(currencyPairIds, session.getService()), intention.getBuyPrice(), intention.getSalePrice(), true));
					intentionRequest.getIntentions().remove(intention.getId());
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
	
	public void setExchangeWSClient(IStrategyManager exchangeWSClient) {
		this.exchangeWSClient = exchangeWSClient;
	}
	
	public IStrategyManager getExchangeWSClient() {
		return exchangeWSClient;
	}

	@Override
	public boolean isReplacePrice() {
		return false;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<Parameter> parameters) {
		this.parameters = parameters;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<String> getMarketsCoin() {
		return marketsCoin;
	}

	public void setMarketsCoin(List<String> marketsCoin) {
		this.marketsCoin = marketsCoin;
	}

	@Override
	public String getInfo() {
		StringBuilder str = new StringBuilder();
		str.append("<div style='width:400px;font-size:10px;'>");
		str.append("<strong>Description: </strong><br/>");
		str.append(this.getDescription()).append("<br/><br/>");
		str.append("<strong>Parameters: </strong><br/>");
		for (Parameter param : getParameters()) {
			str.append("<strong>- Type: </strong>\n").append(param.getType()).append("<br/>");
			str.append("<strong>- Value: </strong>\n").append(param.getValue()).append("<br/><br/>");
		}
		str.append("</div>");
		return str.toString();
	}

	public static class Parameter {

		private String type;
		private String value;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynamicStrategy other = (DynamicStrategy) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
