package com.m2r.botrading.admin.util;

import rx.functions.Action0;

public class TimeCounter {

	private Action0 taskMethod;
	private long start;
	private long count;
	private long timeout;

	public static TimeCounter of(long timeout, Action0 taskMethod) {
		return new TimeCounter(timeout, taskMethod);
	}
	
	public TimeCounter(long timeout, Action0 taskMethod) {
		this.timeout = timeout;
		this.taskMethod = taskMethod;
		this.start = 0;
		this.count = 0;
	}
	
	public void ifTimeoutExecute() {
		count = System.currentTimeMillis() - start;
		if (count > timeout) {
			this.taskMethod.call();
			start = System.currentTimeMillis();
		}
	}
	
}
