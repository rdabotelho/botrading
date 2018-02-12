package com.m2r.botrading.admin.service;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.admin.model.TickerMB;
import com.m2r.botrading.admin.model.TickerMBList;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.util.JsonException;

@Service("mercadoBitcoinService")
@Transactional
public class MercadoBitcoinExchangeServiceImpl implements IRealExchangeService {

	private static final String URL_PUBLIC_API = "https://www.mercadobitcoin.net/api/BTC";
	private static final String COMMAND_TICKER = "ticker";

	private String execPublicAPI(String command) throws Exception {
		
	    String queryArgs = URL_PUBLIC_API + "/" + command;
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    HttpGet get = new HttpGet(queryArgs);
	    CloseableHttpResponse response = httpClient.execute(get);
	    HttpEntity responseEntity = response.getEntity();
	    return EntityUtils.toString(responseEntity);
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

	public Map<String, TickerMB> commandTicker() throws Exception {
		String data = this.execPublicAPI(COMMAND_TICKER);
		try {
			return parseReturn(data, new TypeToken<Map<String, TickerMB>>(){}.getType());
		}
		catch (Exception e) {
			return new HashMap<>();
		}
	}

	@Override
	public ITickerList getTikersWithoutCache() throws Exception {
		Map<String, TickerMB> tikersMap = commandTicker();
		return TickerMBList.of(tikersMap);
	}

}
