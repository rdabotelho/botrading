package com.m2r.botrading.admin.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;

public interface TraderRepository extends JpaRepository<Trader, Long> {

	List<Trader> findAllByTraderJob(TraderJob traderJob);

	long countByTraderJobAndStateIn(TraderJob traderJob, Integer ...states);

	List<Trader> findAllByTraderJobAndStateNotIn(TraderJob traderJob, Integer ... states);
	
}
