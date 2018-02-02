package com.m2r.botrading.ws.example;

import java.io.IOException;

import com.m2r.botrading.ws.IntentionClient;

public class ClientTest {
	
	//private static final String URL = "ws://localhost:8080/ws1";
	private static final String URL = "ws://bt1.yuhull.com:8080/ws1";

	private static IntentionClient client;

	public static void main(String[] args) {
		client = IntentionClient.build(URL, intention -> {
			System.out.println(String.format("Received an order intention: [Coin: %s, buyPrice: %s, salePrice: %s]", intention.getCurrencyPair(), intention.getBuyPrice().toString(), intention.getSalePrice().toString()));
		})
		.start();

		waitUntilKeypressed();
		client.close();
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
