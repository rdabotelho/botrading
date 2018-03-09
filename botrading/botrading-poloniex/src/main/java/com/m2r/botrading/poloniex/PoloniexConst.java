package com.m2r.botrading.poloniex;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PoloniexConst {
	
	private static final List<String> TOP_20 = Stream.of(new String[]{"ETC", "XRP", "XMR", "LTC", "STR", "ETC", "BCH", "NXT", "XEM", "DASH", "ZEC", "LSK", "STRAT", "STEEM", "STORJ", "REP"}).collect(Collectors.toList());

	public static List<String> TOP_20(final String marketCoin) {
		return TOP_20.stream().map(it -> marketCoin+"_"+it).collect(Collectors.toList());
	}
	
}
