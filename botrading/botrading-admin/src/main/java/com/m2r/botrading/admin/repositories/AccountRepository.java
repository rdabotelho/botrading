package com.m2r.botrading.admin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.m2r.botrading.admin.model.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

	Account findByApiKey(String apiKey);

}
