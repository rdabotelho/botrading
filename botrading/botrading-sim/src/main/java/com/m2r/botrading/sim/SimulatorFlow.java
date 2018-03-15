package com.m2r.botrading.sim;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorAmount;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorBuild;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorCandleSizeFactor;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorCoin;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorDelayToBuy;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorMarketCoin;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorSellFactor;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorStopLoss;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorTSSL;
import com.m2r.botrading.sim.SimulatorBuilder.ISimulatorWithPeriod;
import com.m2r.botrading.sim.SimulatorBuilder.Simulator;
import com.m2r.botrading.sim.clplus.CachedExchangeService;
import com.m2r.botrading.sim.clplus.Candle;
import com.m2r.botrading.sim.clplus.UltimateAnalyze;

public class SimulatorFlow implements ISimulatorMarketCoin, ISimulatorCoin, ISimulatorWithPeriod, ISimulatorAmount, ISimulatorStopLoss, ISimulatorCandleSizeFactor, ISimulatorSellFactor, ISimulatorTSSL, ISimulatorDelayToBuy, ISimulatorBuild, Simulator {

	private static final int TIME_DIFERENCE = 3;
	private static int COUNT_PERIODS = 30;
	
	private IExchangeService service;
	private String marketCoin;
	private String coin; 
	private LocalDateTime to; 
	private LocalDateTime from;

	private OrderIntent order;
	private BigDecimal amount;
	private BigDecimal candleSizeFactor;
	private BigDecimal totalFee;
	private boolean stopLossOfCandleLength;
	private BigDecimal stopLoss;
	private BigDecimal balance;
	private int total;
	private BigDecimal sellFactor = new BigDecimal("1.0");
	private BigDecimal tsslPercent;
	private BigDecimal delayToBuyPercent;
	
	private BigDecimal priceToBuy;
	private boolean waitToOut = false;
	private boolean withLog;
	
	public ISimulatorMarketCoin withService(IExchangeService service) {
		this.service = service;
		return this;
	}

	public ISimulatorCoin withMarketCoin(String marketCoin) {
		this.marketCoin = marketCoin;
		return this;
	}
	
	public ISimulatorWithPeriod withCoin(String coin) {
		this.coin = coin;
		return this;
	}
	
	public ISimulatorAmount withPeriod(LocalDateTime from, LocalDateTime to) {
		this.from = from;
		this.to = to;
		this.service = new CachedExchangeService(this.service, from, to);
		return this;
	}
	
	public ISimulatorStopLoss withAmount(BigDecimal amount) {
		this.amount = amount;
		return this;
	}
	
	public ISimulatorCandleSizeFactor withoutStopLoss() {
		this.stopLossOfCandleLength = false;
		return this;
	}
	
	public ISimulatorCandleSizeFactor withStopLoss(BigDecimal stopLoss) {
		this.stopLossOfCandleLength = false;
		this.stopLoss = stopLoss;
		return this;
	}
	
	public ISimulatorCandleSizeFactor withStopLossOfCandleLength() {
		this.stopLossOfCandleLength = true;
		return this;
		
	}
	
	public ISimulatorSellFactor withCandleSizeFactor(BigDecimal candleSizeFactor) {
		this.candleSizeFactor = candleSizeFactor;
		return this;
	}
	
	public ISimulatorTSSL withSellFactor(BigDecimal sellFactor) {
		this.sellFactor = sellFactor;
		return this;
	}
	
	public ISimulatorDelayToBuy withoutTSSL() {
		this.tsslPercent = null;
		return this;
	}
	
	public ISimulatorDelayToBuy withTSSL(BigDecimal tsslPercent) {
		this.tsslPercent = tsslPercent;
		return this;
	}
	
	public ISimulatorBuild withoutDelayToBuy() {
		this.delayToBuyPercent = null;
		return this;
	}
	
	@Override
	public ISimulatorBuild withDelayToBuy(BigDecimal delayToBuyPercent) {
		this.delayToBuyPercent = delayToBuyPercent;
		return this;
	}
		
	public Simulator build() {
		return this;
	}
	
