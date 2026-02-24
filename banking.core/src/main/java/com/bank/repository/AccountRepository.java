/*
 * Specific repository interface for Account entities.
 * Extends the generic Repository interface to provide a specialized contract for account data.
 * This interface can be used to add account-specific query methods in the future.
 */
package com.bank.repository;

import com.bank.model.Account;
import java.sql.Connection;

public interface AccountRepository extends Repository<Account> 
{
	 /* 
	  * Placeholder for account-specific logic.
	  * Future methods could include: findByCustomerId(String customerId) or findByType(String type).
	  */
	
	/* Will be used Inside JdbcAccountRepository.java  in transfer API */
	/**
     * Find account by id using the provided Connection.
     * This method participates in the caller-managed transaction: it does NOT commit or rollback the connection.
     *
     * @param conn The JDBC connection to use (must not be null)
     * @param id   The account id to search for
     * @return The Account instance if found, or null if not found
     * @throws RuntimeException wrapping SQLException on DB error
     */
	public Account findById(Connection conn, String id);
	
	/**
     * Update persistent fields of the given account using the provided Connection.
     * This update participates in the caller-managed transaction and must not commit/rollback the connection.
     *
     * @param conn    The JDBC connection to use (must not be null)
     * @param account The account to persist (must not be null)
     * @throws RuntimeException if a database error occurs or unexpected number of rows updated
     */
	public void update(Connection conn, Account account); 
	
}