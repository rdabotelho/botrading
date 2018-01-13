package com.m2r.botrading.poloniex;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.m2r.botrading.api.enums.DataChartPeriod;
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.IAccount;
import com.m2r.botrading.api.model.IBalance;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.ICurrency;
import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.model.IOrder;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.MarketCoinDefault;
import com.m2r.botrading.api.service.ExchangeService;
import com.m2r.botrading.api.service.ExchangeSession;
import com.m2r.botrading.api.service.IExchangeSession;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.api.util.JsonException;
import com.m2r.botrading.api.util.JsonSuccess;
import com.m2r.botrading.poloniex.model.Balance;
import com.m2r.botrading.poloniex.model.BalanceList;
import com.m2r.botrading.poloniex.model.ChartData;
import com.m2r.botrading.poloniex.model.ChartDataList;
import com.m2r.botrading.poloniex.model.ExchangeOrder;
import com.m2r.botrading.poloniex.model.ExchangeOrderList;
import com.m2r.botrading.poloniex.model.Ticker;
import com.m2r.botrading.poloniex.model.TickerList;

public class PoloniexExchange extends ExchangeService {
	
    private final static Logger LOG = Logger.getLogger(PoloniexExchange.class.getSimpleName());
    
	public static final String EXCHANGE_ID = "POLONIEX";
	
	private static final ZoneId EXCHANGE_ZONE_ID = ZoneId.of("America/New_York");
	
	private static final String URL_PUBLIC_API = "https://poloniex.com/public";
	private static final String URL_TRADING_API = "https://poloniex.com/tradingApi";
	private static final String COMMAND_BALANCES = "returnBalances";
	private static final String COMMAND_COMPLETE_BALANCES = "returnCompleteBalances";
	private static final String COMMAND_CHART_DATA = "returnChartData";
	private static final String COMMAND_CURRENCIES = "returnCurrencies";
	private static final String COMMAND_TICKER = "returnTicker";
	private static final String COMMAND_OPEN_ORDERS = "returnOpenOrders";
	private static final String COMMAND_CANCEL_ORDER = "cancelOrder";
	private static final String COMMAND_ACCOUNT_BALANCES = "returnAvailableAccountBalances";
	private static final String COMMAND_BUY = "buy";
	private static final String COMMAND_SELL = "sell";
	
	private static final long H24 = 86400000;
	
	private String generateNonce() {
		try {
			Thread.sleep(1);
		} catch (InterruptedException e) {
			
		}
		return Long.toString(System.currentTimeMillis()+H24)+"000"; 
	}
	
	private String execPublicAPI(String command, Map<String, String> parameters) throws Exception {
		
	    StringBuilder queryArgs = new StringBuilder();
	    queryArgs.append(URL_PUBLIC_API).append("?command=").append(command);
	    parameters.forEach((k, v) -> {
	    		queryArgs.append("&").append(k).append("=").append(v);
	    });
	    
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    
	    HttpGet get = new HttpGet(queryArgs.toString());
	    CloseableHttpResponse response = httpClient.execute(get);
	    HttpEntity responseEntity = response.getEntity();
	    return EntityUtils.toString(responseEntity);
	}
	
