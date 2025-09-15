package com.example.momskitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "addon")
public class Addon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "price_delta", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal priceDelta = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = Boolean.TRUE;

    /** EAGER inverse side: items that allow this add-on */
    @ManyToMany(mappedBy = "allowedAddons", fetch = FetchType.EAGER)
    @Builder.Default
    private Set<MenuItem> items = new HashSet<>();
}
