package com.example.momskitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "`order`",
       indexes = {
           @Index(name = "ix_order_lookup", columnList = "customer_phone, order_code"),
           @Index(name = "ix_order_created", columnList = "created_at")
       })
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 12)
    private String orderCode;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "pickup_at", nullable = false)
    private LocalDateTime pickupAt;

    /** EAGER: the slot used/validated for this order (nullable) */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pickup_slot_id")
    private PickupSlot pickupSlot;

    @Column(name = "customer_name", nullable = false, length = 160)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 200)
    private String customerEmail;

    @Column(name = "customer_phone", nullable = false, length = 40)
    private String customerPhone;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "payment_status", nullable = false, length = 40)
    @Builder.Default
    private String paymentStatus = "UNPAID";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /** EAGER: load all order items with the order */
    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
}
