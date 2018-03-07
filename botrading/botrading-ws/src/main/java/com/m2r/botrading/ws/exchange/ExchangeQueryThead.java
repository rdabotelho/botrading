package com.m2r.botrading.ws.exchange;

import rx.functions.Action0;

public class ExchangeQueryThead extends Thread {

	private Action0 action;
	private long timeout;
	private boolean isClose;

	public static ExchangeQueryThead build() {
		return new ExchangeQueryThead();
	}
	
	private ExchangeQueryThead() {
		this.timeout = 1000;
		this.isClose = false;
	}

	public ExchangeQueryThead withAction(Action0 action) {
		this.action = action;
		return this;
	}

	public ExchangeQueryThead withTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}
	
	public ExchangeQueryThead execute() {
		this.start();
		return this;
	}
	
	@Override
	public void run() {
		if (action == null) {
			return;
		}
		while (!isClose) {
			action.call();
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void close() {
		this.isClose = true;
	}
	
	public boolean isClose() {
		return this.isClose();
	}
	
}
