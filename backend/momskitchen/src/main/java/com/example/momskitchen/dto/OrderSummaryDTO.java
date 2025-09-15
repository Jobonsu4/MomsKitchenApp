package com.example.momskitchen.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderSummaryDTO used by controllers to return full order details,
 * including nested line items and addons, with monetary fields preserved as BigDecimal.
 */
public class OrderSummaryDTO {

    private Long id;
    private String orderCode;
    private String status;
    private String paymentStatus;

    private LocalDateTime pickupAt;

    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String notes;

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;

    private List<Item> items;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderCode() { return orderCode; }
    public void setOrderCode(String orderCode) { this.orderCode = orderCode; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPickupAt() { return pickupAt; }
    public void setPickupAt(LocalDateTime pickupAt) { this.pickupAt = pickupAt; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    // Nested DTOs for items and addons
    public static class Item {
        private String itemName;
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal lineSubtotal;
        private List<ItemAddon> addons;

        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getLineSubtotal() { return lineSubtotal; }
        public void setLineSubtotal(BigDecimal lineSubtotal) { this.lineSubtotal = lineSubtotal; }

        public List<ItemAddon> getAddons() { return addons; }
        public void setAddons(List<ItemAddon> addons) { this.addons = addons; }
    }

    public static class ItemAddon {
        private String addonName;
        private BigDecimal priceDelta;

        public String getAddonName() { return addonName; }
        public void setAddonName(String addonName) { this.addonName = addonName; }

        public BigDecimal getPriceDelta() { return priceDelta; }
        public void setPriceDelta(BigDecimal priceDelta) { this.priceDelta = priceDelta; }
    }
}
