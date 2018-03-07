package com.m2r.botrading.ws.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.poloniex.PoloniexExchange;
import com.m2r.botrading.ws.exchange.ExchangeWSServer;

public class ExchangeServerTest {
	
	private static ExchangeWSServer server;
	
	public static void main(String[] args) {
		IApiAccess apiAccess = new IApiAccess() {
			public String getSecretKey() {
				return "c034c84ba459f281e3c5ad43694f3f24d024316bb30bdb8a0071f38879b56424b976a5613da101ecf256ae8a43e100ad835d37040b46d607c4738402cfd828e0";
			}
			@Override
			public String getApiKey() {
				return "A6MSDX56-KPVBAZED-YJ53MWN6-GN8JWZ0X";
			}
		};
		
		PoloniexExchange service = new PoloniexExchange();
		service.init();
		
		server = ExchangeWSServer.build(service, 8080, apiAccess, "ws1").start();
		
		do {
			String currencyPair = realOfConsole("Enter to out: ");
			if (currencyPair.equals("")) {
				break;
			}
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

}
