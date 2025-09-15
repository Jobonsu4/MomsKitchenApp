package com.example.momskitchen.controller;

import com.example.momskitchen.dto.CreateOrderRequest;
import com.example.momskitchen.dto.OrderSummaryDTO;
import com.example.momskitchen.dto.QuoteResponse;
import com.example.momskitchen.model.Order;
import com.example.momskitchen.model.OrderItem;
import com.example.momskitchen.model.OrderItemAddon;
import com.example.momskitchen.service.OrderService;
import com.example.momskitchen.service.PricingService;
import com.example.momskitchen.service.PickupService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Public customer order APIs.
 * Base path: /api/orders
 *
 * Endpoints:
 *  - POST /api/orders/quote        : price a cart
 *  - POST /api/orders              : place order (returns summary with code)
 *  - GET  /api/orders/{orderCode}  : lookup by orderCode + phone
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PricingService pricingService;
    private final PickupService pickupService;
    private final OrderService orderService;

    public OrderController(PricingService pricingService,
                           PickupService pickupService,
                           OrderService orderService) {
        this.pricingService = pricingService;
        this.pickupService = pickupService;
        this.orderService = orderService;
    }

    // ---------------------------------------------------------
    // POST /api/orders/quote
    // ---------------------------------------------------------
    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> quote(@Valid @RequestBody CreateOrderRequest req) {
        // Validate pickup timing (e.g., within configured slots)
        pickupService.validatePickup(req.getPickupDay(), req.getPickupSlotId(), req.getPickupAt());

        // Compute subtotal/tax/total based on items + addons
        QuoteResponse quote = pricingService.quote(req);
        return ResponseEntity.ok(quote);
    }

    // ---------------------------------------------------------
    // POST /api/orders
    // ---------------------------------------------------------
    @PostMapping
    public ResponseEntity<OrderSummaryDTO> create(@Valid @RequestBody CreateOrderRequest req) {
        // Validate pickup first
        pickupService.validatePickup(req.getPickupDay(), req.getPickupSlotId(), req.getPickupAt());

        // Create order (persists header + items + addons and assigns order code)
        Order order = orderService.createOrder(req);

        // Return a clean summary for confirmation page
        return ResponseEntity.ok(toSummaryDTO(order));
    }

    // ---------------------------------------------------------
    // GET /api/orders/{orderCode}?phone=3025550123
    // ---------------------------------------------------------
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderSummaryDTO> lookup(
            @PathVariable String orderCode,
            @RequestParam("phone") String phone
    ) {
        Optional<Order> opt = orderService.findByCodeAndPhone(orderCode, phone);
        return opt.map(o -> ResponseEntity.ok(toSummaryDTO(o)))
                  .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // =========================
    // Mapping: Entity -> DTO
    // =========================
    private OrderSummaryDTO toSummaryDTO(Order o) {
        OrderSummaryDTO dto = new OrderSummaryDTO();
        dto.setId(o.getId());
        dto.setOrderCode(o.getOrderCode());
        dto.setStatus(o.getStatus());
        dto.setPaymentStatus(o.getPaymentStatus());
        dto.setPickupAt(o.getPickupAt());
        dto.setCustomerName(o.getCustomerName());
        dto.setCustomerEmail(o.getCustomerEmail());
        dto.setCustomerPhone(o.getCustomerPhone());
        dto.setNotes(o.getNotes());
        dto.setSubtotal(o.getSubtotal());
        dto.setTaxAmount(o.getTaxAmount());
        dto.setTotalAmount(o.getTotalAmount());

        List<OrderSummaryDTO.Item> items = o.getItems().stream()
                .filter(Objects::nonNull)
                .map(this::toSummaryItem)
                .collect(Collectors.toList());
        dto.setItems(items);

        return dto;
    }

    private OrderSummaryDTO.Item toSummaryItem(OrderItem oi) {
        OrderSummaryDTO.Item item = new OrderSummaryDTO.Item();
        item.setItemName(oi.getItemName());
        item.setUnitPrice(oi.getUnitPrice());
        item.setQuantity(oi.getQuantity());
        item.setLineSubtotal(oi.getLineSubtotal());

        List<OrderSummaryDTO.ItemAddon> addons = oi.getAddons().stream()
                .filter(Objects::nonNull)
                .map(this::toSummaryAddon)
                .collect(Collectors.toList());
        item.setAddons(addons);
        return item;
    }

    private OrderSummaryDTO.ItemAddon toSummaryAddon(OrderItemAddon a) {
        OrderSummaryDTO.ItemAddon dto = new OrderSummaryDTO.ItemAddon();
        dto.setAddonName(a.getAddonName());
        dto.setPriceDelta(a.getPriceDelta());
        return dto;
    }
}
