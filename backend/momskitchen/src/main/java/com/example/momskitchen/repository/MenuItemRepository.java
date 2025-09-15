package com.example.momskitchen.repository;

import com.example.momskitchen.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    /** All items for a given category, ordered by display_order */
    List<MenuItem> findByCategoryIdOrderByDisplayOrderAsc(Long categoryId);

    /** Only available items for a given category (active menu display) */
    List<MenuItem> findByCategoryIdAndAvailableTrueOrderByDisplayOrderAsc(Long categoryId);

    /** Case-insensitive search within a category */
    List<MenuItem> findByCategoryIdAndNameContainingIgnoreCase(Long categoryId, String name);

    /** Existence check (for admin validation) */
    boolean existsByCategoryIdAndNameIgnoreCase(Long categoryId, String name);
}
