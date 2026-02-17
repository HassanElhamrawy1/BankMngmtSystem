/* Implements Repository, stores data in an internal List. */
package com.bank.repository;

import com.bank.model.Identifiable;
import java.util.ArrayList;
import java.util.List;


/* FR-01: Create Customer,  FR-02: View Customers */

public class InMemoryRepository<T extends Identifiable>
        implements Repository<T> {

    private List<T> entities = new ArrayList<>();

    @Override
    public void add(T entity) {
        entities.add(entity);
    }

    @Override
    public T findById(String id) {
        for (T entity : entities) {
            if (entity.getId().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(entities);
    }
}