	@Override
	public void run(boolean withLog) {
		this.withLog = withLog;
		LocalDateTime start = from.withHour(0).withMinute(0).withSecond(0).withNano(0).minusHours(TIME_DIFERENCE);
		LocalDateTime stop =  to.withHour(0).withMinute(0).withSecond(0).withNano(0).minusHours(TIME_DIFERENCE);
		try {
			init();
			while (start.isBefore(stop)) {
				Candle candle = UltimateAnalyze.ultimateAnalyze(service, marketCoin, coin, start.minusMinutes(COUNT_PERIODS * 5), start, COUNT_PERIODS, candleSizeFactor);
				synch(candle);
				if (candle.isOpportunity()) {
					tryOrder(candle);
				}
				start = start.plusMinutes(5);
			}
			if (wasBought()) {
				logNoSuccess(order);
			}
			finish();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void init() {
		order = null;
		balance = amount;
		total = 0;
		totalFee = BigDecimal.ZERO;
		logHeader();
	}
	
	private void finish() {
		logTrailler();
	}
	
	private boolean wasNotOrdered() {
		return priceToBuy == null && order == null;
	}
	
	private boolean wasOrdered() {
		return priceToBuy != null && order == null;
	}
	
	private boolean wasBought() {
		return order != null;
	}
	
	private void tryOrder(Candle candle) {
		if (wasNotOrdered()) {
			makeOrderToBuy(candle);
		}
	}
	
	private void synch(Candle candle) {
		if (wasOrdered()) {
			if (testBuy(candle)) {
				executeBuy(candle, waitToOut);
			}
		}
		else if (wasBought()) {
			if (testSell(candle)) {
				executeSell(candle, false);
			}
			else if (testStopLess(candle)) {
				executeStopLoss(candle);
			}
			else if (testTSSL(candle)) {
				executeTSSL(candle);
			}
		}
	}
		
	private boolean testSell(Candle candle) {
		return CalcUtil.isBetween(order.getSellPrice(), candle.getLow(), candle.getHigh());
	}
	
	private void executeSell(Candle candle, boolean immadiate) {
		if (immadiate) {
			order.setSellPrice(candle.getLow());
			order.setSellFee(service.getImmediateFee());
			order = OrderIntent.buildSell(order, candle.getDate(), OrderIntentState.STOPLOSS);
		}
		else {
			order = OrderIntent.buildSell(order, candle.getDate(), OrderIntentState.SUCCESS);
		}
		balance = order.getBalance();
		logSuccess(order);
		total++;
		order = null;			
	}
	
	private boolean testStopLess(Candle candle) {
		return order.hasStoploss() && !CalcUtil.greaterThen(candle.getLow(), order.getStoplossPrice());
	}
	
	private void executeStopLoss(Candle candle) {
		executeSell(candle, true);
	}
	
	private boolean testTSSL(Candle candle) {
		if (isTSSL()) {
			BigDecimal almost = CalcUtil.percent(order.getSellPrice(), CalcUtil.subtract(CalcUtil.HUNDRED, tsslPercent));
			return CalcUtil.greaterThen(candle.getHigh(), almost);
		}
		return false;
	}
	
	private void executeTSSL(Candle candle) {
		cancel(candle);
		BigDecimal newPrice = CalcUtil.percent(order.getSellPrice(), CalcUtil.add(CalcUtil.HUNDRED, tsslPercent));
		BigDecimal stoploss = CalcUtil.percent(order.getSellPrice(), CalcUtil.subtract(CalcUtil.HUNDRED, tsslPercent));
		order.setSellPrice(newPrice);
		order.setStoplossPrice(stoploss);
	}
	
	private void makeOrderToBuy(Candle candle) {
		this.waitToOut = false;
		if (isDelayToBuy()) {
			this.priceToBuy = CalcUtil.add(candle.getClose(), CalcUtil.percent(candle.getClose(), this.delayToBuyPercent));
			if (this.delayToBuyPercent.signum() == 1) {
				this.waitToOut = true;
			}
		}
		else {
			this.priceToBuy = candle.getClose();
			executeBuy(candle, true);
		}
	}
	
	private boolean testBuy(Candle candle) {
		if (waitToOut) { // cat leap
			return !CalcUtil.greaterThen(priceToBuy, candle.getLow());			
		}
		else {
			return CalcUtil.isBetween(priceToBuy, candle.getLow(), candle.getHigh());
		}
	}
	
	private void executeBuy(Candle candle, boolean immediate) {
		BigDecimal buyFee = immediate ? service.getImmediateFee() : service.getFee();
		BigDecimal sellPrice = CalcUtil.add(priceToBuy, CalcUtil.multiply(candle.getLength(), sellFactor));
		order = OrderIntent.buildBuy(candle.getCoin(), candle.getDate(), balance, priceToBuy, buyFee, sellPrice, service.getFee(), candle.getAngle());		

		if (isStopLoss()) {
			if (stopLossOfCandleLength) {
				order.setStoplossPrice(CalcUtil.subtract(order.getBuyPrice(), candle.getLength())); 
			}
			else {
				order.setStoplossPrice(CalcUtil.subtract(order.getBuyPrice(), CalcUtil.percent(order.getBuyPrice(), stopLoss))); 
			}
		}
		
		priceToBuy = null;
	}
	
	private void cancel(Candle candle) {
	}
	
	private void logHeader() {
		String sFrom = from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		String sTo = to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		log("COIN:   %s", coin);
		log("PERIOD: %s TO %s", sFrom, sTo);
		logLine();
		log("%-8s %-16s %-16s %11s %11s %11s %11s %11s %11s", 
				"STATE", 
				"BUY DATE", 
				"SELL DATE", 
				"TOTAL", 
				"PROFIT(%)", 
				"PROFIT",
				"FEE(%)",
				"FEE",
				"BALANCE"
		);
		logLine();
	}

	private void logLine() {
		log(String.format("%114s"," ").replaceAll("\\s","="));		
	}
	
	private void logTrailler() {
		logLine();
		log("%-8s %-16s %-16s %11s %11s %11s %11s %11s %11s", 
				"TOTAL:", 
				String.valueOf(getTotal()), 
				"", 
				"", 
				CalcUtil.formatPercent(getTotalProfit()), 
				"",
				"", 
				CalcUtil.formatReal(getTotalFee()),
				CalcUtil.formatReal(getTotalBalance())
		);
	}
	
	private void logNoSuccess(OrderIntent order) {
		String sBuyDate = order.getBuyDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		log("%-8s %-16s %-16s %11s %11s %11s %11s %11s %11s", 
				order.getState().name(), 
				sBuyDate, 
				"", 
				"", 
				"", 
				"",
				"", 
				"",
				CalcUtil.formatReal(order.getTotal())
		);
	}
	
	private void logSuccess(OrderIntent order) {
		String sBuyDate = order.getBuyDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		String sSellDate = order.getSellDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		log("%-8s %-16s %-16s %11s %11s %11s %11s %11s %11s", 
				order.getState().name(),
				sBuyDate, 
				sSellDate, 
				CalcUtil.formatReal(order.getTotal()), 
				CalcUtil.formatPercent(order.getProfitPercent()), 
				CalcUtil.formatReal(order.getProfit()),
				CalcUtil.formatPercent(order.getAllFeePercent()), 
				CalcUtil.formatReal(order.getAllFee()),
				CalcUtil.formatReal(order.getBalance())
		);
		totalFee = CalcUtil.add(totalFee, order.getAllFee());
	}
	
	private void log(String frm, Object ... params) {
		log(String.format(frm, params));
	}
	
	private void log(String msg) {
		if (withLog) {
			System.out.println(msg);
		}
	}
	
	private boolean isStopLoss() {
		return stopLossOfCandleLength || stopLoss != null;
	}
	
	private boolean isTSSL() {
		return tsslPercent != null;
	}
	
	private boolean isDelayToBuy() {
		return this.delayToBuyPercent != null;
	}
	
	public String getCoin() {
		return coin;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}
	
	public Integer getTotal() {
		return total;
	}
	
	public BigDecimal getTotalProfit() {
		return CalcUtil.multiply(CalcUtil.divide(CalcUtil.subtract(balance, amount), amount), CalcUtil.HUNDRED); 
	}
	
	public BigDecimal getTotalFee() {
		return totalFee;
	}
	
	public BigDecimal getTotalBalance() {
		return balance;
	}
	
	public boolean isStucked() {
		return order != null && OrderIntentState.STUCK.equals(order.getState());
	}
	
	static class OrderIntent {
		private OrderIntentState state;
		private LocalDateTime buyDate;
		private LocalDateTime sellDate;
		private String coin;
		private BigDecimal total;
		private BigDecimal buyPrice;
		private BigDecimal buyFee;
		private BigDecimal sellPrice;
		private BigDecimal sellFee;
		private BigDecimal angle;
		private BigDecimal stoplossPrice;
		public static OrderIntent buildBuy(String coin, LocalDateTime buyDate, BigDecimal total, BigDecimal buyPrice, BigDecimal buyFee, BigDecimal sellPrice, BigDecimal sellFee, BigDecimal angle) {
			return new OrderIntent(coin, buyDate, total, buyPrice, buyFee, sellPrice, sellFee, angle);
		}
		public static OrderIntent buildSell(OrderIntent buy, LocalDateTime sellDate, OrderIntentState state) {
			buy.setState(state);
			buy.setSellDate(sellDate);
			return buy;
		}
		private OrderIntent(String coin, LocalDateTime buyDate, BigDecimal total, BigDecimal buyPrice, BigDecimal buyFee, BigDecimal sellPrice, BigDecimal sellFee, BigDecimal angle) {
			this.state = OrderIntentState.STUCK;
			this.coin = coin;
			this.buyDate = buyDate;
			this.total = total;
			this.buyPrice = buyPrice;
			this.buyFee = buyFee;
			this.sellPrice = sellPrice;
			this.sellFee = sellFee;
			this.angle = angle;
		}
		public void setStoplossPrice(BigDecimal stoploss) {
			this.stoplossPrice = stoploss;
		}
		public BigDecimal getStoplossPrice() {
			return stoplossPrice;
		}
		public boolean hasStoploss() {
			return stoplossPrice != null;
		}		
		public OrderIntentState getState() {
			return state;
		}
		public BigDecimal getAngle() {
			return angle;
		}
		public void setState(OrderIntentState state) {
			this.state = state;
		}
		public String getCoin() {
			return coin;
		}
		public LocalDateTime getBuyDate() {
			return buyDate;
		}
		public LocalDateTime getSellDate() {
			return sellDate;
		}
		public void setSellDate(LocalDateTime sellDate) {
			this.sellDate = sellDate;
		}
		public BigDecimal getBuyPrice() {
			return buyPrice;
		}
		public BigDecimal getSellPrice() {
			return sellPrice;
		}
		public void setSellPrice(BigDecimal sellPrice) {
			this.sellPrice = sellPrice;
		}
		public boolean wasSold() {
			return this.sellDate != null;
		}
		public BigDecimal getBuyFee() {
			return buyFee;
		}
		public BigDecimal getSellFee() {
			return sellFee;
		}
		public void setSellFee(BigDecimal sellFee) {
			this.sellFee = sellFee;
		}
		public BigDecimal getTotal() {
			return total;
		}
		public BigDecimal getProfitPercent() {
			return CalcUtil.calculateProfitPercent(getBuyPrice(), getSellPrice());
		}
		public BigDecimal getProfit() {
			return CalcUtil.percent(getTotal(), getProfitPercent());
		}
		public BigDecimal getAllFeePercent() {
			return CalcUtil.add(getBuyFee(), getSellFee());
		}
		public BigDecimal getAllFee() {
			return CalcUtil.percent(getTotal(), getAllFeePercent());
		}
		public BigDecimal getBalance() {
			return CalcUtil.calculateBalance(getTotal(), getProfitPercent(), getAllFeePercent());
		}
	}
	
	static enum OrderIntentState {
		STUCK, SUCCESS, STOPLOSS
	}

}

