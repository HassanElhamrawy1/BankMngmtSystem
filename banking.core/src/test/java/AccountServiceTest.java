/*
 * Unit tests for the AccountService class.
 * Covers deposit, withdraw, and error handling for missing accounts.
 * Uses Mockito to isolate the service from the repository layer.
 * Implements test coverage for FR-05: Deposit Money and FR-06: Withdraw Money.
 */
package com.bank.service;

import com.bank.model.Account;
import com.bank.model.CurrentAccount;
import com.bank.model.SavingsAccount;
import com.bank.repository.Repository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceTest
{
    private static final Logger logger = LoggerFactory.getLogger(AccountServiceTest.class);

    /* Mocked repository — no real DB or in-memory store involved */
    @Mock
    private Repository<Account> accountRepository;

    /* Service under test — repository is injected via Mockito */
    @InjectMocks
    private AccountService accountService;

    /* Real account instances used as return values from the mock */
    private SavingsAccount savingsAccount;
    private CurrentAccount currentAccount;

    @BeforeEach
    void setUp()
    {
        MockitoAnnotations.openMocks(this);

        /*
         * Use realistic account IDs that follow the system pattern: ACC-CustomerID-Sequence.
         * Initial balances reflect a typical customer scenario.
         */
        savingsAccount = new SavingsAccount("ACC-C00001-1", "C00001", 1000.0);
        currentAccount = new CurrentAccount("ACC-C00001-2", "C00001", 500.0);
    }

    /* ---------------- FR-05: Deposit Money ---------------- */

    @Test
    @DisplayName("Should increase the account balance after a valid deposit")
    void testDepositIncreasesBalance()
    {
        logger.info("Depositing 300.0 into savings account: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        accountService.deposit("ACC-C00001-1", 300.0);

        assertEquals(1300.0, savingsAccount.getBalance(), "Balance must increase by the deposited amount");
        logger.info("Balance after deposit: {}", savingsAccount.getBalance());
    }

    @Test
    @DisplayName("Should record a transaction entry after a successful deposit")
    void testDepositAddsTransactionRecord()
    {
        logger.info("Verifying transaction record is created after deposit on account: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        accountService.deposit("ACC-C00001-1", 500.0);

        assertFalse(savingsAccount.getTransactions().isEmpty(), "A transaction record must be added after deposit");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when depositing into a non-existent account")
    void testDepositThrowsExceptionForUnknownAccount()
    {
        /*
         * The repository returns null when the account is not found.
         * The service must reject the operation immediately with a clear error.
         */
        logger.info("Testing deposit on a non-existent account — expecting IllegalArgumentException");

        when(accountRepository.findById("ACC-INVALID")).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> accountService.deposit("ACC-INVALID", 100.0),
            "Depositing into a non-existent account must throw IllegalArgumentException"
        );
    }

    @Test
    @DisplayName("Should call findById exactly once when processing a deposit")
    void testDepositCallsRepositoryOnce()
    {
        logger.info("Verifying repository interaction during deposit for account: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        accountService.deposit("ACC-C00001-1", 200.0);

        verify(accountRepository, times(1)).findById("ACC-C00001-1");
    }

    /* ---------------- FR-06: Withdraw Money ---------------- */

    @Test
    @DisplayName("Should decrease the account balance after a valid withdrawal")
    void testWithdrawDecreasesBalance()
    {
        logger.info("Withdrawing 150.0 from current account: {}", currentAccount.getId());

        when(accountRepository.findById("ACC-C00001-2")).thenReturn(currentAccount);

        accountService.withdraw("ACC-C00001-2", 150.0);

        assertEquals(350.0, currentAccount.getBalance(), "Balance must decrease by the withdrawn amount");
        logger.info("Balance after withdrawal: {}", currentAccount.getBalance());
    }

    @Test
    @DisplayName("Should record a transaction entry after a successful withdrawal")
    void testWithdrawAddsTransactionRecord()
    {
        logger.info("Verifying transaction record is created after withdrawal on account: {}", currentAccount.getId());

        when(accountRepository.findById("ACC-C00001-2")).thenReturn(currentAccount);

        accountService.withdraw("ACC-C00001-2", 100.0);

        assertFalse(currentAccount.getTransactions().isEmpty(), "A transaction record must be added after withdrawal");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when withdrawing from a non-existent account")
    void testWithdrawThrowsExceptionForUnknownAccount()
    {
        /*
         * Same guard as deposit — the service must not proceed if the account cannot be found.
         */
        logger.info("Testing withdrawal from a non-existent account — expecting IllegalArgumentException");

        when(accountRepository.findById("ACC-INVALID")).thenReturn(null);

        assertThrows(
            IllegalArgumentException.class,
            () -> accountService.withdraw("ACC-INVALID", 50.0),
            "Withdrawing from a non-existent account must throw IllegalArgumentException"
        );
    }

    @Test
    @DisplayName("Should throw an exception when withdrawing more than the available balance")
    void testWithdrawThrowsExceptionForInsufficientFunds()
    {
        /*
         * Business rule enforced at the Account level — the service delegates to account.withdraw().
         * This test verifies that the exception propagates correctly through the service layer.
         */
        logger.info("Testing withdrawal exceeding available balance on account: {}", savingsAccount.getId());

        when(accountRepository.findById("ACC-C00001-1")).thenReturn(savingsAccount);

        assertThrows(
            IllegalArgumentException.class,
            () -> accountService.withdraw("ACC-C00001-1", 9999.0),
            "Withdrawing more than the available balance must throw an exception"
        );
    }

    @Test
    @DisplayName("Should call findById exactly once when processing a withdrawal")
    void testWithdrawCallsRepositoryOnce()
    {
        logger.info("Verifying repository interaction during withdrawal for account: {}", currentAccount.getId());

        when(accountRepository.findById("ACC-C00001-2")).thenReturn(currentAccount);

        accountService.withdraw("ACC-C00001-2", 50.0);

        verify(accountRepository, times(1)).findById("ACC-C00001-2");
    }
}