/*
 * Integration tests for JdbcCustomerRepository.
 * Uses an in-memory SQLite database to verify real SQL behavior.
 * No mocking — the actual JDBC implementation is tested end-to-end.
 * Implements test coverage for FR-12: Save Customer Data and FR-13: Load Customer Data.
 */
package com.bank.repository;

import com.bank.model.Customer;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcCustomerRepositoryTest
{
    /*
     * Testable subclass that overrides getConnection() to use an in-memory SQLite database.
     * Requires getConnection() to be declared as protected in JdbcCustomerRepository.
     * This avoids modifying production code while enabling isolated integration tests.
     */
    static class JdbcCustomerRepositoryTestable extends JdbcCustomerRepository
    {
        private final String url;

        JdbcCustomerRepositoryTestable(String url)
        {
            this.url = url;
        }

        @Override
        protected Connection getConnection() throws SQLException
        {
            return DriverManager.getConnection(url);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(JdbcCustomerRepositoryTest.class);

    /*
     * Named in-memory SQLite DB — shared across multiple connections within the same test.
     * The sharedConn keeps the DB alive between repository calls.
     */
    private static final String IN_MEMORY_URL = "jdbc:sqlite:file:testcustomerdb?mode=memory&cache=shared";

    private JdbcCustomerRepository repository;
    private Connection sharedConn;

    @BeforeEach
    void setUp() throws SQLException
    {
        sharedConn = DriverManager.getConnection(IN_MEMORY_URL);
        createSchema(sharedConn);
        repository = new JdbcCustomerRepositoryTestable(IN_MEMORY_URL);
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        try (Statement stmt = sharedConn.createStatement())
        {
            stmt.execute("DROP TABLE IF EXISTS customers");
        }
        sharedConn.close();
    }

    /*
     * Creates the customers table in the given connection.
     */
    private void createSchema(Connection conn) throws SQLException
    {
        try (Statement stmt = conn.createStatement())
        {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    id    TEXT PRIMARY KEY,
                    name  TEXT NOT NULL,
                    email TEXT NOT NULL,
                    phone TEXT NOT NULL
                )
            """);
        }
    }

    /* ---------------- FR-12: Save Customer Data ---------------- */

    @Test
    @DisplayName("Should save a customer and retrieve it by ID")
    void testSaveAndFindById()
    {
        logger.info("Testing save and findById for customer: C00001");

        Customer customer = new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678");
        repository.save(customer);

        Customer result = repository.findById("C00001");

        assertNotNull(result,                          "Customer must be found after save");
        assertEquals("C00001",           result.getId());
        assertEquals("Hassan El-Hamrawy",result.getName());
        assertEquals("hassan@bank.com",  result.getEmail());
        assertEquals("01012345678",      result.getphone());
    }

    @Test
    @DisplayName("Should replace an existing customer when saved with the same ID")
    void testSaveReplacesExistingCustomer()
    {
        logger.info("Testing INSERT OR REPLACE behavior for duplicate customer ID");

        repository.save(new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678"));
        repository.save(new Customer("C00001", "Hassan Updated",    "new@bank.com",    "01099999999"));

        Customer result = repository.findById("C00001");

        assertEquals("Hassan Updated", result.getName(),  "Name must reflect the replaced record");
        assertEquals("new@bank.com",   result.getEmail(), "Email must reflect the replaced record");
    }

    /* ---------------- FR-13: Load Customer Data ---------------- */

    @Test
    @DisplayName("Should return null when customer ID does not exist")
    void testFindByIdReturnsNullForMissingCustomer()
    {
        logger.info("Testing findById returns null for non-existent ID");

        Customer result = repository.findById("C99999");

        assertNull(result, "findById must return null when customer is not found");
    }

    @Test
    @DisplayName("Should return all saved customers from the database")
    void testFindAllReturnsAllCustomers()
    {
        logger.info("Testing findAll returns all persisted customers");

        repository.save(new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678"));
        repository.save(new Customer("C00002", "Ahmed Sayed",       "ahmed@bank.com",  "01098765432"));

        List<Customer> result = repository.findAll();

        assertEquals(2, result.size(), "findAll must return all saved customers");
    }

    @Test
    @DisplayName("Should return an empty list when no customers exist")
    void testFindAllReturnsEmptyListWhenNoCustomers()
    {
        logger.info("Testing findAll returns empty list when DB is empty");

        List<Customer> result = repository.findAll();

        assertNotNull(result,          "findAll must never return null");
        assertTrue(result.isEmpty(),   "findAll must return empty list when no customers exist");
    }

    @Test
    @DisplayName("Should return all fields correctly for each customer in findAll")
    void testFindAllReturnsCorrectFields()
    {
        logger.info("Testing findAll returns correct field values for all customers");

        repository.save(new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678"));

        List<Customer> result = repository.findAll();
        Customer c = result.get(0);

        assertAll("All fields must match",
            () -> assertEquals("C00001",            c.getId()),
            () -> assertEquals("Hassan El-Hamrawy", c.getName()),
            () -> assertEquals("hassan@bank.com",   c.getEmail()),
            () -> assertEquals("01012345678",        c.getphone())
        );
    }

    /* ---------------- Delete ---------------- */

    @Test
    @DisplayName("Should remove the customer from the database after delete")
    void testDeleteRemovesCustomer()
    {
        logger.info("Testing delete removes customer from DB");

        repository.save(new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678"));
        repository.delete("C00001");

        assertNull(repository.findById("C00001"), "Customer must not be found after deletion");
    }

    @Test
    @DisplayName("Should not throw when deleting a non-existent customer ID")
    void testDeleteNonExistentCustomerDoesNotThrow()
    {
        logger.info("Testing delete on non-existent ID does not throw");

        assertDoesNotThrow(() -> repository.delete("C99999"),
            "Deleting a non-existent customer must not throw an exception");
    }

    @Test
    @DisplayName("Should return correct count after multiple saves and one delete")
    void testFindAllAfterDeleteReturnsCorrectCount()
    {
        logger.info("Testing findAll count after save + delete sequence");

        repository.save(new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678"));
        repository.save(new Customer("C00002", "Ahmed Sayed",       "ahmed@bank.com",  "01098765432"));
        repository.delete("C00001");

        List<Customer> result = repository.findAll();

        assertEquals(1, result.size(),   "Only one customer must remain after deletion");
        assertEquals("C00002", result.get(0).getId());
    }
}