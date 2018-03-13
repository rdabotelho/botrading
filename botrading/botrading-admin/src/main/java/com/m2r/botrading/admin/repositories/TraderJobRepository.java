package com.m2r.botrading.admin.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m2r.botrading.admin.model.Account;
import com.m2r.botrading.admin.model.TraderJob;

public interface TraderJobRepository extends JpaRepository<TraderJob, Long> {
	
	TraderJob findByIdAndAccount(Long id, Account account);
    List<TraderJob> findAllByStrategyAndStateOrderByDateTimeAsc(String strategy, Integer state);
    List<TraderJob> findAllByStateOrderByDateTimeAsc(Integer state);
	List<TraderJob> findAllByAccountAndMarketCoinOrderByStateAscDateTimeDesc(Account account, String marketCoin);

}
