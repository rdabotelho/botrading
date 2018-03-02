package com.m2r.botrading.tests.sim;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.m2r.botrading.api.service.IExchangeService;
import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.tests.clplus.CachedExchangeService;
import com.m2r.botrading.tests.clplus.Candle;
import com.m2r.botrading.tests.clplus.CatLeapPlus2Analyze;
import com.m2r.botrading.tests.sim.SimulatorBuilder.ISimulatorAmount;
import com.m2r.botrading.tests.sim.SimulatorBuilder.ISimulatorBuild;
import com.m2r.botrading.tests.sim.SimulatorBuilder.ISimulatorCoin;
import com.m2r.botrading.tests.sim.SimulatorBuilder.ISimulatorStopLoss;
import com.m2r.botrading.tests.sim.SimulatorBuilder.ISimulatorWithPeriod;
import com.m2r.botrading.tests.sim.SimulatorBuilder.Simulator;

public class SimulatorFlow implements ISimulatorCoin, ISimulatorWithPeriod, ISimulatorAmount, ISimulatorStopLoss, ISimulatorBuild, Simulator {

	private static final int TIME_DIFERENCE = 3;
	private static int COUNT_PERIODS = 30;
	
	private IExchangeService service;
	private String coin; 
	private LocalDateTime to; 
	private LocalDateTime from;

	private OrderIntent order;
	private BigDecimal amount;
	private BigDecimal totalFee;
	private boolean stopLossOfCandleLength;
	private BigDecimal stopLoss;
	private BigDecimal stopLossValue;
	private BigDecimal balance;
	private int total;
	
	public ISimulatorCoin withService(IExchangeService service) {
		this.service = service;
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
	
	public ISimulatorBuild withoutStopLoss() {
		this.stopLossOfCandleLength = false;
		return this;
	}
	
	public ISimulatorBuild withStopLoss(BigDecimal stopLoss) {
		this.stopLossOfCandleLength = false;
		this.stopLoss = stopLoss;
		return this;
	}
	
	public ISimulatorBuild withStopLossOfCandleLength() {
		this.stopLossOfCandleLength = true;
		return this;
	}
	
	public Simulator build() {
		return this;
	}
	
	@Override
	public void run() {
		LocalDateTime start = from.withHour(0).withMinute(0).withSecond(0).withNano(0).minusHours(TIME_DIFERENCE);
		LocalDateTime stop =  to.withHour(0).withMinute(0).withSecond(0).withNano(0).minusHours(TIME_DIFERENCE);
		try {
			init();
			while (start.isBefore(stop)) {
				Candle candle = CatLeapPlus2Analyze.catLeapPlusAnalyze(service, "BTC", coin, start.minusMinutes(COUNT_PERIODS * 5), start, COUNT_PERIODS);
				synch(candle);
				if (candle.isOpportunity()) {
					tryOrder(candle);
				}
				start = start.plusMinutes(5);
			}
			if (isBought()) {
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
		stopLossValue = BigDecimal.ZERO;
		logHeader();
	}
	
	private void finish() {
		logTrailler();
	}
	
	private boolean isBought() {
		return order != null;
	}
	
	private void tryOrder(Candle candle) {
		if (!isBought()) {
			BigDecimal buyPrice = candle.getClose(); 		// immediate
			BigDecimal buyFee = service.getImmediateFee(); 	// immediate;
			BigDecimal sellPrice = CalcUtil.add(buyPrice, candle.getLength());
			BigDecimal sellFee = service.getFee();
			order = OrderIntent.buildBuy(candle.getCoin(), candle.getDate(), balance, buyPrice, buyFee, sellPrice, sellFee, candle.getAngle());
			if (isStopLoss()) {
				if (stopLossOfCandleLength) {
					stopLossValue = CalcUtil.subtract(buyPrice, candle.getLength()); 
				}
				else {
					stopLossValue = CalcUtil.subtract(buyPrice, CalcUtil.percent(buyPrice, stopLoss)); 
				}
			}
		}
	}
	
	private void synch(Candle candle) {
		if (isBought()) {
			if (CalcUtil.isBetween(order.getSellPrice(), candle.getLow(), candle.getHigh())) {
				success(candle);
			}
			else if (isStopLoss() && !CalcUtil.greaterThen(candle.getLow(), stopLossValue)) {
				stopLoss(candle);
			}
		}
	}
	
	private void success(Candle candle) {
		order = OrderIntent.buildSell(order, candle.getDate(), OrderIntentState.SUCCESS);
		balance = order.getBalance();
		logSuccess(order);
		total++;
		order = null;
	}
	
	private void stopLoss(Candle candle) {
		order.setSellPrice(candle.getLow());
		order.setSellFee(service.getImmediateFee());
		order = OrderIntent.buildSell(order, candle.getDate(), OrderIntentState.STOPLOSS);
		balance = order.getBalance();
		logSuccess(order);
		total++;
		order = null;
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
				String.valueOf(total), 
				"", 
				"", 
				CalcUtil.formatPercent(CalcUtil.multiply(CalcUtil.divide(CalcUtil.subtract(balance, amount), amount), CalcUtil.HUNDRED)), 
				"",
				"", 
				CalcUtil.formatReal(totalFee),
				CalcUtil.formatReal(balance)
		);
	}
	
	private void logNoSuccess(OrderIntent order) {
		String sBuyDate = order.getBuyDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		log("%-8s %-16s %-16s %11s %11s %11s %11s %11s %11s %11s", 
				order.getState().name(), 
				sBuyDate, 
				"", 
				"", 
				"", 
				"",
				"", 
				"",
				CalcUtil.formatReal(order.getTotal()),
				CalcUtil.formatPercent(order.getAngle())
		);
	}
	
	private void logSuccess(OrderIntent order) {
		String sBuyDate = order.getBuyDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		String sSellDate = order.getSellDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		log("%-8s %-16s %-16s %11s %11s %11s %11s %11s %11s %11s", 
				order.getState().name(),
				sBuyDate, 
				sSellDate, 
				CalcUtil.formatReal(order.getTotal()), 
				CalcUtil.formatPercent(order.getProfitPercent()), 
				CalcUtil.formatReal(order.getProfit()),
				CalcUtil.formatPercent(order.getAllFeePercent()), 
				CalcUtil.formatReal(order.getAllFee()),
				CalcUtil.formatReal(order.getBalance()),
				CalcUtil.formatPercent(order.getAngle())
		);
		totalFee = CalcUtil.add(totalFee, order.getAllFee());
	}
	
	private void log(String frm, Object ... params) {
		log(String.format(frm, params));
	}
	
	private void log(String msg) {
		System.out.println(msg);
	}
	
	private boolean isStopLoss() {
		return stopLossOfCandleLength || stopLoss != null;
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

