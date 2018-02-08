package com.m2r.botrading.admin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import com.m2r.botrading.api.model.ITraderJob;
import com.m2r.botrading.api.util.CalcUtil;

@Entity
@Table(
	indexes = {
	    @Index(columnList = "id,account_id", name = "traderjob_account"),
	    @Index(columnList = "state", name = "traderjob_state"),
	    @Index(columnList = "account_id,marketCoin", name = "traderjob_account_marketcoin")
	}
)
public class TraderJob implements Serializable, ITraderJob {

	private static final long serialVersionUID = 1L;

	public static final Integer STATE_NEW = 0;
	public static final Integer STATE_STARTED = 1;
	public static final Integer STATE_FINISHED = 2;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private LocalDateTime dateTime;

	@NotNull
	private String strategy;

	@NotNull
	private Integer currencyCount = 3;

	@NotNull
	@Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	private BigDecimal amount;

	@NotNull
	@Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
	private BigDecimal tradingPercent = new BigDecimal("100.00");

	@NotNull
	private Integer state = STATE_NEW; // 0: New, 1: Started, 2: Completed
    
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel1 = new BigDecimal("1.0");
    
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel2 = new BigDecimal("0.0");
    
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel3 = new BigDecimal("0.0");
    
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel4 = new BigDecimal("0.0");

    @NotNull
	private Integer timeToCancel = 24 * 60; //24 hours to cancel a buying
	
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal limitToStop = new BigDecimal("10.0");
    
	@NotNull
	@Column(precision = 19, scale = 8)
	@NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	private BigDecimal profit;
    
	@OneToMany(mappedBy="traderJob", cascade=CascadeType.REMOVE)
	private List<Trader> traders;
	
    @NotNull
    @OneToOne
	private Account account;

    /*
     * OPTIONS 
     */
    
    private Boolean continuoMode = true;
    private Boolean cancelBuyWhenExpire = false;
    private Boolean executeSellWhenExpire = false;
    private Boolean stopLoss = false;
    
    private String marketCoin;
    
    @Transient
	@NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal todayProfitPercent = BigDecimal.ZERO;
    
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TraderJobOptions options = new TraderJobOptions();
    
	public TraderJob() {
		this.traders = new ArrayList<>();
		this.profit = BigDecimal.ZERO;
		this.todayProfitPercent = BigDecimal.ZERO;
		this.options = new TraderJobOptions();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public Integer getCurrencyCount() {
		return currencyCount;
	}
	
	public void setCurrencyCount(Integer currencyCount) {
		this.currencyCount = currencyCount;
	}
	
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getTradingPercent() {
		return tradingPercent;
	}
	
	public void setTradingPercent(BigDecimal tradingPercent) {
		this.tradingPercent = tradingPercent;
	}
	
	@NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	public BigDecimal getTradingAmount() {
		return CalcUtil.percent(amount, tradingPercent);
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}
	
	public List<Trader> getTraders() {
		return traders;
	}
	
	public void setTraders(List<Trader> traders) {
		this.traders = traders;
	}
	
	public String getStateName() {
		switch (this.state) {
		case 0: return "NEW";
		case 1: return "STARTED";
		case 2: return "FINISHED";
		}
		return null;
	}

	public Integer getTimeToCancel() {
		return timeToCancel;
	}
	
	public void setTimeToCancel(Integer timeToCancel) {
		this.timeToCancel = timeToCancel;
	}
	
	public BigDecimal getLimitToStop() {
		return limitToStop;
	}
	
	public void setLimitToStop(BigDecimal limitToStop) {
		this.limitToStop = limitToStop;
	}

	public Boolean getContinuoMode() {
		return continuoMode == null ? false : continuoMode;
	}

	public void setContinuoMode(Boolean continuoMode) {
		this.continuoMode = continuoMode;
	}

	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}
	
	@NumberFormat(style=Style.NUMBER, pattern="0.00")
	public BigDecimal getProfitPercent() {
		return this.getProfit().divide(this.getTradingAmount(), CalcUtil.DECIMAL_COIN).multiply(CalcUtil.HUNDRED, CalcUtil.DECIMAL_PERCENT);
	}
	
	@NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	public BigDecimal getBalance() {
		return CalcUtil.add(this.getTradingAmount(), this.getProfit());
	}

	public BigDecimal getTodayProfitPercent() {
		return todayProfitPercent;
	}

	public void setTodayProfitPercent(BigDecimal todayProfitPercent) {
		this.todayProfitPercent = todayProfitPercent;
	}

	public void start() {
		this.setState(STATE_STARTED);
	}
	
	public BigDecimal getParcel1() {
		return parcel1;
	}

	public void setParcel1(BigDecimal parcel1) {
		this.parcel1 = parcel1;
	}

	public BigDecimal getParcel2() {
		return parcel2;
	}

	public void setParcel2(BigDecimal parcel2) {
		this.parcel2 = parcel2;
	}

	public BigDecimal getParcel3() {
		return parcel3;
	}

	public void setParcel3(BigDecimal parcel3) {
		this.parcel3 = parcel3;
	}

	public BigDecimal getParcel4() {
		return parcel4;
	}

	public void setParcel4(BigDecimal parcel4) {
		this.parcel4 = parcel4;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}
	
	public String getMarketCoin() {
		return marketCoin;
	}
	
	public void setMarketCoin(String marketCoin) {
		this.marketCoin = marketCoin;
	}

	public Boolean getCancelBuyWhenExpire() {
		return cancelBuyWhenExpire == null ? false : cancelBuyWhenExpire;
	}

	public void setCancelBuyWhenExpire(Boolean cancelBuyWhenExpire) {
		this.cancelBuyWhenExpire = cancelBuyWhenExpire;
	}

	public Boolean getExecuteSellWhenExpire() {
		return executeSellWhenExpire == null ? false : executeSellWhenExpire;
	}

	public void setExecuteSellWhenExpire(Boolean executeSellWhenExpire) {
		this.executeSellWhenExpire = executeSellWhenExpire;
	}
	
	public Boolean getStopLoss() {
		return stopLoss == null ? false : stopLoss;
	}
	
	public void setStopLoss(Boolean stopLoss) {
		this.stopLoss = stopLoss;
	}

	public void finish() {
		this.setState(STATE_FINISHED);
	}
	
	public boolean isNew() {
		return this.state == STATE_NEW;
	}
	
	public boolean isStarted() {
		return this.state == STATE_STARTED;
	}
	
	public boolean isFinished() {
		return this.state == STATE_FINISHED;
	}
	
	public void addProfit(BigDecimal profit) {
		this.profit = CalcUtil.add(this.profit, profit);
	}
	
	public TraderJobOptions getOptions() {
		return options;
	}

	public void setOptions(TraderJobOptions options) {
		this.options = options;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).
			       append("id", id).
			       append("dateTime", dateTime).
			       append("strategy", strategy).
			       append("amount", amount).
			       append("tradingPercent", tradingPercent).
			       append("state", state).
			       toString();
	}

}
