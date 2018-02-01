package com.m2r.botrading.ws.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;

import com.m2r.botrading.strategy.IIntention;
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
			IIntention intention = new Intention("", currencyPair, buyPrice, selePrice);
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
		private String uuidStrategy;
		private String currencyPair;
		private BigDecimal buyPrice;
		private BigDecimal selePrice;
		public Intention(String uuidStrategy, String currencyPair, String buyPrice, String selePrice) {
			this.uuidStrategy = uuidStrategy;
			this.currencyPair = currencyPair;
			this.buyPrice = new BigDecimal(buyPrice);
			this.selePrice = new BigDecimal(selePrice);
		}
		@Override
		public String getUuidStrategy() {
			return this.uuidStrategy;
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
