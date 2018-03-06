package com.m2r.botrading.ws.example;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.ws.server.ExchangeWSClient;

public class ExchangeClientTest {

	private static final String URL = "ws://localhost:8080/ws1";

	private static ExchangeWSClient client;

	public static void main(String[] args) throws Exception {
		IApiAccess apiAccess = new IApiAccess() {
			public String getSecretKey() {
				return "c034c84ba459f281e3c5ad43694f3f24d024316bb30bdb8a0071f38879b56424b976a5613da101ecf256ae8a43e100ad835d37040b46d607c4738402cfd828e0";
			}
			@Override
			public String getApiKey() {
				return "A6MSDX56-KPVBAZED-YJ53MWN6-GN8JWZ0X";
			}
		};
		
		client = ExchangeWSClient.build(URL, 
				it -> {
					//System.out.println(it);
				}, 
				it -> {
					System.out.println("NOTIFICATION OF LIQUIDATION: " + it);
				}, 
				it -> {
					System.out.println("NOTIFICATION OF CANCELING: " + it);
				}
			)
			.start();

		while (!client.isConnected()) {
			Thread.sleep(1000);
		}
		
		test();
		
		waitUntilKeypressed();
		client.close();
	}
	
	private static void test() throws Exception {
		Set<String> on = new HashSet<>();
		on.add("64532048444");
		on.add("59701378050");
		on.add("190962194823");
		//client.init(on);
		String id = client.sell("BTC_LTC", "0.01834100", "0.00818522");
		
		Thread.sleep(5*60000);
		
		client.cancel("BTC_LTC", id);
		
		System.out.println("fim");
	}

	private static void waitUntilKeypressed() {
		try {
			System.in.read();
			while (System.in.available() > 0) {
				System.in.read();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
