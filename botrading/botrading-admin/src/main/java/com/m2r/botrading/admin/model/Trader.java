package com.m2r.botrading.admin.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.format.annotation.NumberFormat;
import org.springframework.format.annotation.NumberFormat.Style;

import com.m2r.botrading.api.model.ITrader;
import com.m2r.botrading.api.util.CalcUtil;

@Entity
@Table(
	indexes = {
	    @Index(columnList = "state", name = "trader_state"),
	    @Index(columnList = "trader_job_id,state", name = "trader_state2")
	}
)
public class Trader implements Serializable, ITrader {
 
	private static final long serialVersionUID = 1L;

	public static final Integer STATE_NEW = 0;
	public static final Integer STATE_STARTED = 1;
	public static final Integer STATE_COMPLETED = 2;
	
	@Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
 
    @NotNull
    private String coin;
    
    @NotNull
    private LocalDateTime dateTime;

    @NotNull
    private Integer state = STATE_NEW; //0: New, 1: Started, 2: Completed
    
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
    private BigDecimal investment;
    
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal fee;
    
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel1;
    
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel2;
    
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel3;
    
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00")
    private BigDecimal parcel4;
        
    @NotNull
    @Column(precision = 19, scale = 8)
    @NumberFormat(style=Style.NUMBER, pattern="0.00000000")
    private BigDecimal profit;
    
	@OneToMany(mappedBy="trader", cascade=CascadeType.REMOVE)
	private List<Order> orders;
	
    @ManyToOne
    private TraderJob traderJob;

    @Transient
    private long orderedTotal = 0;

    @Transient
    private long liquidedTotal = 0;
    
    @Transient
    private List<BigDecimal> parcels;
    
    private LocalDateTime stateDateTime;
        
    public Trader() {
    		this.profit = BigDecimal.ZERO;
    }
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCoin() {
		return coin;
	}

	public void setCoin(String coin) {
		this.coin = coin;
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		if (this.stateDateTime == null) {
			this.stateDateTime = dateTime;
		}
		this.dateTime = dateTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public BigDecimal getInvestment() {
		return investment;
	}

	public void setInvestment(BigDecimal investment) {
		this.investment = investment;
	}

	public BigDecimal getFee() {
		return fee;
	}

	public void setFee(BigDecimal fee) {
		this.fee = fee;
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
	
	public TraderJob getTraderJob() {
		return traderJob;
	}

	public void setTraderJob(TraderJob traderJob) {
		this.traderJob = traderJob;
	}

	public List<BigDecimal> getParcels() {
		if (parcels == null) {
	    	this.parcels = new ArrayList<>();
			if (CalcUtil.isNotZeroPercent(this.getParcel1())) {
				this.parcels.add(this.getParcel1());
			}
			if (CalcUtil.isNotZeroPercent(this.getParcel2())) {
				this.parcels.add(this.getParcel2());
			}
			if (CalcUtil.isNotZeroPercent(this.getParcel3())) {
				this.parcels.add(this.getParcel3());
			}
			if (CalcUtil.isNotZeroPercent(this.getParcel4())) {
				this.parcels.add(this.getParcel4());
			}
		}
		return parcels;
	}
	
	public void setLiquidedTotal(long liquidadTotal) {
		this.liquidedTotal = liquidadTotal;
	}
	
	public long getLiquidedTotal() {
		return liquidedTotal;
	}
	
	public void setOrderedTotal(long orderedTotal) {
		this.orderedTotal = orderedTotal;
	}
	
	public long getOrderedTotal() {
		return orderedTotal;
	}
	
	public String getStateName() {
		switch (this.state) {
		case 0: return "NEW";
		case 1: return "STARTED";
		case 2: return "COMPLETED";
		}
		return null;
	}
	
	public BigDecimal getProfit() {
		return profit;
	}

	public void setProfit(BigDecimal profit) {
		this.profit = profit;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public void start() {
		this.setState(STATE_STARTED);
	}
	
	public void complete() {
		this.setStateDateTime(LocalDateTime.now());
		this.setState(STATE_COMPLETED);
	}
	
	public boolean isNew() {
		return this.state == STATE_NEW;
	}
	
	public boolean isStarted() {
		return this.state == STATE_STARTED;
	}
	
	public boolean isCompleted() {
		return this.state == STATE_COMPLETED;
	}
	
	public void addProfit(BigDecimal profit) {
		this.profit = CalcUtil.add(this.profit, profit);
	}
	
	@Override
	public String toString() {
		return new ToStringBuilder(this).
			       append("id", id).
			       append("coin", coin).
			       append("dateTime", dateTime).
			       append("state", state).
			       append("investment", investment).
			       append("parcel1", parcel1).
			       append("parcel2", parcel2).
			       append("parcel3", parcel3).
			       append("parcel4", parcel4).
			       toString();
	}

	public LocalDateTime getStateDateTime() {
		if (stateDateTime == null) {
			stateDateTime = dateTime;
		}
		return stateDateTime;
	}

	public void setStateDateTime(LocalDateTime stateDateTime) {
		this.stateDateTime = stateDateTime;
	}
	
}