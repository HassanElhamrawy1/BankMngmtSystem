/**
 * Enumeration representing the main menu options in the Bank Management System.
 * Each constant corresponds to a specific user action and menu item.
 * Used to map user input (numeric codes) to actions in the main application loop.
 */

package com.bank.app;

public enum MenuChoice 
{
    CREATE_CUSTOMER(1, "Create Customer"),
    VIEW_CUSTOMERS(2, "View All Customers"),
    CREATE_ACCOUNT(3, "Create Account"),
    DEPOSIT(4, "Deposit"),
    WITHDRAW(5, "Withdraw"),
    TRANSFER(6, "Transfer"),
    VIEW_ACCOUNT(7, "View Account"),
    LIST_ACCOUNTS(8, "List Accounts"),
    VIEW_REPORTS(9, "List Reports"),
    VIEW_TRANSACTIONS(10, "List Transactions"),
    GENERATE_ACCOUNTS_STATEMENT(11, "Generate Accounts Statement"),
    GENERATE_SUMMARY_REPORT(12, "Generate Summary Report"),
    EXIT(13, "Exit"),
    INVALID(-1, "Invalid Choice");

    private final int code;
    private final String description;

    /**
     * Constructs a MenuChoice enum constant with the specified code and description.
     * @param code The numeric code representing the menu option
     * @param description The human-readable description of the menu option
     */
    
    MenuChoice(int code, String description) 
    {
        this.code = code;
        this.description = description;
    }

    /**
     * Returns the numeric code associated with this menu choice.
     * @return The numeric code
     */
    public int getCode() 
    {
        return code;
    }

    /**
     * Returns the human-readable description of this menu choice.
     * @return The description string
     */
    public String getDescription() 
    {
        return description;
    }

    /**
     * Maps a numeric code to its corresponding MenuChoice enum constant.
     * @param code The numeric code to look up
     * @return The matching MenuChoice constant, or INVALID if no match is found
     */
    public static MenuChoice fromCode(int code) 
    {
        for (MenuChoice choice : MenuChoice.values()) 
        {
            if (choice.code == code) 
            {
                return choice;
            }
        }
        return INVALID;
    }
}