package com.example.momskitchen.dto;  // Package for Data Transfer Objects (DTOs)

import java.util.List;

/**
 * CartItemDTO represents a single item in a user's cart.
 * This object is used to transfer cart-related data between layers
 * without exposing internal entity details.
 */
public class CartItemDTO {

    private Long menuItemId;      // ID of the menu item being added to the cart
    private String name;          // Name of the menu item
    private String description;   // Short description of the menu item
    private Integer quantity;         // Quantity selected by the user (nullable for validation)
    private double price;         // Price per unit of the menu item
    private double subtotal;      // Calculated as price * quantity
    private List<CartAddonDTO> addons; // Selected addons for this item (optional)

    // Default no-args constructor (needed for frameworks like Spring)
    public CartItemDTO() {}

    // All-args constructor for quick object creation
    public CartItemDTO(Long menuItemId, String name, String description, int quantity, double price) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = quantity * price; // auto-calc subtotal
    }

    // Getters and Setters
    public Long getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(Long menuItemId) {
        this.menuItemId = menuItemId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.subtotal = this.price * this.quantity; // update subtotal whenever quantity changes
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        this.subtotal = this.price * this.quantity; // update subtotal whenever price changes
    }

    public double getSubtotal() {
        return subtotal;
    }

    public List<CartAddonDTO> getAddons() {
        return addons;
    }

    public void setAddons(List<CartAddonDTO> addons) {
        this.addons = addons;
    }

    // Compatibility: services expect getItemId() matching menuItemId
    public Long getItemId() {
        return getMenuItemId();
    }

    // toString for debugging/logging
    @Override
    public String toString() {
        return "CartItemDTO{" +
                "menuItemId=" + menuItemId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                ", subtotal=" + subtotal +
                '}';
    }
}
