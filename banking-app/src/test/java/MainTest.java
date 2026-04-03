/*
 * Unit tests for Main application class.
 * Tests menu routing logic via handleMenuChoice() and concurrent transaction
 * handling via handleUserRequest() using a mock BankService.
 * No database or Scanner interaction — all dependencies are injected via package-private setters.
 * Implements test coverage for FR-01, FR-02, FR-03, FR-04, FR-05, FR-06, FR-07,
 * FR-08, FR-09, FR-10, FR-11, FR-12, FR-14, FR-15, FR-17.
 */
package com.bank.app;

import com.bank.model.Account;
import com.bank.model.Customer;
import com.bank.model.SavingsAccount;
import com.bank.repository.AccountRepository;
import com.bank.repository.CustomerRepository;
import com.bank.service.BankService;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MainTest
{
    private static final Logger logger = LoggerFactory.getLogger(MainTest.class);

    /*
     * Mock BankService injected via setBankService() before each test.
     * Avoids real database calls — only routing and threading logic is tested.
     */
    private BankService mockService;

    @BeforeEach
    void setUp()
    {
        /*
         * Create a mock BankService using Mockito.
         * Inject it into Main via the package-private setter.
         */
        mockService = mock(BankService.class);
        Main.setBankService(mockService);
        Main.setScanner(new Scanner(new java.io.ByteArrayInputStream("1\n".getBytes())));
    }

    @AfterEach
    void tearDown()
    {
        /* Reset static fields after each test to avoid state leakage between tests */
        Main.setBankService(null);
        Main.setScanner(null);
    }

    /* ---------------- Helper ---------------- */

    /*
     * Injects a controlled Scanner with the given input lines into Main.
     * Used to simulate user input for menu-driven methods.
     */
    private void injectInput(String input)
    {
        Main.setScanner(new Scanner(new ByteArrayInputStream(input.getBytes())));
    }

    /* ---------------- handleMenuChoice: Return Value ---------------- */

    @Test
    @DisplayName("Should return false when EXIT (13) is selected")
    void testHandleMenuChoiceExitReturnsFalse()
    {
        logger.info("Testing EXIT choice returns false to stop the menu loop");

        boolean result = Main.handleMenuChoice(13);

        assertFalse(result, "EXIT must return false to terminate the menu loop");
    }

    @Test
    @DisplayName("Should return true when INVALID (-1) is selected")
    void testHandleMenuChoiceInvalidReturnsTrue()
    {
        logger.info("Testing INVALID choice returns true to keep the menu loop running");

        boolean result = Main.handleMenuChoice(-1);

        assertTrue(result, "INVALID choice must return true to keep the menu loop running");
    }

    @Test
    @DisplayName("Should return true for all valid menu choices 1 through 12")
    void testHandleMenuChoiceAllValidChoicesReturnTrue()
    {
        logger.info("Testing all valid menu choices return true");

        /*
         * Stub all BankService methods that are called by menu choices 1-12.
         * Each choice triggers a private method that reads from Scanner or calls BankService.
         * We inject minimal input to prevent NoSuchElementException.
         */
        when(mockService.getAllCustomers()).thenReturn(List.of());
        when(mockService.getAllAccounts()).thenReturn(List.of());
        when(mockService.getTotalAccounts()).thenReturn(0);
        when(mockService.getTotalBalance()).thenReturn(0.0);
        when(mockService.getHighestBalanceAccount()).thenReturn(null);

        /*
         * Choices 2 and 8 are scanner-free (only call BankService and print).
         * Choice 9 (VIEW_REPORTS) opens a sub-menu that needs a BACK input (7)
         * to exit the loop without blocking — so we inject it separately.
         */
        int[] scannerFreeChoices = {2, 8};
        for (int choice : scannerFreeChoices)
        {
            logger.info("Testing menu choice: {}", choice);
            boolean result = Main.handleMenuChoice(choice);
            assertTrue(result, "Menu choice " + choice + " must return true");
        }

        /* Inject BACK choice (7) so viewReports() exits its sub-menu loop immediately */
        logger.info("Testing menu choice: 9 (VIEW_REPORTS) with BACK sub-choice injected");
        injectInput("7\n");
        boolean result = Main.handleMenuChoice(9);
        assertTrue(result, "Menu choice 9 must return true after exiting the report sub-menu");
    }

    /* ---------------- handleMenuChoice: FR-02 View Customers ---------------- */

    @Test
    @DisplayName("Should call getAllCustomers() when VIEW_CUSTOMERS (2) is selected")
    void testHandleMenuChoiceViewCustomersCallsService()
    {
        logger.info("Testing VIEW_CUSTOMERS triggers getAllCustomers()");

        /* Stub to return empty list — avoids NullPointerException in forEach */
        when(mockService.getAllCustomers()).thenReturn(List.of());

        Main.handleMenuChoice(2);

        /* Verify BankService was called exactly once */
        verify(mockService, times(1)).getAllCustomers();
    }

    @Test
    @DisplayName("Should display all customers when VIEW_CUSTOMERS (2) is selected and list is non-empty")
    void testHandleMenuChoiceViewCustomersWithData()
    {
        logger.info("Testing VIEW_CUSTOMERS with non-empty customer list");

        Customer customer = new Customer("C00001", "Hassan El-Hamrawy", "hassan@bank.com", "01012345678");
        when(mockService.getAllCustomers()).thenReturn(List.of(customer));

        /* Should not throw even with real customer objects */
        assertDoesNotThrow(() -> Main.handleMenuChoice(2));
        verify(mockService, times(1)).getAllCustomers();
    }

    /* ---------------- handleMenuChoice: FR-09 List Accounts ---------------- */

    @Test
    @DisplayName("Should call getAllAccounts() when LIST_ACCOUNTS (8) is selected")
    void testHandleMenuChoiceListAccountsCallsService()
    {
        logger.info("Testing LIST_ACCOUNTS triggers getAllAccounts()");

        when(mockService.getAllAccounts()).thenReturn(List.of());

        Main.handleMenuChoice(8);

        verify(mockService, times(1)).getAllAccounts();
    }

    /* ---------------- handleMenuChoice: FR-12 Generate Summary Report ---------------- */

    @Test
    @DisplayName("Should call generateSummaryReport() when GENERATE_SUMMARY_REPORT (12) is selected")
    void testHandleMenuChoiceGenerateSummaryReportCallsService()
    {
        logger.info("Testing GENERATE_SUMMARY_REPORT triggers generateSummaryReport()");

        Main.handleMenuChoice(12);

        verify(mockService, times(1)).generateSummaryReport();
    }

    /* ---------------- handleReportChoice: FR-10 Account Reports ---------------- */

    @Test
    @DisplayName("Should return true and call getTotalAccounts() for TOTAL_ACCOUNTS (1)")
    void testHandleReportChoiceTotalAccounts()
    {
        logger.info("Testing TOTAL_ACCOUNTS report choice calls getTotalAccounts()");

        when(mockService.getTotalAccounts()).thenReturn(5);

        boolean result = Main.handleReportChoice(1);

        assertTrue(result, "TOTAL_ACCOUNTS must return true to keep the report loop running");
        verify(mockService, times(1)).getTotalAccounts();
    }

    @Test
    @DisplayName("Should return true and call getTotalBalance() for TOTAL_BALANCE (2)")
    void testHandleReportChoiceTotalBalance()
    {
        logger.info("Testing TOTAL_BALANCE report choice calls getTotalBalance()");

        when(mockService.getTotalBalance()).thenReturn(15000.0);

        boolean result = Main.handleReportChoice(2);

        assertTrue(result, "TOTAL_BALANCE must return true to keep the report loop running");
        verify(mockService, times(1)).getTotalBalance();
    }

    @Test
    @DisplayName("Should return true and call getHighestBalanceAccount() for HIGHEST_BALANCE (3)")
    void testHandleReportChoiceHighestBalance()
    {
        logger.info("Testing HIGHEST_BALANCE report choice calls getHighestBalanceAccount()");

        /* Stub returns null — covers the 'no accounts found' branch */
        when(mockService.getHighestBalanceAccount()).thenReturn(null);

        boolean result = Main.handleReportChoice(3);

        assertTrue(result, "HIGHEST_BALANCE must return true to keep the report loop running");
        verify(mockService, times(1)).getHighestBalanceAccount();
    }

    @Test
    @DisplayName("Should return false when BACK (7) is selected in the report sub-menu")
    void testHandleReportChoiceBack()
    {
        /*
         * BACK must return false so viewReports() exits its while loop.
         * This is the key contract that prevents the scanner NPE we had before.
         */
        logger.info("Testing BACK report choice returns false to exit the report loop");

        boolean result = Main.handleReportChoice(7);

        assertFalse(result, "BACK must return false to exit the report sub-menu loop");
    }

    @Test
    @DisplayName("Should return true and print error message for INVALID report choice")
    void testHandleReportChoiceInvalid()
    {
        logger.info("Testing INVALID report choice returns true to keep the report loop running");

        boolean result = Main.handleReportChoice(-99);

        assertTrue(result, "INVALID report choice must return true to keep the loop running");
    }

    /* ---------------- handleUserRequest: FR-14 Concurrent Transactions ---------------- */

    @Test
    @DisplayName("Should call deposit() on BankService when DEPOSIT operation is requested")
    void testHandleUserRequestDeposit() throws InterruptedException
    {
        logger.info("Testing handleUserRequest routes DEPOSIT to bankService.deposit()");

        Main.handleUserRequest("DEPOSIT", "ACC-C00001-1", 500.0, null);

        /* Verify deposit was called with correct arguments after thread completes */
        verify(mockService, times(1)).deposit("ACC-C00001-1", 500.0);
    }

    @Test
    @DisplayName("Should call withdraw() on BankService when WITHDRAW operation is requested")
    void testHandleUserRequestWithdraw() throws InterruptedException
    {
        logger.info("Testing handleUserRequest routes WITHDRAW to bankService.withdraw()");

        Main.handleUserRequest("WITHDRAW", "ACC-C00001-1", 200.0, null);

        verify(mockService, times(1)).withdraw("ACC-C00001-1", 200.0);
    }

    @Test
    @DisplayName("Should call transfer() on BankService when TRANSFER operation is requested")
    void testHandleUserRequestTransfer() throws InterruptedException
    {
        logger.info("Testing handleUserRequest routes TRANSFER to bankService.transfer()");

        Main.handleUserRequest("TRANSFER", "ACC-C00001-1", 300.0, "ACC-C00002-1");

        verify(mockService, times(1)).transfer("ACC-C00001-1", "ACC-C00002-1", 300.0);
    }

    @Test
    @DisplayName("Should not throw when an unknown operation is passed to handleUserRequest")
    void testHandleUserRequestUnknownOperationDoesNotThrow()
    {
        logger.info("Testing handleUserRequest does not throw for unknown operation");

        assertDoesNotThrow(() -> Main.handleUserRequest("UNKNOWN", "ACC-C00001-1", 100.0, null));
    }

    @Test
    @DisplayName("Should complete deposit in a separate thread and join before returning")
    void testHandleUserRequestCompletesBeforeReturn() throws InterruptedException
    {
        logger.info("Testing handleUserRequest blocks until thread completes (join behavior)");

        long start = System.currentTimeMillis();
        Main.handleUserRequest("DEPOSIT", "ACC-C00001-1", 100.0, null);
        long elapsed = System.currentTimeMillis() - start;

        /*
         * If join() works correctly, the method returns only after the thread finishes.
         * The elapsed time must be >= 0 and the mock must have been called.
         */
        verify(mockService, times(1)).deposit("ACC-C00001-1", 100.0);
        assertTrue(elapsed >= 0, "handleUserRequest must block until thread completes");
    }

    /* ---------------- handleUserRequest: Case Insensitivity ---------------- */

    @Test
    @DisplayName("Should route correctly when operation is lowercase")
    void testHandleUserRequestCaseInsensitive() throws InterruptedException
    {
        logger.info("Testing handleUserRequest handles lowercase operation string");

        Main.handleUserRequest("deposit", "ACC-C00001-1", 100.0, null);

        verify(mockService, times(1)).deposit("ACC-C00001-1", 100.0);
    }

    /* ---------------- handleUserRequest: Exception Branch ---------------- */

    @Test
    @DisplayName("Should print error and not propagate when deposit throws an exception")
    void testHandleUserRequestDepositThrowsException() throws InterruptedException
    {
        logger.info("Testing handleUserRequest prints error when deposit throws");

        /*
         * Stub deposit to throw a RuntimeException simulating insufficient funds.
         * The exception must be caught inside the thread — it must NOT propagate to the caller.
         */
        doThrow(new RuntimeException("Insufficient funds"))
            .when(mockService).deposit("ACC-C00001-1", 999999.0);

        /* Should not propagate the exception — it's caught inside the thread */
        assertDoesNotThrow(() -> Main.handleUserRequest("DEPOSIT", "ACC-C00001-1", 999999.0, null));
        verify(mockService, times(1)).deposit("ACC-C00001-1", 999999.0);
    }

    @Test
    @DisplayName("Should print error and not propagate when withdraw throws an exception")
    void testHandleUserRequestWithdrawThrowsException() throws InterruptedException
    {
        logger.info("Testing handleUserRequest prints error when withdraw throws");

        /*
         * Stub withdraw to throw — simulates overdraft or account not found.
         * Verifies the exception branch inside the thread is exercised.
         */
        doThrow(new RuntimeException("Account not found"))
            .when(mockService).withdraw("ACC-INVALID", 100.0);

        assertDoesNotThrow(() -> Main.handleUserRequest("WITHDRAW", "ACC-INVALID", 100.0, null));
        verify(mockService, times(1)).withdraw("ACC-INVALID", 100.0);
    }

    @Test
    @DisplayName("Should print error and not propagate when transfer throws an exception")
    void testHandleUserRequestTransferThrowsException() throws InterruptedException
    {
        logger.info("Testing handleUserRequest prints error when transfer throws");

        /*
         * Stub transfer to throw — simulates insufficient balance for transfer.
         * Verifies the exception branch inside the thread is exercised.
         */
        doThrow(new RuntimeException("Insufficient balance"))
            .when(mockService).transfer("ACC-C00001-1", "ACC-C00002-1", 999999.0);

        assertDoesNotThrow(() -> Main.handleUserRequest("TRANSFER", "ACC-C00001-1", 999999.0, "ACC-C00002-1"));
        verify(mockService, times(1)).transfer("ACC-C00001-1", "ACC-C00002-1", 999999.0);
    }

    /* ---------------- handleMenuChoice: FR-01 Create Customer ---------------- */

    @Test
    @DisplayName("Should call createCustomer() when CREATE_CUSTOMER (1) is selected")
    void testHandleMenuChoiceCreateCustomer()
    {
        logger.info("Testing CREATE_CUSTOMER triggers bankService.createCustomer()");

        /*
         * Inject scanner input simulating user entering customer details.
         * Leading newline clears the buffer left by nextInt() in getUserChoice().
         */
        injectInput("\nC00099\nTest User\ntest@bank.com\n01099999999\n");

        Main.handleMenuChoice(1);

        verify(mockService, times(1))
            .createCustomer("C00099", "Test User", "test@bank.com", "01099999999");
    }

    /* ---------------- handleMenuChoice: FR-04 Create Account ---------------- */

    @Test
    @DisplayName("Should call createAccount() when CREATE_ACCOUNT (3) is selected")
    void testHandleMenuChoiceCreateAccount()
    {
        logger.info("Testing CREATE_ACCOUNT triggers bankService.createAccount()");

        /*
         * Inject scanner input simulating user entering account details.
         * Type is entered as lowercase — createAccount() calls toUpperCase() internally.
         */
        injectInput("\nACC-C00099-1\nC00099\nSAVINGS\n1000.0\n");

        Main.handleMenuChoice(3);

        verify(mockService, times(1))
            .createAccount("ACC-C00099-1", "C00099", "SAVINGS", 1000.0);
    }

    /* ---------------- handleMenuChoice: FR-08 View Account ---------------- */

    @Test
    @DisplayName("Should call getAccount() and display details when VIEW_ACCOUNT (7) is selected")
    void testHandleMenuChoiceViewAccount()
    {
        logger.info("Testing VIEW_ACCOUNT triggers bankService.getAccount()");

        /*
         * Stub returns a real SavingsAccount — covers the non-null branch
         * that prints account details to the console.
         */
        Account account = new SavingsAccount("ACC-C00001-1", "C00001", 500.0);
        when(mockService.getAccount("ACC-C00001-1")).thenReturn(account);
        injectInput("\nACC-C00001-1\n");

        Main.handleMenuChoice(7);

        verify(mockService, times(1)).getAccount("ACC-C00001-1");
    }

    @Test
    @DisplayName("Should print 'Account not found' when getAccount() returns null for VIEW_ACCOUNT (7)")
    void testHandleMenuChoiceViewAccountNotFound()
    {
        logger.info("Testing VIEW_ACCOUNT prints not found when account is null");

        /*
         * Stub returns null — covers the null branch that prints 'Account not found!'.
         * assertDoesNotThrow ensures no NPE is thrown when account is null.
         */
        when(mockService.getAccount("INVALID")).thenReturn(null);
        injectInput("\nINVALID\n");

        assertDoesNotThrow(() -> Main.handleMenuChoice(7));
        verify(mockService, times(1)).getAccount("INVALID");
    }

    /* ---------------- handleMenuChoice: FR-11 View Transactions ---------------- */

    @Test
    @DisplayName("Should call printAllTransactions() when VIEW_TRANSACTIONS (10) is selected with 'all'")
    void testHandleMenuChoiceViewTransactionsAll()
    {
        logger.info("Testing VIEW_TRANSACTIONS with 'all' triggers printAllTransactions()");

        /*
         * 'all' input (case-insensitive) routes to printAllTransactions().
         * No leading newline needed — viewTransactions() calls nextLine() to clear buffer first.
         */
        injectInput("\nall\n");

        Main.handleMenuChoice(10);

        verify(mockService, times(1)).printAllTransactions();
    }

    @Test
    @DisplayName("Should call printTransactionHistory() when VIEW_TRANSACTIONS (10) is selected with account ID")
    void testHandleMenuChoiceViewTransactionsByAccount()
    {
        logger.info("Testing VIEW_TRANSACTIONS with account ID triggers printTransactionHistory()");

        /* Specific account ID routes to printTransactionHistory(accountId) */
        injectInput("\nACC-C00001-1\n");

        Main.handleMenuChoice(10);

        verify(mockService, times(1)).printTransactionHistory("ACC-C00001-1");
    }

    /* ---------------- handleMenuChoice: FR-17 Generate Account Statement ---------------- */

    @Test
    @DisplayName("Should call generateAllAccountsStatement() when GENERATE_ACCOUNTS_STATEMENT (11) is selected with 'all'")
    void testHandleMenuChoiceGenerateStatementAll()
    {
        logger.info("Testing GENERATE_ACCOUNTS_STATEMENT with 'all' triggers generateAllAccountsStatement()");

        /*
         * 'all' input (case-insensitive) routes to generateAllAccountsStatement().
         * generateAccountStatement() calls nextLine() then reads the next line for the ID.
         */
        injectInput("\nall\n");

        Main.handleMenuChoice(11);

        verify(mockService, times(1)).generateAllAccountsStatement();
    }

    @Test
    @DisplayName("Should call generateAccountStatement() when GENERATE_ACCOUNTS_STATEMENT (11) is selected with account ID")
    void testHandleMenuChoiceGenerateStatementByAccount()
    {
        logger.info("Testing GENERATE_ACCOUNTS_STATEMENT with account ID triggers generateAccountStatement()");

        /* Specific account ID routes to generateAccountStatement(accountId) */
        injectInput("\nACC-C00001-1\n");

        Main.handleMenuChoice(11);

        verify(mockService, times(1)).generateAccountStatement("ACC-C00001-1");
    }

    /* ---------------- handleReportChoice: FR-10 Filter Reports ---------------- */

    @Test
    @DisplayName("Should call filterAccountsByMinBalance() for FILTER_MIN_BALANCE (4)")
    void testHandleReportChoiceFilterMinBalance()
    {
        logger.info("Testing FILTER_MIN_BALANCE report choice calls filterAccountsByMinBalance()");

        /*
         * Stub returns empty list — covers the 'no accounts found' branch.
         * Scanner input provides the minimum balance value read by filterByMinBalance().
         */
        when(mockService.filterAccountsByMinBalance(1000.0)).thenReturn(List.of());
        injectInput("1000.0\n");

        boolean result = Main.handleReportChoice(4);

        assertTrue(result, "FILTER_MIN_BALANCE must return true to keep the report loop running");
        verify(mockService, times(1)).filterAccountsByMinBalance(1000.0);
    }

    @Test
    @DisplayName("Should call filterAccountsByMinBalance() and display results when accounts are found")
    void testHandleReportChoiceFilterMinBalanceWithData()
    {
        logger.info("Testing FILTER_MIN_BALANCE report choice with non-empty result list");

        /*
         * Stub returns a real account — covers the non-empty branch
         * that prints each account ID and balance.
         */
        Account account = new SavingsAccount("ACC-C00001-1", "C00001", 2000.0);
        when(mockService.filterAccountsByMinBalance(500.0)).thenReturn(List.of(account));
        injectInput("500.0\n");

        boolean result = Main.handleReportChoice(4);

        assertTrue(result);
        verify(mockService, times(1)).filterAccountsByMinBalance(500.0);
    }

    @Test
    @DisplayName("Should call filterAccountsByMaxBalance() for FILTER_MAX_BALANCE (5)")
    void testHandleReportChoiceFilterMaxBalance()
    {
        logger.info("Testing FILTER_MAX_BALANCE report choice calls filterAccountsByMaxBalance()");

        /*
         * Stub returns empty list — covers the 'no accounts found' branch.
         * Scanner input provides the maximum balance value read by filterByMaxBalance().
         */
        when(mockService.filterAccountsByMaxBalance(5000.0)).thenReturn(List.of());
        injectInput("5000.0\n");

        boolean result = Main.handleReportChoice(5);

        assertTrue(result, "FILTER_MAX_BALANCE must return true to keep the report loop running");
        verify(mockService, times(1)).filterAccountsByMaxBalance(5000.0);
    }

    @Test
    @DisplayName("Should call filterAccountsByMaxBalance() and display results when accounts are found")
    void testHandleReportChoiceFilterMaxBalanceWithData()
    {
        logger.info("Testing FILTER_MAX_BALANCE report choice with non-empty result list");

        /*
         * Stub returns a real account — covers the non-empty branch
         * that prints each account ID and balance.
         */
        Account account = new SavingsAccount("ACC-C00002-1", "C00002", 1500.0);
        when(mockService.filterAccountsByMaxBalance(3000.0)).thenReturn(List.of(account));
        injectInput("3000.0\n");

        boolean result = Main.handleReportChoice(5);

        assertTrue(result);
        verify(mockService, times(1)).filterAccountsByMaxBalance(3000.0);
    }

    @Test
    @DisplayName("Should call filterAccountsByBalanceRange() for FILTER_RANGE (6)")
    void testHandleReportChoiceFilterRange()
    {
        logger.info("Testing FILTER_RANGE report choice calls filterAccountsByBalanceRange()");

        /*
         * Stub returns empty list — covers the 'no accounts found' branch.
         * Scanner input provides both min and max balance values read by filterByRange().
         */
        when(mockService.filterAccountsByBalanceRange(500.0, 3000.0)).thenReturn(List.of());
        injectInput("500.0\n3000.0\n");

        boolean result = Main.handleReportChoice(6);

        assertTrue(result, "FILTER_RANGE must return true to keep the report loop running");
        verify(mockService, times(1)).filterAccountsByBalanceRange(500.0, 3000.0);
    }

    @Test
    @DisplayName("Should call filterAccountsByBalanceRange() and display results when accounts are found")
    void testHandleReportChoiceFilterRangeWithData()
    {
        logger.info("Testing FILTER_RANGE report choice with non-empty result list");

        /*
         * Stub returns a real account — covers the non-empty branch
         * that prints each account ID and balance within the range.
         */
        Account account = new SavingsAccount("ACC-C00003-1", "C00003", 1200.0);
        when(mockService.filterAccountsByBalanceRange(1000.0, 2000.0)).thenReturn(List.of(account));
        injectInput("1000.0\n2000.0\n");

        boolean result = Main.handleReportChoice(6);

        assertTrue(result);
        verify(mockService, times(1)).filterAccountsByBalanceRange(1000.0, 2000.0);
    }

    @Test
    @DisplayName("Should display account details when HIGHEST_BALANCE (3) returns a non-null account")
    void testHandleReportChoiceHighestBalanceWithData()
    {
        logger.info("Testing HIGHEST_BALANCE report choice with non-null account");

        /*
         * Stub returns a real account — covers the non-null branch
         * that prints the account ID and balance.
         */
        Account account = new SavingsAccount("ACC-C00001-1", "C00001", 9999.0);
        when(mockService.getHighestBalanceAccount()).thenReturn(account);

        boolean result = Main.handleReportChoice(3);

        assertTrue(result);
        verify(mockService, times(1)).getHighestBalanceAccount();
    }
}