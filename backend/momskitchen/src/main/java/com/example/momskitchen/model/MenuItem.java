package com.example.momskitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "menu_item",
       indexes = @Index(name = "ix_item_category", columnList = "category_id, is_available, display_order"))
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** EAGER: owning category */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private MenuCategory category;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean available = Boolean.TRUE;

    @Column(name = "image_url", length = 600)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    /** EAGER: allowed add-ons for this item */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "menu_item_addon",
        joinColumns = @JoinColumn(name = "menu_item_id"),
        inverseJoinColumns = @JoinColumn(name = "addon_id")
    )
    @Builder.Default
    private Set<Addon> allowedAddons = new HashSet<>();
}
