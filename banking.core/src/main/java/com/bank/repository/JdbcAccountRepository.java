/*
 * JDBC-based implementation of the Repository interface for Account entities.
 * Stores and retrieves account data (including transactions) from an SQLite database.
 * Implements FR-12: Save Account Data and FR-13: Load Account Data.
 */
package com.bank.repository;

import com.bank.model.Account;
import com.bank.model.SavingsAccount;
import com.bank.model.CurrentAccount;
import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcAccountRepository implements Repository<Account> 
{
	/* Database connection URL for SQLite */
    private static final String DB_URL = "jdbc:sqlite:bank-system.db";

    /**
     * Establishes a connection to the SQLite database.
     * @return A new database connection
     * @throws SQLException if a database access error occurs
     */
    private Connection getConnection() throws SQLException 
    {
        return DriverManager.getConnection(DB_URL);
    }
    
    /* ---------------- FR 12 Create Customers ---------------- */
    /**
     * Saves an account and its associated transactions to the database.
     * If the account ID already exists, it will be replaced.
     * @param account The account entity to save
     */
    @Override
    public void save(Account account) 
    {
        String sql = "INSERT OR REPLACE INTO accounts (id, customer_id, type, balance) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, account.getId());
            pstmt.setString(2, account.getCustomerId());
            pstmt.setString(3, account.getClass().getSimpleName());
            pstmt.setDouble(4, account.getBalance());
            pstmt.executeUpdate();

            /* Save transactions */
            saveTransactions(account);
        } catch (SQLException e) 
        {
            System.err.println("Error saving account: " + e.getMessage());
        }
    }
    
    /* ---------------- FR 12 Save Account Data ---------------- */
    /**
     * Saves all transactions associated with an account.
     * @param account The account whose transactions are to be saved
     */
    private void saveTransactions(Account account) 
    {
        String sql = "INSERT OR REPLACE INTO transactions (id, account_id, type, amount, timestamp, description) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            for (Transaction transaction : account.getTransactions()) 
            {
                pstmt.setString(1, transaction.getId());
                pstmt.setString(2, account.getId());
                pstmt.setString(3, transaction.getType().toString());
                pstmt.setDouble(4, transaction.getAmount());
                pstmt.setString(5, transaction.getTimestampAsString());
                pstmt.setString(6, transaction.getDescription());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        } catch (SQLException e) 
        {
            System.err.println("Error saving transactions: " + e.getMessage());
        }
    }
    
    /* ---------------- FR 13 Load Account Data ---------------- */
    /**
     * Finds an account by ID from the database and loads its transactions.
     * @param id The unique identifier of the account
     * @return The account if found, or null if not found
     */
    @Override
    public Account findById(String id) 
    {
        String sql = "SELECT * FROM accounts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) 
            {
                Account account = createAccount(rs);
                loadTransactions(account);
                return account;
            }
        } catch (SQLException e) 
        {
            System.err.println("Error finding account: " + e.getMessage());
        }
        return null;
    }
    
    
    /**
     * Retrieves all accounts from the database and loads their transactions.
     * @return A list of all accounts
     */
    @Override
    public List<Account> findAll() 
    {
        List<Account> accounts = new ArrayList<>();
        String sql = "SELECT * FROM accounts";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) 
        {
            while (rs.next()) 
            {
                Account account = createAccount(rs);
                loadTransactions(account);
                accounts.add(account);
            }
        } catch (SQLException e) 
        {
            System.err.println("Error loading accounts: " + e.getMessage());
        }
        return accounts;
    }
    
    /**
     * Helper method to instantiate the correct account type from a database row.
     * @param rs The ResultSet containing account data
     * @return A new Account instance (Savings or Current)
     * @throws SQLException if a database access error occurs
     */
    private Account createAccount(ResultSet rs) throws SQLException 
    {
        String id = rs.getString("id");
        String customerId = rs.getString("customer_id");
        String type = rs.getString("type");
        double balance = rs.getDouble("balance");

        if ("SavingsAccount".equals(type)) 
        {
            return new SavingsAccount(id, customerId, balance);
        } else 
        {
            return new CurrentAccount(id, customerId, balance);
        }
    }

    /**
     * Loads all transactions associated with an account from the database.
     * @param account The account to load transactions for
     */
    private void loadTransactions(Account account) 
    {
        String sql = "SELECT * FROM transactions WHERE account_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, account.getId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) 
            {
                Transaction transaction = new Transaction(
                    rs.getString("id"),
                    TransactionType.fromString(rs.getString("type")),
                    rs.getDouble("amount"),
                    rs.getString("timestamp"),
                    rs.getString("description")
                );
                account.addTransaction(transaction);
            }
        } catch (SQLException e) 
        {
            System.err.println("Error loading transactions: " + e.getMessage());
        }
    }

    /**
     * Deletes an account from the database by ID.
     * @param id The unique identifier of the account to delete
     */
    @Override
    public void delete(String id) 
    {
        String sql = "DELETE FROM accounts WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) 
        {
            System.err.println("Error deleting account: " + e.getMessage());
        }
    }
}