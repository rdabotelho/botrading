package com.m2r.botrading.admin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import com.m2r.botrading.api.model.IOrder;
import com.m2r.botrading.api.util.CalcUtil;

@Entity
@Table(name="Ordder")
public class Order implements Serializable, IOrder {
 
	private static final long serialVersionUID = 1L;

	public static final Integer STATE_NEW = 0;
	public static final Integer STATE_ORDERED = 1;
	public static final Integer STATE_LIQUIDED = 2; 	// complete
	public static final Integer STATE_NEW_CANCEL = 3;
	public static final Integer STATE_CANCELED = 4;	// complete
	public static final Integer STATE_ERROR = 5;
	public static final Integer STATE_ERROR_CANCEL = 6;
	public static final Integer STATE_ORDERED_TO_CANCEL = 7;
	public static final Integer STATE_IMMEDIATE_SELL = 8;
	
	public static final Integer KIND_BUY = 0;
	public static final Integer KIND_SELL = 1;
	
	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
 
    @NotNull
	private Integer parcel;
    
	private String orderNumber;
	
    @ManyToOne
    private Trader trader;
    
    @NotNull
    private LocalDateTime dateTime;

    @NotNull
    private Integer state = STATE_NEW; //0: New, 1: Ordered, 2: Liquided, 3: Canceled, 4: Error
    
    @NotNull
    private Integer kind; //0: Buy, 1: Sell
    
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
    private BigDecimal price;
    
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
    private BigDecimal amount;

    private String log;
    
    private LocalDateTime stateDateTime;
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public Integer getParcel() {
		return parcel;
	}
	
	public void setParcel(Integer parcel) {
		this.parcel = parcel;
	}
	
	public String getOrderNumber() {
		return orderNumber;
	}
	
	public void setOrderNumber(String orderNumber) {
		this.orderNumber = orderNumber;
	}

	public Trader getTrader() {
		return trader;
	}

	public void setTrader(Trader trader) {
		this.trader = trader;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getKind() {
		return kind;
	}

	public void setKind(Integer kind) {
		this.kind = kind;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal total) {
		this.amount = total;
	}
	
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	public BigDecimal getTotal() {
		return CalcUtil.multiply(this.getPrice(), this.getAmount());
	}

	public String getLog() {
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}
	
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	public BigDecimal getFee() {
		if (isBuy()) {
			return CalcUtil.percent(getAmount(), getTrader().getFee());
		}
		else {
			return CalcUtil.percent(getTotal(), getTrader().getFee());
		}
	}
	
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	public BigDecimal getBalance() {
		if (isBuy()) {
			return getTotal();
		}
		else {
			return CalcUtil.subtract(getTotal(), getFee());
		}
	}
	
	public void ordered() {
		this.setState(STATE_ORDERED);
	}
	
	public void liquided() {
		this.setState(STATE_LIQUIDED);
	}
	
	public void newCancel() {
		this.setState(STATE_NEW_CANCEL);
	}
	
	public void retry() {
		this.setState(STATE_NEW);
	}
	
	public void immediateSell() {
		this.setState(STATE_IMMEDIATE_SELL);
	}
	
	public void orderToCancel() {
		this.setState(STATE_ORDERED_TO_CANCEL);
	}
	
	public void cancel() {
		this.setState(STATE_CANCELED);
	}
	
	public boolean isNew() {
		return this.state == STATE_NEW;
	}
	
	public boolean isOrdered() {
		return this.state == STATE_ORDERED;
	}	
	
	public boolean isNewCancel() {
		return this.state == STATE_NEW_CANCEL;
	}
	
	public boolean isOrderedToCancel() {
		return this.state == STATE_ORDERED_TO_CANCEL;
	}	
	
	public boolean isCanceled() {
		return this.state == STATE_CANCELED;
	}
	
	public boolean isLiquided() {
		return this.state == STATE_LIQUIDED;
	}
	
	public boolean isImmediateSell() {
		return this.state == STATE_IMMEDIATE_SELL;
	}
	
	public boolean isExpired() {
		if (isOrdered()) {
			long minutes = this.getDateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES);
			return minutes > getTrader().getTraderJob().getTimeToCancel();
		}
		return false;
	}
	
	public void error(String msg) {
		this.setState(STATE_ERROR);
		this.setLog(msg);
	}
	
	public void errorCancel(String msg) {
		this.setState(STATE_ERROR_CANCEL);
		this.setLog(msg);
	}
	
	public String getStateName() {
		switch (this.state) {
		case 0: return "NEW";
		case 1: return "ORDERED";
		case 2: return "LIQUIDED";
		case 3: return "NEW CANCEL";
		case 4: return "CANCELED";
		case 5: return "ERROR";
		case 6: return "CANCEL ERROR";
		case 7: return "ORDERED TO CANCEL";
		case 8: return "IMMEDIATE SELL";
		case 9: return "NEW EXPIRE";
		case 10: return "EXPIRED";
		case 11: return "ORDERED TO EXPIRE";
		}
		return null;
	}
	
	public String getKindName() {
		switch (this.kind) {
		case 0: return "BUY";
		case 1: return "SELL";
		}
		return null;
	}
	
	public Integer getInverseKind() {
		if (this.kind == KIND_BUY) {
			return KIND_SELL;
		}
		else {
			return KIND_BUY;			
		}
	}
	
	public LocalDateTime getStateDateTime() {
		return stateDateTime;
	}

	public void setStateDateTime(LocalDateTime stateDateTime) {
		this.stateDateTime = stateDateTime;
	}

	public boolean isBuy() {
		return this.kind == KIND_BUY;
	}
	
	public boolean isSell() {
		return this.kind == KIND_SELL;
	}
	
	public boolean isCanSell() {
		return this.kind == KIND_SELL && (this.state == STATE_ORDERED || this.state == STATE_CANCELED);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).
			       append("id", id).
			       append("orderNumber", orderNumber).
			       append("dateTime", dateTime).
			       append("state", state).
			       append("kind", kind).
			       append("price", price).
			       append("amount", amount).
			       toString();
	}

}