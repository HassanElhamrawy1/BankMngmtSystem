/* Contains high-level business logic (e.g., create customer, transfer between accounts). */

package com.bank.service;

import com.bank.model.Customer;
import com.bank.model.Account;
import com.bank.model.SavingsAccount;
import com.bank.model.CurrentAccount;
import com.bank.repository.Repository;
import com.bank.model.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Collection;
import java.util.Optional;
import java.util.Comparator;
import java.util.ArrayList;
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

    /**
     * Creates a new customer with the provided details.
     * Implements FR-01: Create Customer and FR-03: Validate Customer Data.
     * @param id          The unique identifier for the customer
     * @param name        The full name of the customer
     * @param email       The email address (must be valid format)
     * @param phoneNumber The phone number (must be valid format)
     */
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
        customerRepository.save(customer);
    }

    /**
     * Retrieves a list of all customers in the system.
     * Implements FR-02: View All Customers.
     */
    public List<Customer> getAllCustomers() 
    {
        return customerRepository.findAll();
    }
    
    /**
     * Creates a new account for a customer.
     * Implements FR-04: Create Account.
     * Supports both Savings and Current account types.
     */
    public void createAccount(String accountId, String customerId, String type) 
    {
        createAccount(accountId, customerId, type, 0.0);
    }
    
    /**
     * Creates a new account for a customer with an initial balance.
     * Implements FR-04: Create Account.
     * @param accountId      The unique identifier for the new account
     * @param customerId     The ID of the customer who owns the account
     * @param type           The type of account (SAVINGS or CURRENT)
     * @param initialBalance The starting balance for the account
     */
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
        
        accountRepository.save(account);
    }
    
    /**
     * Deposits money into an account in a thread-safe manner.
     * Implements FR-05: Deposit Money and FR-14: Concurrency.
     * @param accountId The ID of the account to deposit into
     * @param amount    The positive amount to be deposited
     */
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
        
        account.getLock().lock(); 
        try 
        {
            account.deposit(amount);
            accountRepository.save(account);
        } 
        finally 
        {
            account.getLock().unlock(); 
        }
    }
    
    /**
     * Withdraws money from an account in a thread-safe manner.
     * Implements FR-06: Withdraw Money and FR-14: Concurrency.
     * @param accountId The ID of the account to withdraw from
     * @param amount    The positive amount to be withdrawn
     */
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
        
        
        account.getLock().lock();
        try 
        {
            account.withdraw(amount);
            accountRepository.save(account); 
        } finally 
        {
            account.getLock().unlock();
        }
        
    }
    
    /**
     * Transfers money between two accounts safely to prevent deadlocks.
     * Implements FR-07: Transfer Funds and FR-14: Concurrency.
     * @param fromAccountId The source account ID
     * @param toAccountId   The destination account ID
     * @param amount        The amount to transfer
     */
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

        /* the look sequence to prevent the DeadLock */
        Account first = fromAccount.getId().compareTo(toAccount.getId()) < 0 ? fromAccount : toAccount;
        Account second = first == fromAccount ? toAccount : fromAccount;

        /*----------------  FR-14: Concurrent Transaction ---------------- */
        /* Perform transfer */
        first.getLock().lock();
        second.getLock().lock();
        try {
            fromAccount.withdraw(amount);  // استخدم fromAccount
            toAccount.deposit(amount);     // استخدم toAccount
            accountRepository.save(fromAccount);  // استخدم save بدل update
            accountRepository.save(toAccount);
        } finally {
            second.getLock().unlock();
            first.getLock().unlock();
        }
    }
    
    
    /**
     * Retrieves account details by ID.
     * Implements FR-08: View Account.
     */
    public Account getAccount(String accountId) 
    {
        return accountRepository.findById(accountId);
    }
    
    /**
     * Retrieves a list of all accounts in the system.
     * Implements FR-09: List Accounts.
     */
    public List<Account> getAllAccounts() 
    {
        return accountRepository.findAll();
    }
    
    
    /* ----------------  Validate Customer data ---------------- */
    /**
     * Validate customer email format.
     * Implements FR-03: Validate Customer data.
     */
    private void validateEmail(String email) 
    {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) 
        {
            throw new IllegalArgumentException("Invalid email format.");
        }
    }

    /**
     * Validate customer phone format.
     * Implements FR-03: Validate Customer data.
     * @param phone phone number which need to be validated
     */
    private void validatePhone(String phone) 
    {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) 
        {
            throw new IllegalArgumentException("Invalid phone number format.");
        }
    }
    
    /* ---------------- FR-10: Account Queries and Reporting ---------------- */
    /**
     * Filters accounts that have a balance greater than or equal to a minimum.
     * Implements FR-10: Account Queries.
     * @param minBalance The minimum balance threshold
     * @return A list of accounts matching the criteria
     */
    public List<Account> filterAccountsByMinBalance(double minBalance) 
    {
        return accountRepository.findAll()
            .stream().filter(account -> account.getBalance() >= minBalance).toList();
    }
    
    /**
     * Filters accounts with balance less than or equal to maxBalance.
     * Implements FR-10: Account Queries.
     * @param maxBalance MaxBalance to filter the account with 
     */
    public List<Account> filterAccountsByMaxBalance(double maxBalance) 
    {
        return accountRepository.findAll()
            .stream().filter(account -> account.getBalance() <= maxBalance).toList();
    }
    
    /**
     * Filters accounts within a specific balance range.
     * Implements FR-10: Account Queries.
     * * @param minBalance MinBalance to filter the account with 
     * * @param maxBalance MaxBalance to filter the account with 
     */
    public List<Account> filterAccountsByBalanceRange(double minBalance, double maxBalance) 
    {
        return accountRepository.findAll()
            .stream().filter(account -> account.getBalance() >= minBalance && 
            			account.getBalance() <= maxBalance).toList();
    }

    /**
     * Calculates the total balance across all accounts.
     * Implements FR-10: Account Queries.
     */
    public double getTotalBalance() 
    {
        return accountRepository.findAll().stream().mapToDouble(Account::getBalance).sum();
    }
    
    /**
     * Finds the account with the highest balance.
     * Implements FR-10: Account Queries.
     */
    public Account getHighestBalanceAccount() 
    {
        return accountRepository.findAll()
            .stream().max((a1, a2) -> Double.compare(a1.getBalance(), a2.getBalance())).orElse(null);
    }
    
    /**
     * Counts the total number of accounts.
     * Implements FR-10: Account Queries.
     */
    public int getTotalAccounts() 
    {
        return accountRepository.findAll().size();
    }
    
    /**
     * Gets the current balance of an account.
     * Implements FR-10: Account Queries.
     * * @param accountId The ID of the account to get account balance
     */
    public double getAccountBalance(String accountId) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        {
            throw new IllegalArgumentException("Account not found.");
        }
        return account.getBalance();
    }
   
    

    /* ---------------- FR-11: Transaction History ---------------- */
    /**
     * Prints transaction history for all accounts.
     * Implements FR-11: Transaction History.
     */
    public void printAllTransactions() 
    {
        accountRepository.findAll().forEach(account -> {
            System.out.println("\n--- Account: " + account.getId() + " ---");
            account.getTransactions().forEach(System.out::println);
        });
    }
    
    /**
     * Prints the transaction history of a specific account.
     * Implements FR-11: Transaction History.
     * * @param accountId The ID of the account to print transaction history
     */
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
    
    /* ---------------- FR-17: Account Statement Generation ---------------- */
    /**
     * Generates a detailed transaction statement for a specific account.
     * Implements FR-17: Account Statement Generation.
     * @param accountId The ID of the account to generate the statement for
     */
    public void generateAccountStatement(String accountId) 
    {
        try 
        {
            String statement = generateAccountStatementString(accountId);
            String filename = "account_statement_" + accountId + ".txt";
            /* try with resource toprevent resource leak  it will automatically close the file */
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) 
            {
                writer.write(statement);
            }
            
            System.out.println("[✓] Account statement for " + accountId + " saved to " + filename);
        } 
        catch (IllegalArgumentException e) 
        {
            System.err.println("[✗] " + e.getMessage());
        } 
        catch (IOException e) 
        {
            System.err.println("[✗] Error saving account statement to file: " + e.getMessage());
        }
    }
    
    /**
     * Generates statements for all accounts.
     * Implements FR-17: Account Statement Generation and save the data into text file.
     */
    public void generateAllAccountsStatement() 
    {
        List<Account> accounts = accountRepository.findAll();

        if (accounts.isEmpty()) 
        {
            System.out.println("[!] No accounts found to generate statements.");
            return;
        }
        /* try with resource toprevent resource leak  it will automatically close the file */
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("all_accounts_statements.txt"))) 
        {
            writer.write("========== All Accounts Transaction Statements ==========\n\n");

            for (Account account : accounts) 
            {
                if (account != null && account.getId() != null && !account.getId().trim().isEmpty()) 
                {
                    try 
                    {
                        /* using helper method to generate the ccount statement we neaded new one as 
                         * the one above  will generate file for the account */
                        String statement = generateAccountStatementString(account.getId());
                        writer.write(statement);
                        writer.write("\n\n"); /* 2 lines as separate between accounts */
                    } 
                    catch (Exception e) 
                    {
                        writer.write("[!] Could not generate statement for Account: " + account.getId() + "\n");
                    }
                }
            }
        } 
        catch (IOException e) 
        {
            System.err.println("Error writing all accounts statements to file: " + e.getMessage());
        }
    }

    /**
     * Helper method to generate account statement for one account
     * @param accountId The ID of the account to generate the statement for
     */
    private String generateAccountStatementString(String accountId) 
    {
        Account account = accountRepository.findById(accountId);
        if (account == null) 
        {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("========== Account Statement for ").append(accountId).append(" ==========\n");
        sb.append(String.format("%-20s %-15s %-12s %-30s%n", "Date", "Type", "Amount", "Description"));
        sb.append("------------------------------------------------------------\n");

        for (Transaction tran : account.getTransactions()) 
        {
            sb.append(String.format("%-20s %-15s %-12.2f %-30s%n",
                tran.getTimestampAsString(),
                tran.getType().getDisplayName(),
                tran.getAmount(),
                tran.getDescription()
            ));
        }
        sb.append("------------------------------------------------------------\n");
        sb.append(String.format("Current Balance: %.2f%n", account.getBalance()));

        return sb.toString();
    }
    
    /* ---------------- FR-18: Bank Summary Reporting ---------------- */
    /**
     * Generates a summary report of the bank's status and save it into file.
     * Implements FR-18: Bank Summary Reporting.
     */
    public void generateSummaryReport() 
    {
        Collection<Account> accounts = accountRepository.findAll();
        double totalBalance = accounts.stream().mapToDouble(Account::getBalance).sum();
        long totalAccounts = accounts.size();

        /* try with resource toprevent resource leak  it will automatically close the file */
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("summary_report.txt"))) 
        {
            writer.write("============= Bank Summary Report =============\n");
            writer.write("Total Number of Accounts: " + totalAccounts + "\n");
            writer.write(String.format("Total Balance in Bank: %.2f%n", totalBalance));
            
            Optional<Account> richest = accounts.stream().max(Comparator.comparing(Account::getBalance));
            if (richest.isPresent()) 
            {
                writer.write(String.format("Richest Account: %s with %.2f%n", richest.get().getId(), richest.get().getBalance()));
            }
            writer.write("===============================================\n");
        } 
        catch (IOException e) 
        {
            System.err.println("Error writing summary report to file: " + e.getMessage());
        }
    }
    
    
}




