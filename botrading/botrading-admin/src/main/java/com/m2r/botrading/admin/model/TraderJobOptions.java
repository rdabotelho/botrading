package com.m2r.botrading.admin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2r.botrading.api.model.ITraderJobOptions;

@Entity
public class TraderJobOptions implements Serializable, ITraderJobOptions {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 2048)
	private String coins = "[]";

	private BigDecimal minimimPrice = new BigDecimal("0.00000001");

	private BigDecimal minimumVolume = new BigDecimal("150.0");

	@Transient
	private List<SelCoin> selCoins;

	public TraderJobOptions() {
		this.selCoins = new ArrayList<>();
	}

	public String getCoins() {
		return coins;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setCoins(String coins) {
		this.coins = coins;
	}

	public BigDecimal getMinimimPrice() {
		return minimimPrice;
	}

	public void setMinimimPrice(BigDecimal minimimPrice) {
		this.minimimPrice = minimimPrice;
	}

	public BigDecimal getMinimumVolume() {
		return minimumVolume;
	}

	public void setMinimumVolume(BigDecimal minimumVolume) {
		this.minimumVolume = minimumVolume;
	}

	public List<SelCoin> getSelCoins() {
		return selCoins;
	}

	public void setSelCoins(List<SelCoin> selCoins) {
		this.selCoins = selCoins;
	}

	public String[] getArrayCoins() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(getCoins(), String[].class);
		} catch (Exception e) {
			return new String[] {};
		}
	}

}
