package com.bank.model;

public enum TransactionType 
{
    DEPOSIT("Deposit"),
    WITHDRAW("Withdraw"),
    TRANSFER("Transfer");

    private final String displayName;

    TransactionType(String displayName) 
    {
        this.displayName = displayName;
    }

    public String getDisplayName() 
    {
        return displayName;
    }

    /**
     * Safe way to convert String from DB to Enum constant.
     * Handles cases like "Transfer", "transfer", or "TRANSFER".
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

    @Override
    public String toString() 
    {
        return displayName;
    }
}