/* Implements Repository, stores data in an Database. */
package com.bank.repository;

import com.bank.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCustomerRepository implements Repository<Customer> 
{
    private static final String DB_URL = "jdbc:sqlite:bank-system.db";

    private Connection getConnection() throws SQLException 
    {
        return DriverManager.getConnection(DB_URL);
    }
    /* ---------------- FR 12 Save Customers Data ---------------- */
    @Override
    public void save(Customer customer) 
    {
        String sql = "INSERT OR REPLACE INTO customers (id, name, email, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getName());
            pstmt.setString(3, customer.getEmail());
            pstmt.setString(4, customer.getphone());
            pstmt.executeUpdate();
        } catch (SQLException e) 
        {
            System.err.println("Error saving customer: " + e.getMessage());
        }
    }

    /* ---------------- FR 13 Load Customers Data ---------------- */
    @Override
    public Customer findById(String id) 
    {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) 
            {
                return new Customer(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                );
            }
        } catch (SQLException e) 
        {
            System.err.println("Error finding customer: " + e.getMessage());
        }
        return null;
    }
    
    /* ---------------- FR 13 Load Customers Data ---------------- */
    @Override
    public List<Customer> findAll() 
    {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) 
        {
            while (rs.next()) 
            {
                customers.add(new Customer(
                    rs.getString("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone")
                ));
            }
        } catch (SQLException e) 
        {
            System.err.println("Error loading customers: " + e.getMessage());
        }
        return customers;
    }

    @Override
    public void delete(String id) 
    {
        String sql = "DELETE FROM customers WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) 
        {
            System.err.println("Error deleting customer: " + e.getMessage());
        }
    }
}