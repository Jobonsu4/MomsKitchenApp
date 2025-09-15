package com.example.momskitchen.service;

import com.example.momskitchen.model.PickupSlot;
import com.example.momskitchen.repository.PickupSlotRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Validates customer pickup selections against configured pickup slots.
 *
 * Config (application.yml):
 *   pickup:
 *     requireFutureMinutes: 30   # pickup time must be at least N minutes in the future
 *     strictDayMatch: true       # if true, pickupAt's day-of-week must equal selected pickupDay
 *     zoneId: UTC                # interpret pickupAt in this zone when computing day/time
 *
 * Conventions:
 *   - dayOfWeek: 0=Sun ... 6=Sat (matches DB and your entity)
 */
@Service
public class PickupService {

    private final PickupSlotRepository pickupSlotRepository;

    /** pickup must be at least this many minutes in the future (0 = allow now) */
    private final long requireFutureMinutes;

    /** if true, pickupAt day-of-week must equal provided pickupDay (0..6) */
    private final boolean strictDayMatch;

    /** Zone used to interpret pickupAt when checking day/time windows */
    private final ZoneId zoneId;

    public PickupService(PickupSlotRepository pickupSlotRepository,
                         @Value("${pickup.requireFutureMinutes:30}") long requireFutureMinutes,
                         @Value("${pickup.strictDayMatch:true}") boolean strictDayMatch,
                         @Value("${pickup.zoneId:UTC}") String zoneIdStr) {
        this.pickupSlotRepository = pickupSlotRepository;
        this.requireFutureMinutes = Math.max(0, requireFutureMinutes);
        this.strictDayMatch = strictDayMatch;
        this.zoneId = ZoneId.of(Objects.requireNonNullElse(zoneIdStr, "UTC"));
    }

    /**
     * Validate a requested pickup.
     *
     * @param pickupDay     0=Sun ... 6=Sat (nullable if slotId or pickupAt is provided)
     * @param pickupSlotId  optional chosen slot id (should be active)
     * @param pickupAt      chosen local date-time (nullable to allow "slot only" selection)
     *
     * Throws IllegalArgumentException with a clear message on validation failure.
     */
    public void validatePickup(Integer pickupDay, Long pickupSlotId, LocalDateTime pickupAt) {
        // 1) Load and validate the chosen slot (if any)
        PickupSlot chosenSlot = null;
        if (pickupSlotId != null) {
            chosenSlot = pickupSlotRepository.findById(pickupSlotId)
                    .orElseThrow(() -> new IllegalArgumentException("Pickup slot not found: " + pickupSlotId));
            if (!Boolean.TRUE.equals(chosenSlot.getActive())) {
                throw new IllegalArgumentException("Pickup slot is not active");
            }
        }

        // 2) If pickupAt provided, check "future" constraint
        if (pickupAt != null && requireFutureMinutes > 0) {
            var now = java.time.ZonedDateTime.now(zoneId);
            if (pickupAt.atZone(zoneId).isBefore(now.plusMinutes(requireFutureMinutes))) {
                throw new IllegalArgumentException("Pickup time must be at least " + requireFutureMinutes + " minutes from now");
            }
        }

        // 3) Determine effective day-of-week (0..6)
        Integer effectiveDay = pickupDay;
        if (effectiveDay == null && pickupAt != null) {
            effectiveDay = toZeroBasedDay(pickupAt.atZone(zoneId).getDayOfWeek());
        }
        if (effectiveDay != null && (effectiveDay < 0 || effectiveDay > 6)) {
            throw new IllegalArgumentException("pickupDay must be between 0 (Sun) and 6 (Sat)");
        }

        // 4) If strict day match, ensure pickupAt (if provided) matches pickupDay
        if (strictDayMatch && pickupAt != null && effectiveDay != null) {
            int actual = toZeroBasedDay(pickupAt.atZone(zoneId).getDayOfWeek());
            if (actual != effectiveDay) {
                throw new IllegalArgumentException("Pickup date does not match selected pickup day");
            }
        }

        // 5) Validate against slots
        if (chosenSlot != null) {
            // slot provided: optionally ensure day match and always ensure time window
            if (strictDayMatch && effectiveDay != null && !effectiveDay.equals(chosenSlot.getDayOfWeek())) {
                throw new IllegalArgumentException("Chosen slot is not available on the selected day");
            }
            if (pickupAt != null && !isWithinSlot(chosenSlot, pickupAt)) {
                throw new IllegalArgumentException("Pickup time is outside the chosen slot window");
            }
        } else {
            // no slot provided: ensure the day has at least one active slot (and time fits if pickupAt provided)
            if (effectiveDay == null) {
                // Allow a minimal flow where only pickupAt is provided; day derived above already
                throw new IllegalArgumentException("Either pickupDay or pickupSlotId must be provided");
            }
            var activeSlots = pickupSlotRepository.findByDayOfWeekAndActiveTrueOrderByStartTimeAsc(effectiveDay);
            if (activeSlots.isEmpty()) {
                throw new IllegalArgumentException("No active pickup slots for the selected day");
            }
            if (pickupAt != null) {
                boolean fitsAny = activeSlots.stream().anyMatch(s -> isWithinSlot(s, pickupAt));
                if (!fitsAny) {
                    throw new IllegalArgumentException("Pickup time does not fit any active slot on the selected day");
                }
            }
        }
    }

    /** List all active slots (sorted by day then start time) */
    public List<PickupSlot> getActiveSlots() {
        return pickupSlotRepository.findByActiveTrueOrderByDayOfWeekAscStartTimeAsc();
    }

    /** List active slots for a specific day (0..6) */
    public List<PickupSlot> getActiveSlotsForDay(int dayOfWeek) {
        if (dayOfWeek < 0 || dayOfWeek > 6) {
            throw new IllegalArgumentException("dayOfWeek must be between 0 (Sun) and 6 (Sat)");
        }
        return pickupSlotRepository.findByDayOfWeekAndActiveTrueOrderByStartTimeAsc(dayOfWeek);
    }

    /** Fetch a single slot */
    public Optional<PickupSlot> getSlot(Long id) {
        return pickupSlotRepository.findById(id);
    }

    // =========================
    // Helpers
    // =========================

    private boolean isWithinSlot(PickupSlot slot, LocalDateTime pickupAt) {
        // Interpret pickupAt in the configured zone
        var zdt = pickupAt.atZone(zoneId);
        int actualDay = toZeroBasedDay(zdt.getDayOfWeek());
        // When strictDayMatch is disabled (dev friendliness), ignore day-of-week mismatches
        if (strictDayMatch && actualDay != slot.getDayOfWeek()) return false;

        LocalTime t = zdt.toLocalTime();
        // inclusive start, exclusive end is a common choice; adjust if you want inclusive end
        return !t.isBefore(slot.getStartTime()) && t.isBefore(slot.getEndTime());
    }

    private int toZeroBasedDay(DayOfWeek dow) {
        // Java: MONDAY=1..SUNDAY=7 → convert to 0(Sun)..6(Sat)
        return switch (dow) {
            case SUNDAY -> 0;
            case MONDAY -> 1;
            case TUESDAY -> 2;
            case WEDNESDAY -> 3;
            case THURSDAY -> 4;
            case FRIDAY -> 5;
            case SATURDAY -> 6;
        };
    }
}