	private String execTradingAPI(IAccount account, String command, Map<String, String> parameters) throws Exception {
		
		parameters.put("nonce", generateNonce());
		
	    StringBuilder queryArgs = new StringBuilder();
	    queryArgs.append("command=").append(command);
	    parameters.forEach((k, v) -> {
	    		queryArgs.append("&").append(k).append("=").append(v);
	    });

	    Mac shaMac = Mac.getInstance("HmacSHA512");
	    SecretKeySpec keySpec = new SecretKeySpec(account.getSecretKey().getBytes(), "HmacSHA512");
	    shaMac.init(keySpec);
	    final byte[] macData = shaMac.doFinal(queryArgs.toString().getBytes());
	    String sign = Hex.encodeHexString(macData);

	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    
	    HttpPost post = new HttpPost(URL_TRADING_API);
	    post.addHeader("Key", account.getApiKey()); 
	    post.addHeader("Sign", sign);

	    List<NameValuePair> params = new ArrayList<NameValuePair>();
	    params.add(new BasicNameValuePair("command", command));
	    parameters.forEach((k, v) -> {
		    params.add(new BasicNameValuePair(k, v));
	    });
	    post.setEntity(new UrlEncodedFormEntity(params));

	    CloseableHttpResponse response = httpClient.execute(post);
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
	
	public Map<String, ITicker> commandTicker() throws Exception {
		Map<String, String> params = new HashMap<>();
		String data = this.execPublicAPI(COMMAND_TICKER, params);
	    return parseReturn(data, new TypeToken<Map<String, Ticker>>(){}.getType());
	}
	
	public Map<String, Object> commandCurrencies() throws Exception {
		Map<String, String> params = new HashMap<>();
		String data = this.execPublicAPI(COMMAND_CURRENCIES, params);
	    return parseReturn(data, new TypeToken<Map<String, Object>>(){}.getType());
	}

	/*
	 * Period (seconds) = [300, 900, 1800, 7200, 14400, 86400]
	 * Start and End = Timestamp Unix
	 */
	public List<IChartData> commandChartDatas(String currencyPair, DataChartPeriod period, Long dateStart, Long dateEnd) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("currencyPair", currencyPair);
		params.put("period", period.getSeconds().toString());
		params.put("start", dateStart.toString());
		params.put("end", dateEnd.toString());
		String data = this.execPublicAPI(COMMAND_CHART_DATA, params);
	    return parseReturn(data, new TypeToken<List<ChartData>>(){}.getType());
	}
	
	public Map<String, Object> commandBalances(IAccount account) throws Exception {
		Map<String, String> params = new HashMap<>();
		String data = this.execTradingAPI(account, COMMAND_BALANCES, params);
	    return parseReturn(data, new TypeToken<Map<String, Object>>(){}.getType());
	}
	
	public Map<String, IBalance> commandCompleteBalances(IAccount account) throws Exception {
		Map<String, String> params = new HashMap<>();
		String data = this.execTradingAPI(account, COMMAND_COMPLETE_BALANCES, params);
	    return parseReturn(data, new TypeToken<Map<String, Balance>>(){}.getType());
	}
	
