package com.acms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base repository interface with common methods for all repositories.
 *
 * @param <T>  the entity type
 * @param <ID> the type of the entity's identifier
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID> {
    
    /**
     * Find all entities with pagination and sorting.
     *
     * @param pageable pagination and sorting information
     * @return a page of entities
     */
    @Override
    Page<T> findAll(Pageable pageable);
    
    /**
     * Find all active entities.
     *
     * @return list of active entities
     */
    List<T> findAllByActiveTrue();
    
    /**
     * Find all active entities with pagination and sorting.
     *
     * @param pageable pagination and sorting information
     * @return a page of active entities
     */
    Page<T> findAllByActiveTrue(Pageable pageable);
    
    /**
     * Find an entity by ID if it's active.
     *
     * @param id the ID of the entity
     * @return an Optional containing the entity if found and active, empty otherwise
     */
    Optional<T> findByIdAndActiveTrue(ID id);
    
    /**
     * Soft delete an entity by ID.
     *
     * @param id the ID of the entity to delete
     */
    default void softDelete(ID id) {
        findById(id).ifPresent(entity -> {
            try {
                entity.getClass().getMethod("setActive", boolean.class).invoke(entity, false);
                save(entity);
            } catch (Exception e) {
                throw new RuntimeException("Error during soft delete", e);
            }
        });
    }
}
