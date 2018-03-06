package com.m2r.botrading.ws.server;

import com.m2r.botrading.api.model.IApiAccess;

public class JsonApiAccess implements IApiAccess {

	private String apiKey;
	private String secretKey;
	
	public JsonApiAccess() {
	}
	
	public JsonApiAccess(String apiKey, String secretKey) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
	}

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
