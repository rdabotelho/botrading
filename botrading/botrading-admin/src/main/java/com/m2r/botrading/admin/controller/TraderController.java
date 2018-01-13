package com.m2r.botrading.admin.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.m2r.botrading.admin.model.Account;
import com.m2r.botrading.admin.model.AccountUser;
import com.m2r.botrading.admin.model.Order;
import com.m2r.botrading.admin.model.Trader;
import com.m2r.botrading.admin.model.TraderJob;
import com.m2r.botrading.admin.service.AdminService;
import com.m2r.botrading.admin.util.AccountExchangeInfo;
import com.m2r.botrading.admin.util.OrderExchangeInfo;
import com.m2r.botrading.api.model.IMarketCoin;
import com.m2r.botrading.api.strategy.IStrategy;

@Controller
@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
@PreAuthorize("hasAuthority('ROLE_USER')")
public class TraderController {

    private final static Logger LOG = Logger.getLogger(TraderController.class.getSimpleName());

	@Autowired
	private AdminService adminService;
	
	private Account account;
	private TraderJob traderJob;
	private IMarketCoin selectedMarketCoin;
	
	@GetMapping("/")
    public String home(Model model, Principal principal) {
        return initTraderJob(adminService.getDefaultMarketCoin().getId(), model, principal);
    }	
	
	/*
	 * TRADERJOB
	 */
    
    @GetMapping("traderJob/{marketCoin}")
    public String initTraderJob(@PathVariable("marketCoin") String marketCoin, Model model, Principal principal) {
    		selectedMarketCoin = adminService.getMarketCoin(marketCoin);
		validateAccount(principal, null);
		model.addAttribute("selectedMarketCoin", selectedMarketCoin);
    		model.addAttribute("traderJob", adminService.newTraderJob(account));
    		model.addAttribute("listTraderJobs", this.adminService.findAllTraderJobs(account));
        return "traderJob";
    }    
    
    @PostMapping("/traderJob/add")
    public String traderJobAdd(@ModelAttribute TraderJob traderJob, Model model, Principal principal) {
		validateAccount(principal, null);
    		traderJob.setDateTime(LocalDateTime.now());
    		traderJob.setAccount(account);
    		traderJob.setMarketCoin(selectedMarketCoin.getId());
    		adminService.saveTraderJob(traderJob);
    		model.addAttribute("selectedMarketCoin", selectedMarketCoin);
		model.addAttribute("traderJob", adminService.newTraderJob(account));
		model.addAttribute("listTraderJobs", this.adminService.findAllTraderJobs(account));
        return "traderJob";
    }
    
    @GetMapping("/traderJob/start/{tjId}")
    public String startTraderJob(@PathVariable("tjId") Long tjId, Model model, Principal principal) {
		validateAccount(principal, tjId);
		try {
			adminService.startTraderJob(traderJob);
		} 
		catch (Exception e) {
			LOG.warning(e.getMessage());
		}
		model.addAttribute("selectedMarketCoin", selectedMarketCoin);
		model.addAttribute("traderJob", adminService.newTraderJob(account));
		model.addAttribute("listTraderJobs", this.adminService.findAllTraderJobs(account));
		return "traderJob";
    }    
    
    @GetMapping("/traderJob/finish/{tjId}")
    public String finish(@PathVariable("tjId") Long tjId, Model model, Principal principal) {
		validateAccount(principal, tjId);
    		adminService.finishTraderJob(tjId);
    		model.addAttribute("selectedMarketCoin", selectedMarketCoin);
    		model.addAttribute("traderJob", adminService.newTraderJob(account));
    		model.addAttribute("listTraderJobs", this.adminService.findAllTraderJobs(account));
        return "traderJob";
    }    
    
    @GetMapping("/traderJob/delete/{tjId}")
    public String delete(@PathVariable("tjId") Long tjId, Model model, Principal principal) {
		validateAccount(principal, tjId);
    		adminService.deleteTraderJob(tjId);
    		model.addAttribute("selectedMarketCoin", selectedMarketCoin);
    		model.addAttribute("traderJob", adminService.newTraderJob(account));
    		model.addAttribute("listTraderJobs", this.adminService.findAllTraderJobs(account));
        return "traderJob";
    }    

    /*
     * TRADER
     */
    @GetMapping("/trader2/{tjId}")
    public String getTrader2(@PathVariable("tjId") Long tjId, Model model, Principal principal) {
		validateAccount(principal, tjId);
        model.addAttribute("tjId", tjId);
		model.addAttribute("listTraders", traderJob.getTraders());
    		return "trader"; 
    }
    
    @GetMapping("/trader/{tjId}")
    public String getTrader(@PathVariable("tjId") Long tjId, Model model, Principal principal, Pageable pageable) {
		validateAccount(principal, tjId);
        model.addAttribute("tjId", tjId);
        model.addAttribute("page", pageable);
        Page<Trader> list = adminService.findAllTraderByTraderJob(traderJob, pageable);
		model.addAttribute("hasPrevious", list.hasPrevious());        
		model.addAttribute("hasNext", list.hasNext());        
		model.addAttribute("listTraders", list);
    		return "trader"; 
    }
    
