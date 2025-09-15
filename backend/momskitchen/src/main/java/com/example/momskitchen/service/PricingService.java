package com.example.momskitchen.service;

import com.example.momskitchen.dto.CartAddonDTO;
import com.example.momskitchen.dto.CartItemDTO;
import com.example.momskitchen.dto.CreateOrderRequest;
import com.example.momskitchen.dto.QuoteResponse;
import com.example.momskitchen.model.Addon;
import com.example.momskitchen.model.MenuItem;
import com.example.momskitchen.repository.AddonRepository;
import com.example.momskitchen.repository.MenuItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

/**
 * Computes pricing for a cart request.
 *
 * Rules:
 *  - Line subtotal = (menu item base price + sum(selected addon priceDelta)) * quantity
 *  - Subtotal = sum(line subtotals)
 *  - Tax = subtotal * taxRate
 *  - Total = subtotal + tax
 *
 * Configuration (application.yml):
 *   pricing:
 *     taxRate: 0.06           # 6% (example) — default 0.00 if not provided
 *     validateAddons: true    # ensure selected addons are allowed for the item
 *
 * DTO expectations:
 *  - QuoteResponse provides setters: setSubtotal, setTax, setTotal
 *  - CreateOrderRequest contains a list of CartItemDTO (with itemId, quantity, addons)
 */
@Service
public class PricingService {

    private final MenuItemRepository menuItemRepository;
    private final AddonRepository addonRepository;

    /** Example: set in application.yml as pricing.taxRate: 0.06 (6%) */
    private final BigDecimal taxRate;

    /** If true (default), ensure each selected addon is allowed for that MenuItem */
    private final boolean validateAddons;

    public PricingService(MenuItemRepository menuItemRepository,
                          AddonRepository addonRepository,
                          @Value("${pricing.taxRate:0.00}") BigDecimal taxRate,
                          @Value("${pricing.validateAddons:true}") boolean validateAddons) {
        this.menuItemRepository = menuItemRepository;
        this.addonRepository = addonRepository;
        this.taxRate = taxRate == null ? BigDecimal.ZERO : taxRate;
        this.validateAddons = validateAddons;
    }

    /**
     * Price the given cart request.
     */
    public QuoteResponse quote(CreateOrderRequest req) {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (req != null && req.getItems() != null) {
            for (CartItemDTO line : req.getItems()) {
                subtotal = subtotal.add(lineSubtotal(line));
            }
        }

        BigDecimal tax = money(subtotal.multiply(taxRate));
        BigDecimal total = money(subtotal.add(tax));

        QuoteResponse out = new QuoteResponse();
        out.setSubtotal(subtotal);
        out.setTax(tax);
        out.setTotal(total);
        return out;
    }

    // =========================
    // Internal helpers
    // =========================

    /**
     * Compute a line subtotal for one cart line:
     *  (item price + sum(addon deltas)) * quantity
     */
    private BigDecimal lineSubtotal(CartItemDTO line) {
        if (line == null || line.getItemId() == null) {
            throw new IllegalArgumentException("Cart line is missing itemId");
        }

        // Load the catalog item (throws if not found)
        MenuItem item = menuItemRepository.findById(line.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + line.getItemId()));

        int qty = (line.getQuantity() != null && line.getQuantity() > 0) ? line.getQuantity() : 1;

        // Sum of addon price deltas (per unit)
        BigDecimal addonSum = BigDecimal.ZERO;

        if (line.getAddons() != null && !line.getAddons().isEmpty()) {
            // Optional validation: check selected addons are allowed for this item
            Set<Long> allowed = new HashSet<>();
            if (validateAddons) {
                item.getAllowedAddons().forEach(a -> allowed.add(a.getId()));
            }

            for (CartAddonDTO a : line.getAddons()) {
                if (a == null || a.getAddonId() == null) continue;

                Long addonId = a.getAddonId();

                if (validateAddons && !allowed.isEmpty() && !allowed.contains(addonId)) {
                    throw new IllegalArgumentException("Addon " + addonId + " is not allowed for item " + item.getId());
                }

                Addon addon = addonRepository.findById(addonId)
                        .orElseThrow(() -> new IllegalArgumentException("Addon not found: " + addonId));

                addonSum = addonSum.add(safe(addon.getPriceDelta()));
            }
        }

        BigDecimal unit = safe(item.getPrice()).add(addonSum);
        BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(qty));
        return money(lineTotal);
    }

    /** Normalize nulls and enforce 2-decimal currency rounding */
    private BigDecimal money(BigDecimal v) {
        return safe(v).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
