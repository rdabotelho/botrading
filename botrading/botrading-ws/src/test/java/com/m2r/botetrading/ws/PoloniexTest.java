package com.m2r.botetrading.ws;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.connection.IWampConnectorProvider;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class PoloniexTest {

	static Subscription eventSubscription;

	public static void main(String[] args) {

		IWampConnectorProvider connectorProvider = new NettyWampClientConnectorProvider();
		WampClientBuilder builder = new WampClientBuilder();

		final WampClient client;
		try {
			builder.withConnectorProvider(connectorProvider).withUri("wss://api.poloniex.com").withRealm("realm1")
					.withInfiniteReconnects().withReconnectInterval(3, TimeUnit.SECONDS);
			client = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		client.statusChanged().subscribe(new Action1<WampClient.State>() {
			@Override
			public void call(WampClient.State t1) {
				System.out.println("Sessionstatus changed to " + t1);
				if (t1 instanceof WampClient.ConnectedState) {
                    eventSubscription = client.makeSubscription("ticker", String.class).subscribe((data)->{
                    		System.out.println(data);
                    });
                    eventSubscription = client.makeSubscription("BTC_ETC", String.class).subscribe((data)->{
                			System.out.println(data);
                    });
				}
			}
		}, new Action1<Throwable>() {
			@Override
			public void call(Throwable t) {
				System.out.println("Sessionended with error " + t);
			}
		}, new Action0() {
			@Override
			public void call() {
				System.out.println("Sessionended normally");
			}
		});
		
		client.open();

		waitUntilKeypressed();
		if (eventSubscription != null) {
			eventSubscription.unsubscribe();
		}		
		System.out.println("Closing the client 1");
		client.close().toBlocking().last();

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
