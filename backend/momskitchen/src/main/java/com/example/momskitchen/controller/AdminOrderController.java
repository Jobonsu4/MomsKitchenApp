package com.example.momskitchen.controller;

import com.example.momskitchen.model.Order;
import com.example.momskitchen.model.OrderItem;
import com.example.momskitchen.model.OrderItemAddon;
import com.example.momskitchen.repository.OrderRepository;
import com.example.momskitchen.dto.OrderListItemDTO;
import com.example.momskitchen.dto.OrderSummaryDTO;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Admin endpoints (secured by your AdminApiKeyFilter via X-Admin-Key).
 * Base path: /api/admin/orders
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepository;

    public AdminOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ---------------------------------------------
    // GET /api/admin/orders?status=&paymentStatus=&page=0&size=20&sort=createdAt,desc
    // ---------------------------------------------
    @GetMapping
    public ResponseEntity<Page<OrderListItemDTO>> list(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "paymentStatus", required = false) String paymentStatus,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sortParam
    ) {
        Pageable pageable = resolvePageable(page, size, sortParam);

        Page<Order> pageResult = findOrders(status, paymentStatus, pageable);

        Page<OrderListItemDTO> dtoPage = pageResult.map(this::toListItemDTO);
        return ResponseEntity.ok(dtoPage);
    }

    // ---------------------------------------------
    // GET /api/admin/orders/{id}
    // ---------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<OrderSummaryDTO> details(@PathVariable Long id) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        OrderSummaryDTO dto = toSummaryDTO(opt.get());
        return ResponseEntity.ok(dto);
    }

    // ---------------------------------------------
    // PUT /api/admin/orders/{id}/status/{newStatus}
    // ---------------------------------------------
    @PutMapping("/{id}/status/{newStatus}")
    public ResponseEntity<OrderListItemDTO> updateStatus(
            @PathVariable Long id,
            @PathVariable String newStatus
    ) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Order order = opt.get();
        order.setStatus(normalize(newStatus));
        orderRepository.save(order);

        return ResponseEntity.ok(toListItemDTO(order));
    }

    // ---------------------------------------------
    // PUT /api/admin/orders/{id}/payment/{newPaymentStatus}
    // ---------------------------------------------
    @PutMapping("/{id}/payment/{newPaymentStatus}")
    public ResponseEntity<OrderListItemDTO> updatePayment(
            @PathVariable Long id,
            @PathVariable String newPaymentStatus
    ) {
        Optional<Order> opt = orderRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Order order = opt.get();
        order.setPaymentStatus(normalize(newPaymentStatus));
        orderRepository.save(order);

        return ResponseEntity.ok(toListItemDTO(order));
    }

    // =========================
    // Helpers
    // =========================

    private Pageable resolvePageable(int page, int size, String sortParam) {
        // supports "field,dir" (e.g., "createdAt,desc"); defaults to DESC
        String[] parts = sortParam.split(",");
        String field = parts.length > 0 ? parts[0].trim() : "createdAt";
        Sort.Direction dir = (parts.length > 1 && parts[1].equalsIgnoreCase("asc")) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, field));
        }

    /**
     * Finds orders with optional status/paymentStatus filters.
     *
     * NOTE: For best performance, declare these Spring Data methods in OrderRepository:
     *   Page<Order> findByStatusAndPaymentStatus(String status, String paymentStatus, Pageable p);
     *   Page<Order> findByStatus(String status, Pageable p);
     *   Page<Order> findByPaymentStatus(String paymentStatus, Pageable p);
     *   Page<Order> findAll(Pageable p);
     */
    private Page<Order> findOrders(String status, String paymentStatus, Pageable pageable) {
        String s = emptyToNull(status);
        String p = emptyToNull(paymentStatus);

        // If your OrderRepository has these methods declared, Spring Data will implement them automatically.
        if (s != null && p != null) {
            return orderRepository.findByStatusAndPaymentStatus(s, p, pageable);
        } else if (s != null) {
            return orderRepository.findByStatus(s, pageable);
        } else if (p != null) {
            return orderRepository.findByPaymentStatus(p, pageable);
        } else {
            return orderRepository.findAll(pageable);
        }
    }

    private String emptyToNull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private String normalize(String v) {
        return v == null ? null : v.trim().toUpperCase();
    }

    // =========================
    // Mapping to DTOs
    // =========================

    private OrderListItemDTO toListItemDTO(Order o) {
        OrderListItemDTO dto = new OrderListItemDTO();
        dto.setOrderId(o.getId());
        dto.setOrderCode(o.getOrderCode());
        dto.setCustomerName(o.getCustomerName());
        dto.setCustomerPhone(o.getCustomerPhone());
        dto.setPickupTime(o.getPickupAt());
        dto.setTotal(o.getTotalAmount() != null ? o.getTotalAmount().doubleValue() : 0.0);
        dto.setPickupStatus(o.getStatus());
        dto.setPaymentStatus(o.getPaymentStatus());
        dto.setCreatedAt(o.getCreatedAt());
        return dto;
    }

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
