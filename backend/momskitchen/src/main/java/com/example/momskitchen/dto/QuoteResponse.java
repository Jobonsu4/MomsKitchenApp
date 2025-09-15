package com.example.momskitchen.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * QuoteResponse represents the calculated summary of an order
 * before the user confirms and creates it.
 * It includes itemized cart details, subtotal, tax, and total.
 */
public class QuoteResponse {

    // Itemized list of what the user selected
    private List<CartItemDTO> items;

    // Order-level pricing breakdown
    private BigDecimal subtotal;   // Sum of item subtotals
    private BigDecimal tax;        // Calculated tax (if applicable)
    private BigDecimal fees;       // Any extra fees (e.g., service, packaging)
    private BigDecimal discount;   // Any applied discounts
    private BigDecimal total;      // Final amount = subtotal + tax + fees - discount

    // Optional message (e.g., "Pickup only available Friday–Sunday")
    private String message;

    // Default constructor
    public QuoteResponse() {}

    // All-args constructor
    public QuoteResponse(List<CartItemDTO> items, BigDecimal subtotal, BigDecimal tax,
                         BigDecimal fees, BigDecimal discount, BigDecimal total, String message) {
        this.items = items;
        this.subtotal = subtotal;
        this.tax = tax;
        this.fees = fees;
        this.discount = discount;
        this.total = total;
        this.message = message;
    }

    // Getters and setters
    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getFees() {
        return fees;
    }

    public void setFees(BigDecimal fees) {
        this.fees = fees;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "QuoteResponse{" +
                "items=" + items +
                ", subtotal=" + subtotal +
                ", tax=" + tax +
                ", fees=" + fees +
                ", discount=" + discount +
                ", total=" + total +
                ", message='" + message + '\'' +
                '}';
    }
}
