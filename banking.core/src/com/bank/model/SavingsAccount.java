/* Extends Account, has interestRate and method to apply interest. */

package com.bank.model;

public class SavingsAccount extends Account 
{

    public SavingsAccount(String id, String customerId) 
    {
        super(id, customerId);
    }
    
    
    /* ---------------- FR-06: Withdraw Money ---------------- */
    @Override
    public void withdraw(double amount) 
    {
        if (amount <= 0) 
        {
            throw new IllegalArgumentException("Withdraw amount must be positive.");
        }

        if (balance < amount) 
        {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        balance -= amount;
        addTransaction(TransactionType.WITHDRAW, amount, "Withdraw from Savings Account " + id);
    }
}
