package com.bank.repository;
/* Implements Repository, stores data in an Database. */
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
    private static final String DB_URL = "jdbc:sqlite:bank-system.db";

    private Connection getConnection() throws SQLException 
    {
        return DriverManager.getConnection(DB_URL);
    }
    
    /* ---------------- FR 12 Create Customers ---------------- */
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