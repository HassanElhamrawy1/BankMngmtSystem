/*
 * Interface for entities that have a unique identifier.
 * Provides a common contract for any class that can be identified by a String ID.
 * Used by repositories to handle generic entities.
 */
package com.bank.model;


public interface Identifiable 
{
	/**
     * Returns the unique identifier of the entity.
     * @return The unique ID as a String
     */
    String getId();
}
