/*
 * Enumeration representing the type of a financial transaction.
 * Defines standard transaction types used throughout the banking system.
 * Implements FR-11: Transaction History.
 */
package com.bank.model;

public enum TransactionType 
{
	/* Standard transaction types */
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    TRANSFER("Transfer");

	/* Human-readable display name for the transaction type */
    private final String displayName;

    /**
     * Constructs a TransactionType with a display name.
     * @param displayName The human-readable name
     */
    TransactionType(String displayName) 
    {
        this.displayName = displayName;
    }
    
    /**
     * Gets the human-readable display name.
     * @return The display name
     */
    public String getDisplayName() 
    {
        return displayName;
    }

    /**
     * Safely converts a string value to a TransactionType enum constant.
     * Handles case-insensitive matching for both enum name and display name.
     * @param value The string to convert (e.g., "DEPOSIT", "deposit", "Deposit")
     * @return The corresponding TransactionType
     * @throws IllegalArgumentException if no matching type is found
     */
    public static TransactionType fromString(String value) 
    {
        if (value == null) return null;
        for (TransactionType type : TransactionType.values()) {
            if (type.name().equalsIgnoreCase(value) || type.displayName.equalsIgnoreCase(value)) 
            {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
    
    /**
     * Returns the display name when converted to string.
     * @return The display name
     */
    @Override
    public String toString() 
    {
        return displayName;
    }
}