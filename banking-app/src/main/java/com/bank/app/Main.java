/* Main class to run the application; creates customers and accounts, 
 * performs deposits/withdrawals, and displays account info.           */
package com.bank.app;

import com.bank.model.Customer;
import com.bank.model.Account;

import com.bank.repository.Repository;
import com.bank.repository.JdbcCustomerRepository;
import com.bank.repository.JdbcAccountRepository;
import com.bank.service.BankService;

import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.List;
import java.lang.Thread;
import java.lang.InterruptedException;


public class Main 
{
	/* Create service */
    private static BankService bankService;
    /* Create scanner */
    private static Scanner scanner;

    public static void main(String[] args) 
    {
    	/* ----------------  FR-12 & FR-13: Initialize Database and Load Data  ---------------- */
    	try 
        {
            /* Initialize database */
            DatabaseConfig.initializeDatabase();

            /* Load repositories from database */
            Repository<Customer> customerRepository = new JdbcCustomerRepository();
            Repository<Account> accountRepository = new JdbcAccountRepository();

            /* Initialize services */
            bankService = new BankService(customerRepository, accountRepository);
            /* Initialize scanner */
            scanner = new Scanner(System.in);

            /* ----------------  Main menu loop  ---------------- */
            boolean running = true;
            while (running) 
            {
                displayMenu();
                int choice = getUserChoice();
                running = handleMenuChoice(choice);
            }

            scanner.close();
            /* ----------------  FR-15: Graceful Shutdown  ---------------- */
            DatabaseConfig.closeConnection();
            System.out.println("Thank you for using Bank Management System!");
	    } 
    	catch (SQLException e) 
	    {
	        System.err.println("Database error: " + e.getMessage());
	    }
    }
    
    /**
     * Displays the main menu options to the user.
     */
    private static void displayMenu() 
    {
        System.out.println("\n========== Bank Management System ==========");
        System.out.println("1. Create Customer");
        System.out.println("2. View All Customers");
        System.out.println("3. Create Account");
        System.out.println("4. Deposit");
        System.out.println("5. Withdraw");
        System.out.println("6. Transfer");
        System.out.println("7. View Account");
        System.out.println("8. List Accounts");
        System.out.println("9. List Reports");
        System.out.println("10. List Transactions");
        System.out.println("11. Generate Accounts Statement");
        System.out.println("12. Generate Summary Report");
        System.out.println("13. Exit");
        System.out.println("==========================================");
        System.out.print("Enter your choice (The NO.): ");
    }

    /**
     * Gets the user's menu choice from input.
     * @return The selected menu option number, or -1 if invalid input
     */
    private static int getUserChoice() 
    {
        try 
        {
            return scanner.nextInt();
        } catch (InputMismatchException e) 
        {
            scanner.nextLine(); 						/* clear the buffer */
            return -1;
        }
    }
    
