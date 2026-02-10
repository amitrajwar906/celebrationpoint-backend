package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.entity.Payment;
import com.celebrationpoint.backend.entity.PaymentStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // ✅ Get payment by order
    Optional<Payment> findByOrder(Order order);
    
    // ✅ Delete payment by order
    void deleteByOrder(Order order);
    
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0)
        FROM Payment p
        WHERE p.status = :status
    """)
    Double getTotalRevenueByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0.0) FROM Payment p WHERE p.status = 'SUCCESS'")
    Double getTotalSuccessfulRevenue();
}
