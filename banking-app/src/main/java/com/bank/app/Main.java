/* Main class to run the application; creates customers and accounts, 
 * performs deposits/withdrawals, and displays account info.           */
package com.bank.app;

import com.bank.model.Customer;
import com.bank.model.Account;

import com.bank.repository.Repository;
import com.bank.repository.InMemoryRepository;
import com.bank.service.BankService;

import java.util.InputMismatchException;
import java.util.Scanner;


public class Main 
{
	/* Create service */
    private static BankService bankService;
    /* Create scanner */
    private static Scanner scanner;

    public static void main(String[] args) 
    {
        /* create repository */
        InMemoryRepository<Customer> customerRepo = new InMemoryRepository<>();
        InMemoryRepository<Account> accountRepo = new InMemoryRepository<>();
        
        /* initialize service */
        bankService = new BankService(customerRepo, accountRepo);
        /* initialize scanner */
        scanner = new Scanner(System.in);


        /* ---------------- FR 01 Create Customers ---------------- */
        loadSampleData();
        

        /* ---------------- Main menu loop---------------- */
        boolean running = true;
        while (running) 
        {
            displayMenu();
            int choice = getUserChoice();
            running = handleMenuChoice(choice);
        }

        scanner.close();
        System.out.println("Thank you for using Bank Management System!");
        
    }
    
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
        System.out.println("9. Exit");
        System.out.println("==========================================");
        System.out.print("Enter your choice (The NO.): ");
    }

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
            case EXIT:
                return false; /* Exit */
            case INVALID:
                System.out.println("Invalid choice! Please try again.");
                break;
        }
        return true;
    }
    
    /*----------------  FR-01: Create Customer---------------- */
    private static void createCustomer() 
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
    }

    /* ---------------- FR 02 View Customers ---------------- */
    private static void viewAllCustomers() 
    {
        System.out.println("\n--- All Customers ---");
        bankService.getAllCustomers().forEach(System.out::println);
    }
    /*----------------  FR-04: Create Account---------------- */
    private static void createAccount() 
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
    }
    /*----------------  FR-05: Transfer Money ---------------- */
    private static void deposit() 
    {
        scanner.nextLine(); 								/* clear the buffer */
        System.out.print("Enter Account ID: ");
        String accountId = scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();

        bankService.deposit(accountId, amount);
        System.out.println("✓ Deposit successful!");
    }
    /*----------------  FR-06: Withdraw Money ---------------- */
    private static void withdraw() 
    {
        scanner.nextLine(); 								/* clear the buffer */
        System.out.print("Enter Account ID: ");
        String accountId = scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();

        bankService.withdraw(accountId, amount);
        System.out.println("✓ Withdrawal successful!");
    }
    /*----------------  FR-07: Transfer Money ---------------- */
    private static void transfer() 
    {
        scanner.nextLine();    								/* clear the buffer */
        System.out.print("Enter From Account ID: ");
        String fromAccountId = scanner.nextLine();
        System.out.print("Enter To Account ID: ");
        String toAccountId = scanner.nextLine();
        System.out.print("Enter Amount: ");
        double amount = scanner.nextDouble();

        bankService.transfer(fromAccountId, toAccountId, amount);
        System.out.println("✓ Transfer successful!");
    }
    
    /*----------------  FR-08: View Account Details ---------------- */
    private static void viewAccount() 
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
    }
    
    /*----------------  FR-09: List All Accounts ---------------- */
    private static void listAccounts() 
    {
        System.out.println("\n--- All Accounts ---");
        bankService.getAllAccounts().forEach(System.out::println);
        /*for (Customer c : bankService.getAllCustomers()) 
        {
            System.out.println(c);
        }*/
    }

    private static void loadSampleData() 
    {
        /* Sample customers */
        bankService.createCustomer("C1", "Hassan", "hassan@example.com", "+49123456789");
        bankService.createCustomer("C2", "Ali", "ali@example.com", "+49198765432");
        bankService.createCustomer("C3", "Ahmed", "ahmed@example.com", "+49123456789");

        /* Sample accounts */
        bankService.createAccount("A1", "C1", "SAVINGS", 5000);
        bankService.createAccount("A2", "C2", "CURRENT", 3000);
    }
    
}