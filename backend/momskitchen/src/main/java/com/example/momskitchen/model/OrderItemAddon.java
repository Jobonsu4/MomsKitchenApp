package com.example.momskitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "order_item_addon",
       indexes = @Index(name = "ix_oia_oi", columnList = "order_item_id"))
public class OrderItemAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** EAGER: owning line item */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;

    /** EAGER: optional link back to catalog Addon */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "addon_id")
    private Addon addon;

    @Column(name = "addon_name", nullable = false, length = 160)
    private String addonName;

    @Column(name = "price_delta", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceDelta = BigDecimal.ZERO;
}
