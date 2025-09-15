package com.example.momskitchen.repository;

import com.example.momskitchen.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** All items for a given order, ordered by id */
    List<OrderItem> findByOrderIdOrderByIdAsc(Long orderId);

    /** Find items for an order filtered by menu item id (e.g., "how many Jollof in order X") */
    List<OrderItem> findByOrderIdAndMenuItemId(Long orderId, Long menuItemId);

    /** Count items for an order (handy for quick summaries) */
    long countByOrderId(Long orderId);
}
