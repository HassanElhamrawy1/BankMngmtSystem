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
    EXIT(9, "Exit"),
    INVALID(-1, "Invalid Choice");

    private final int code;
    private final String description;

    MenuChoice(int code, String description) 
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

    /* Get enum from code */
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