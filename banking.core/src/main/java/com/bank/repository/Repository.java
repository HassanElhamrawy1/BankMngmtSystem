/* Generic interface: add(T), findById(String), findAll(). */

package com.bank.repository;

import java.util.List;

public interface Repository<T> 
{

    void save(T entity);

    T findById(String id);

    List<T> findAll();
    void delete(String id);
}
