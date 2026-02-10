package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    
    List<OrderItem> findByOrder(Order order);
    
    // ✅ Delete all items in an order
    void deleteByOrder(Order order);
}
