package com.m2r.botrading.tests.clplus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.util.CalcUtil;

public class Candle {
	
	private static final BigDecimal MULT_LENGTH = new BigDecimal("6");
	private String coin;
	private BigDecimal sum;
	private int total;
	private BigDecimal close;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private LocalDateTime date;
	private BigDecimal firstClose;

	public Candle(String coin) {
		this.coin = coin;
		this.sum = BigDecimal.ZERO;
		this.total = 0;
		this.firstClose = null;
	}

	public void update(IChartData data) {
		this.close = new BigDecimal(data.getClose());
		this.open = new BigDecimal(data.getOpen());
		this.low = new BigDecimal(data.getLow());
		this.high = new BigDecimal(data.getHigh());
		this.date = LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.valueOf(data.getDate())), ZoneId.systemDefault()).plusHours(3);
		this.sum = CalcUtil.add(sum, getLength());
		if (this.firstClose == null) {
			this.firstClose = this.close;
		}
		this.total++;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public BigDecimal getAverage() {
		return CalcUtil.divide(sum, new BigDecimal(total + ""));
	}

	public LocalDateTime getDate() {
		return date;
	}

	public BigDecimal getLengthToFilter() {
		return CalcUtil.multiply(getAverage(), MULT_LENGTH);
	}

	public BigDecimal getLength() {
		if (isRed()) {
			return CalcUtil.subtract(open, close);
		} else {
			return CalcUtil.subtract(close, open);
		}
	}

	public BigDecimal getOpen() {
		return open;
	}

	public BigDecimal getClose() {
		return close;
	}

	public BigDecimal getGain() {
		BigDecimal sellPrice = CalcUtil.add(close, getLength());
		return CalcUtil.subtract(CalcUtil.divide(CalcUtil.multiply(sellPrice, CalcUtil.HUNDRED), close),
				CalcUtil.HUNDRED);
	}

	public boolean isOpportunity() {
		return isRed() && CalcUtil.greaterThen(getLength(), getLengthToFilter());
	}

	public boolean isRed() {
		return CalcUtil.lessThen(close, open);
	}

	public BigDecimal getHigh() {
		return this.high;
	}

	public BigDecimal getLow() {
		return this.low;
	}
	
	public BigDecimal getAngle() {
		double angle = 90 - Math.toDegrees(Math.atan((this.close.doubleValue() / this.firstClose.doubleValue())*100));
		return BigDecimal.valueOf(angle);
	}
	
}
