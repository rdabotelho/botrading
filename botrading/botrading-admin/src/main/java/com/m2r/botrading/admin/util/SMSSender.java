package com.m2r.botrading.admin.util;

import java.math.BigDecimal;

import com.m2r.botrading.api.util.CalcUtil;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SMSSender {

	public static final String ACCOUNT_SID = "AC83b075be0f3c07fbd1e36c542f25890c";
	public static final String AUTH_TOKEN = "3761ca9a42a07724ea83e3b6fc1d24f3";
	public static final String TWILIO_NUMBER = "+12242040794";

	public static void sendSMS(String toNumber, String msg) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message.creator(new PhoneNumber(toNumber), new PhoneNumber(TWILIO_NUMBER), msg).create();
	}
	
	public static void sendLiquidedSellSMS(String toNumber, String strategyName, String coinId, BigDecimal profit, BigDecimal percent) {
		String profitStr = CalcUtil.formatBR(profit);
		String percentStr = CalcUtil.formatPercent(percent);
		sendSMS(toNumber, "SELL ORDER LIQUIDED IN THE BOTRADING.\nSTRATEGY: "+strategyName+"\nCOIN: "+coinId+"\nPROFIT: "+profitStr+" ("+percentStr+"%)");
	}
	
	public static void main(String[] args) {
//		sendLiquidedSellSMS("+5591991534801", "EXPONENTIAL MOVING AVERAGE", "DOGE", new BigDecimal("0.00234509"), new BigDecimal("0.35"));
		sendLiquidedSellSMS("+5591984983939", "EXPONENTIAL MOVING AVERAGE", "DOGE", new BigDecimal("0.00234509"), new BigDecimal("0.35"));
	}

}
