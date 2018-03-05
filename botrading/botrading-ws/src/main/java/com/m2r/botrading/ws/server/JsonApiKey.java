package com.m2r.botrading.ws.server;

import com.m2r.botrading.api.model.IApiAccess;

public class JsonApiKey implements IApiAccess {

	private String apiKey;
	private String secretKey;
	
	@Override
	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	
	@Override
	public String getSecretKey() {
		return this.secretKey;
	}
	
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	
}
