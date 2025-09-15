package com.example.momskitchen.controller;

import com.example.momskitchen.model.Addon;
import com.example.momskitchen.model.Menu;
import com.example.momskitchen.model.MenuCategory;
import com.example.momskitchen.model.MenuItem;
import com.example.momskitchen.service.MenuService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Public menu endpoints for the customer app.
 * Base path: /api/menu
 *
 * Notes:
 * - We map entities -> DTOs to avoid circular JSON from bidirectional relationships.
 * - Your entities are EAGER, so Menu -> Categories -> Items -> Addons come preloaded.
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // ---------------------------------------------------------
    // GET /api/menu/menus       -> list all menus (id, name, active)
    // ---------------------------------------------------------
    @GetMapping("/menus")
    public ResponseEntity<List<MenuSummaryDTO>> listMenus() {
        List<Menu> menus = menuService.getMenus();
        List<MenuSummaryDTO> dtos = menus.stream()
                .sorted(Comparator.comparing(Menu::getId))
                .map(m -> new MenuSummaryDTO(m.getId(), m.getName(), m.getDescription(), Boolean.TRUE.equals(m.getActive())))
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // ---------------------------------------------------------
    // GET /api/menu/{menuId}/tree  -> menu with categories/items/addons
    // Useful for your MenuPage to render everything in one request.
    // ---------------------------------------------------------
    @GetMapping("/{menuId}/tree")
    public ResponseEntity<MenuTreeDTO> getMenuTree(@PathVariable Long menuId) {
        Optional<Menu> opt = menuService.getMenu(menuId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        MenuTreeDTO dto = toMenuTreeDTO(opt.get());
        return ResponseEntity.ok(dto);
    }

    // ---------------------------------------------------------
    // GET /api/menu/categories/{categoryId}/items  -> items in a category
    // ---------------------------------------------------------
    @GetMapping("/categories/{categoryId}/items")
    public ResponseEntity<List<ItemDTO>> itemsByCategory(@PathVariable Long categoryId) {
        List<MenuItem> items = menuService.getItemsByCategory(categoryId);
        List<ItemDTO> dtos = items.stream().map(this::toItemDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    // ---------------------------------------------------------
    // GET /api/menu/items/{itemId}           -> single item (with allowedAddons)
    // GET /api/menu/items/{itemId}/addons    -> allowed addons for the item
    // ---------------------------------------------------------
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ItemDTO> getItem(@PathVariable Long itemId) {
        Optional<MenuItem> opt = menuService.getItem(itemId);
        return opt.map(item -> ResponseEntity.ok(toItemDTO(item)))
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/items/{itemId}/addons")
    public ResponseEntity<List<AddonDTO>> getItemAddons(@PathVariable Long itemId) {
        List<Addon> addons = menuService.getAddonsForItem(itemId);
        List<AddonDTO> dtos = addons.stream().map(this::toAddonDTO).toList();
        return ResponseEntity.ok(dtos);
    }

    // =========================
    // Mapping helpers (Entity -> DTO)
    // =========================

    private MenuTreeDTO toMenuTreeDTO(Menu menu) {
        List<CategoryDTO> categories = menu.getCategories().stream()
                .sorted(Comparator.comparing(MenuCategory::getDisplayOrder))
                .map(cat -> {
                    List<ItemDTO> items = cat.getItems().stream()
                            .sorted(Comparator.comparing(MenuItem::getDisplayOrder))
                            .map(this::toItemDTO)
                            .collect(Collectors.toList());
                    return new CategoryDTO(
                            cat.getId(),
                            cat.getName(),
                            cat.getDescription(),
                            cat.getDisplayOrder(),
                            Boolean.TRUE.equals(cat.getActive()),
                            items
                    );
                })
                .collect(Collectors.toList());

        return new MenuTreeDTO(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                Boolean.TRUE.equals(menu.getActive()),
                categories
        );
    }

    private ItemDTO toItemDTO(MenuItem item) {
        List<AddonDTO> addons = item.getAllowedAddons().stream()
                .sorted(Comparator.comparing(Addon::getId))
                .map(this::toAddonDTO)
                .toList();

        Long categoryId = (item.getCategory() != null) ? item.getCategory().getId() : null;

        return new ItemDTO(
                item.getId(),
                categoryId,
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                Boolean.TRUE.equals(item.getAvailable()),
                item.getImageUrl(),
                item.getDisplayOrder(),
                addons
        );
    }

    private AddonDTO toAddonDTO(Addon a) {
        return new AddonDTO(
                a.getId(),
                a.getName(),
                a.getDescription(),
                a.getPriceDelta(),
                Boolean.TRUE.equals(a.getActive())
        );
    }

    // =========================
    // DTOs (records) used by this controller
    // =========================

    /** Minimal menu list item */
    public record MenuSummaryDTO(
            Long id,
            String name,
            String description,
            boolean active
    ) {}

    /** Full menu tree for the customer app */
    public record MenuTreeDTO(
            Long id,
            String name,
            String description,
            boolean active,
            List<CategoryDTO> categories
    ) {}

    public record CategoryDTO(
            Long id,
            String name,
            String description,
            Integer displayOrder,
            boolean active,
            List<ItemDTO> items
    ) {}

    public record ItemDTO(
            Long id,
            Long categoryId,
            String name,
            String description,
            BigDecimal price,
            boolean available,
            String imageUrl,
            Integer displayOrder,
            List<AddonDTO> allowedAddons
    ) {}

    public record AddonDTO(
            Long id,
            String name,
            String description,
            BigDecimal priceDelta,
            boolean active
    ) {}
}
