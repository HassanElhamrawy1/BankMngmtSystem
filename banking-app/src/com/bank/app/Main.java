/* Main class to run the application; creates customers and accounts, 
 * performs deposits/withdrawals, and displays account info.           */
package com.bank.app;

import com.bank.model.Customer;
import com.bank.repository.InMemoryRepository;
import com.bank.service.BankService;

public class Main 
{

    public static void main(String[] args) 
    {
        /* Create repository */
        InMemoryRepository<Customer> customerRepo = new InMemoryRepository<>();

        /* Create service */
        BankService bankService = new BankService(customerRepo);

        /* Create Customers */
        bankService.createCustomer("C1", "Hassan", "hassan@example.com", "+49123456789");
        bankService.createCustomer("C2", "Ali", "ali@example.com", "+49198765432");

        /* View Customers */
        System.out.println("All Customers:");
        for (Customer c : bankService.getAllCustomers()) 
        {
            System.out.println(c);
        }

        /* Test duplicate */
        try 
        {
            bankService.createCustomer("C1", "Mohamed", "mohamed@example.com", "+49111111111");
        } catch (IllegalArgumentException e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }
}