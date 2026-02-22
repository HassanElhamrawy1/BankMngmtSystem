/*
 * Service class that provides operations on a single account.
 * Handles account-specific actions like deposit, withdraw, and interest calculations.
 * Implements FR-05: Deposit Money and FR-06: Withdraw Money.
 */
package com.bank.service;

import com.bank.model.Account;
import com.bank.repository.Repository;

public class AccountService 
{
	/* Repository for account data access */
    private Repository<Account> accountRepository;

    /**
     * Constructs an AccountService with the specified account repository.
     * Implements Dependency Injection for repository access.
     */
    public AccountService(Repository<Account> accountRepository) 
    {
        this.accountRepository = accountRepository;
    }
    /* ---------------- FR-05: Deposit Money ---------------- */
    /**
     * Deposits a specified amount into an account.
     * @param accountId The ID of the account to deposit into
     * @param amount    The positive amount to deposit
     * @throws IllegalArgumentException if account not found
     */
    public void deposit(String accountId, double amount) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) throw new IllegalArgumentException("Account not found");
        account.deposit(amount);   /* transaction will be done automatically in withdraw */
    }
     
    /* ---------------- FR-06: Withdraw Money ---------------- */
    /**
     * Withdraws a specified amount from an account.
     * @param accountId The ID of the account to withdraw from
     * @param amount    The positive amount to withdraw
     * @throws IllegalArgumentException if account not found
     */
    public void withdraw(String accountId, double amount) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        	throw new IllegalArgumentException("Account not found");
        account.withdraw(amount);   /* add transaction will be done automatically in withdraw */
    }
    
    
}
