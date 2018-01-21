package com.m2r.botrading.admin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import com.m2r.botrading.api.util.IConfigProperties;

@Service("propertiesService")
@ConfigurationProperties
public class PropertiesService implements IConfigProperties {

	public String profile;
	private Integer httpPort;
	private Integer httpsPort;
	private Boolean enableScheduling;
	
	@Value("bollinger.targetUrl")
	private String bollingerTargetUrl;

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public Integer getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(Integer httpPort) {
		this.httpPort = httpPort;
	}

	public Integer getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(Integer httpsPort) {
		this.httpsPort = httpsPort;
	}

	public Boolean getEnableScheduling() {
		return enableScheduling;
	}

	public void setEnableScheduling(Boolean enableScheduling) {
		this.enableScheduling = enableScheduling;
	}

	@Override
	public String getProperty(String name) {
		return null;
	}
	
}
