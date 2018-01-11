package com.m2r.botrading.poloniex.model;

import com.m2r.botrading.api.model.IChartData;

public class ChartData implements IChartData {

	private String date;
	private String high;
	private String low;
	private String open;
	private String close;
	private String volume;
	private String quoteVolume;
	private String weightedAverage;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getHigh() {
		return high;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public String getLow() {
		return low;
	}

	public void setLow(String getLow) {
		this.low = getLow;
	}

	public String getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public String getClose() {
		return close;
	}

	public void setClose(String close) {
		this.close = close;
	}

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getQuoteVolume() {
		return quoteVolume;
	}

	public void setQuoteVolume(String quoteVolume) {
		this.quoteVolume = quoteVolume;
	}

	public String getWeightedAverage() {
		return weightedAverage;
	}

	public void setWeightedAverage(String weightedAverage) {
		this.weightedAverage = weightedAverage;
	}

}
