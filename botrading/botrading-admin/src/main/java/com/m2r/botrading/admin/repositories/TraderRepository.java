package com.m2r.botrading.admin.repositories;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;

public interface TraderRepository extends JpaRepository<Trader, Long> {

	List<Trader> findAllByTraderJobAndStateNot(TraderJob traderJob, Integer state);

	long countByTraderJobAndStateIn(TraderJob traderJob, Integer ...states);

	List<Trader> findAllByTraderJobAndStateNotIn(TraderJob traderJob, Integer ... states);

	Page<Trader> findAllTraderByTraderJobOrderByStateAscDateTimeDesc(TraderJob traderJob, Pageable pageable);
	
	default void complete(Trader trader) {
		trader.complete();
		this.save(trader);		
	}

	@Query("select sum(t.investment) from Trader t where t.traderJob = :traderJob and t.state in :states")
	BigDecimal sumInvestmentByTraderJobAndStateIn(@Param("traderJob") TraderJob traderJob, @Param("states") Integer ... states);
	
	@Query("select sum(t.profit) from Trader t where t.traderJob = :traderJob and t.stateDateTime >= :stateDateTime")
	BigDecimal sumProfitByTraderJobAndStateDateTimeGreaterThanEqual(@Param("traderJob") TraderJob traderJob, @Param("stateDateTime") LocalDateTime stateDateTime);
	
}
