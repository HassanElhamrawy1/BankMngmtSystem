package com.bank.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Unit tests for the Account class using real-world ID patterns from the database.
 * This test covers scenarios where one customer has multiple accounts.
 */
public class AccountTest 
{
    private Account savingsAccount;
    private Account currentAccount;
    private final String customerId = "C00002";
    private static final Logger logger = LoggerFactory.getLogger(AccountTest.class);

    @BeforeEach
    void setUp() 
    {
        /* 
         * Initialize two different accounts for the same customer 
         * following the database ID pattern: ACC-CustomerID-Sequence
         */
        savingsAccount = new SavingsAccount("ACC-C00002-1", customerId, 1000.0);
        currentAccount = new CurrentAccount("ACC-C00002-2", customerId, 500.0);
    }

    @Test
    @DisplayName("Should verify that both accounts belong to the same customer")
    void testMultipleAccountsSameCustomer() 
    {
    	logger.info("Chekking if multiple accounts have the same Customer");
        /* Verify IDs match the expected database concept */
        assertEquals("C00002", savingsAccount.getCustomerId());
        assertEquals("C00002", currentAccount.getCustomerId());
        logger.info("Customer C00002 has: ", savingsAccount.getCustomerId(), currentAccount.getCustomerId());
        
        assertNotEquals(savingsAccount.getId(), currentAccount.getId(), "Account IDs must be unique");
    }

    
    @Test
    @DisplayName("Should handle deposit independently for each account")
    void testIndependentDeposits() 
    {
        logger.info("Depositing 200.0 into savingsAccount");
        /* Deposit into the first account */
        savingsAccount.deposit(200.0);
        logger.info("SavingsAccount balance after deposit: {}", savingsAccount.getBalance());

        /* Verify only the first account balance changed */
        assertEquals(1200.0, savingsAccount.getBalance());
        assertEquals(500.0, currentAccount.getBalance(), "Second account balance should remain unchanged");
    }


    @Test
    @DisplayName("Should record separate transactions for each account")
    void testSeparateTransactions() 
    {
    	logger.info("Depositing 100.0 into savingsAccount and 50.0 into currentAccount");
        savingsAccount.deposit(100.0);
        currentAccount.deposit(50.0);
        
        logger.info("Customer C00002 has: ", savingsAccount.getBalance(), savingsAccount.getBalance());
        
        /* Verify each account has its own transaction history */
        assertEquals(1, savingsAccount.getTransactions().size());
        assertEquals(1, currentAccount.getTransactions().size());
    }

    @Test
    @DisplayName("Should throw exception for negative deposit on any account")
    void testNegativeDepositValidation() 
    {
    	logger.info("Throwing exception for negative deposit on any account");
        /* Verify validation logic works across different account instances */
        assertThrows(IllegalArgumentException.class, () -> { savingsAccount.deposit(-10.0); });
        assertThrows(IllegalArgumentException.class, () -> { currentAccount.deposit(-10.0); });
    }
}