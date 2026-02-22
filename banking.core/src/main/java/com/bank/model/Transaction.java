/*
 * Represents a single financial transaction (Deposit/Withdraw/Transfer) with a timestamp.
 * Used to track all movements of money in and out of accounts.
 * Implements FR-11: Transaction History.
 */

package com.bank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Transaction 
{
	/* Unique identifier for the transaction */
    private String id;
    /* Type of transaction (DEPOSIT, WITHDRAW, TRANSFER) */
    private TransactionType type;  
    /* The monetary amount of the transaction */
    private double amount;
    /* The exact date and time the transaction occurred */
    private LocalDateTime timestamp;
    /* A brief description of the transaction */
    private String description;
    
    /* Formatters to handle different timestamp formats from the DB */
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DB_FORMATTER_WITH_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    
    /**
     * Constructs a new transaction with the current timestamp.
     * Used when creating a new transaction in the system.
     * @param id          Unique transaction ID
     * @param type        Type of transaction
     * @param amount      Transaction amount
     * @param description Brief description of the transaction
     */
    public Transaction(String id, TransactionType type, double amount, String description) 
    {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();  /* Automatic to the DB */
        this.description = description;
    }

    /**
     * Constructs a transaction by parsing a timestamp string from the database.
     * Used when loading existing transactions from storage.
     * @param id          Unique transaction ID
     * @param type        Type of transaction
     * @param amount      Transaction amount
     * @param timestamp   Timestamp string in various formats
     * @param description Brief description of the transaction
     * @throws IllegalArgumentException if timestamp string cannot be parsed
     */
    public Transaction(String id, TransactionType type, double amount, String timestamp, String description) 
    {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.description = description;
        
        /*
         * Try to read the timestamp with different format
         * parse it (convert to string before saving to the DB ) 
         */
        try 
        {
            this.timestamp = LocalDateTime.parse(timestamp, DB_FORMATTER_WITH_SPACE);
        } 
        catch (DateTimeParseException e1) 
        {
            try 
            {
                this.timestamp = LocalDateTime.parse(timestamp, ISO_FORMATTER);
            } 
            catch (DateTimeParseException e2) 
            {
                throw new IllegalArgumentException("Unable to parse timestamp: " + timestamp, e2);
            }
        }    
    }
    /* Getters APIs */
    public String getId() 
    { 
    	return id; 
    }
    
    public TransactionType getType() 
    { 
    	return type; 
    }
    
    public double getAmount() 
    { 
    	return amount; 
    }
    
    public LocalDateTime getTimestamp() 
    { 
    	return timestamp; 
    }
    
    public String getDescription() 
    { 
    	return description; 
    }

    /**
     * Gets the timestamp formatted as a string for display or saving to DB.
     * @return Formatted timestamp string
     */
    public String getTimestampAsString() 
    {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Returns a formatted string representation of the transaction.
     * @return Formatted string for logging or display
     */
    @Override
    public String toString() 
    {
        return String.format("[%s] %s: %.2f - %s", 
            getTimestampAsString(), type.getDisplayName(), amount, description);
    }
}