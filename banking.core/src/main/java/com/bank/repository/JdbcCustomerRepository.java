/*
 * JDBC-based implementation of the Repository interface for Customer entities.
 * Stores and retrieves customer data from an SQLite database.
 * Implements FR-12: Save Customers Data and FR-13: Load Customers Data.
 */
package com.bank.repository;

import com.bank.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class JdbcCustomerRepository implements CustomerRepository
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
    /* ---------------- FR 12 Save Customers Data ---------------- */
    /**
     * Saves a customer to the database. If the customer ID already exists, it will be replaced.
     * @param customer The customer entity to save
     */
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
    /**
     * Finds a customer by ID from the database.
     * @param id The unique identifier of the customer
     * @return The customer if found, or null if not found
     */
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
    /**
     * Retrieves all customers from the database.
     * @return A list of all customers
     */
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

    /**
     * Deletes a customer from the database by ID.
     * @param id The unique identifier of the customer to delete
     */
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