package com.m2r.botrading.ws.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import com.m2r.botrading.ws.IIntention;
import com.m2r.botrading.ws.IntentionServer;

public class ServerTest {

	private static IntentionServer server;
	
	public static void main(String[] args) {
		server = IntentionServer.build(8080).start();
		
		do {
			String currencyPair = realOfConsole("Market Coin: ");
			String buyPrice = realOfConsole("Buy Price: ");
			String selePrice = realOfConsole("Sele Price: ");
			if (currencyPair.equals("") || buyPrice.equals("") || selePrice.equals("")) {
				break;
			}
			IIntention intention = new Intention(currencyPair, buyPrice, selePrice);
			server.publish(intention);
			System.out.println("Intention sent!");
		}
		while (true);
		
		server.close();
	}
	
	private static String realOfConsole(String text){
		System.out.print(text);
		try {
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			return bufferRead.readLine();
		} catch (IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}  
	
	public static class Intention implements IIntention {
		private String currencyPair;
		private BigDecimal buyPrice;
		private BigDecimal selePrice;
		public Intention(String currencyPair, String buyPrice, String selePrice) {
			this.currencyPair = currencyPair;
			this.buyPrice = new BigDecimal(buyPrice);
			this.selePrice = new BigDecimal(selePrice);
		}
		public String getCurrencyPair() {
			return this.currencyPair;
		}
		public BigDecimal getBuyPrice() {
			return this.buyPrice;
		}
		public BigDecimal getSalePrice() {
			return this.selePrice;
		}
	}
}
