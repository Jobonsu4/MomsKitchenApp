package com.example.momskitchen.service;

import com.example.momskitchen.model.Addon;
import com.example.momskitchen.model.Menu;
import com.example.momskitchen.model.MenuCategory;
import com.example.momskitchen.model.MenuItem;
import com.example.momskitchen.repository.MenuCategoryRepository;
import com.example.momskitchen.repository.MenuItemRepository;
import com.example.momskitchen.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository itemRepository;

    public MenuService(MenuRepository menuRepository,
                       MenuCategoryRepository categoryRepository,
                       MenuItemRepository itemRepository) {
        this.menuRepository = menuRepository;
        this.categoryRepository = categoryRepository;
        this.itemRepository = itemRepository;
    }

    /* ---------------------------
       MENUS
       --------------------------- */

    /** Return all menus (usually just one active menu). */
    public List<Menu> getMenus() {
        return menuRepository.findAll();
    }

    /** Get one menu by id with its categories/items (your entities are EAGER so tree is populated). */
    public Optional<Menu> getMenu(Long menuId) {
        return menuRepository.findById(menuId);
    }

    /* ---------------------------
       CATEGORIES
       --------------------------- */

    /** Categories for a given menu, ordered by display_order. */
    public List<MenuCategory> getCategoriesByMenu(Long menuId) {
        // If you have a custom finder: categoryRepository.findByMenuIdOrderByDisplayOrderAsc(menuId)
        return categoryRepository.findAll()
                .stream()
                .filter(c -> c.getMenu() != null && c.getMenu().getId().equals(menuId))
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .toList();
    }

    /* ---------------------------
       ITEMS
       --------------------------- */

    /** Items for a given category, ordered by display_order. */
    public List<MenuItem> getItemsByCategory(Long categoryId) {
        // If you have a custom finder: itemRepository.findByCategoryIdOrderByDisplayOrderAsc(categoryId)
        return itemRepository.findAll()
                .stream()
                .filter(i -> i.getCategory() != null && i.getCategory().getId().equals(categoryId))
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .toList();
    }

    /** One item by id (includes allowedAddons due to EAGER mapping). */
    public Optional<MenuItem> getItem(Long itemId) {
        return itemRepository.findById(itemId);
    }

    /* ---------------------------
       ADD-ONS
       --------------------------- */

    /** Allowed add-ons for a given item. */
    public List<Addon> getAddonsForItem(Long itemId) {
        return getItem(itemId)
                .map(item -> item.getAllowedAddons().stream().toList())
                .orElseGet(List::of);
    }
}
