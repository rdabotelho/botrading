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
	public static final Integer STATE_LIQUIDED = 2;
	public static final Integer STATE_ORDERED_CANCEL = 3;
	public static final Integer STATE_CANCELED = 4;
	public static final Integer STATE_ERROR = 5;
	
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
    private Integer state = STATE_NEW;
    
    @NotNull
    private Boolean immediate = false;
    
    @NotNull
    private Boolean pending = true;
    
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
    
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal fee;

    private String log;
    
    private LocalDateTime stateDateTime;
    
    public Order() {
		this.state = STATE_NEW;
		this.immediate = false;
		this.pending = false;
		this.fee = new BigDecimal("0.00");
    }
    
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

	public Boolean getImmediate() {
		return immediate;
	}

	public void setImmediate(Boolean immediate) {
		this.immediate = immediate;
	}

	public Boolean getPending() {
		return pending;
	}

	public void setPending(Boolean pending) {
		this.pending = pending;
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
	public BigDecimal getFeeValue() {
		if (isBuy()) {
			return CalcUtil.multiply(getPrice(), CalcUtil.percent(getAmount(), getFee()));
		}
		else {
			return CalcUtil.percent(getTotal(), getFee());
		}
	}
	
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
	public BigDecimal getBalance() {
		if (isBuy()) {
			return getTotal();
		}
		else {
			return CalcUtil.subtract(getTotal(), getFeeValue());
		}
	}
	
	public boolean isNew() {
		return this.state == STATE_NEW;
	}
	
	public boolean isOrdered() {
		return this.state == STATE_ORDERED;
	}	
	
	public boolean isLiquided() {
		return this.state == STATE_LIQUIDED;
	}
	
	public boolean isOrderedCancel() {
		return this.state == STATE_ORDERED_CANCEL;
	}	
	
	public boolean isCanceled() {
		return this.state == STATE_CANCELED;
	}
	
	public boolean isError() {
		return this.state == STATE_ERROR;
	}
	
	public boolean isCompleted() {
		return isLiquided() || isCanceled();
	}
	
	public boolean isImmediate() {
		return getImmediate();
	}
	
	public boolean isPending() {
		return getPending();
	}
	
	public boolean isExpired() {
		if (isOrdered()) {
			long minutes = this.getDateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES);
			return minutes > getTrader().getTraderJob().getTimeToCancel();
		}
		return false;
	}
	
	public String getStateName() {
		switch (this.state) {
		case 0: return "NEW";
		case 1: return "ORDERED" + (isPending()?" (*)" : "");
		case 2: return "LIQUIDED";
		case 3: return "ORDERED TO CANCEL" + (isPending()?" (*)" : "");
		case 4: return "CANCELED";
		case 5: return "ERROR";
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

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
	}

	public boolean isBuy() {
		return this.kind == KIND_BUY;
	}
	
	public boolean isSell() {
		return this.kind == KIND_SELL;
	}
	
	public boolean isCanSell() {
		return isOrdered() && pending == true;
	}
	
	/*
	 * PREPERS
	 */
	
     public Order preperToBuy() {
 		this.setState(Order.STATE_ORDERED);
 		this.setImmediate(false);
 		this.setPending(false);
		return this;    		
    }
    
     public Order preperToSell() {
 		this.setState(Order.STATE_ORDERED);
 		this.setImmediate(false);
 		this.setPending(false);
		return this;    		
    }
    
     public Order preperToImmediateSel() {
 		this.setState(Order.STATE_ORDERED_CANCEL);
 		this.setImmediate(true);
 		this.setPending(false);
		return this;    		
    }
    
     public Order preperToCancel() {
 		this.setState(Order.STATE_ORDERED_CANCEL);
 		this.setImmediate(false);
 		this.setPending(false);
		return this;    		
    }
    
     public Order confirmLiquidation() {
		this.setState(Order.STATE_LIQUIDED);
 		this.setPending(false);
		return this;    		
    }
    
     public Order confirmCancellation() {
		if (this.isImmediate()) {
	 		this.setState(Order.STATE_ORDERED);
		}
		else {
			this.setState(Order.STATE_CANCELED);
		}
 		this.setPending(false);
		return this;    		
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