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

public class JdbcAccountRepository implements AccountRepository 
{
	/* Database connection URL for SQLite */
	/* Add a timeout so SQLite waits instead of locking immediately */
	private static final String DB_URL = "jdbc:sqlite:bank-system.db?busy_timeout=5000";

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
    
    /**
     * Find account by id using the provided Connection.
     * This method participates in the caller-managed transaction: it does NOT commit or rollback the connection.
     *
     * @param conn The JDBC connection to use (must not be null)
     * @param id   The account id to search for
     * @return The Account instance if found, or null if not found
     * @throws RuntimeException wrapping SQLException on DB error
     */
    @Override
    public Account findById(Connection conn, String id) 
    {
        final String sql = "SELECT id, customer_id, balance, type FROM accounts WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) 
        {
            ps.setString(1, id);

            try (ResultSet rs = ps.executeQuery()) 
            {
                if (!rs.next()) {
                    return null;
                }

                String accId = rs.getString("id");
                String customerId = rs.getString("customer_id");
                double balance = rs.getDouble("balance");

                String type = null;
                try 
                {
                    type = rs.getString("type");
                } 
                catch (SQLException ignore) 
                {
                    /* If column not present, keep type null and fall back to default account type */
                }

                Account account;
                if (type != null && "CURRENT".equalsIgnoreCase(type.trim())) {
                    account = new CurrentAccount(accId, customerId, balance);
                } 
                else 
                {
                    /* Default to SavingsAccount when type is unknown or missing */
                    account = new SavingsAccount(accId, customerId, balance);
                }

                return account;
            }
        } catch (SQLException e) 
        {
            throw new RuntimeException("Error finding account by id: " + id, e);
        }
    }

    /**
     * Update persistent fields of the given account using the provided Connection.
     * This update participates in the caller-managed transaction and must not commit/rollback the connection.
     *
     * @param conn    The JDBC connection to use (must not be null)
     * @param account The account to persist (must not be null)
     * @throws RuntimeException if a database error occurs or unexpected number of rows updated
     */
    @Override
    public void update(Connection conn, Account account) 
    {
        final String sql = "UPDATE accounts SET balance = ? WHERE id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) 
        {
            ps.setDouble(1, account.getBalance());
            ps.setString(2, account.getId());

            int rows = ps.executeUpdate();
            if (rows != 1) 
            {
                throw new RuntimeException("Update affected " + rows + " rows for account id=" + account.getId());
            }
        } 
        catch (SQLException e) 
        {
            throw new RuntimeException("Error updating account id: " + account.getId(), e);
        }
    }
}