package com.bank.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Unit tests for CurrentAccount behavior.
 * Comments use block comment style (/* ... *\/).
 *
 * Notes / Assumptions:
 * - CurrentAccount has constructor:
 *     CurrentAccount(String id, String customerId, double initialBalance)
 * - deposit(...) adds a Transaction of type DEPOSIT.
 * - withdraw(...) adds a Transaction of type WITHDRAW and enforces overdraft rules if any.
 * - If your CurrentAccount supports overdraft, adjust tests below to match that logic.
 */
public class CurrentAccountTest 
{
    private static final Logger logger = LoggerFactory.getLogger(CurrentAccountTest.class);

    private CurrentAccount current;
    private final String accountId = "ACC-C00002-2";
    private final String customerId = "C00002";

    /*
     * Prepare a CurrentAccount instance before each test.
     */
    @BeforeEach
    void setUp() 
    {
        logger.info("Initializing CurrentAccount for test: ID={}", accountId);
        current = new CurrentAccount(accountId, customerId, 500.0);
    }

    /*
     * Deposit should update balance and create a DEPOSIT transaction.
     */
    @Test
    @DisplayName("Deposit increases balance and records a DEPOSIT transaction")
    void depositAddsBalanceAndTransaction() 
    {
        double amount = 100.0;
        logger.info("Depositing {} into CurrentAccount {}", amount, accountId);
        
        current.deposit(amount);

        logger.info("New balance after deposit: {}", current.getBalance());

        assertEquals(600.0, current.getBalance(), 0.0001, "Balance should increase by deposit amount");
        assertFalse(current.getTransactions().isEmpty(), "Transactions list should contain at least one entry after deposit");
        assertTrue(current.getTransactions().stream().anyMatch(t -> t.getType() == TransactionType.DEPOSIT),
                "At least one transaction of type DEPOSIT should be present");
    }

    /*
     * Withdraw with sufficient funds should reduce the balance and record a WITHDRAW transaction.
     * If overdraft is supported by CurrentAccount, modify expected balance and behavior accordingly.
     */
    @Test
    @DisplayName("Withdraw reduces balance and records a WITHDRAW transaction")
    void withdrawReducesBalanceAndRecordsTransaction() 
    {
        double amount = 200.0;
        logger.info("Withdrawing {} from CurrentAccount {}", amount, accountId);

        current.withdraw(amount);

        logger.info("New balance after withdrawal: {}", current.getBalance());

        assertEquals(300.0, current.getBalance(), 0.0001, "Current account balance should be reduced by withdrawal amount");
        assertTrue(current.getTransactions().stream().anyMatch(t -> t.getType() == TransactionType.WITHDRAW),
                "At least one transaction of type WITHDRAW should be present");
    }

    /*
     * Deposit of a negative amount should throw IllegalArgumentException.
     */
    @Test
    @DisplayName("Negative deposit amount throws IllegalArgumentException")
    void negativeDepositThrows() 
    {
        logger.info("Verifying protection against negative deposit for account {}", accountId);
        
        assertThrows(IllegalArgumentException.class, () -> current.deposit(-50.0),
                "Depositing a negative amount should throw IllegalArgumentException");
        
        logger.info("Negative deposit correctly rejected.");
    }

    /*
     * Withdraw exceeding available balance (and overdraft, if not allowed) should throw IllegalArgumentException.
     * If overdraft is allowed, change this test to assert the expected overdraft behavior.
     */
    @Test
    @DisplayName("Withdraw with insufficient funds throws when overdraft not allowed")
    void withdrawInsufficientThrowsIfNoOverdraft() 
    {
        double largeAmount = 10_000.0;
        logger.info("Verifying protection against insufficient funds (Attempt: {}) for account {}", largeAmount, accountId);

        assertThrows(IllegalArgumentException.class, () -> current.withdraw(largeAmount),
                "Withdrawing more than available balance (and overdraft) should throw IllegalArgumentException when not allowed");
        
        logger.info("Insufficient funds correctly rejected.");
    }
}