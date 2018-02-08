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
import com.m2r.botrading.api.exception.ExchangeException;
import com.m2r.botrading.api.model.Currency;
import com.m2r.botrading.api.model.CurrencyFactory;
import com.m2r.botrading.api.model.CurrencyPairIds;
import com.m2r.botrading.api.model.IApiAccess;
import com.m2r.botrading.api.model.IBalance;
import com.m2r.botrading.api.model.IBalanceList;
import com.m2r.botrading.api.model.IChartData;
import com.m2r.botrading.api.model.IChartDataList;
import com.m2r.botrading.api.model.IDataChartPeriod;
import com.m2r.botrading.api.model.IExchangeOrder;
import com.m2r.botrading.api.model.IOrderList;
import com.m2r.botrading.api.model.ITicker;
import com.m2r.botrading.api.model.ITickerList;
import com.m2r.botrading.api.model.MarketCoin;
import com.m2r.botrading.api.service.ExchangeService;
import com.m2r.botrading.api.service.ExchangeSession;
import com.m2r.botrading.api.service.IExchangeService;
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
import com.m2r.botrading.poloniex.model.PoloniexCurrencyFactory;
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
	
	private static final BigDecimal FEE = new BigDecimal("0.15");
	private static final BigDecimal IMMEDIATE_FEE = new BigDecimal("0.25");
	
	@Override
	public String getId() {
		return EXCHANGE_ID;
	}
	
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
	
	private String execTradingAPI(IApiAccess apiAccess, String command, Map<String, String> parameters) throws Exception {
		
		parameters.put("nonce", generateNonce());
		
	    StringBuilder queryArgs = new StringBuilder();
	    queryArgs.append("command=").append(command);
	    parameters.forEach((k, v) -> {
	    		queryArgs.append("&").append(k).append("=").append(v);
	    });

	    Mac shaMac = Mac.getInstance("HmacSHA512");
	    SecretKeySpec keySpec = new SecretKeySpec(apiAccess.getSecretKey().getBytes(), "HmacSHA512");
	    shaMac.init(keySpec);
	    final byte[] macData = shaMac.doFinal(queryArgs.toString().getBytes());
	    String sign = Hex.encodeHexString(macData);

	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    
	    HttpPost post = new HttpPost(URL_TRADING_API);
	    post.addHeader("Key", apiAccess.getApiKey()); 
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
	public List<IChartData> commandChartDatas(String currencyPair, IDataChartPeriod period, Long dateStart, Long dateEnd) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("currencyPair", currencyPair);
		params.put("period", period.getSeconds());
		params.put("start", dateStart.toString());
		params.put("end", dateEnd.toString());
		String data = this.execPublicAPI(COMMAND_CHART_DATA, params);
	    return parseReturn(data, new TypeToken<List<ChartData>>(){}.getType());
	}
	
	public Map<String, Object> commandBalances(IApiAccess apiAccess) throws Exception {
		Map<String, String> params = new HashMap<>();
		String data = this.execTradingAPI(apiAccess, COMMAND_BALANCES, params);
	    return parseReturn(data, new TypeToken<Map<String, Object>>(){}.getType());
	}
	
	public Map<String, IBalance> commandCompleteBalances(IApiAccess apiAccess) throws Exception {
		Map<String, String> params = new HashMap<>();
		String data = this.execTradingAPI(apiAccess, COMMAND_COMPLETE_BALANCES, params);
	    return parseReturn(data, new TypeToken<Map<String, Balance>>(){}.getType());
	}
	
	public Map<String, List<IExchangeOrder>> commandOpenOrders(IApiAccess apiAccess) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("currencyPair", "all");
		String data = this.execTradingAPI(apiAccess, COMMAND_OPEN_ORDERS, params);
		return parseReturn(data, new TypeToken<Map<String, List<ExchangeOrder>>>(){}.getType());
	}
	
	public JsonSuccess commandCancelOrder(IApiAccess apiAccess, String orderNumber) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("orderNumber", orderNumber);
		String data = this.execTradingAPI(apiAccess, COMMAND_CANCEL_ORDER, params);
		return parseReturn(data, new TypeToken<JsonSuccess>(){}.getType());
	}
	
	public Map<String, Map<String, String>> commandAvailableAccountBalances(IApiAccess userAccount, String apiAccess) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("apiAccess", apiAccess);
		String data = this.execTradingAPI(userAccount, COMMAND_ACCOUNT_BALANCES, params);
	    return parseReturn(data, new TypeToken<Map<String, Map<String, String>>>(){}.getType());
	}
	
	public JsonSuccess commandSell(IApiAccess apiAccess, String currencyPair, String rate, String amount) throws Exception {
		Map<String, String> params = new HashMap<>();
		params.put("currencyPair", currencyPair);
		params.put("rate", rate);
		params.put("amount", amount);
		String data = this.execTradingAPI(apiAccess, COMMAND_SELL, params);
	    return parseReturn(data, new TypeToken<JsonSuccess>(){}.getType());
	}
	
	@Override
	public IExchangeSession getSession(MarketCoin marketCoin, boolean resetPublic, boolean resetPrivate) {
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
	protected IChartDataList getChartDatas(String currencyPair, IDataChartPeriod period, LocalDateTime start,
			LocalDateTime end, IExchangeSession session) throws ExchangeException {
		try {
	    		ZonedDateTime endDate = ZonedDateTime.of(end, EXCHANGE_ZONE_ID);
	    		ZonedDateTime startDate = ZonedDateTime.of(start, EXCHANGE_ZONE_ID);
	    		return ChartDataList.of(this.commandChartDatas(currencyPair, period, startDate.toInstant().getEpochSecond(), endDate.toInstant().getEpochSecond()));
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	protected IBalanceList getBanlances(IApiAccess apiAccess, IExchangeSession session) throws ExchangeException {
		try {
			Map<String, IBalance>  map = commandCompleteBalances(apiAccess);
			return BalanceList.of(map);
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	protected IOrderList getOrders(IApiAccess apiAccess, IExchangeSession session) throws ExchangeException {
		try {
			Map<String, List<IExchangeOrder>> orderMap = commandOpenOrders(apiAccess);
			return ExchangeOrderList.of(orderMap);
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}    
	
	@Override
	protected Map<String, MarketCoin> loadMarketCoins() {
		Map<String, MarketCoin> map = new HashMap<>();
		try {
			Map<String, ITicker> tikersMap = commandTicker();
			tikersMap.forEach((k, v) -> {
				CurrencyPairIds currencyPairIds = getCurrencyFactory().getCurrencyPairConverter().stringToCurrencyPair(k);
				MarketCoin marketCoin = map.get(currencyPairIds.getMarketCoinId());
				if (marketCoin == null) {
					marketCoin = getCurrencyFactory().currencyPairToMarketCoin(currencyPairIds);
					map.put(currencyPairIds.getMarketCoinId(), marketCoin);
				}
				marketCoin.addCurrency(getCurrencyFactory().currencyPairToCurrency(marketCoin, currencyPairIds));
			});
		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Cannot load market coins", e);
		} 
		return map;
	}
	
	@Override
	public MarketCoin getDefaultMarketCoin() {
		return getMarketCoin(Currency.BTC);
	}

	@Override
	public String buy(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException {
		try {
			Map<String, String> params = new HashMap<>();
			params.put("currencyPair", currencyPair);
			params.put("rate", price);
			params.put("amount", amount);
			String data = this.execTradingAPI(apiAccess, COMMAND_BUY, params);
			JsonSuccess result = parseReturn(data, new TypeToken<JsonSuccess>(){}.getType());
			return result.getOrderNumber();
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	public String sell(IApiAccess apiAccess, String currencyPair, String price, String amount) throws ExchangeException {
		try {
			Map<String, String> params = new HashMap<>();
			params.put("currencyPair", currencyPair);
			params.put("rate", price);
			params.put("amount", amount);
			String data = this.execTradingAPI(apiAccess, COMMAND_SELL, params);
			JsonSuccess result = parseReturn(data, new TypeToken<JsonSuccess>(){}.getType());
			return result.getOrderNumber();
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	public void cancel(IApiAccess apiAccess, String currencyPair, String orderNumber) throws ExchangeException {
		try {
			Map<String, String> params = new HashMap<>();
			params.put("orderNumber", orderNumber);
			this.execTradingAPI(apiAccess, COMMAND_CANCEL_ORDER, params);
		}
		catch (Exception e) {
			throw new ExchangeException(e);
		}
	}

	@Override
	public BigDecimal getFee() {
		return FEE;
	}

	@Override
	public BigDecimal getImmediateFee() {
		return IMMEDIATE_FEE;
	}
	
	@Override
	public CurrencyFactory getCurrencyFactory() {
		return PoloniexCurrencyFactory.getInstance();
	}
	
	public static void cancelAllOrdersInTheExchange(IApiAccess apiAccess) throws Exception {
		IExchangeService service = new PoloniexExchange().init();
		MarketCoin mc = service.getDefaultMarketCoin();
		IExchangeSession session = service.getSession(mc, true, true);
		BigDecimal amin = new BigDecimal("0.000001");
		IOrderList lords = session.getOrders(apiAccess);
		for (String coin : mc.getCurrencies().keySet()) {
			Currency currency = mc.getCurrency(coin);
			List<IExchangeOrder> list = lords.getOrders(currency.getCurrencyPair());
			for (IExchangeOrder order : list) {
				if (CalcUtil.greaterThen(order.getTotal(), amin)) {
					System.out.print(coin+"\t"+order.getTotal());
					String orderNumber = order.getOrderNumber();
					try {
						service.cancel(apiAccess, currency.getCurrencyPair(), orderNumber);
						System.out.print("\t CANCELED");
					}
					catch (ExchangeException ex) {
						System.out.print("\t ERRO: " + ex.getMessage());						
					}
					System.out.println();
				}
			}
		}
	}
	
	public static void sellAllInTheExchange(IApiAccess apiAccess) throws Exception {
		IExchangeService service = new PoloniexExchange().init();
		MarketCoin mc = service.getDefaultMarketCoin();
		IExchangeSession session = service.getSession(mc, true, true);
		BigDecimal amin = new BigDecimal("0.000001");
		IBalanceList blist = session.getBanlances(apiAccess);
		for (IBalance b : blist.getBalances()) {
			if (!b.getCoin().equals("BTC") && !b.getCoin().equals("USDT") && CalcUtil.greaterThen(b.getAvailable(), amin)) {
				System.out.print(b.getCoin()+"\t"+b.getBtcValue());
				String currencyPair = mc.getCurrency(b.getCoin()).getCurrencyPair();
				String price = CalcUtil.formatUS(session.getLastPrice(b.getCoin()));
				String amount = CalcUtil.formatUS(b.getAvailable());
				try {
					service.sell(apiAccess, currencyPair, price, amount);
					System.out.print("\t SOLD");
				}
				catch (ExchangeException ex) {
					System.out.print("\t ERRO: " + ex.getMessage());						
				}
				System.out.println();
			}
		}
	}
	
//	public static void main(String[] args) throws Exception {
//		IApiAccess apiAccess = new IApiAccess() {
//			public String getSecretKey() {
//				return "c034c84ba459f281e3c5ad43694f3f24d024316bb30bdb8a0071f38879b56424b976a5613da101ecf256ae8a43e100ad835d37040b46d607c4738402cfd828e0";
//			}
//			@Override
//			public String getApiKey() {
//				return "A6MSDX56-KPVBAZED-YJ53MWN6-GN8JWZ0X";
//			}
//		};
//		cancelAllOrdersInTheExchange(apiAccess);
//		sellAllInTheExchange(apiAccess);
//	}
	
}
