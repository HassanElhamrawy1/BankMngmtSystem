/**
 * Configuration class for managing the SQLite database connection and initialization.
 * Responsible for establishing connection, creating required tables, and closing connection.
 * Implements FR-12: Initialize Database and FR-13: Load Data.
 */
package com.bank.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig 
{
	/* SQLite database URL for the bank system */
	private static final String DB_URL = "jdbc:sqlite:bank-system.db?busy_timeout=5000";
    /* Singleton database connection instance */
    private static Connection connection;

    /**
     * Returns the singleton database connection instance.
     * If no connection exists or it's closed, creates a new connection.
     * @return Active database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException 
    {
        if (connection == null || connection.isClosed()) 
        {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }
    
    /**
     * Initializes the database by creating required tables if they don't exist.
     * Creates tables for customers, accounts, and transactions with proper relationships.
     * Implements FR-12: Initialize Database and FR-13: Load Data.
     * @throws SQLException if a database access error occurs
     */
    public static void initializeDatabase() throws SQLException 
    {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) 
        {
            /* Create Customers table */
            stmt.execute("CREATE TABLE IF NOT EXISTS customers (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "email TEXT NOT NULL, " +
                    "phone TEXT NOT NULL)");

            /* Create Accounts table */
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "id TEXT PRIMARY KEY, " +
                    "customer_id TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "balance REAL NOT NULL, " +
                    "FOREIGN KEY(customer_id) REFERENCES customers(id))");

            /* Create Transactions table */
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id TEXT PRIMARY KEY, " +
                    "account_id TEXT NOT NULL, " +
                    "type TEXT NOT NULL, " +
                    "amount REAL NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "description TEXT, " +
                    "FOREIGN KEY(account_id) REFERENCES accounts(id))");

            System.out.println("Database initialized successfully!");
        }
    }

    /**
     * Closes the database connection if it's open.
     * Implements FR-15: Graceful Shutdown.
     * @throws SQLException if a database access error occurs
     */
    public static void closeConnection() throws SQLException 
    {
        if (connection != null && !connection.isClosed()) 
        {
            connection.close();
        }
    }
}