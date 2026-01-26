package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.entity.Payment;
import com.celebrationpoint.backend.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundRepository extends JpaRepository<Refund, Long> {

    // ✅ Get refund by payment (prevent duplicate refunds)
    Optional<Refund> findByPayment(Payment payment);
}
