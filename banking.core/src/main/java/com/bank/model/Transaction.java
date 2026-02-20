/* Represents a bank transaction (Deposit/Withdraw/Transfer) with a timestamp. */

package com.bank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Transaction 
{
    private String id;
    private TransactionType type;  /* DEPOSIT, WITHDRAW, TRANSFER */
    private double amount;
    private LocalDateTime timestamp;
    private String description;
    
    /* Formatters to handle different timestamp formats from DB */
    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter DB_FORMATTER_WITH_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    
    /* Constructor for NEW transactions (uses current time) */
    public Transaction(String id, TransactionType type, double amount, String description) 
    {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();  /* Automatic to the DB */
        this.description = description;
    }

    /* Constructor for LOADING from DB (parses timestamp string) */
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

    public String getTimestampAsString() 
    {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Override
    public String toString() 
    {
        return String.format("[%s] %s: %.2f - %s", 
            getTimestampAsString(), type.getDisplayName(), amount, description);
    }
}