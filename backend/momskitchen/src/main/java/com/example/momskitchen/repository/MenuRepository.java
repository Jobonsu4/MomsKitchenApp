package com.example.momskitchen.repository;

import com.example.momskitchen.model.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    // Common helpers (use any that fit your flows)

    /** All active menus (usually 1) ordered by id */
    List<Menu> findByActiveTrueOrderByIdAsc();

    /** First active menu (handy for /menu default) */
    Optional<Menu> findFirstByActiveTrueOrderByIdAsc();

    /** Search by name (case-insensitive) */
    List<Menu> findByNameContainingIgnoreCaseOrderByIdAsc(String name);

    /** Exact name existence check (for admin validations) */
    boolean existsByNameIgnoreCase(String name);
}
