/*
 * Represents a Savings Account, a type of bank account that earns interest over time.
 * Extends the base Account class with specific behavior for savings accounts.
 * Implements FR-04: Create Account and FR-06: Withdraw Money.
 */

package com.bank.model;

public class SavingsAccount extends Account 
{

	/**
     * Constructs a new Savings Account with zero balance.
     * @param id          Unique account identifier
     * @param customerId  ID of the customer who owns this account
     */
	public SavingsAccount(String id, String customerId) 
	{
	    super(id, customerId);
	}

	/**
     * Constructs a new Savings Account with an initial balance.
     * @param id          Unique account identifier
     * @param customerId  ID of the customer who owns this account
     * @param balance     Initial account balance
     */
	public SavingsAccount(String id, String customerId, double balance) 
	{
	    super(id, customerId);
	    this.balance = balance;
	}
    
    
    /* ---------------- FR-06: Withdraw Money ---------------- */
	/**
     * Withdraws money from the savings account.
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
        addTransaction(TransactionType.WITHDRAW, amount, "Withdraw from Savings Account " + id);
    }
}
