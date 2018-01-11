package com.m2r.botrading.admin.model;

import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

public class AccountUser extends User {

	private static final long serialVersionUID = 1L;
	
	private Account account;
	
	public AccountUser(Account account) {
		super(account.getApiKey(), "", AuthorityUtils.commaSeparatedStringToAuthorityList("ROLE_USER"));
		this.account = account;
	}

	public Account getAccount() {
		return account;
	}
	
}
