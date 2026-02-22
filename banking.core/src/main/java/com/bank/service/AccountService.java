/* Contains operations on a single account (deposit, withdraw, applyInterest). */

package com.bank.service;

import com.bank.model.Account;
import com.bank.repository.Repository;

public class AccountService 
{
    private Repository<Account> accountRepository;

    /* Constructor: inject accountRepository */
    public AccountService(Repository<Account> accountRepository) 
    {
        this.accountRepository = accountRepository;
    }
    /* ---------------- FR-05: Deposit Money ---------------- */
    public void deposit(String accountId, double amount) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) throw new IllegalArgumentException("Account not found");
        account.deposit(amount);   /* transaction will be done automatically in withdraw */
    }
     
    /* ---------------- FR-06: Withdraw Money ---------------- */
    public void withdraw(String accountId, double amount) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        	throw new IllegalArgumentException("Account not found");
        account.withdraw(amount);   /* add transaction will be done automatically in withdraw */
    }
    
    
}
