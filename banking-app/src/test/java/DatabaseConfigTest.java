/**
 * Test class for DatabaseConfig.
 * Covers FR-12: Initialize Database and FR-13: Load Data and FR-15: Graceful Shutdown.
 */
package com.bank.app;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DatabaseConfigTest {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfigTest.class);

    /* ------------------------------------------------------------------ */
    /* Setup & Teardown                                                     */
    /* ------------------------------------------------------------------ */

    @BeforeEach
    void setUp() throws SQLException {
        /* Ensure a fresh connection before each test */
        DatabaseConfig.closeConnection();
    }

    @AfterEach
    void tearDown() throws SQLException {
        /* Close connection after each test to avoid leaks */
        DatabaseConfig.closeConnection();
    }

    /* ------------------------------------------------------------------ */
    /* FR-12 / FR-13 — getConnection()                                     */
    /* ------------------------------------------------------------------ */

    /**
     * getConnection() returns a non-null, open connection.
     */
    @Test
    @Order(1)
    void testGetConnection_returnsOpenConnection() throws SQLException {
        logger.info("TC-DB-01: Testing getConnection() returns open connection");

        Connection conn = DatabaseConfig.getConnection();

        assertNotNull(conn, "Connection should not be null");
        assertFalse(conn.isClosed(), "Connection should be open");

        logger.info("TC-DB-01: PASSED — connection is open");
    }

    /**
     * getConnection() returns the same singleton instance on repeated calls.
     */
    @Test
    @Order(2)
    void testGetConnection_returnsSingletonInstance() throws SQLException {
        logger.info("TC-DB-02: Testing getConnection() returns singleton instance");

        Connection conn1 = DatabaseConfig.getConnection();
        Connection conn2 = DatabaseConfig.getConnection();

        assertSame(conn1, conn2, "Should return the same singleton connection");

        logger.info("TC-DB-02: PASSED — same instance returned");
    }

    /**
     * getConnection() creates a new connection after the previous one is closed.
     */
    @Test
    @Order(3)
    void testGetConnection_createsNewConnectionAfterClose() throws SQLException {
        logger.info("TC-DB-03: Testing getConnection() creates new connection after close");

        Connection conn1 = DatabaseConfig.getConnection();
        DatabaseConfig.closeConnection();

        Connection conn2 = DatabaseConfig.getConnection();

        assertNotNull(conn2, "New connection should not be null");
        assertFalse(conn2.isClosed(), "New connection should be open");

        logger.info("TC-DB-03: PASSED — new connection created after close");
    }

    /* ------------------------------------------------------------------ */
    /* FR-12 — initializeDatabase()                                        */
    /* ------------------------------------------------------------------ */

    /**
     * initializeDatabase() creates the customers table.
     */
    @Test
    @Order(4)
    void testInitializeDatabase_createsCustomersTable() throws SQLException {
        logger.info("TC-DB-04: Testing customers table creation");

        DatabaseConfig.initializeDatabase();

        assertTrue(tableExists("customers"), "customers table should exist");

        logger.info("TC-DB-04: PASSED — customers table exists");
    }

    /**
     * initializeDatabase() creates the accounts table.
     */
    @Test
    @Order(5)
    void testInitializeDatabase_createsAccountsTable() throws SQLException {
        logger.info("TC-DB-05: Testing accounts table creation");

        DatabaseConfig.initializeDatabase();

        assertTrue(tableExists("accounts"), "accounts table should exist");

        logger.info("TC-DB-05: PASSED — accounts table exists");
    }

    /**
     * initializeDatabase() creates the transactions table.
     */
    @Test
    @Order(6)
    void testInitializeDatabase_createsTransactionsTable() throws SQLException {
        logger.info("TC-DB-06: Testing transactions table creation");

        DatabaseConfig.initializeDatabase();

        assertTrue(tableExists("transactions"), "transactions table should exist");

        logger.info("TC-DB-06: PASSED — transactions table exists");
    }

    /**
     * initializeDatabase() is idempotent — calling it twice does not throw.
     */
    @Test
    @Order(7)
    void testInitializeDatabase_isIdempotent() {
        logger.info("TC-DB-07: Testing initializeDatabase() is idempotent");

        assertDoesNotThrow(() -> {
            DatabaseConfig.initializeDatabase();
            DatabaseConfig.initializeDatabase();
        }, "Calling initializeDatabase() twice should not throw");

        logger.info("TC-DB-07: PASSED — no exception on repeated initialization");
    }

    /* ------------------------------------------------------------------ */
    /* FR-15 — closeConnection()                                           */
    /* ------------------------------------------------------------------ */

    /**
     * closeConnection() closes an open connection.
     */
    @Test
    @Order(8)
    void testCloseConnection_closesOpenConnection() throws SQLException {
        logger.info("TC-DB-08: Testing closeConnection() closes open connection");

        Connection conn = DatabaseConfig.getConnection();
        assertFalse(conn.isClosed(), "Connection should be open before close");

        DatabaseConfig.closeConnection();

        assertTrue(conn.isClosed(), "Connection should be closed after closeConnection()");

        logger.info("TC-DB-08: PASSED — connection closed successfully");
    }

    /**
     * closeConnection() does not throw when called with no active connection.
     */
    @Test
    @Order(9)
    void testCloseConnection_noExceptionWhenAlreadyClosed() {
        logger.info("TC-DB-09: Testing closeConnection() when already closed");

        assertDoesNotThrow(() -> {
            DatabaseConfig.closeConnection();
            DatabaseConfig.closeConnection(); // second call — should be safe
        }, "closeConnection() should not throw when connection is already closed");

        logger.info("TC-DB-09: PASSED — no exception on double close");
    }

    /* ------------------------------------------------------------------ */
    /* Helper                                                               */
    /* ------------------------------------------------------------------ */

    /**
     * Checks whether a table with the given name exists in the database.
     */
    private boolean tableExists(String tableName) throws SQLException {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "'";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            return rs.next();
        }
    }
}