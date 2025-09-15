package com.example.momskitchen.repository;

import com.example.momskitchen.model.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuCategoryRepository extends JpaRepository<MenuCategory, Long> {

    /** All categories for a given menu, ordered by display_order */
    List<MenuCategory> findByMenuIdOrderByDisplayOrderAsc(Long menuId);

    /** Only active categories for a given menu */
    List<MenuCategory> findByMenuIdAndActiveTrueOrderByDisplayOrderAsc(Long menuId);

    /** Case-insensitive name search within a menu (useful for admin lookups) */
    List<MenuCategory> findByMenuIdAndNameContainingIgnoreCase(Long menuId, String name);

    /** Existence check (e.g., prevent duplicates in admin) */
    boolean existsByMenuIdAndNameIgnoreCase(Long menuId, String name);
}