	public Map<String, List<IExchangeOrder>> commandOpenOrders(IAccount account) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("currencyPair", "all");
		String data = this.execTradingAPI(account, COMMAND_OPEN_ORDERS, params);
		return parseReturn(data, new TypeToken<Map<String, List<ExchangeOrder>>>(){}.getType());
	}
	
	public JsonSuccess commandCancelOrder(IAccount account, String orderNumber) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("orderNumber", orderNumber);
		String data = this.execTradingAPI(account, COMMAND_CANCEL_ORDER, params);
		return parseReturn(data, new TypeToken<JsonSuccess>(){}.getType());
	}
	
	public Map<String, Map<String, String>> commandAvailableAccountBalances(IAccount userAccount, String account) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("account", account);
		String data = this.execTradingAPI(userAccount, COMMAND_ACCOUNT_BALANCES, params);
	    return parseReturn(data, new TypeToken<Map<String, Map<String, String>>>(){}.getType());
	}
	
	public JsonSuccess commandBuy(IAccount account, String currencyPair, String rate, String amount) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("currencyPair", currencyPair);
		params.put("rate", rate);
		params.put("amount", amount);
		String data = this.execTradingAPI(account, COMMAND_BUY, params);
	    return parseReturn(data, new TypeToken<JsonSuccess>(){}.getType());
	}
	
	public JsonSuccess commandSell(IAccount account, String currencyPair, String rate, String amount) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("currencyPair", currencyPair);
		params.put("rate", rate);
		params.put("amount", amount);
		String data = this.execTradingAPI(account, COMMAND_SELL, params);
	    return parseReturn(data, new TypeToken<JsonSuccess>(){}.getType());
	}
	
	@Override
	public IExchangeSession getSession(IMarketCoin marketCoin, boolean resetPublic, boolean resetPrivate) {
		IExchangeSession session = ExchangeSession.createSession(this, marketCoin);
		if (resetPublic) {
			session.resetPublicCache();
		}
		if (resetPrivate) {
			session.resetPrivateCache();
		}
		return session;
	}
	
	@Override
	protected ITickerList getTikers(IExchangeSession session) throws ExchangeException {
		try {
			Map<String, ITicker> tikersMap = commandTicker();
			return TickerList.of(tikersMap);
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	protected IChartDataList getChartDatas(String currencyPair, DataChartPeriod period, LocalDateTime start,
			LocalDateTime end, IExchangeSession session) throws ExchangeException {
		try {
	    		ZonedDateTime endDate = ZonedDateTime.of(end, EXCHANGE_ZONE_ID);
	    		ZonedDateTime startDate = ZonedDateTime.of(start, EXCHANGE_ZONE_ID);
	    		return ChartDataList.of(this.commandChartDatas(currencyPair, DataChartPeriod.FIVE_MINUTES, startDate.toInstant().getEpochSecond(), endDate.toInstant().getEpochSecond()));
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	protected IBalanceList getBanlances(IAccount account, IExchangeSession session) throws ExchangeException {
		try {
			Map<String, IBalance>  map = commandCompleteBalances(account);
			return BalanceList.of(map);
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	protected IOrderList getOrders(IAccount account, IExchangeSession session) throws ExchangeException {
		try {
			Map<String, List<IExchangeOrder>> orderMap = commandOpenOrders(account);
			return ExchangeOrderList.of(orderMap);
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}    
	
	@Override
	protected String buy(IOrder order, IExchangeSession session) throws ExchangeException {
		try {
			String currencyPair = session.getCurrencyOfTrader(order.getTrader()).getCurrencyPair();
			String rate = CalcUtil.formatUS(order.getPrice());
			String amount = CalcUtil.formatUS(order.getAmount());
			JsonSuccess result = this.commandBuy(order.getTrader().getTraderJob().getAccount(), currencyPair, rate, amount);
			return result.getOrderNumber();
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}
	
	@Override
	protected String sell(IOrder order, IExchangeSession session) throws ExchangeException {
		try {
			String currencyPair = session.getCurrencyOfTrader(order.getTrader()).getCurrencyPair();
			String rate = CalcUtil.formatUS(order.getPrice());
			String amount = CalcUtil.formatUS(order.getAmount());
			JsonSuccess result = this.commandSell(order.getTrader().getTraderJob().getAccount(), currencyPair, rate, amount);
			return result.getOrderNumber();
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}
	
	@Override
	protected void cancel(IOrder order, IExchangeSession session) throws ExchangeException {
		try {
			this.commandCancelOrder(order.getTrader().getTraderJob().getAccount(), order.getOrderNumber());
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}
	
	@Override
	protected String immediateSell(IOrder order, IExchangeSession session) throws ExchangeException {
		try {
			cancel(order, session);
			Thread.sleep(3000);
			BigDecimal newAmount = session.calculateSellToAmount(order.getTrader(), order.getAmount());
			order.setAmount(newAmount);
			order.setPrice(session.getLastPrice(order.getTrader().getCoin()));
			return sell(order, session);
		} 
		catch (ExchangeException e1) {
			throw e1;
		}
		catch (Exception e2) {
			throw new ExchangeException(e2);
		}
	}
	
	@Override
	protected Map<String, IMarketCoin> loadMarketCoins() {
		Map<String, IMarketCoin> map = new HashMap<>();
		try {
			Map<String, ITicker> tikersMap = commandTicker();
			tikersMap.forEach((k, v) -> {
				String marketCoinId = MarketCoinDefault.currencyPairToMarketCoinId(k);
				IMarketCoin marketCoin = map.get(marketCoinId);
				if (marketCoin == null) {
					marketCoin = MarketCoinDefault.of(marketCoinId);
					map.put(marketCoinId, marketCoin);
				}
				String currencyId = MarketCoinDefault.currencyPairToCurrencyId(k);
				marketCoin.createAndAddCurrency(currencyId, currencyId);
			});
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Cannot load market coins", e);
		} 
		return map;
	}
	
	@Override
	public IMarketCoin getDefaultMarketCoin() {
		return getMarkeyCoin(ICurrency.BTC);
	}
	
}
