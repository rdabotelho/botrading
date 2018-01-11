package com.m2r.botrading.admin.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.m2r.botrading.api.model.IAccount;
import com.m2r.botrading.api.model.IMarketCoin;

@Entity
public class Account implements Serializable, IAccount {
 
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
 
    @NotNull
	private String name;

    @NotNull
    private String apiKey;
    
    @NotNull
    private String secretKey;
    
    @Transient
    private IMarketCoin selectedMarketCoin;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public void setSelectedMarketCoin(IMarketCoin selectedMarketCoin) {
		this.selectedMarketCoin = selectedMarketCoin;
	}
	
	public IMarketCoin getSelectedMarketCoin() {
		return selectedMarketCoin;
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).
			       append("id", id).
			       append("name", name).
			       append("apiKey", apiKey).
			       append("secretKey", secretKey).
			       toString();
	}

}