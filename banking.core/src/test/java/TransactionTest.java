/*
 * Unit tests for the Transaction model class.
 * Covers object construction, field retrieval, timestamp parsing, and string representation.
 * Implements test coverage for FR-11: Transaction History.
 */
package com.bank.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionTest
{
    private static final Logger logger = LoggerFactory.getLogger(TransactionTest.class);

    /* Reusable transaction instances for common test scenarios */
    private Transaction depositTransaction;
    private Transaction withdrawTransaction;

    @BeforeEach
    void setUp()
    {
        /*
         * Create two basic transactions that represent real-world banking operations.
         * These are used across multiple tests to avoid repetition.
         */
        depositTransaction  = new Transaction("TXN-001", TransactionType.DEPOSIT,  1500.0, "Salary deposit");
        withdrawTransaction = new Transaction("TXN-002", TransactionType.WITHDRAW,  200.0, "ATM cash withdrawal");
    }

    /* ---------------- Constructor 1: Auto Timestamp ---------------- */

    @Test
    @DisplayName("Should store all fields correctly when using the auto-timestamp constructor")
    void testAutoTimestampConstructorStoresAllFields()
    {
        logger.info("Verifying fields stored by auto-timestamp constructor for transaction: {}", depositTransaction.getId());

        assertAll("All fields must match the values passed to the constructor",
            () -> assertEquals("TXN-001",                depositTransaction.getId(),          "Transaction ID must be stored as-is"),
            () -> assertEquals(TransactionType.DEPOSIT,  depositTransaction.getType(),        "Transaction type must be DEPOSIT"),
            () -> assertEquals(1500.0,                   depositTransaction.getAmount(),      "Amount must be stored as-is"),
            () -> assertEquals("Salary deposit",         depositTransaction.getDescription(), "Description must be stored as-is")
        );
    }

    @Test
    @DisplayName("Should automatically assign a non-null timestamp close to the current time")
    void testAutoTimestampIsAssignedOnCreation()
    {
        logger.info("Verifying auto-assigned timestamp for transaction: {}", depositTransaction.getId());

        LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        LocalDateTime after  = LocalDateTime.now().plusSeconds(2);

        assertNotNull(depositTransaction.getTimestamp(), "Timestamp must not be null after construction");
        assertTrue(
            depositTransaction.getTimestamp().isAfter(before) &&
            depositTransaction.getTimestamp().isBefore(after),
            "Timestamp must be within 2 seconds of the current time"
        );
    }

    /* ---------------- Constructor 2: DB Timestamp Parsing ---------------- */

    @Test
    @DisplayName("Should parse a DB-formatted timestamp string in yyyy-MM-dd HH:mm:ss format")
    void testDbTimestampFormatIsParsedCorrectly()
    {
        logger.info("Testing DB timestamp parsing with space-separated format");

        Transaction txn = new Transaction("TXN-003", TransactionType.WITHDRAW, 300.0, "2024-01-15 10:30:00", "Rent payment");

        assertNotNull(txn.getTimestamp(), "Timestamp must be parsed and stored correctly");
        assertEquals(2024,  txn.getTimestamp().getYear(),       "Year must be 2024");
        assertEquals(1,     txn.getTimestamp().getMonthValue(), "Month must be January");
        assertEquals(15,    txn.getTimestamp().getDayOfMonth(), "Day must be 15");
        assertEquals(10,    txn.getTimestamp().getHour(),       "Hour must be 10");
        assertEquals(30,    txn.getTimestamp().getMinute(),     "Minute must be 30");
    }

    @Test
    @DisplayName("Should parse an ISO-formatted timestamp string in yyyy-MM-dd'T'HH:mm:ss format")
    void testIsoTimestampFormatIsParsedCorrectly()
    {
        logger.info("Testing ISO timestamp parsing with T-separator format");

        Transaction txn = new Transaction("TXN-004", TransactionType.DEPOSIT, 500.0, "2024-06-20T14:45:00", "Online transfer received");

        assertNotNull(txn.getTimestamp(), "Timestamp must be parsed correctly from ISO format");
        assertEquals(2024, txn.getTimestamp().getYear(),        "Year must be 2024");
        assertEquals(6,    txn.getTimestamp().getMonthValue(),  "Month must be June");
        assertEquals(20,   txn.getTimestamp().getDayOfMonth(),  "Day must be 20");
        assertEquals(14,   txn.getTimestamp().getHour(),        "Hour must be 14");
        assertEquals(45,   txn.getTimestamp().getMinute(),      "Minute must be 45");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when the timestamp string cannot be parsed")
    void testInvalidTimestampThrowsException()
    {
        /*
         * An unrecognized timestamp format must be rejected immediately.
         * This prevents silent data corruption when loading records from the DB.
         */
        logger.info("Verifying that an unrecognized timestamp format throws IllegalArgumentException");

        assertThrows(
            IllegalArgumentException.class,
            () -> new Transaction("TXN-005", TransactionType.DEPOSIT, 100.0, "15/01/2024 10:30", "Invalid format"),
            "An unrecognized timestamp format must throw IllegalArgumentException"
        );
    }

    /* ---------------- Getters ---------------- */

    @Test
    @DisplayName("Should return the correct transaction type for a withdrawal")
    void testWithdrawTransactionTypeIsCorrect()
    {
        logger.info("Verifying transaction type for: {}", withdrawTransaction.getId());

        assertEquals(TransactionType.WITHDRAW, withdrawTransaction.getType(), "Transaction type must be WITHDRAW");
    }

    @Test
    @DisplayName("Should return the correct amount for a withdrawal transaction")
    void testWithdrawAmountIsCorrect()
    {
        logger.info("Verifying amount for transaction: {}", withdrawTransaction.getId());

        assertEquals(200.0, withdrawTransaction.getAmount(), "Withdrawal amount must be 200.0");
    }

    /* ---------------- getTimestampAsString ---------------- */

    @Test
    @DisplayName("Should return the timestamp formatted as yyyy-MM-dd HH:mm:ss")
    void testGetTimestampAsStringMatchesExpectedFormat()
    {
        logger.info("Verifying timestamp string format for transaction: {}", depositTransaction.getId());

        String timestampStr = depositTransaction.getTimestampAsString();

        assertNotNull(timestampStr, "Formatted timestamp string must not be null");
        assertTrue(
            timestampStr.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"),
            "Timestamp string must follow the pattern yyyy-MM-dd HH:mm:ss"
        );
    }

    /* ---------------- toString ---------------- */

    @Test
    @DisplayName("Should include type, amount, and description in the toString output")
    void testToStringContainsAllRelevantFields()
    {
        logger.info("Verifying toString output for transaction: {}", depositTransaction.getId());

        String result = depositTransaction.toString();

        assertAll("toString must contain all relevant transaction fields",
            () -> assertTrue(result.contains("1500"),             "toString must include the amount"),
            () -> assertTrue(result.contains("Salary deposit"),   "toString must include the description"),
            () -> assertTrue(result.contains(
                    TransactionType.DEPOSIT.getDisplayName()),     "toString must include the display name of the type")
        );
    }

    /* ---------------- Edge Cases ---------------- */

    @Test
    @DisplayName("Should allow a zero-amount transaction without throwing an exception")
    void testZeroAmountTransactionIsAccepted()
    {
        /*
         * The Transaction class does not currently validate the amount.
         * This test documents the current behavior — validation should be considered in FR-03.
         */
        logger.info("Testing zero-amount transaction — documenting current behavior");

        Transaction zeroTxn = new Transaction("TXN-006", TransactionType.DEPOSIT, 0.0, "Zero amount test");

        assertEquals(0.0, zeroTxn.getAmount(), "Zero amount is currently accepted — validation should be added if needed");
    }

    @Test
    @DisplayName("Should allow a negative amount without throwing an exception")
    void testNegativeAmountTransactionIsAccepted()
    {
        /*
         * Negative amounts are not blocked at the Transaction level.
         * Business rules for rejecting negatives should be enforced in AccountService or Account.
         */
        logger.info("Testing negative-amount transaction — documenting current behavior");

        Transaction negativeTxn = new Transaction("TXN-007", TransactionType.WITHDRAW, -50.0, "Negative amount test");

        assertEquals(-50.0, negativeTxn.getAmount(), "Negative amount is currently accepted at the Transaction level");
    }
}