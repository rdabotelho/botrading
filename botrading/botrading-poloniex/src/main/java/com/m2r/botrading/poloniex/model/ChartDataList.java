package com.m2r.botrading.poloniex.model;

import java.util.List;
import java.util.stream.Collectors;

import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;

public class ChartDataList<T extends IChartData> implements IChartDataList {

	private List<IChartData> chartDatas;
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T extends IChartData> IChartDataList of(List<T> chartDatas) {
		return new ChartDataList(chartDatas);
	}
	
	public ChartDataList(List<IChartData> chartDatas) {
		this.chartDatas = chartDatas.stream().map(i -> {
			return i;
		})
		.collect(Collectors.toList());
	}
	
	@Override
	public List<IChartData> getChartDatas() {
		return this.chartDatas;
	}
	
}
