/* Represents a bank transaction (Deposit/Withdraw/Transfer) with a timestamp. */

package com.bank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Transaction 
{
    private String id;
    private TransactionType type;  // DEPOSIT, WITHDRAW, TRANSFER
    private double amount;
    private LocalDateTime timestamp;
    private String description;

    public Transaction(String id, TransactionType type, double amount, String description) 
    {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();  /* Automatic to the DB */
        this.description = description;
    }

    public Transaction(String id, TransactionType type, double amount, String timestamp, String description) 
    {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.timestamp = LocalDateTime.parse(timestamp);  /* parse it (convert to string before saving to the DB )*/
        this.description = description;
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