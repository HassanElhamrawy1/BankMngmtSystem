/*
 * Abstract class representing a Bank Account.
 * Contains common properties like id, customerId, balance, and transactions.
 * Provides base implementation for deposit and defines abstract withdraw method.
 * Thread-safe for concurrent access.
 *
 * Implements FR-05: Deposit Money
 * Implements FR-14: Concurrent Transactions
 */

package com.bank.model;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

import com.bank.model.Transaction;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Account implements Identifiable 
{
	
	/* Unique identifier for the account */
    protected String id;
    /* ID of the customer who owns this account */
    protected String customerId;
    /* Current account balance */
    protected double balance;
    /* List of all transactions for this account */
    protected List<Transaction> transactions;
    
    /* Thread lock for concurrent access control */
    private final transient Lock lock = new ReentrantLock();
    
    /**
     * Constructor to initialize account with ID and associated customer ID.
     * Initializes balance to 0.0 and creates a thread-safe list for transactions.
     * @param id          Unique account identifier
     * @param customerId  ID of the customer who owns this account
     */
    public Account(String id, String customerId) 
    {
        this.id = id;
        this.customerId = customerId;
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    /**
     * Returns the unique identifier of the account.
     * @return The account ID
     */
    @Override
    public String getId() 
    {
        return id;
    }

    /**
     * Returns the ID of the customer who owns this account.
     * @return The customer ID
     */
    public String getCustomerId() 
    {
        return customerId;
    }

    /*
     * Returns the current account balance.
     * @return The account balance
     */
    public double getBalance() 
    {
        return balance;
    }
    	
    /**
     * Adds funds to the account balance and records the transaction.
     * Thread-safe due to synchronized keyword.
     * Implements FR-05: Deposit Money
     * @param amount the amount to deposit (must be positive)
     * @throws IllegalArgumentException if amount is not positive
     */
    public synchronized void deposit(double amount) 
    {
        if (amount <= 0) 
        {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        this.balance += amount;
        /* Add transaction record */
        addTransaction(TransactionType.DEPOSIT, amount, "Deposit to account " + id);
    }
    
    protected void addTransaction(TransactionType type, double amount, String description) 
    {
        Transaction newTx = new Transaction(
            java.util.UUID.randomUUID().toString(),
            type,
            amount,
            description
        );
        /* Delegate to the core synchronized method to ensure thread-safety */
        addTransaction(newTx);
    }
    
    
    /**
     * Helper method to add an existing transaction record to the list.
     * Implements FR-14: Concurrent Transactions.
     *
     * @param transaction the transaction object to be added to the account
     */
    public synchronized void addTransaction(Transaction transaction) 
    {
        transactions.add(transaction);
    }
    
    /**
     * Returns the lock object for this account to handle concurrent access.
     * @return the ReentrantLock instance for this account
     */
    public Lock getLock() 
    {
        return lock;
    }
    

    /* Withdraw logic will be override from the SavingsAccount and CurrentAccount classes */
    
    
    /**
     * Abstract method for withdrawing money.
     * Implementation varies between SavingsAccount and CurrentAccount.
     *
     * @param amount the amount to withdraw
     */
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