    /**
     * Handles the user's menu choice and executes the corresponding action.
     * @param choiceCode The menu option number selected by the user
     * @return true to continue the menu loop, false to exit
     */
    private static boolean handleMenuChoice(int choiceCode) 
    {
    	MenuChoice choice = MenuChoice.fromCode(choiceCode);
        
        switch (choice) 
        {
            case CREATE_CUSTOMER:
                createCustomer();
                break;
            case VIEW_CUSTOMERS:
                viewAllCustomers();
                break;
            case CREATE_ACCOUNT:
                createAccount();
                break;
            case DEPOSIT:
                deposit();
                break;
            case WITHDRAW:
                withdraw();
                break;
            case TRANSFER:
                transfer();
                break;
            case VIEW_ACCOUNT:
                viewAccount();
                break;
            case LIST_ACCOUNTS:
                listAccounts();
                break;
            case VIEW_REPORTS:
                viewReports();
                break;
            case VIEW_TRANSACTIONS:
                viewTransactions();
                break;  
            case GENERATE_ACCOUNTS_STATEMENT:
                generateAccountStatement();
                break;
            case GENERATE_SUMMARY_REPORT:
                bankService.generateSummaryReport();
                break; 
                
            case EXIT:
                return false; /* Exit */
            case INVALID:
                System.out.println("Invalid choice! Please try again.");
                break;
        }
        return true;
    }
    
    
    /**
     * Creates a new customer with user-provided details.
     * Implements FR-01: Create Customer.
     */
    private static void createCustomer() 
    {
        try 
        {
            scanner.nextLine(); 								/* clear the buffer */
            System.out.print("Enter Customer ID: ");
            String id = scanner.nextLine();
            System.out.print("Enter Name: ");
            String name = scanner.nextLine();
            System.out.print("Enter Email: ");
            String email = scanner.nextLine();
            System.out.print("Enter Phone Number: ");
            String phone = scanner.nextLine();

            bankService.createCustomer(id, name, email, phone);
            System.out.println("✓ Customer created successfully!");
        } catch (Exception e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays all customers in the system.
     * Implements FR-02: View All Customers.
     */
    private static void viewAllCustomers() 
    {
        System.out.println("\n--- All Customers ---");
        List<Customer> customers = bankService.getAllCustomers();
        if (customers.isEmpty()) 
        {
            System.out.println("No customers found.");
        } else 
        {
            customers.forEach(System.out::println);
        }
    }

    /**
     * Creates a new account for an existing customer.
     * Implements FR-04: Create Account.
     */
    private static void createAccount() 
    {
        try 
        {
            scanner.nextLine(); 								/* clear the buffer */
            System.out.print("Enter Account ID: ");
            String accountId = scanner.nextLine();
            System.out.print("Enter Customer ID: ");
            String customerId = scanner.nextLine();
            System.out.print("Enter Account Type (SAVINGS/CURRENT): ");
            String type = scanner.nextLine().toUpperCase();
            System.out.print("Enter Initial Balance: ");
            double balance = scanner.nextDouble();

            bankService.createAccount(accountId, customerId, type, balance);
            System.out.println("✓ Account created successfully!");
        } catch (Exception e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Deposits money into an account.
     * Implements FR-05: Deposit Money.
     * Executes in a separate thread for concurrent processing (FR-14).
     */
    private static void deposit() 
    {
        try 
        {
            scanner.nextLine(); 								/* clear the buffer */
            System.out.print("Enter Account ID: ");
            String accountId = scanner.nextLine();
            System.out.print("Enter Amount: ");
            double amount = scanner.nextDouble();

            /* perform deposit in a separate thread */
            handleUserRequest("DEPOSIT", accountId, amount, null);
            System.out.println("✓ Deposit successful!");
        } catch (Exception e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
        

    /**
     * Withdraws money from an account.
     * Implements FR-06: Withdraw Money.
     * Executes in a separate thread for concurrent processing (FR-14).
     */
    private static void withdraw() 
    {
        try 
        {
            scanner.nextLine(); 								/* clear the buffer */
            System.out.print("Enter Account ID: ");
            String accountId = scanner.nextLine();
            System.out.print("Enter Amount: ");
            double amount = scanner.nextDouble();

            /* perform withdraw in a separate thread */
            handleUserRequest("WITHDRAW", accountId, amount, null);
        } 
        catch (Exception e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Transfers money between two accounts.
     * Implements FR-07: Transfer Money.
     * Executes in a separate thread for concurrent processing (FR-14).
     */
    private static void transfer() 
    {
        try 
        {
            scanner.nextLine();    								/* clear the buffer */
            System.out.print("Enter From Account ID: ");
            String fromAccountId = scanner.nextLine();
            System.out.print("Enter To Account ID: ");
            String toAccountId = scanner.nextLine();
            System.out.print("Enter Amount: ");
            double amount = scanner.nextDouble();

            /* perform withdraw in a separate thread */
            handleUserRequest("TRANSFER", fromAccountId, amount, toAccountId);
        } catch (Exception e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays detailed information about a specific account.
     * Implements FR-08: View Account Details.
     */
    private static void viewAccount() 
    {
        try 
        {
            scanner.nextLine(); 								/* clear the buffer */
            System.out.print("Enter Account ID: ");
            String accountId = scanner.nextLine();

            Account account = bankService.getAccount(accountId);
            if (account != null) 
            {
                System.out.println("\n--- Account Details ---");
                System.out.println(account);
            } 
            else 
            {
                System.out.println("Account not found!");
            }
        } catch (Exception e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }
    
    /**
     * Lists all accounts in the system.
     * Implements FR-09: List All Accounts.
     */
    private static void listAccounts() 
    {
        System.out.println("\n--- All Accounts ---");
        List<Account> accounts = bankService.getAllAccounts();
        if (accounts.isEmpty()) 
        {
            System.out.println("No accounts found.");
        } else 
        {
            accounts.forEach(System.out::println);
        }
    }

    
    /**
     * Displays transaction history for a specific account or all accounts.
     * Implements FR-11: Transaction History.
     */
    private static void viewTransactions() 
    {
        try 
        {
            System.out.print("Enter Account ID (or 'all' for all accounts): ");
            scanner.nextLine(); /* clear buffer */
            String input = scanner.nextLine();
            
            if ("all".equalsIgnoreCase(input)) 
            {
                bankService.printAllTransactions();
            } else 
            {
                bankService.printTransactionHistory(input);
            }
        } catch (Exception e) 
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Displays various account reports and filtering options.
     * Implements FR-10: Account Queries and Reporting.
     */
    private static void viewReports() 
	{
	    boolean reportingMenu = true;
	    
	    while (reportingMenu) 
	    {
	        System.out.println("\n========== Account Reports ==========");
	        System.out.println("1. Total Accounts");
	        System.out.println("2. Total Balance");
	        System.out.println("3. Highest Balance Account");
	        System.out.println("4. Filter by Minimum Balance");
	        System.out.println("5. Filter by Maximum Balance");
	        System.out.println("6. Filter by Balance Range");
	        System.out.println("7. Back to main menu");
	        System.out.print("Choose option: ");
	        
	        int choice = getUserChoice();
	        ReportChoice reportChoice = ReportChoice.fromCode(choice);
	        
	        switch (reportChoice) 
	        {
	            case TOTAL_ACCOUNTS:
	                System.out.println("Total Accounts: " + bankService.getTotalAccounts());
	                break;
	            case TOTAL_BALANCE:
	                System.out.println("Total Balance: " + bankService.getTotalBalance());
	                break;
	            case HIGHEST_BALANCE:
	                Account highest = bankService.getHighestBalanceAccount();
	                if (highest != null) 
	                {
	                    System.out.println("Highest Balance Account: " + highest.getId() + 
	                                     " (" + highest.getBalance() + ")");
	                } else 
	                {
	                    System.out.println("No accounts found.");
	                }
	                break;
	            case FILTER_MIN_BALANCE:
	                filterByMinBalance();
	                break;
	            case FILTER_MAX_BALANCE:
	                filterByMaxBalance();
	                break;
	            case FILTER_RANGE:
	                filterByRange();
	                break;
	            case BACK:
	                reportingMenu = false;
	                break;
	            case INVALID:
	                System.out.println("Invalid choice!");
	        }
	    }
	    System.out.println("=====================================");
	}

    private static void filterByMinBalance() 
    {
        System.out.print("Enter minimum balance: ");
        double minBalance = scanner.nextDouble();
        
        List<Account> filteredAccounts = bankService.filterAccountsByMinBalance(minBalance);
        
        if (filteredAccounts.isEmpty()) 
        {
            System.out.println("No accounts found with balance >= " + minBalance);
        } else 
        {
            System.out.println("\nAccounts with balance >= " + minBalance + ":");
            filteredAccounts.forEach(account -> System.out.println("  " + account.getId() + 
                                                       ": " + account.getBalance()));
        }
    }

    private static void filterByMaxBalance() 
    {
        System.out.print("Enter maximum balance: ");
        double maxBalance = scanner.nextDouble();
        
        List<Account> filtered = bankService.filterAccountsByMaxBalance(maxBalance);
        
        if (filtered.isEmpty()) 
        {
            System.out.println("No accounts found with balance <= " + maxBalance);
        } else 
        {
            System.out.println("\nAccounts with balance <= " + maxBalance + ":");
            filtered.forEach(account -> System.out.println("  " + account.getId() + 
                                                       ": " + account.getBalance()));
        }
    }

    private static void filterByRange() 
    {
        System.out.print("Enter minimum balance: ");
        double minBalance = scanner.nextDouble();
        System.out.print("Enter maximum balance: ");
        double maxBalance = scanner.nextDouble();
        
        List<Account> filteredAccounts = bankService.filterAccountsByBalanceRange(minBalance, maxBalance);
        
        if (filteredAccounts.isEmpty()) 
        {
            System.out.println("No accounts found in range [" + minBalance + ", " + maxBalance + "]");
        } else 
        {
            System.out.println("\nAccounts with balance in range [" + minBalance + ", " + maxBalance + "]:");
            filteredAccounts.forEach(account -> System.out.println("  " + account.getId() + 
                                                       ": " + account.getBalance()));
        }
    }
    
    
    /*----------------  FR-14: Concurrent Transaction ---------------- */
    /**
     * Handles user requests by executing them in a separate thread.
     * Implement FR-14 This enables concurrent processing of transactions.
     * @param operation The type of operation (DEPOSIT, WITHDRAW, or TRANSFER)
     * @param accountId The account involved in the operation
     * @param amount The amount to process
     * @param toAccountId The destination account ID (used only for transfers)
     */
    public static void handleUserRequest(String operation, String accountId, double amount, String toAccountId) throws InterruptedException
    {
    	Thread handelThread = new Thread(() -> {
            try {
                switch (operation.toUpperCase()) 
                {
                    case "DEPOSIT":
                        bankService.deposit(accountId, amount);
                        System.out.println("\n[✓] Deposit of " + amount + " to " + accountId + " completed.");
                        break;
                    case "WITHDRAW":
                        bankService.withdraw(accountId, amount);
                        System.out.println("\n[✓] Withdrawal of " + amount + " from " + accountId + " completed.");
                        break;
                    case "TRANSFER":
                        bankService.transfer(accountId, toAccountId, amount);
                        System.out.println("\n[✓] Transfer of " + amount + " from " + accountId + " to " + toAccountId + " completed.");
                        break;
                    default:
                        System.out.println("[!] Unknown operation: " + operation);
                }
            } 
            catch (Exception e) 
            {
                System.err.println("\n[✗] Operation failed: " + e.getMessage());
            }
        });
    	handelThread.start();
        handelThread.join();
    }
    
    /**
     * Generates a detailed statement for a specific account.
     * Implements FR-17: Account Statement Generation.
     */
    public static void generateAccountStatement()
    {
    	scanner.nextLine();
    	System.out.print("Enter Account ID (or 'all' for all accounts): ");
        String accId = scanner.nextLine().trim();
        if ("all".equalsIgnoreCase(accId)) 
        {
            bankService.generateAllAccountsStatement();
        } else 
        {
            bankService.generateAccountStatement(accId);
        }
    }
    
}