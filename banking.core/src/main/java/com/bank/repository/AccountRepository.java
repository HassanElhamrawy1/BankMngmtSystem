/*
 * Specific repository interface for Account entities.
 * Extends the generic Repository interface to provide a specialized contract for account data.
 * This interface can be used to add account-specific query methods in the future.
 */
package com.bank.repository;

import com.bank.model.Account;

public interface AccountRepository extends Repository<Account> 
{
	 /* 
	  * Placeholder for account-specific logic.
	  * Future methods could include: findByCustomerId(String customerId) or findByType(String type).
	  */

}