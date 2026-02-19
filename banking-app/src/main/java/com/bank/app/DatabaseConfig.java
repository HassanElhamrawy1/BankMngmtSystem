/* Configuring the database (connect, initialize and close connection) */
package com.bank.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig 
{
    private static final String DB_URL = "jdbc:sqlite:bank.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException 
    {
        if (connection == null || connection.isClosed()) 
        {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

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

    public static void closeConnection() throws SQLException 
    {
        if (connection != null && !connection.isClosed()) 
        {
            connection.close();
        }
    }
}