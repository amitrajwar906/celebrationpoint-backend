package com.celebrationpoint.backend.service.payment;

import com.celebrationpoint.backend.entity.*;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.OrderRepository;
import com.celebrationpoint.backend.repository.PaymentRepository;
import com.celebrationpoint.backend.service.audit.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AuditLogService auditLogService;

    // =================================================
    // 💳 ONLINE PAYMENT INITIATION
    // =================================================
    @Transactional
    public Payment initiatePayment(Long orderId, String provider, String userEmail) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        paymentRepository.findByOrder(order).ifPresent(p -> {
            throw new RuntimeException("Payment already exists for this order");
        });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(PaymentMethod.ONLINE);
        payment.setProvider(provider);
        payment.setStatus(PaymentStatus.INITIATED);
        payment.setCreatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        // 📝 AUDIT LOG
        auditLogService.logAction(
                "PAYMENT_INITIATED",
                "PAYMENT",
                payment.getId(),
                null,
                PaymentStatus.INITIATED.name(),
                userEmail,
                "USER"
        );

        return payment;
    }

    // =================================================
    // 💵 CASH ON DELIVERY PAYMENT
    // =================================================
    @Transactional
    public void createCodPayment(Order order) {

        paymentRepository.findByOrder(order).ifPresent(p -> {
            throw new RuntimeException("Payment already exists for this order");
        });

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setMethod(PaymentMethod.COD);
        payment.setProvider("COD");
        payment.setStatus(PaymentStatus.COD_PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        payment = paymentRepository.save(payment);

        // Update order status to CONFIRMED for COD
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // AUDIT LOG
        auditLogService.logAction(
                "COD_PAYMENT_CREATED",
                "PAYMENT",
                payment.getId(),
                null,
                PaymentStatus.COD_PENDING.name(),
                order.getUser().getEmail(),
                "USER"
        );
    }

    // =================================================
    // ✅ PAYMENT SUCCESS (ONLINE / COD)
    // =================================================
    @Transactional
    public void markPaymentSuccess(
            Long paymentId,
            String providerPaymentId,
            String performedBy,
            String role
    ) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        String oldStatus = payment.getStatus().name();

        if (payment.getMethod() == PaymentMethod.COD) {
            payment.setStatus(PaymentStatus.COD_PAID);
        } else {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setProviderPaymentId(providerPaymentId);
        }

        paymentRepository.save(payment);

        Order order = payment.getOrder();
        String oldOrderStatus = order.getStatus().name();

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        // 📝 AUDIT LOG — PAYMENT
        auditLogService.logAction(
                "PAYMENT_SUCCESS",
                "PAYMENT",
                payment.getId(),
                oldStatus,
                payment.getStatus().name(),
                performedBy,
                role
        );

        // 📝 AUDIT LOG — ORDER
        auditLogService.logAction(
                "ORDER_CONFIRMED_AFTER_PAYMENT",
                "ORDER",
                order.getId(),
                oldOrderStatus,
                OrderStatus.CONFIRMED.name(),
                performedBy,
                role
        );
    }

    // =================================================
    // ❌ PAYMENT FAILED (ONLINE ONLY)
    // =================================================
    @Transactional
    public void markPaymentFailed(Long paymentId, String userEmail) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));

        if (payment.getMethod() == PaymentMethod.COD) {
            throw new RuntimeException("COD payment cannot fail");
        }

        String oldStatus = payment.getStatus().name();

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        // 📝 AUDIT LOG
        auditLogService.logAction(
                "PAYMENT_FAILED",
                "PAYMENT",
                payment.getId(),
                oldStatus,
                PaymentStatus.FAILED.name(),
                userEmail,
                "USER"
        );
    }

    // =================================================
    // 🔍 GET PAYMENT BY ORDER
    // =================================================
    @Transactional(readOnly = true)
    public Payment getPaymentByOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }
}

