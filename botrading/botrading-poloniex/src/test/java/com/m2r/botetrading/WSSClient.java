package com.m2r.botetrading;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;
import ws.wamp.jawampa.transport.netty.NettyWampClientConnectorProvider;

public class WSSClient implements AutoCloseable {
	
	private final static Logger LOG = Logger.getLogger(WSSClient.class.getSimpleName());
	private final WampClient wampClient;

	public WSSClient(String uri, String realm) throws ApplicationError, Exception {
		WampClientBuilder builder = new WampClientBuilder();
		builder.withConnectorProvider(new NettyWampClientConnectorProvider())
			.withUri(uri)
			.withRealm(realm)
			.withInfiniteReconnects()
			.withReconnectInterval(5, TimeUnit.SECONDS);
		wampClient = builder.build();
	}

	public void run(long runTimeInMillis) {
		try {
			wampClient.statusChanged().subscribe((WampClient.State newState) -> {
				
				if (newState instanceof WampClient.ConnectedState) {
					LOG.info("Connected");
					wampClient.makeSubscription("trollbox").subscribe((s) -> { 
						System.out.println(s.arguments()); 
					});
					wampClient.makeSubscription("ticker").subscribe((s) -> { 
						System.out.println(s.arguments()); 
					});					
				} 
				else if (newState instanceof WampClient.DisconnectedState) {
					LOG.info("Disconnected");
				} 
				else if (newState instanceof WampClient.ConnectingState) {
					LOG.info("Connecting...");
				}
			});

			wampClient.open();
			long startTime = System.currentTimeMillis();

			while (wampClient.getTerminationFuture().isDone() == false && (startTime + runTimeInMillis > System.currentTimeMillis())) {
				TimeUnit.MINUTES.sleep(1);
			}
		} catch (Exception ex) {
			LOG.log(Level.SEVERE, "Caught exception - " + ex.getMessage(), ex);
		}
	}

	@Override
	public void close() throws Exception {
		wampClient.close().toBlocking().last();
	}
	
}