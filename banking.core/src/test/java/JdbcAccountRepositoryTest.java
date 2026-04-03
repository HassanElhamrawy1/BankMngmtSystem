/*
 * Integration tests for JdbcAccountRepository.
 * Uses an in-memory SQLite database to verify real SQL behavior.
 * No mocking — the actual JDBC implementation is tested end-to-end.
 * Implements test coverage for FR-12: Save Account Data and FR-13: Load Account Data.
 */
package com.bank.repository;

import com.bank.model.*;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JdbcAccountRepositoryTest
{
    /*
     * Testable subclass that overrides getConnection() to use an in-memory SQLite database.
     * Requires getConnection() to be declared as protected in JdbcAccountRepository.
     * This avoids modifying production code while enabling isolated integration tests.
     */
    static class JdbcAccountRepositoryTestable extends JdbcAccountRepository
    {
        private final String url;

        JdbcAccountRepositoryTestable(String url)
        {
            this.url = url;
        }

        @Override
        protected Connection getConnection() throws SQLException
        {
            return DriverManager.getConnection(url);
        }
    }
    private static final Logger logger = LoggerFactory.getLogger(JdbcAccountRepositoryTest.class);

    /*
     * In-memory SQLite connection shared across all tests in one class instance.
     * Using a named in-memory DB so the same DB is reused across multiple connections.
     */
    private static final String IN_MEMORY_URL = "jdbc:sqlite:file:testdb?mode=memory&cache=shared";

    private JdbcAccountRepository repository;
    private Connection sharedConn;

    @BeforeEach
    void setUp() throws SQLException
    {
        /*
         * Keep one connection open to prevent SQLite from dropping the in-memory DB
         * between repository calls (each call opens its own connection internally).
         */
        sharedConn = DriverManager.getConnection(IN_MEMORY_URL);
        createSchema(sharedConn);
        repository = new JdbcAccountRepositoryTestable(IN_MEMORY_URL);
    }

    @AfterEach
    void tearDown() throws SQLException
    {
        /* Drop tables to reset state between tests */
        try (Statement stmt = sharedConn.createStatement())
        {
            stmt.execute("DROP TABLE IF EXISTS transactions");
            stmt.execute("DROP TABLE IF EXISTS accounts");
        }
        sharedConn.close();
    }

    /*
     * Creates the accounts and transactions tables in the given connection.
     */
    private void createSchema(Connection conn) throws SQLException
    {
        try (Statement stmt = conn.createStatement())
        {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS accounts (
                    id          TEXT PRIMARY KEY,
                    customer_id TEXT NOT NULL,
                    type        TEXT NOT NULL,
                    balance     REAL NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id          TEXT PRIMARY KEY,
                    account_id  TEXT NOT NULL,
                    type        TEXT NOT NULL,
                    amount      REAL NOT NULL,
                    timestamp   TEXT NOT NULL,
                    description TEXT,
                    FOREIGN KEY (account_id) REFERENCES accounts(id)
                )
            """);
        }
    }

    /* ---------------- FR-12: Save Account Data ---------------- */

    @Test
    @DisplayName("Should save a SavingsAccount and retrieve it by ID")
    void testSaveSavingsAccountAndFindById()
    {
        logger.info("Testing save and findById for SavingsAccount");

        SavingsAccount account = new SavingsAccount("ACC-C00001-1", "C00001", 1000.0);
        repository.save(account);

        Account result = repository.findById("ACC-C00001-1");

        assertNotNull(result, "Account must be found after save");
        assertEquals("ACC-C00001-1", result.getId());
        assertEquals("C00001",       result.getCustomerId());
        assertEquals(1000.0,         result.getBalance(), 0.0001);
        assertInstanceOf(SavingsAccount.class, result, "Type must be SavingsAccount");
    }

    @Test
    @DisplayName("Should save a CurrentAccount and retrieve it by ID")
    void testSaveCurrentAccountAndFindById()
    {
        logger.info("Testing save and findById for CurrentAccount");

        CurrentAccount account = new CurrentAccount("ACC-C00001-2", "C00001", 500.0);
        repository.save(account);

        Account result = repository.findById("ACC-C00001-2");

        assertNotNull(result, "Account must be found after save");
        assertInstanceOf(CurrentAccount.class, result, "Type must be CurrentAccount");
        assertEquals(500.0, result.getBalance(), 0.0001);
    }

    @Test
    @DisplayName("Should replace an existing account when saved with the same ID")
    void testSaveReplacesExistingAccount()
    {
        logger.info("Testing INSERT OR REPLACE behavior for duplicate account ID");

        SavingsAccount original = new SavingsAccount("ACC-C00001-1", "C00001", 1000.0);
        repository.save(original);

        SavingsAccount updated = new SavingsAccount("ACC-C00001-1", "C00001", 2000.0);
        repository.save(updated);

        Account result = repository.findById("ACC-C00001-1");
        assertEquals(2000.0, result.getBalance(), 0.0001, "Balance must reflect the replaced record");
    }

    @Test
    @DisplayName("Should save transactions along with the account")
    void testSaveAccountWithTransactions()
    {
        logger.info("Testing that transactions are persisted when account is saved");

        SavingsAccount account = new SavingsAccount("ACC-C00001-1", "C00001", 1000.0);
        account.deposit(300.0);
        repository.save(account);

        Account result = repository.findById("ACC-C00001-1");

        assertFalse(result.getTransactions().isEmpty(), "Transactions must be loaded after save");
        assertEquals(TransactionType.DEPOSIT, result.getTransactions().get(0).getType());
    }

    /* ---------------- FR-13: Load Account Data ---------------- */

    @Test
    @DisplayName("Should return null when account ID does not exist")
    void testFindByIdReturnsNullForMissingAccount()
    {
        logger.info("Testing findById returns null for non-existent ID");

        Account result = repository.findById("ACC-INVALID");

        assertNull(result, "findById must return null when account is not found");
    }

    @Test
    @DisplayName("Should return all saved accounts from the database")
    void testFindAllReturnsAllAccounts()
    {
        logger.info("Testing findAll returns all persisted accounts");

        repository.save(new SavingsAccount("ACC-C00001-1", "C00001", 1000.0));
        repository.save(new CurrentAccount("ACC-C00001-2", "C00001", 500.0));

        List<Account> result = repository.findAll();

        assertEquals(2, result.size(), "findAll must return all saved accounts");
    }

    @Test
    @DisplayName("Should return an empty list when no accounts exist")
    void testFindAllReturnsEmptyListWhenNoAccounts()
    {
        logger.info("Testing findAll returns empty list when DB is empty");

        List<Account> result = repository.findAll();

        assertNotNull(result, "findAll must never return null");
        assertTrue(result.isEmpty(), "findAll must return empty list when no accounts exist");
    }

    @Test
    @DisplayName("Should load transactions for each account returned by findAll")
    void testFindAllLoadsTransactionsForEachAccount()
    {
        logger.info("Testing that findAll loads transactions for all accounts");

        SavingsAccount account = new SavingsAccount("ACC-C00001-1", "C00001", 1000.0);
        account.deposit(200.0);
        repository.save(account);

        List<Account> result = repository.findAll();

        assertFalse(result.get(0).getTransactions().isEmpty(), "Transactions must be loaded for each account in findAll");
    }

    /* ---------------- Delete ---------------- */

    @Test
    @DisplayName("Should remove the account from the database after delete")
    void testDeleteRemovesAccount()
    {
        logger.info("Testing delete removes account from DB");

        repository.save(new SavingsAccount("ACC-C00001-1", "C00001", 1000.0));
        repository.delete("ACC-C00001-1");

        assertNull(repository.findById("ACC-C00001-1"), "Account must not be found after deletion");
    }

    @Test
    @DisplayName("Should not throw when deleting a non-existent account ID")
    void testDeleteNonExistentAccountDoesNotThrow()
    {
        logger.info("Testing delete on non-existent ID does not throw");

        assertDoesNotThrow(() -> repository.delete("ACC-INVALID"),
            "Deleting a non-existent account must not throw an exception");
    }

    /* ---------------- findById(Connection, id) — Transactional ---------------- */

    @Test
    @DisplayName("Should find account using caller-managed connection")
    void testFindByIdWithConnectionReturnsAccount() throws SQLException
    {
        logger.info("Testing transactional findById(conn, id)");

        repository.save(new CurrentAccount("ACC-C00001-2", "C00001", 500.0));

        Account result = repository.findById(sharedConn, "ACC-C00001-2");

        assertNotNull(result, "Account must be found via caller-managed connection");
        assertEquals("ACC-C00001-2", result.getId());
    }

    @Test
    @DisplayName("Should return null via caller-managed connection when account does not exist")
    void testFindByIdWithConnectionReturnsNullForMissingAccount() throws SQLException
    {
        logger.info("Testing transactional findById(conn, id) returns null for missing account");

        Account result = repository.findById(sharedConn, "ACC-INVALID");

        assertNull(result, "findById(conn, id) must return null when account is not found");
    }

    /* ---------------- update(Connection, Account) — Transactional ---------------- */

    @Test
    @DisplayName("Should update the account balance using caller-managed connection")
    void testUpdateChangesBalance() throws SQLException
    {
        logger.info("Testing transactional update changes account balance");

        SavingsAccount account = new SavingsAccount("ACC-C00001-1", "C00001", 1000.0);
        repository.save(account);

        account.deposit(500.0);
        repository.update(sharedConn, account);

        Account result = repository.findById("ACC-C00001-1");
        assertEquals(1500.0, result.getBalance(), 0.0001, "Balance must reflect the update");
    }

    @Test
    @DisplayName("Should throw RuntimeException when updating a non-existent account")
    void testUpdateThrowsForNonExistentAccount() throws SQLException
    {
        logger.info("Testing update throws RuntimeException for non-existent account");

        SavingsAccount ghost = new SavingsAccount("ACC-GHOST", "C00099", 999.0);

        assertThrows(RuntimeException.class,
            () -> repository.update(sharedConn, ghost),
            "Updating a non-existent account must throw RuntimeException");
    }
}