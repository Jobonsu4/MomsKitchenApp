package com.example.momskitchen.dto;

import java.time.LocalDateTime;

/**
 * OrderListItemDTO is a lightweight summary of an order,
 * used for displaying order lists (e.g. history, admin dashboard).
 */
public class OrderListItemDTO {

    private Long orderId;            // Unique order ID
    private String customerName;     // Customer's name
    private String customerPhone;    // Customer's phone (for quick lookup)
    private LocalDateTime pickupTime; // Scheduled pickup time
    private String pickupStatus;     // e.g. "SCHEDULED", "READY", "PICKED_UP"
    private String paymentStatus;    // e.g. "PENDING", "PAID"
    private double total;            // Final total for the order
    private LocalDateTime createdAt; // When the order was placed
    private String orderCode; // Added orderCode field

    // Default constructor
    public OrderListItemDTO() {}

    // All-args constructor
    public OrderListItemDTO(Long orderId, String customerName, String customerPhone,
                            LocalDateTime pickupTime, String pickupStatus,
                            String paymentStatus, double total, LocalDateTime createdAt, String orderCode) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.pickupTime = pickupTime;
        this.pickupStatus = pickupStatus;
        this.paymentStatus = paymentStatus;
        this.total = total;
        this.createdAt = createdAt;
        this.orderCode = orderCode;
    }

    // Getters and setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getPickupStatus() {
        return pickupStatus;
    }

    public void setPickupStatus(String pickupStatus) {
        this.pickupStatus = pickupStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    @Override
    public String toString() {
        return "OrderListItemDTO{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", customerPhone='" + customerPhone + '\'' +
                ", pickupTime=" + pickupTime +
                ", pickupStatus='" + pickupStatus + '\'' +
                ", paymentStatus='" + paymentStatus + '\'' +
                ", total=" + total +
                ", createdAt=" + createdAt +
                ", orderCode='" + orderCode + '\'' +
                '}';
    }
}
