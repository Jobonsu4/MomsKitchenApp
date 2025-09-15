package com.example.momskitchen.repository;

import com.example.momskitchen.model.PickupSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface PickupSlotRepository extends JpaRepository<PickupSlot, Long> {

    /** All active slots ordered by day_of_week and start_time */
    List<PickupSlot> findByActiveTrueOrderByDayOfWeekAscStartTimeAsc();

    /** All slots for a specific day (0=Sun ... 6=Sat) */
    List<PickupSlot> findByDayOfWeekOrderByStartTimeAsc(Integer dayOfWeek);

    /** Active slots for a given day, ordered */
    List<PickupSlot> findByDayOfWeekAndActiveTrueOrderByStartTimeAsc(Integer dayOfWeek);

    /** Find slots that match an exact window (rare, but useful for validation) */
    List<PickupSlot> findByDayOfWeekAndStartTimeAndEndTime(Integer dayOfWeek, LocalTime start, LocalTime end);
}
