/*
 * Unit tests for the Customer model class.
 * Covers object creation, field retrieval, identity contract, and string representation.
 * Implements test coverage for FR-01: Create Customer and FR-03: Validate Customer Data.
 */
package com.bank.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest
{
    private static final Logger logger = LoggerFactory.getLogger(CustomerTest.class);

    /* Customer under test */
    private Customer customer;

    @BeforeEach
    void setUp()
    {
        /*
         * Create a realistic customer instance that mirrors actual database records.
         * ID follows the pattern used in the system: C + 5-digit sequence.
         */
        customer = new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678");
    }

    /* ---------------- FR-01: Create Customer ---------------- */

    @Test
    @DisplayName("Should store the customer ID exactly as provided during construction")
    void testCustomerIdIsStoredCorrectly()
    {
        logger.info("Verifying customer ID is stored correctly for: {}", customer.getId());

        assertEquals("C00001", customer.getId(), "Customer ID must match the value passed to the constructor");
    }

    @Test
    @DisplayName("Should store the full name without any modification")
    void testCustomerNameIsStoredCorrectly()
    {
        logger.info("Verifying customer name for ID: {}", customer.getId());

        assertEquals("Hassan El-Hamrawy", customer.getName(), "Customer name must be stored as-is");
    }

    @Test
    @DisplayName("Should store the email address without any modification")
    void testCustomerEmailIsStoredCorrectly()
    {
        logger.info("Verifying email for customer: {}", customer.getId());

        assertEquals("hassan@bank.com", customer.getEmail(), "Email must match the value passed to the constructor");
    }

    @Test
    @DisplayName("Should store the phone number without any modification")
    void testCustomerPhoneIsStoredCorrectly()
    {
        logger.info("Verifying phone number for customer: {}", customer.getId());

        assertEquals("01012345678", customer.getphone(), "Phone number must match the value passed to the constructor");
    }

    /* ---------------- Identifiable Contract ---------------- */

    @Test
    @DisplayName("Should return a non-null ID to satisfy the Identifiable contract")
    void testCustomerIdIsNotNull()
    {
        logger.info("Checking that customer ID is not null");

        assertNotNull(customer.getId(), "Customer ID must never be null — required by Identifiable interface");
    }

    @Test
    @DisplayName("Should have a unique ID compared to a different customer")
    void testTwoCustomersHaveDifferentIds()
    {
        logger.info("Verifying that two different customers do not share the same ID");

        Customer anotherCustomer = new Customer("C00002", "Ahmed Sayed", "ahmed@bank.com", "01098765432");

        assertNotEquals(customer.getId(), anotherCustomer.getId(), "Two distinct customers must have different IDs");
    }

    /* ---------------- toString ---------------- */

    @Test
    @DisplayName("Should include all customer fields in the toString output")
    void testToStringContainsAllFields()
    {
        logger.info("Verifying toString output for customer: {}", customer.getId());

        String result = customer.toString();

        assertAll("toString must contain all customer fields",
            () -> assertTrue(result.contains("C00001"),          "toString must include the customer ID"),
            () -> assertTrue(result.contains("Hassan El-Hamrawy"), "toString must include the customer name"),
            () -> assertTrue(result.contains("hassan@bank.com"), "toString must include the email"),
            () -> assertTrue(result.contains("01012345678"),     "toString must include the phone number")
        );
    }

    /* ---------------- Edge Cases ---------------- */

    @Test
    @DisplayName("Should allow creating a customer with an empty name without throwing an exception")
    void testCustomerWithEmptyName()
    {
        /*
         * The Customer class currently does not validate input.
         * This test documents the current behavior — validation should be added in FR-03.
         */
        logger.info("Testing customer creation with an empty name — documenting current behavior");

        Customer emptyNameCustomer = new Customer("C00003", "", "test@bank.com", "01000000000");

        assertEquals("", emptyNameCustomer.getName(), "Empty name is currently accepted — FR-03 should enforce validation");
    }

    @Test
    @DisplayName("Should allow creating a customer with null fields without throwing an exception")
    void testCustomerWithNullFields()
    {
        /*
         * Documents that the constructor does not guard against null values.
         * This is a known gap that FR-03 validation should address.
         */
        logger.info("Testing customer creation with null fields — documenting current behavior");

        Customer nullFieldCustomer = new Customer(null, null, null, null);

        assertNull(nullFieldCustomer.getId(),     "Null ID is currently accepted — should be rejected after FR-03");
        assertNull(nullFieldCustomer.getName(),   "Null name is currently accepted — should be rejected after FR-03");
        assertNull(nullFieldCustomer.getEmail(),  "Null email is currently accepted — should be rejected after FR-03");
        assertNull(nullFieldCustomer.getphone(),  "Null phone is currently accepted — should be rejected after FR-03");
    }
}