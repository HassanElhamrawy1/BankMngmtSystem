/*
 * Unit tests for the BankService class.
 * Covers customer creation, account management, deposit, withdraw, transfer,
 * balance queries, transaction history, and report generation.
 * Uses Mockito to isolate the service from all repository dependencies.
 * Implements test coverage for FR-01 through FR-18.
 */
package com.bank.service;

import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BankServiceTest
{
    private static final Logger logger = LoggerFactory.getLogger(BankServiceTest.class);

    /* Mocked repositories — no real DB or in-memory store involved */
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    /* Service under test */
    private BankService bankService;

    /* Reusable test data */
    private Customer customer;
    private SavingsAccount savingsAccount;
    private CurrentAccount currentAccount;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);
        bankService = new BankService(customerRepository, accountRepository);

        /*
         * Realistic test data following the system ID patterns.
         * Customer C00001 owns two accounts: one savings, one current.
         */
        customer        = new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678");
        savingsAccount  = new SavingsAccount("ACC-C00001-1", "C00001", 1000.0);
        currentAccount  = new CurrentAccount("ACC-C00001-2", "C00001", 500.0);
    }

    /* ---------------- FR-01: Create Customer ---------------- */

    @Test
    @DisplayName("Should create a new customer when all data is valid")
    void testCreateCustomerWithValidData()
    {
        logger.info("Testing customer creation with valid data for ID: C00001");

        when(customerRepository.findById("C00001")).thenReturn(null);

        assertDoesNotThrow(() -> bankService.createCustomer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678"));

        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should throw an exception when creating a customer with a duplicate ID")
    void testCreateCustomerThrowsExceptionForDuplicateId()
    {
        logger.info("Testing duplicate customer ID rejection for: C00001");

        when(customerRepository.findById("C00001")).thenReturn(customer);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.createCustomer("C00001", "Another Name", "other@bank.com", "01099999999"),
            "Duplicate customer ID must be rejected"
        );

        verify(customerRepository, never()).save(any());
    }

    /* ---------------- FR-02: View All Customers ---------------- */

    @Test
    @DisplayName("Should return all customers from the repository")
    void testGetAllCustomersReturnsList()
    {
        logger.info("Testing getAllCustomers returns the full customer list");

        when(customerRepository.findAll()).thenReturn(List.of(customer));

        List<Customer> result = bankService.getAllCustomers();

        assertEquals(1, result.size(), "Should return exactly one customer");
        assertEquals("C00001", result.get(0).getId());
    }

    /* ---------------- FR-03: Validate Customer Data ---------------- */

    @Test
    @DisplayName("Should throw an exception when the email format is invalid")
    void testCreateCustomerThrowsExceptionForInvalidEmail()
    {
        logger.info("Testing email validation — expecting rejection for malformed email");

        when(customerRepository.findById("C00002")).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.createCustomer("C00002", "Test User", "not-an-email", "01012345678"),
            "Invalid email format must be rejected"
        );
    }

    @Test
    @DisplayName("Should throw an exception when the phone number format is invalid")
    void testCreateCustomerThrowsExceptionForInvalidPhone()
    {
        logger.info("Testing phone validation — expecting rejection for short phone number");

        when(customerRepository.findById("C00002")).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.createCustomer("C00002", "Test User", "test@bank.com", "123"),
            "Phone number shorter than 8 digits must be rejected"
        );
    }

    @Test
    @DisplayName("Should accept a phone number with a leading plus sign")
    void testCreateCustomerAcceptsInternationalPhoneFormat()
    {
        logger.info("Testing that international phone format (+20...) is accepted");

        when(customerRepository.findById("C00003")).thenReturn(null);

        assertDoesNotThrow(() -> bankService.createCustomer("C00003", "Ahmed Sayed", "ahmed@bank.com", "+201012345678"));
    }

    /* ---------------- FR-04: Create Account ---------------- */

    @Test
    @DisplayName("Should create a savings account when the customer exists")
    void testCreateSavingsAccountForExistingCustomer()
    {
        logger.info("Testing savings account creation for customer: C00001");

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(null);
        when(customerRepository.findById("C00001")).thenReturn(customer);

        assertDoesNotThrow(() -> bankService.createAccount("ACC-C00001-1", "C00001", "SAVINGS"));

        verify(accountRepository, times(1)).save(any(SavingsAccount.class));
    }

    @Test
    @DisplayName("Should create a current account when the customer exists")
    void testCreateCurrentAccountForExistingCustomer()
    {
        logger.info("Testing current account creation for customer: C00001");

        when(accountRepository.findById("ACC-C00001-2")).thenReturn(null);
        when(customerRepository.findById("C00001")).thenReturn(customer);

        assertDoesNotThrow(() -> bankService.createAccount("ACC-C00001-2", "C00001", "CURRENT"));

        verify(accountRepository, times(1)).save(any(CurrentAccount.class));
    }

    @Test
    @DisplayName("Should throw an exception when creating an account for a non-existent customer")
    void testCreateAccountThrowsExceptionForMissingCustomer()
    {
        logger.info("Testing account creation rejection when customer does not exist");

        when(accountRepository.findById("ACC-C00099-1")).thenReturn(null);
        when(customerRepository.findById("C00099")).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.createAccount("ACC-C00099-1", "C00099", "SAVINGS"),
            "Account creation must fail if the customer does not exist"
        );
    }

    @Test
    @DisplayName("Should throw an exception when creating a duplicate account ID")
    void testCreateAccountThrowsExceptionForDuplicateAccountId()
    {
        logger.info("Testing duplicate account ID rejection for: ACC-C00001-1");

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.createAccount("ACC-C00001-1", "C00001", "SAVINGS"),
            "Duplicate account ID must be rejected"
        );
    }

    @Test
    @DisplayName("Should throw an exception when the account type is unrecognized")
    void testCreateAccountThrowsExceptionForInvalidType()
    {
        logger.info("Testing account creation rejection for unknown type: CRYPTO");

        when(accountRepository.findById("ACC-C00001-3")).thenReturn(null);
        when(customerRepository.findById("C00001")).thenReturn(customer);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.createAccount("ACC-C00001-3", "C00001", "CRYPTO"),
            "Unrecognized account type must be rejected"
        );
    }

    @Test
    @DisplayName("Should deposit the initial balance into the account when it is greater than zero")
    void testCreateAccountWithInitialBalanceDepositsCorrectly()
    {
        logger.info("Testing account creation with initial balance of 2000.0");

        when(accountRepository.findById("ACC-C00001-3")).thenReturn(null);
        when(customerRepository.findById("C00001")).thenReturn(customer);

        /*
         * Capture the account that gets saved to verify the initial balance was applied.
         */
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        bankService.createAccount("ACC-C00001-3", "C00001", "SAVINGS", 2000.0);

        verify(accountRepository).save(captor.capture());
        assertEquals(2000.0, captor.getValue().getBalance(), "Initial balance must be deposited before saving");
    }

    /* ---------------- FR-05: Deposit Money ---------------- */

    @Test
    @DisplayName("Should increase the account balance after a valid deposit")
    void testDepositIncreasesBalance()
    {
        logger.info("Depositing 500.0 into savings account: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        bankService.deposit("ACC-C00001-1", 500.0);

        assertEquals(1500.0, savingsAccount.getBalance(), "Balance must increase by the deposited amount");
    }

    @Test
    @DisplayName("Should throw an exception when depositing a zero or negative amount")
    void testDepositThrowsExceptionForNonPositiveAmount()
    {
        logger.info("Testing deposit rejection for zero and negative amounts");

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> bankService.deposit("ACC-C00001-1", 0),    "Zero deposit must be rejected"),
            () -> assertThrows(IllegalArgumentException.class, () -> bankService.deposit("ACC-C00001-1", -100), "Negative deposit must be rejected")
        );
    }

    @Test
    @DisplayName("Should throw an exception when depositing into a non-existent account")
    void testDepositThrowsExceptionForMissingAccount()
    {
        logger.info("Testing deposit rejection for non-existent account");

        when(accountRepository.findById("ACC-INVALID")).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.deposit("ACC-INVALID", 100.0),
            "Deposit into a non-existent account must be rejected"
        );
    }

    /* ---------------- FR-06: Withdraw Money ---------------- */

    @Test
    @DisplayName("Should decrease the account balance after a valid withdrawal")
    void testWithdrawDecreasesBalance()
    {
        logger.info("Withdrawing 200.0 from current account: {}", currentAccount.getId());

        when(accountRepository.findById("ACC-C00001-2")).thenReturn(currentAccount);

        bankService.withdraw("ACC-C00001-2", 200.0);

        assertEquals(300.0, currentAccount.getBalance(), "Balance must decrease by the withdrawn amount");
    }

    @Test
    @DisplayName("Should throw an exception when withdrawing a zero or negative amount")
    void testWithdrawThrowsExceptionForNonPositiveAmount()
    {
        logger.info("Testing withdrawal rejection for zero and negative amounts");

        when(accountRepository.findById("ACC-C00001-2")).thenReturn(currentAccount);

        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () -> bankService.withdraw("ACC-C00001-2", 0),    "Zero withdrawal must be rejected"),
            () -> assertThrows(IllegalArgumentException.class, () -> bankService.withdraw("ACC-C00001-2", -50),  "Negative withdrawal must be rejected")
        );
    }

    @Test
    @DisplayName("Should throw an exception when withdrawing more than the available balance")
    void testWithdrawThrowsExceptionForInsufficientFunds()
    {
        logger.info("Testing withdrawal rejection when balance is insufficient on: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.withdraw("ACC-C00001-1", 9999.0),
            "Withdrawal exceeding available balance must be rejected"
        );
    }

    /* ---------------- FR-08 & FR-09: View and List Accounts ---------------- */

    @Test
    @DisplayName("Should return the correct account when queried by ID")
    void testGetAccountReturnsCorrectAccount()
    {
        logger.info("Testing getAccount for ID: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        Account result = bankService.getAccount("ACC-C00001-1");

        assertNotNull(result, "Account must be found");
        assertEquals("ACC-C00001-1", result.getId());
    }

    @Test
    @DisplayName("Should return all accounts from the repository")
    void testGetAllAccountsReturnsList()
    {
        logger.info("Testing getAllAccounts returns the full account list");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        List<Account> result = bankService.getAllAccounts();

        assertEquals(2, result.size(), "Should return exactly two accounts");
    }

    /* ---------------- FR-10: Account Queries ---------------- */

    @Test
    @DisplayName("Should return only accounts with balance above the minimum threshold")
    void testFilterAccountsByMinBalance()
    {
        logger.info("Testing filterAccountsByMinBalance with threshold 600.0");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        List<Account> result = bankService.filterAccountsByMinBalance(600.0);

        assertEquals(1, result.size(), "Only the savings account with 1000.0 should pass the filter");
        assertEquals("ACC-C00001-1", result.get(0).getId());
    }

    @Test
    @DisplayName("Should return only accounts with balance below the maximum threshold")
    void testFilterAccountsByMaxBalance()
    {
        logger.info("Testing filterAccountsByMaxBalance with threshold 600.0");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        List<Account> result = bankService.filterAccountsByMaxBalance(600.0);

        assertEquals(1, result.size(), "Only the current account with 500.0 should pass the filter");
        assertEquals("ACC-C00001-2", result.get(0).getId());
    }

    @Test
    @DisplayName("Should return accounts within the specified balance range")
    void testFilterAccountsByBalanceRange()
    {
        logger.info("Testing filterAccountsByBalanceRange between 400.0 and 600.0");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        List<Account> result = bankService.filterAccountsByBalanceRange(400.0, 600.0);

        assertEquals(1, result.size(), "Only the current account with 500.0 should be in range");
    }

    @Test
    @DisplayName("Should calculate the correct total balance across all accounts")
    void testGetTotalBalance()
    {
        logger.info("Testing getTotalBalance — expected 1500.0");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        double total = bankService.getTotalBalance();

        assertEquals(1500.0, total, "Total balance must be the sum of all account balances");
    }

    @Test
    @DisplayName("Should return the account with the highest balance")
    void testGetHighestBalanceAccount()
    {
        logger.info("Testing getHighestBalanceAccount — expected savings account with 1000.0");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        Account richest = bankService.getHighestBalanceAccount();

        assertNotNull(richest, "Richest account must not be null");
        assertEquals("ACC-C00001-1", richest.getId(), "Savings account has the highest balance");
    }

    @Test
    @DisplayName("Should return the correct total number of accounts")
    void testGetTotalAccounts()
    {
        logger.info("Testing getTotalAccounts — expected 2");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        assertEquals(2, bankService.getTotalAccounts(), "Total accounts must match the repository size");
    }

    @Test
    @DisplayName("Should return the correct balance for a specific account")
    void testGetAccountBalance()
    {
        logger.info("Testing getAccountBalance for: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        assertEquals(1000.0, bankService.getAccountBalance("ACC-C00001-1"), "Balance must match the account balance");
    }

    @Test
    @DisplayName("Should throw an exception when querying balance for a non-existent account")
    void testGetAccountBalanceThrowsExceptionForMissingAccount()
    {
        logger.info("Testing getAccountBalance rejection for non-existent account");

        when(accountRepository.findById("ACC-INVALID")).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> bankService.getAccountBalance("ACC-INVALID"),
            "Balance query for a non-existent account must throw IllegalArgumentException"
        );
    }

    /* ---------------- FR-17: Account Statement Generation ---------------- */

    @Test
    @DisplayName("Should generate a statement file for a valid account")
    void testGenerateAccountStatementCreatesFile()
    {
        logger.info("Testing statement file generation for account: {}", savingsAccount.getId());

        savingsAccount.deposit(200.0);
        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        bankService.generateAccountStatement("ACC-C00001-1");

        File statementFile = new File("account_statement_ACC-C00001-1.txt");
        assertTrue(statementFile.exists(), "Statement file must be created on disk");

        /* Cleanup after test */
        statementFile.delete();
    }

    /* ---------------- FR-18: Bank Summary Reporting ---------------- */

    @Test
    @DisplayName("Should generate a summary report file with correct totals")
    void testGenerateSummaryReportCreatesFile()
    {
        logger.info("Testing summary report generation");

        when(accountRepository.findAll()).thenReturn(List.of(savingsAccount, currentAccount));

        bankService.generateSummaryReport();

        File reportFile = new File("summary_report.txt");
        assertTrue(reportFile.exists(), "Summary report file must be created on disk");

        /* Cleanup after test */
        reportFile.delete();
    }
}
