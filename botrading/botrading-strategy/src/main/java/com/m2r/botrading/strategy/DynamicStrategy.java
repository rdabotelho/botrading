package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;

import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.service.IStrategyManager;
import com.m2r.botrading.api.strategy.IStrategy;

public class DynamicStrategy implements IStrategy {

	private static final String OFFERS_BUOK_ID = "offersBook";
	
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
	@SuppressWarnings({ "unused", "unchecked" })
	public List<IOrderIntent> selectOrderIntent(IStrategyManager manager, IExchangeSession session, ITraderJob traderJob, int count, List<String> ignoredCoins) {
		List<JobOffer> offers = manager.getEnviromentObject(List.class, OFFERS_BUOK_ID);
		JobOffer offer = offers.stream().filter(o -> o.getTraderJobId().equals(traderJob.getId())).findFirst().orElse(null);
		if (offer != null) {
			
		}
		return emptyList;
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

}
