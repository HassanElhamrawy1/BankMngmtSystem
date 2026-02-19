/* Abstract class, contains id, balance, transactions, and methods for deposit and withdraw. */

package com.bank.model;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.bank.model.Transaction;

public abstract class Account implements Identifiable 
{

    protected String id;
    protected String customerId;
    protected double balance;
    protected List<Transaction> transactions;
    
    public Account(String id, String customerId) 
    {
        this.id = id;
        this.customerId = customerId;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
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
    
    /* ---------------- FR-05: Deposit Money ---------------- */	
    /* Common Deposit Method */
    public void deposit(double amount) 
    {
        if (amount <= 0) 
        {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.balance += amount;
        /* Add transaction record */
        addTransaction(TransactionType.DEPOSIT, amount, "Deposit to account " + id);
    }
    
    
    
    
    /* Helper method to add transaction*/
    /* InMemory Operations */
    protected void addTransaction(TransactionType type, double amount, String description)
    {
        transactions.add(new Transaction(
            UUID.randomUUID().toString(),
            type,
            amount,
            description
        ));
    }
    /* DB Operations */
    public void addTransaction(Transaction transaction) 
    {
        transactions.add(transaction);
    }

    /* Withdraw logic will be override from the SavingsAccount and CurrentAccount classes */
    public abstract void withdraw(double amount);
    
    public List<Transaction> getTransactions() 
    {
        return new ArrayList<>(transactions);
    }

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
