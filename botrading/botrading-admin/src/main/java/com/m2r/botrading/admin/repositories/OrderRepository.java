package com.m2r.botrading.admin.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;

public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findAllByTraderOrderByIdAsc(Trader trader);
	@Query("select o from Order o where o.trader.traderJob.state = 1 and o.state in (0,3,8)")
	List<Order> findAllToSchedule();
	List<Order> findAllByStateIn(Integer ... states);
	List<Order> findAllByTraderAndStateIn(Trader trader, Integer ... states);
	List<Order> findAllByTraderAndStateAndKind(Trader trader, Integer state, Integer kind);
	Order findByTraderAndParcelAndKind(Trader trader, Integer parcel, Integer kind);
	long countByTraderAndState(Trader trader, Integer state);
	long countByTraderAndStateNotIn(Trader trader, Integer ... states);
	
}
