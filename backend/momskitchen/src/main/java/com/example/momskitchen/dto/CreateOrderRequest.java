package com.example.momskitchen.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CreateOrderRequest represents the payload sent by the client
 * when creating a new order. It contains customer details, pickup info,
 * payment choice, and the cart items.
 */
public class CreateOrderRequest {

    // Basic customer info (collected at checkout)
    private String customerName;     // Customer's name
    private String customerEmail;    // Customer's email (optional for receipts)
    private String customerPhone;    // Customer's phone number (used to view orders later)

    // Pickup information
    private LocalDateTime pickupTime; // When the user wants to pick up the order
    private Long pickupSlotId;        // Optional: reference to a predefined pickup slot entity
    
    // Selected pickup day-of-week: 0=Sun .. 6=Sat
    private Integer pickupDay;

    // Payment information
    private String paymentMethod;    // e.g. "CASH" or "CASHAPP"
    private String paymentStatus;    // e.g. "PENDING", "PAID"

    // Cart items being ordered
    private List<CartItemDTO> items;

    // Default constructor (needed for frameworks like Spring to deserialize JSON)
    public CreateOrderRequest() {}

    // All-args constructor for convenience
    public CreateOrderRequest(String customerName, String customerEmail, String customerPhone,
                              LocalDateTime pickupTime, Long pickupSlotId,
                              String paymentMethod, String paymentStatus,
                              List<CartItemDTO> items) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.pickupTime = pickupTime;
        this.pickupSlotId = pickupSlotId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.items = items;
    }

    // Getters and setters
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public LocalDateTime getPickupTime() {
        return pickupTime;
    }

    public void setPickupTime(LocalDateTime pickupTime) {
        this.pickupTime = pickupTime;
    }

    public Long getPickupSlotId() {
        return pickupSlotId;
    }

    public void setPickupSlotId(Long pickupSlotId) {
        this.pickupSlotId = pickupSlotId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
    
    public Integer getPickupDay() { return pickupDay; }
    public void setPickupDay(Integer pickupDay) { this.pickupDay = pickupDay; }

    // Alias for compatibility with code expecting getPickupAt()
    public java.time.LocalDateTime getPickupAt() {
        return getPickupTime();
    }

    @Override
    public String toString() {
        return "CreateOrderRequest{" +
                "customerName='" + customerName + '\'' +
                ", customerEmail='" + customerEmail + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", pickupTime=" + pickupTime +
                ", pickupSlotId=" + pickupSlotId +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", items=" + items +
                '}';
    }
}
