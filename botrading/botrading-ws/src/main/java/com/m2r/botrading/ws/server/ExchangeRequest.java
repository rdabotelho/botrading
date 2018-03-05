package com.m2r.botrading.ws.server;

import ws.wamp.jawampa.Request;

public class ExchangeRequest implements Comparable<ExchangeRequest> {

	private Long id;
	private ExchangeTopicEnum topic;
	private Request request;

	public ExchangeRequest(Long id, ExchangeTopicEnum topic, Request request) {
		this.id = id;
		this.topic = topic;
		this.request = request;
	}

	public ExchangeTopicEnum getTopic() {
		return topic;
	}

	public void setTopic(ExchangeTopicEnum topic) {
		this.topic = topic;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
	
	private String idToCampare() {
		return String.format("%02d%015d", topic.getPriority(), id);
	}

	@Override
	public int compareTo(ExchangeRequest other) {
		return other.idToCampare().compareTo(this.idToCampare());
	}
	
}
