package com.m2r.botrading.tests.rsi;

import java.util.ArrayList;
import java.util.List;

import org.ta4j.core.Bar;
import org.ta4j.core.TimeSeries;

import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;

public class MyTimeSeries implements TimeSeries {

	private static final long serialVersionUID = 1L;
	
	private Currency currency;
	private List<Bar> bars;
	
	public MyTimeSeries(Currency currency, IChartDataList chartDataList) {
		this.currency = currency;
		this.bars = new ArrayList<>();
		for (IChartData cd : chartDataList.getChartDatas()) {
			bars.add(new MyBar(cd));
		}
	}

	@Override
	public String getName() {
		return currency.getCurrencyPair();
	}

	@Override
	public Bar getBar(int i) {
		return bars.get(i);
	}

	@Override
	public int getBarCount() {
		return bars.size();
	}

	@Override
	public List<Bar> getBarData() {
		return bars;
	}

	@Override
	public int getBeginIndex() {
		return 0;
	}

	@Override
	public int getEndIndex() {
		return bars.size()-1;
	}

	@Override
	public void setMaximumBarCount(int maximumBarCount) {
	}

	@Override
	public int getMaximumBarCount() {
		return bars.size();
	}

	@Override
	public int getRemovedBarsCount() {
		return 0;
	}

	@Override
	public void addBar(Bar bar) {
		this.bars.add(bar);
	}

	@Override
	public TimeSeries getSubSeries(int startIndex, int endIndex) {
		return null;
	}

}
