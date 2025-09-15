package com.example.momskitchen.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Represents a selected add-on inside a cart item.
 * Sent from the frontend as part of CreateOrderRequest.
 *
 * Example JSON:
 * {
 *   "addonId": 1,
 *   "name": "Extra Protein",
 *   "priceDelta": 3.00
 * }
 */
public class CartAddonDTO {

    @NotNull
    private Long addonId;         // which add-on was selected (FK)

    private String name;          // optional: can be echoed back in request

    private BigDecimal priceDelta; // optional: frontend can preview, server will validate

    public CartAddonDTO() {}

    public CartAddonDTO(Long addonId, String name, BigDecimal priceDelta) {
        this.addonId = addonId;
        this.name = name;
        this.priceDelta = priceDelta;
    }

    public Long getAddonId() {
        return addonId;
    }

    public void setAddonId(Long addonId) {
        this.addonId = addonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPriceDelta() {
        return priceDelta;
    }

    public void setPriceDelta(BigDecimal priceDelta) {
        this.priceDelta = priceDelta;
    }
}
