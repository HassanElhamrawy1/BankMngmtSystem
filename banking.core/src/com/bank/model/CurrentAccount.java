/* Extends Account, a checking account without interest. */

package com.bank.model;

/* for not it will de same as SavingAccount but we can add overdraft later */
public class CurrentAccount extends Account 
{

    public CurrentAccount(String id, String customerId) 
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
        addTransaction(TransactionType.WITHDRAW, amount, "Withdraw from Current Account " + id);
    }
}
