package com.m2r.botrading.api.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CalcUtil {
	
	public static int SCALE_COIN = 8;
	
	public static int SCALE_PERCENT = 2;
	
	public static final MathContext DECIMAL_COIN = new MathContext(SCALE_COIN, RoundingMode.HALF_UP);
	
	public static final MathContext DECIMAL_PERCENT = new MathContext(SCALE_PERCENT, RoundingMode.HALF_UP);
	
	public static final BigDecimal HUNDRED = new BigDecimal("100.0");

	private static final DecimalFormat DECIMAL_FORMAT_PERCENT = new DecimalFormat("0.00");
	
	private static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00000000");

	private static DecimalFormat DECIMAL_REAL_FORMAT = new DecimalFormat("#,##0.00");
	
	public static boolean valueEqualZero(BigDecimal value) {
		return value.equals(BigDecimal.ZERO);		
	}
	
	public static boolean isNotZeroPercent(BigDecimal value) {
		if (value == null) {
			return false;
		}
		return !BigDecimal.ZERO.setScale(SCALE_PERCENT).equals(value.setScale(SCALE_PERCENT, RoundingMode.FLOOR));
	}

	public static String formatUS(BigDecimal value) {
		return DECIMAL_FORMAT.format(value).replace(",", ".");
	}

	public static String formatBR(BigDecimal value) {
		return DECIMAL_FORMAT.format(value);
	}

	public static String formatReal(BigDecimal value) {
		return DECIMAL_REAL_FORMAT.format(value);
	}

	public static String formatPercent(BigDecimal value) {
		return DECIMAL_FORMAT_PERCENT.format(value);
	}
	
	public static BigDecimal divide(BigDecimal a, BigDecimal b) {
		return a.divide(b, DECIMAL_COIN);
	}
	
	public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
		return a.multiply(b, DECIMAL_COIN);
	}
	
	public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
		return a.subtract(b, DECIMAL_COIN);
	}
	
	public static BigDecimal add(BigDecimal a, BigDecimal b) {
		return a.add(b, DECIMAL_COIN);
	}
	
	public static BigDecimal percent(BigDecimal value, BigDecimal fee) {
		return value.multiply(fee.divide(CalcUtil.HUNDRED, DECIMAL_COIN), DECIMAL_COIN);
	}
	
	public static boolean lessThen(BigDecimal a, BigDecimal b) {
		return a.setScale(SCALE_COIN, RoundingMode.HALF_UP).compareTo(b.setScale(SCALE_COIN, RoundingMode.HALF_UP)) < 0;
	}
	
	public static boolean equalThen(BigDecimal a, BigDecimal b) {
		return a.setScale(SCALE_COIN, RoundingMode.HALF_UP).compareTo(b.setScale(SCALE_COIN, RoundingMode.HALF_UP)) == 0;
	}
	
	public static boolean greaterThen(BigDecimal a, BigDecimal b) {
		return a.setScale(SCALE_COIN, RoundingMode.HALF_UP).compareTo(b.setScale(SCALE_COIN, RoundingMode.HALF_UP)) > 0;
	}
	
	public static boolean isZero(BigDecimal value) {
		return BigDecimal.ZERO.setScale(SCALE_COIN).equals(value.setScale(SCALE_COIN, RoundingMode.FLOOR));
	}
	
	public static BigDecimal toCoinScale(BigDecimal value) {
		return value.setScale(SCALE_COIN, RoundingMode.HALF_UP);
	}
	
}
