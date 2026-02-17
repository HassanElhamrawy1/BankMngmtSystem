/* Contains high-level business logic (e.g., create customer, transfer between accounts). */

package com.bank.service;

import com.bank.model.Customer;
import com.bank.repository.Repository;

import java.util.List;

public class BankService 
{

    private Repository<Customer> customerRepository;

    public BankService(Repository<Customer> customerRepository) 
    {
        this.customerRepository = customerRepository;
    }

    // FR-01: Create Customer
    public void createCustomer(String id, String name, String email, String phoneNumber) 
    {
        /* check duplicate id */
        if (customerRepository.findById(id) != null) 
        {
            throw new IllegalArgumentException("Customer with id " + id + " already exists.");
        }

        Customer customer = new Customer(id, name, email, phoneNumber);
        customerRepository.add(customer);
    }

    /* FR-02: View Customers */
    public List<Customer> getAllCustomers() 
    {
        return customerRepository.findAll();
    }
}
