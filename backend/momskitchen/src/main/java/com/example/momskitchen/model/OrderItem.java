package com.example.momskitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "order_item",
       indexes = @Index(name = "ix_oi_order", columnList = "order_id"))
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** EAGER: parent order */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** EAGER: optional reference to the catalog item used */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_item_id")
    private MenuItem menuItem;

    @Column(name = "item_name", nullable = false, length = 160)
    private String itemName;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "line_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal lineSubtotal;

    /** EAGER: add-ons chosen for this line */
    @OneToMany(mappedBy = "orderItem", fetch = FetchType.EAGER,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItemAddon> addons = new ArrayList<>();
}
