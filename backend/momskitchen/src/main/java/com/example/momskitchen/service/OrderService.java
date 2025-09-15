package com.example.momskitchen.service;

import com.example.momskitchen.dto.CartAddonDTO;
import com.example.momskitchen.dto.CartItemDTO;
import com.example.momskitchen.dto.CreateOrderRequest;
import com.example.momskitchen.model.Addon;
import com.example.momskitchen.model.MenuItem;
import com.example.momskitchen.model.Order;
import com.example.momskitchen.model.OrderItem;
import com.example.momskitchen.model.OrderItemAddon;
import com.example.momskitchen.model.PickupSlot;
import com.example.momskitchen.repository.AddonRepository;
import com.example.momskitchen.repository.MenuItemRepository;
import com.example.momskitchen.repository.OrderRepository;
import com.example.momskitchen.repository.PickupSlotRepository;
import com.example.momskitchen.util.OrderCodeGenerator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

/**
 * Handles creating and fetching Orders.
 * Notes:
 *  - Snapshots item/add-on names & prices to keep history stable even if catalog changes later.
 *  - Uses repositories to fetch MenuItem/Addons and validate references.
 *  - Defensive pickup validation (controller already calls PickupService; we double-check here).
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final AddonRepository addonRepository;
    private final PickupSlotRepository pickupSlotRepository;
    private final PricingService pricingService;
    private final PickupService pickupService;

    public OrderService(OrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        AddonRepository addonRepository,
                        PickupSlotRepository pickupSlotRepository,
                        PricingService pricingService,
                        PickupService pickupService) {
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.addonRepository = addonRepository;
        this.pickupSlotRepository = pickupSlotRepository;
        this.pricingService = pricingService;
        this.pickupService = pickupService;
    }

    /**
     * Create and persist an Order from a cart request.
     * Steps:
     *  1) Validate pickup (day/slot/dateTime).
     *  2) Build Order + OrderItems + OrderItemAddons (snapshot names/prices).
     *  3) Compute totals via PricingService.
     *  4) Assign human-friendly order code and save.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        // ---- 1) Validate pickup defensively (controller already does this)
        pickupService.validatePickup(req.getPickupDay(), req.getPickupSlotId(), req.getPickupAt());

        // Optional: resolve the selected pickup slot (nullable)
        PickupSlot slot = null;
        if (req.getPickupSlotId() != null) {
            slot = pickupSlotRepository.findById(req.getPickupSlotId())
                    .orElse(null); // keep null if not found; validation above should have caught errors
        }

        // ---- 2) Build the Order skeleton
        Order order = new Order();
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setPickupAt(resolvePickupAt(req));
        order.setPickupSlot(slot);
        order.setCustomerName(req.getCustomerName());
        order.setCustomerEmail(req.getCustomerEmail());
        // normalize phone before persisting so lookups match stored format
        order.setCustomerPhone(normalizePhone(req.getCustomerPhone()));
        // notes optional; request may not supply it

        // Build line items with snapshots
        List<OrderItem> orderItems = new ArrayList<>();
        if (req.getItems() != null) {
            for (CartItemDTO cartLine : req.getItems()) {
                OrderItem oi = buildOrderItem(cartLine);
                oi.setOrder(order);           // back-reference
                orderItems.add(oi);
            }
        }
        order.setItems(orderItems);

        // ---- 3) Compute totals with PricingService (keeps one source of truth)
        var quote = pricingService.quote(req);
        order.setSubtotal(quote.getSubtotal());
        order.setTaxAmount(quote.getTax());
        order.setTotalAmount(quote.getTotal());

        // ---- 4) Assign human-friendly order code & persist
        order.setOrderCode(generateUniqueOrderCode());
        return orderRepository.save(order);
    }

    /**
     * Lookup an order by orderCode + phone (for customer self-serve).
     */
    public Optional<Order> findByCodeAndPhone(String code, String phone) {
        if (code == null || phone == null) return Optional.empty();
        String normalizedCode = code.trim();
        String normalizedPhone = normalizePhone(phone);
        return orderRepository.findByOrderCodeAndCustomerPhone(normalizedCode, normalizedPhone);
    }

    // =========================
    // Internal helpers
    // =========================

    /**
     * Build an OrderItem from a cart line, snapshotting name & price and mapping addons.
     */
    private OrderItem buildOrderItem(CartItemDTO cartLine) {
        if (cartLine == null || cartLine.getItemId() == null) {
            throw new IllegalArgumentException("Invalid cart line: missing itemId");
        }

        MenuItem item = menuItemRepository.findById(cartLine.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("Menu item not found: " + cartLine.getItemId()));

        int qty = (cartLine.getQuantity() != null && cartLine.getQuantity() > 0) ? cartLine.getQuantity() : 1;

        OrderItem oi = new OrderItem();
        oi.setMenuItem(item);                          // keep reference (nullable in schema, but we set it)
        oi.setItemName(item.getName());                // snapshot
        oi.setUnitPrice(item.getPrice());              // snapshot
        oi.setQuantity(qty);

        // Add-ons: map ids to entities, snapshot name + priceDelta
        List<OrderItemAddon> chosen = new ArrayList<>();
        if (cartLine.getAddons() != null && !cartLine.getAddons().isEmpty()) {
            // create a Set of allowed IDs for validation (optional, since your PricingService likely validates)
            Set<Long> allowed = new HashSet<>();
            item.getAllowedAddons().forEach(a -> allowed.add(a.getId()));

            for (CartAddonDTO a : cartLine.getAddons()) {
                if (a == null || a.getAddonId() == null) continue;
                Long addonId = a.getAddonId();
                // Optional validation: ensure addon belongs to allowed set
                if (!allowed.isEmpty() && !allowed.contains(addonId)) {
                    throw new IllegalArgumentException("Addon " + addonId + " is not allowed for item " + item.getId());
                }

                Addon addon = addonRepository.findById(addonId)
                        .orElseThrow(() -> new IllegalArgumentException("Addon not found: " + addonId));

                OrderItemAddon oia = new OrderItemAddon();
                oia.setOrderItem(oi);
                oia.setAddon(addon);                   // keep reference
                oia.setAddonName(addon.getName());     // snapshot
                oia.setPriceDelta(safe(addon.getPriceDelta())); // snapshot
                chosen.add(oia);
            }
        }
        oi.setAddons(chosen);

        // Calculate line subtotal = unitPrice * qty + sum(addon deltas * qty or per-item?)
        // For simplicity, treat addon delta as per unit:
        BigDecimal base = safe(item.getPrice()).multiply(BigDecimal.valueOf(qty));
        BigDecimal addonSumPerUnit = chosen.stream()
                .map(OrderItemAddon::getPriceDelta)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal addonsTotal = addonSumPerUnit.multiply(BigDecimal.valueOf(qty));
        oi.setLineSubtotal(base.add(addonsTotal));

        return oi;
    }

    private String normalizePhone(String raw) {
        if (raw == null) return null;
        // keep only digits; your frontend has phone.ts but we normalize defensively on the backend too
        return raw.replaceAll("\\D", "");
    }

    private BigDecimal safe(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String generateUniqueOrderCode() {
        // Try a few times to avoid rare collisions
        for (int i = 0; i < 5; i++) {
            String code = OrderCodeGenerator.generate();
            if (!orderRepository.existsByOrderCode(code)) {
                return code;
            }
        }
        // Extremely unlikely fallback
        return OrderCodeGenerator.generate() + System.currentTimeMillis() % 1000;
    }

    /**
     * Determine a concrete pickup timestamp to persist.
     *
     * Rules:
     * - If the request provides pickupAt, use it as-is.
     * - Else, if a pickupDay (0..6) is provided, choose the next occurrence of that day.
     *   Use the earliest active slot's start_time as the time-of-day, falling back to 12:00 if none.
     * - Else, fall back to now (dev-friendly default).
     */
    private LocalDateTime resolvePickupAt(CreateOrderRequest req) {
        if (req.getPickupAt() != null) return req.getPickupAt();

        Integer day = req.getPickupDay();
        if (day == null || day < 0 || day > 6) {
            return LocalDateTime.now();
        }

        // Prefer earliest active slot start time
        var slots = pickupSlotRepository.findByDayOfWeekAndActiveTrueOrderByStartTimeAsc(day);
        LocalTime time = slots != null && !slots.isEmpty() ? slots.get(0).getStartTime() : LocalTime.NOON;

        DayOfWeek targetDow = switch (day) {
            case 0 -> DayOfWeek.SUNDAY;
            case 1 -> DayOfWeek.MONDAY;
            case 2 -> DayOfWeek.TUESDAY;
            case 3 -> DayOfWeek.WEDNESDAY;
            case 4 -> DayOfWeek.THURSDAY;
            case 5 -> DayOfWeek.FRIDAY;
            case 6 -> DayOfWeek.SATURDAY;
            default -> DayOfWeek.FRIDAY; // unreachable due to bounds check
        };

        LocalDate today = LocalDate.now();
        LocalDate candidateDate = today.with(TemporalAdjusters.nextOrSame(targetDow));
        LocalDateTime candidate = LocalDateTime.of(candidateDate, time);
        if (candidate.isBefore(LocalDateTime.now())) {
            // If the time today has already passed, use next week
            candidateDate = today.with(TemporalAdjusters.next(targetDow));
            candidate = LocalDateTime.of(candidateDate, time);
        }
        return candidate;
    }
}
