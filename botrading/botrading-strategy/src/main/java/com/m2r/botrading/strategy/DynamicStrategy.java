package com.m2r.botrading.strategy;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.model.IOrderIntent;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.strategy.IStrategy;

public class DynamicStrategy implements IStrategy {

    private final static Logger LOG = Logger.getLogger(DynamicStrategy.class.getSimpleName());
    
	public static final String TARGET_URL = "http://bt3.yuhull.com:8180/strategies";

	private String uuid;
	private String name;
	private String strategy;
	private List<Parameter> parameters;
	
	public static List<DynamicStrategy> loadDynamicStrategies() {
		try {
		    CloseableHttpClient httpClient = HttpClients.createDefault();
		    HttpGet get = new HttpGet(TARGET_URL);
		    CloseableHttpResponse response = httpClient.execute(get);
		    HttpEntity responseEntity = response.getEntity();
		    String data = EntityUtils.toString(responseEntity);
		    return new Gson().fromJson(data, new TypeToken<List<DynamicStrategy>>(){}.getType());
		}
		catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
			return new ArrayList<>();
		}
	}

	public DynamicStrategy() {
		this.parameters = new ArrayList<>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public List<IOrderIntent> selectOrderIntent(IExchangeSession session, int count, List<String> ignoredCoins) {
		return null;
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

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
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
