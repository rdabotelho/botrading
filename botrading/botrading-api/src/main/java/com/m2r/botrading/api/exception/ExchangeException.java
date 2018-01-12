package com.m2r.botrading.api.exception;

public class ExchangeException extends Exception {

	private static final long serialVersionUID = 1L;

	public ExchangeException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	public ExchangeException(String msg) {
		super(msg);
	}
	
	public ExchangeException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
	
}
