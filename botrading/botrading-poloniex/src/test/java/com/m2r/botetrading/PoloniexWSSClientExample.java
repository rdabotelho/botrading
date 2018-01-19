package com.m2r.botetrading;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PoloniexWSSClientExample {
	
	private final static Logger LOG = Logger.getLogger(PoloniexWSSClientExample.class.getSimpleName());
	private static final String ENDPOINT_URL = "wss://api.poloniex.com";
	private static final String DEFAULT_REALM = "realm1";

	public static void main(String[] args) {
		try {
			new PoloniexWSSClientExample().run();
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "An exception occurred when running PoloniexWSSClientExample - {}", ex.getMessage());
			System.exit(-1);
		}
	}

	public void run() throws Exception {
		try (WSSClient poloniexWSSClient = new WSSClient(ENDPOINT_URL, DEFAULT_REALM)) {
			poloniexWSSClient.run(120000);
		}
	}
	
}