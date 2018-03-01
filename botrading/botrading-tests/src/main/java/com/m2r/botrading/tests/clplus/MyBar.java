package com.m2r.botrading.tests.clplus;

import java.time.Duration;
import java.time.ZonedDateTime;

import org.ta4j.core.Bar;
import org.ta4j.core.Decimal;

import com.m2r.botrading.api.model.IChartData;

public class MyBar implements Bar {

	private static final long serialVersionUID = 1L;

	private Decimal openPrice;
	private Decimal minPrice;
	private Decimal maxPrice;
	private Decimal closePrice;
	private Decimal volume;
	
	public MyBar(IChartData charData) {
		this.openPrice = Decimal.valueOf(charData.getOpen());
		this.minPrice = Decimal.valueOf(charData.getLow());
		this.maxPrice = Decimal.valueOf(charData.getHigh());
		this.closePrice = Decimal.valueOf(charData.getClose());
		this.volume = Decimal.valueOf(charData.getVolume());
	}

	@Override
	public Decimal getOpenPrice() {
		return openPrice;
	}

	@Override
	public Decimal getMinPrice() {
		return minPrice;
	}

	@Override
	public Decimal getMaxPrice() {
		return maxPrice;
	}

	@Override
	public Decimal getClosePrice() {
		return closePrice;
	}

	@Override
	public Decimal getVolume() {
		return volume;
	}

	@Override
	public int getTrades() {
		return 0;
	}

	@Override
	public Decimal getAmount() {
		return null;
	}

	@Override
	public Duration getTimePeriod() {
		return null;
	}

	@Override
	public ZonedDateTime getBeginTime() {
		return null;
	}

	@Override
	public ZonedDateTime getEndTime() {
		return null;
	}

	@Override
	public void addTrade(Decimal tradeVolume, Decimal tradePrice) {
	}

}
