package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ✅ Get all orders of a user (order history)
    List<Order> findByUserOrderByCreatedAtDesc(User user);
}
