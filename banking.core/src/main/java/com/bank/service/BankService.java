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

    /* ---------------- FR-01: Create Customer ---------------- */
    public void createCustomer(String id, String name, String email, String phoneNumber) 
    {
        /* check duplicate id */
        if (customerRepository.findById(id) != null) 
        {
            throw new IllegalArgumentException("Customer with id " + id + " already exists.");
        }
        /*----------------  FR-03: Validate Customer Data ---------------- */
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
        createAccount(accountId, customerId, type, 0.0);
    }

    public void createAccount(String accountId, String customerId, String type, double initialBalance) 
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
        
        /* Add initial balance if provided */
        if (initialBalance > 0) 
        {
            account.deposit(initialBalance);
        }
        
        accountRepository.add(account);
    }
    
    /*----------------  FR-05: Deposit ---------------- */
    public void deposit(String accountId, double amount) 
    {
        /* Check account exists */
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        {
            throw new IllegalArgumentException("Account not found.");
        }
        
        /* Validate amount */
        if (amount <= 0) 
        {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        
        account.deposit(amount);
    }
    
    /*----------------  FR-06: Withdraw ---------------- */
    public void withdraw(String accountId, double amount) 
    {
        /* Check account exists */
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        {
            throw new IllegalArgumentException("Account not found.");
        }
        
        /* Validate amount */
        if (amount <= 0) 
        {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        
        account.withdraw(amount);
    }
    
    /*----------------  FR-07: Transfer ---------------- */
    public void transfer(String fromAccountId, String toAccountId, double amount) 
    {
        /* Check both accounts exist */
        Account fromAccount = accountRepository.findById(fromAccountId);
        Account toAccount = accountRepository.findById(toAccountId);
        
        if (fromAccount == null || toAccount == null) 
        {
            throw new IllegalArgumentException("One or both accounts not found.");
        }
        
        /* Validate amount */
        if (amount <= 0) 
        {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        
        /* Perform transfer */
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
    }
    
    /*----------------  FR-08: View Account Details ---------------- */
    public Account getAccount(String accountId) 
    {
        return accountRepository.findById(accountId);
    }
    
    /*----------------  FR-09: List All Accounts ---------------- */
    public List<Account> getAllAccounts() 
    {
        return accountRepository.findAll();
    }
    
    /*----------------  FR-10: Account Queries ---------------- */
    public double getAccountBalance(String accountId) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        {
            throw new IllegalArgumentException("Account not found.");
        }
        return account.getBalance();
    }
    
    /*----------------  FR-11: Transaction History ---------------- */
    public void printTransactionHistory(String accountId) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        {
            throw new IllegalArgumentException("Account not found.");
        }
        System.out.println("Transaction History for Account: " + accountId);
        account.getTransactions().forEach(System.out::println);
    }
    
    /* ----------------  Validate Customer data ---------------- */

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