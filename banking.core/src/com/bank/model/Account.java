/* Abstract class, contains id, balance, transactions, and methods for deposit and withdraw. */

package com.bank.model;

public abstract class Account implements Identifiable 
{

    protected String id;
    protected String customerId;
    protected double balance;

    public Account(String id, String customerId) 
    {
        this.id = id;
        this.customerId = customerId;
        this.balance = 0.0;
    }

    @Override
    public String getId() 
    {
        return id;
    }

    public String getCustomerId() 
    {
        return customerId;
    }

    public double getBalance() 
    {
        return balance;
    }

    // Common deposit logic
    public void deposit(double amount) 
    {
        if (amount <= 0) 
        {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance += amount;
    }

    /* Withdraw logic will be override from the SavingsAccount and CurrentAccount classes */
    public abstract void withdraw(double amount);

    @Override
    public String toString() 
    {
        return "Account{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", balance=" + balance +
                '}';
    }
}
