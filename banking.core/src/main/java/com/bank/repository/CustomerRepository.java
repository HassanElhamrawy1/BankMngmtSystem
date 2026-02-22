/*
 * Specific repository interface for Customer entities.
 * Extends the generic Repository interface to provide a specialized contract for customer data.
 * This interface can be used to add customer-specific query methods in the future.
 */

package com.bank.repository;

import com.bank.model.Customer;
import java.util.List;

import com.bank.model.Customer;

public interface CustomerRepository extends Repository<Customer> 
{
	   /* 
	    * Placeholder for customer-specific logic.
	    * Future methods could include: findByEmail(String email) or findByName(String name).
	    */
}
