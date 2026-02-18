/* Contains high-level business logic (e.g., create customer, transfer between accounts). */

package com.bank.service;

import com.bank.model.Customer;
import com.bank.model.Account;
import com.bank.model.SavingsAccount;
import com.bank.model.CurrentAccount;
import com.bank.repository.Repository;

import java.util.List;
import java.util.regex.Pattern;

public class BankService 
{

    private Repository<Customer> customerRepository;
    private Repository<Account> accountRepository;
    private AccountService accountService;

    /* Simple regex patterns */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{8,15}$");

    
    public BankService(Repository<Customer> customerRepository, Repository<Account> accountRepository) 
    {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.accountService = new AccountService(accountRepository);
    }

    /* FR-01: Create Customer ---------------- */
    public void createCustomer(String id, String name, String email, String phoneNumber) 
    {
        /* check duplicate id */
        if (customerRepository.findById(id) != null) 
        {
            throw new IllegalArgumentException("Customer with id " + id + " already exists.");
        }
        /* Validate Email and phone number */ 
        validateEmail(email);
        validatePhone(phoneNumber);

        Customer customer = new Customer(id, name, email, phoneNumber);
        customerRepository.add(customer);
    }

    /*----------------  FR-02: View Customers ---------------- */
    public List<Customer> getAllCustomers() 
    {
        return customerRepository.findAll();
    }
    /*----------------  FR-04: Create Accounts ---------------- */
    public void createAccount(String accountId, String customerId, String type) 
    {

		/* Check account already exists */
		if (accountRepository.findById(accountId) != null) 
		{
			throw new IllegalArgumentException("Account already exists.");
		}
		
		/* Check customer exists */
		if (customerRepository.findById(customerId) == null) 
		{
			throw new IllegalArgumentException("Customer does not exist.");
		}
		
		Account account;
		
		switch (type.toUpperCase()) 
		{
			case "SAVINGS":
			account = new SavingsAccount(accountId, customerId);
			break;
			
			case "CURRENT":
			account = new CurrentAccount(accountId, customerId);
			break;
			
			default:
			throw new IllegalArgumentException("Invalid account type.");
		}
		
		accountRepository.add(account);
	}
    
    
    
 /* ---------------- FR Validate Customer data ---------------- */

    private void validateEmail(String email) 
    {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) 
        {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    private void validatePhone(String phone) 
    {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) 
        {
            throw new IllegalArgumentException("Invalid phone number format.");
        }
    }
    
}
