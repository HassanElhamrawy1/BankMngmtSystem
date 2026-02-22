/*
 * Represents a Current (Checking) Account, a type of bank account for everyday transactions.
 * Extends the base Account class. Currently behaves like a SavingsAccount but can be 
 * extended to support features like overdraft in the future.
 * Implements FR-04: Create Account and FR-06: Withdraw Money.
 */

package com.bank.model;

/*
 * Note: Currently has the same behavior as SavingsAccount, but designed for future 
 * enhancements like overdraft protection or higher transaction limits.
 */
public class CurrentAccount extends Account 
{
	
	/**
     * Constructs a new Current Account with zero balance.
     * @param id          Unique account identifier
     * @param customerId  ID of the customer who owns this account
     */
	public CurrentAccount(String id, String customerId) 
	{
	    super(id, customerId);
	}

	/**
     * Constructs a new Current Account with an initial balance.
     * @param id          Unique account identifier
     * @param customerId  ID of the customer who owns this account
     * @param balance     Initial account balance
     */
	public CurrentAccount(String id, String customerId, double balance) 
	{
	    super(id, customerId);
	    this.balance = balance;
	}

    /* ---------------- FR-06: Withdraw Money ---------------- */
	/**
     * Withdraws money from the current account.
     * Ensures sufficient funds are available and records the transaction.
     * @param amount The amount to withdraw (must be positive)
     * @throws IllegalArgumentException if amount is not positive or insufficient funds
     */
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
