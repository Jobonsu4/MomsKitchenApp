package com.example.momskitchen.repository;

import com.example.momskitchen.model.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Long> {

    /** All active add-ons (useful for admin UIs or global listing) */
    List<Addon> findByActiveTrueOrderByNameAsc();

    /** Search add-ons by name (case-insensitive) */
    List<Addon> findByNameContainingIgnoreCaseOrderByNameAsc(String name);

    /** Existence check (to avoid duplicates) */
    boolean existsByNameIgnoreCase(String name);
}
