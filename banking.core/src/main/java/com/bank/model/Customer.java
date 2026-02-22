/*
 * Represents a bank customer with personal and contact information.
 * Implements Identifiable to provide a unique identifier for repository operations.
 * Implements FR-01: Create Customer and FR-03: Validate Customer Data.
 */
package com.bank.model;


/*---------------- FR-01: Create Customer ---------------- */
public class Customer implements Identifiable 
{
	/* Unique identifier for the customer */
    private String id;
    /* Full name of the customer */
    private String name;
    /* Email address of the customer */
    private String email;
    /* Phone number of the customer */
    private String phoneNumber;

    /**
     * Constructs a Customer with the specified details.
     * @param id          The unique identifier
     * @param name        The full name
     * @param email       The email address
     * @param phoneNumber The phone number
     */
    public Customer(String id, String name, String email, String phoneNumber) 
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the unique identifier of the customer.
     * @return The customer ID
     */
    @Override
    public String getId() 
    {
        return id;
    }
    
    /**
     * Returns a string representation of the customer.
     * @return Formatted string with customer details
     */
    @Override
    public String toString() 
    {
        return "Customer{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }

    /* Getters  APIs */
    public String getName() 
    {
        return name;
    }

    public String getEmail() 
    {
        return email;
    }

    public String getphone() 
    {
        return phoneNumber;
    }
}
