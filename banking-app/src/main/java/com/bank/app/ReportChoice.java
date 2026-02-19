package com.bank.app;

public enum ReportChoice 
{
    TOTAL_ACCOUNTS(1, "Total Accounts"),
    TOTAL_BALANCE(2, "Total Balance"),
    HIGHEST_BALANCE(3, "Highest Balance Account"),
    FILTER_MIN_BALANCE(4, "Filter by Minimum Balance"),
    FILTER_MAX_BALANCE(5, "Filter by Maximum Balance"),
    FILTER_RANGE(6, "Filter by Balance Range"),
    BACK(7, "Back to main menu"),
    INVALID(-1, "Invalid Choice");

    private final int code;
    private final String description;

    ReportChoice(int code, String description) 
    {
        this.code = code;
        this.description = description;
    }

    public int getCode() 
    {
        return code;
    }

    public String getDescription() 
    {
        return description;
    }

    public static ReportChoice fromCode(int code) 
    {
        for (ReportChoice choice : ReportChoice.values()) 
        {
            if (choice.code == code) 
            {
                return choice;
            }
        }
        return INVALID;
    }
}