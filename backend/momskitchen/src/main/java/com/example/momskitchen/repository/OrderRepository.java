package com.example.momskitchen.repository;

import com.example.momskitchen.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAll(Pageable pageable);
    Page<Order> findByStatus(String status, Pageable pageable);
    Page<Order> findByPaymentStatus(String paymentStatus, Pageable pageable);
    Page<Order> findByStatusAndPaymentStatus(String status, String paymentStatus, Pageable pageable);
    Optional<Order> findByOrderCodeAndCustomerPhone(String orderCode, String customerPhone);
    boolean existsByOrderCode(String orderCode);
}
