/**
 * Enumeration representing the report submenu options in the Bank Management System.
 * Each constant corresponds to a specific reporting or querying action.
 * Used to map user input (numeric codes) to actions in the reports submenu.
 */
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

    /**
     * Constructs a ReportChoice enum constant with the specified code and description.
     * @param code The numeric code representing the report option
     * @param description The human-readable description of the report option
     */
    ReportChoice(int code, String description) 
    {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the numeric code associated with this report choice.
     * @return The numeric code
     */
    public int getCode() 
    {
        return code;
    }

    /**
     * Returns the human-readable description of this report choice.
     * @return The description string
     */
    public String getDescription() 
    {
        return description;
    }

    /**
     * Maps a numeric code to its corresponding ReportChoice enum constant.
     * @param code The numeric code to look up
     * @return The matching ReportChoice constant, or INVALID if no match is found
     */
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