package com.m2r.botrading.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.m2r.botrading.admin.model.Account;
import com.m2r.botrading.admin.model.AccountUser;
import com.m2r.botrading.admin.service.AdminService;
import com.m2r.botrading.admin.ws.ExchengeWSServer;

@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class BotetradingApplication extends WebSecurityConfigurerAdapter {

	@Autowired
	private AdminService adminService;	
	
	@Autowired
	private ExchengeWSServer exchengeWSServer;
	
	public static void main(String[] args) {
		SpringApplication.run(BotetradingApplication.class, args);
	}
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
			.anyRequest()
			.authenticated()
			.and()
			.x509()
			.subjectPrincipalRegex("EmailAddress=(.*?)(?:,|$)")
			.userDetailsService(userDetailsService());
	}

	@Bean
	public UserDetailsService userDetailsService() {
		return new UserDetailsService() {
			@Override
			public UserDetails loadUserByUsername(String username) {
				Account account = adminService.findAccount(username);
				if (account != null) {
					return new AccountUser(account);
				}
				return null;
			}
		};
	}

}