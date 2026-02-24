package com.bank.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;

/*
 * Unit tests for SavingsAccount behavior.
 * Comments use block comment style (/* ... *\/).
 *
 * Assumptions:
 * - SavingsAccount has constructors:
 *     SavingsAccount(String id, String customerId)
 *     or SavingsAccount(String id, String customerId, double initialBalance)
 * - deposit(...) adds a Transaction of type DEPOSIT.
 * - withdraw(...) throws IllegalArgumentException when insufficient funds.
 */
public class SavingsAccountTest 
{

    private SavingsAccount savings;
    private final String accountId = "ACC-C00002-1";
    private final String customerId = "C00002";

    /*
     * Initialize a SavingsAccount with a starting balance before each test.
     */
    @BeforeEach
    void setUp() 
    {
        savings = new SavingsAccount(accountId, customerId, 1000.0);
    }

    /*
     * Deposit should increase the balance and create a DEPOSIT transaction.
     */
    @Test
    @DisplayName("Deposit increases balance and records a DEPOSIT transaction")
    void depositAddsBalanceAndTransaction() 
    {
        double amount = 250.0;

        savings.deposit(amount);

        /* 0.0001 is the Tolerance value */
        assertEquals(1250.0, savings.getBalance(), 0.0001, "Balance should increase by deposit amount");
        assertFalse(savings.getTransactions().isEmpty(), "Transactions list should not be empty after deposit");
        assertTrue(savings.getTransactions().stream().anyMatch(t -> t.getType() == TransactionType.DEPOSIT),
                "At least one transaction of type DEPOSIT should be present");
    }

    /*
     * Withdraw with sufficient funds should reduce the balance and record a WITHDRAW transaction.
     */
    @Test
    @DisplayName("Withdraw reduces balance and records a WITHDRAW transaction")
    void withdrawReducesBalanceAndRecordsTransaction() 
    {
        double amount = 300.0;

        savings.withdraw(amount);

        /* 0.0001 is the Tolerance value */
        assertEquals(700.0, savings.getBalance(), 0.0001, "Balance should be reduced by withdrawal amount");
        assertTrue(savings.getTransactions().stream().anyMatch(t -> t.getType() == TransactionType.WITHDRAW),
                "At least one transaction of type WITHDRAW should be present");
    }

    /*
     * Withdraw exceeding balance should throw IllegalArgumentException.
     */
    @Test
    @DisplayName("Withdraw with insufficient funds throws IllegalArgumentException")
    void withdrawInsufficientFundsThrows() 
    {
        double largeAmount = 10_000.0;

        assertThrows(IllegalArgumentException.class, () -> savings.withdraw(largeAmount),
                "Withdrawing more than available balance should throw IllegalArgumentException");
    }
}