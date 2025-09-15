package com.example.momskitchen.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "pickup_slot",
       indexes = @Index(name = "ix_slot_active", columnList = "day_of_week, is_active"))
public class PickupSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 0=Sun ... 6=Sat */
    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = Boolean.TRUE;
}
