package com.celebrationpoint.backend.controller.admin;

import com.celebrationpoint.backend.entity.Order;
import com.celebrationpoint.backend.entity.OrderStatus;
import com.celebrationpoint.backend.entity.PaymentMethod;
import com.celebrationpoint.backend.entity.PaymentStatus;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.OrderRepository;
import com.celebrationpoint.backend.repository.PaymentRepository;
import com.celebrationpoint.backend.service.audit.AuditLogService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderCancelController {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final AuditLogService auditLogService;

    public AdminOrderCancelController(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            AuditLogService auditLogService
    ) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.auditLogService = auditLogService;
    }

    // ===============================
    // ❌ ADMIN CANCEL ORDER
    // ===============================
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrderByAdmin(
            @PathVariable Long orderId,
            Authentication authentication
    ) {

        String adminEmail = authentication.getName();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        // ❌ VALIDATION
        if (order.getStatus() == OrderStatus.CANCELLED) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Order already cancelled")
            );
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", "Delivered orders cannot be cancelled")
            );
        }

        // 🔄 UPDATE ORDER STATUS
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // 💵 HANDLE PAYMENT REFUND
        paymentRepository.findByOrder(order).ifPresent(payment -> {

            if (payment.getMethod() != PaymentMethod.COD) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }

        });

        // 📝 AUDIT LOG
        auditLogService.logAction("ORDER_CANCELLED", "Admin cancelled order", orderId, adminEmail, "ADMIN", "ORDER", "CANCELLED");

        return ResponseEntity.ok(
                Map.of(
                        "orderId", orderId,
                        "status", "CANCELLED",
                        "message", "Order cancelled successfully"
                )
        );
    }
}
