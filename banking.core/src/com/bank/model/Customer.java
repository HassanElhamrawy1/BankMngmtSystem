/* Represents a bank customer, has id, name, and a list of Accounts. */

package com.bank.model;


/* FR-01: Create Customer */
public class Customer implements Identifiable 
{

    private String id;
    private String name;
    private String email;
    private String phoneNumber;

    public Customer(String id, String name, String email, String phoneNumber) 
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String getId() 
    {
        return id;
    }

    public String getName() 
    {
        return name;
    }

    public String getEmail() 
    {
        return email;
    }

    public String getPhoneNumber() 
    {
        return phoneNumber;
    }
}
