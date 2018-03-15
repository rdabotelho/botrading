package com.m2r.botrading.ws.exchange;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.util.JsonException;

import rx.Observable;
import rx.functions.Action1;

public class CallResult {

	private static int TIMEOUT = 30000;
	private Observable<String> observable;
	private boolean isCompleted;
	private String data;
	private Throwable exception;

	public static CallResult of(Observable<String> observable) {
		return new CallResult(observable);
	}

	private CallResult(Observable<String> observable) {
		this.observable = observable;
		this.isCompleted = false;
		this.run();
	}

	public void waitToResult() throws ExchangeException {
		int timeout = TIMEOUT;
		while (!isCompleted) {
			try {
				Thread.sleep(1000);
				timeout -= 1000;
				if (timeout <= 0) {
					throw new ExchangeException("Timeout in the procedure call");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void run() {
		observable.subscribe(new Action1<String>() {
			@Override
			public void call(String jsonData) {
				data = jsonData;
				isCompleted = true;
			}
		}, new Action1<Throwable>() {
			@Override
			public void call(Throwable t1) {
				exception = t1;
				isCompleted = true;
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private <T> T parseReturn(String data, Type typeOf) throws JsonException {
		Gson gson = new Gson();
		if (data.startsWith("{\"error\"")) {
			Map<String, Object> result = gson.fromJson(data, new TypeToken<Map<String, Object>>(){}.getType());
			throw new JsonException(result.get("error").toString());
		}
		return (T) gson.fromJson(data, typeOf);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getResult(Type typeOf) throws ExchangeException {
        if (exception != null) {
        	throw new ExchangeException(exception);
        }
        try {
			return (T) parseReturn(data, typeOf);
		} 
        catch (JsonException e) {
			throw new ExchangeException(e);
		}			
	}

}