    /*
     * ORDER
     */
    @GetMapping("/order/{tId}")
    public String order(@PathVariable("tId") Long tId, Model model, Principal principal) {
    		Trader trader = adminService.findTrader(tId);
    		validateAccount(principal, trader.getTraderJob().getId());
        return returnOrder(tId, model, principal);
    }
    
    @GetMapping("/order/cancel/{oId}")
    public String cancelOrder(@PathVariable("oId") Long oId, Model model, Principal principal) {
    		Order order = adminService.findOrder(oId);
    		validateAccount(principal, order.getTrader().getTraderJob().getId());
    		adminService.cancelOrder(order);
        return returnOrder(order.getTrader().getId(), model, principal);
    }
    
    @GetMapping("/order/retry/{oId}")
    public String retryOrder(@PathVariable("oId") Long oId, Model model, Principal principal) {
    		Order order = adminService.findOrder(oId);
    		validateAccount(principal, order.getTrader().getTraderJob().getId());
   		adminService.retryOrder(order);
        return returnOrder(order.getTrader().getId(), model, principal);
    }
    
    @GetMapping("/order/immediateSell/{oId}")
    public String immediateSell(@PathVariable("oId") Long oId, Model model, Principal principal) {
   		Order order = adminService.findOrder(oId);
   		validateAccount(principal, order.getTrader().getTraderJob().getId());
   		adminService.immediateSell(order);
        return returnOrder(order.getTrader().getId(), model, principal);
    }
        
    @ModelAttribute("allStrategies")
    public List<IStrategy> getAllStrategies() {
        return this.adminService.getStrategies();
    }
    
	@ModelAttribute("allMarketCoins")
	public List<IMarketCoin> getAllMarketCoins() {
	    return this.adminService.getMarketCoins();
	}
    
    public String returnOrder(Long tId, Model model, Principal principal) {
		Trader trader = adminService.findTrader(tId);
	    model.addAttribute("tjId", trader.getTraderJob().getId());
	    model.addAttribute("tId", tId);
	    model.addAttribute("orders", adminService.findAllOrders(trader));
	    model.addAttribute("coin", trader.getCoin());
	    model.addAttribute("investment", trader.getInvestment());
	    return "order";
    }
    
    private void validateAccount(Principal principal, Long tjId) throws RuntimeException {
		AccountUser user = (AccountUser) ((Authentication) principal).getPrincipal();   
    		this.account = user.getAccount();
    		if (account != null) {
    			account.setSelectedMarketCoin(selectedMarketCoin);
    			if (tjId != null) {
	        		traderJob = adminService.findTraderJob(tjId, account);    		
	        		if (traderJob != null && account.getId().equals(traderJob.getAccount().getId())) {
	        			return;  			
	        		}
    			}
    			else {
    				return;
    			}
    		}
		throw new RuntimeException("User denied");
    }
    
    /*
     * AJAX CALL
     */
    
    @GetMapping("/traderJob/changeExecuteSellWhenExpire/{tjId}/{value}")
    @ResponseBody
    public String changeExecuteSellWhenExpire(@PathVariable("tjId") Long tjId, Model model, @PathVariable("value") Boolean value, Principal principal) {
		validateAccount(principal, tjId);
		traderJob.setExecuteSellWhenExpire(value);
		adminService.saveTraderJob(traderJob);
        return "{}";
    }    
    
    @GetMapping("/traderJob/changeCancelBuyWhenExpire/{tjId}/{value}")
    @ResponseBody
    public String changeCancelBuyWhenExpire(@PathVariable("tjId") Long tjId, Model model, @PathVariable("value") Boolean value, Principal principal) {
		validateAccount(principal, tjId);
		traderJob.setCancelBuyWhenExpire(value);
		adminService.saveTraderJob(traderJob);
        return "{}";
    }    
    
    @GetMapping("/traderJob/changeContinuoMode/{tjId}/{value}")
    @ResponseBody
    public String changeContinuoMode(@PathVariable("tjId") Long tjId, Model model, @PathVariable("value") Boolean value, Principal principal) {
		validateAccount(principal, tjId);
		traderJob.setContinuoMode(value);
		adminService.saveTraderJob(traderJob);
        return "{}";
    }    
    
    @GetMapping("/traderJob/traderJobExchangeInfo")
    @ResponseBody
    public AccountExchangeInfo traderJobExchangeInfo(Principal principal) {
		validateAccount(principal, null);
		return adminService.getAccountExchangeInfo(account);
    }
    
    @GetMapping("/order/information/{tId}")
    @ResponseBody
    public OrderExchangeInfo orderExchangeInfo(@PathVariable("tId") Long tId, Principal principal) {
		Trader trader = adminService.findTrader(tId);
		validateAccount(principal, trader.getTraderJob().getId());
		return adminService.getOrderExchangeInfo(trader);
    }
    
}
