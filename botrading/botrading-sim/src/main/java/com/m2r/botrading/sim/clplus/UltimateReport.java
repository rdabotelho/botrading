package com.m2r.botrading.sim.clplus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.m2r.botrading.api.util.CalcUtil;
import com.m2r.botrading.sim.SimulatorBuilder.Simulator;

public class UltimateReport {

	public static void printSummary(List<Simulator> simmulators, LocalDateTime from, LocalDateTime to) {
		String sFrom = from.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		String sTo = to.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
		print("PERIOD: %s TO %s", sFrom, sTo);
		printLine();
		print("%-8s %11s %11s %11s %11s %11s %7s",
				"COIN", 
				"AMOUNT", 
				"TOTAL", 
				"PROFIT", 
				"FEE", 
				"BALANCE",
				"STUCKED"
		);
		
		printLine();
		
		BigDecimal amount = BigDecimal.ZERO;
		Integer total = 0;
		BigDecimal profit = BigDecimal.ZERO;
		BigDecimal fee = BigDecimal.ZERO;
		BigDecimal balance = BigDecimal.ZERO;
		
		for (Simulator simulator : simmulators) {
			print("%-8s %11s %11s %11s %11s %11s %7s",
					simulator.getCoin() , 
					CalcUtil.formatReal(simulator.getAmount()),
					simulator.getTotal().toString(),
					CalcUtil.formatPercent(simulator.getTotalProfit()),
					CalcUtil.formatReal(simulator.getTotalFee()),
					CalcUtil.formatReal(simulator.getTotalBalance()),
					(simulator.isStucked() ? "YES" : "NO")
			);
			
			amount = CalcUtil.add(amount, simulator.getAmount());
			total+= simulator.getTotal();
			profit = CalcUtil.add(profit, simulator.getTotalProfit());
			fee = CalcUtil.add(fee, simulator.getTotalFee());
			balance = CalcUtil.add(balance, simulator.getTotalBalance());
		}
		
		printLine();
		
		print("%-8s %11s %11s %11s %11s %11s %7s",
				"", 
				CalcUtil.formatReal(amount),
				total.toString(),
				CalcUtil.formatPercent(profit),
				CalcUtil.formatReal(fee),
				CalcUtil.formatReal(balance),
				""
		);
	}
	
	public static void printLine() {
		print(String.format("%76s"," ").replaceAll("\\s","="));		
	}
	
	public static void print(String frm, Object ... params) {
		print(String.format(frm, params));
	}
	
	public static void print(String msg) {
		System.out.println(msg);
	}

}
