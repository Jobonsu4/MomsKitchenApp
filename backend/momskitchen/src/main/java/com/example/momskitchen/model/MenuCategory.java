package com.example.momskitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "menu_category",
       indexes = @Index(name = "ix_category_menu", columnList = "menu_id, is_active, display_order"))
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** EAGER: parent menu */
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "menu_id", nullable = false)
    private Menu menu;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = Boolean.TRUE;

    /** EAGER: items inside this category */
    @OneToMany(mappedBy = "category", fetch = FetchType.EAGER,
               cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<MenuItem> items = new ArrayList<>();
}
