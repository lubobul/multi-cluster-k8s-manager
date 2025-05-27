package com.multikube_rest_service.repositories;

import com.multikube_rest_service.entities.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List; // Added for findByNameNotIn
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    Optional<Tenant> findByName(String name);

    boolean existsByName(String name);

    /**
     * Finds tenants whose names contain the given string, ignoring case, and are not in the excluded names list, with pagination.
     *
     * @param name The string to search for in tenant names.
     * @param excludedNames A list of names to exclude from the search.
     * @param pageable Pagination information.
     * @return A page of tenants matching the criteria.
     */
    Page<Tenant> findByNameContainingIgnoreCaseAndNameNotIn(String name, List<String> excludedNames, Pageable pageable);

    /**
     * Finds all tenants whose names are not in the excluded names list, with pagination.
     *
     * @param excludedNames A list of names to exclude from the search.
     * @param pageable Pagination information.
     * @return A page of all tenants excluding the specified names.
     */
    Page<Tenant> findByNameNotIn(List<String> excludedNames, Pageable pageable);
}