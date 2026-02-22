/*
 * Generic repository interface for basic CRUD operations.
 * Provides a contract for data access layers to implement.
 * Used for both Customer and Account entities.
 */

package com.bank.repository;

import java.util.List;

public interface Repository<T> 
{
	/**
     * Saves or updates an entity in the data store.
     * @param entity The entity to save
     */
    void save(T entity);
    
    /**
     * Finds an entity by its unique identifier.
     * @param id The ID of the entity to find
     * @return The entity if found, or null if not found
     */
    T findById(String id);

    /**
     * Retrieves all entities of type T from the data store.
     * @return A list of all entities
     */
    List<T> findAll();
    
    /**
     * Deletes an entity by its unique identifier.
     * @param id The ID of the entity to delete
     */
    void delete(String id);
}
