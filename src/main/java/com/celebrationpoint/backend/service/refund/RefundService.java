package com.celebrationpoint.backend.service.refund;

import com.celebrationpoint.backend.entity.*;
import com.celebrationpoint.backend.exception.ResourceNotFoundException;
import com.celebrationpoint.backend.repository.OrderRepository;
import com.celebrationpoint.backend.repository.PaymentRepository;
import com.celebrationpoint.backend.repository.RefundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefundService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private OrderRepository orderRepository;

    /**
     * ✅ INITIATE REFUND
     * Conditions:
     * - Order must be CANCELLED
     * - Payment must be SUCCESS
     * - Only one refund allowed
     */
    @Transactional
    public Refund initiateRefund(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found")
                );

        if (order.getStatus() != OrderStatus.CANCELLED) {
            throw new RuntimeException("Order is not cancelled");
        }

        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() ->
                        new RuntimeException("Payment not found for this order")
                );

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Payment is not successful, refund not allowed");
        }

        // Prevent duplicate refund
        refundRepository.findByPayment(payment).ifPresent(r -> {
            throw new RuntimeException("Refund already initiated for this payment");
        });

        Refund refund = new Refund(
                payment,
                payment.getAmount(),
                RefundStatus.INITIATED
        );

        refund = refundRepository.save(refund);

        // Mark payment as refunded
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return refund;
    }

    /**
     * ✅ MARK REFUND SUCCESS
     */
    @Transactional
    public void markRefundSuccess(Long refundId, String providerRefundId) {

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refund not found")
                );

        refund.setStatus(RefundStatus.SUCCESS);
        refund.setProviderRefundId(providerRefundId);

        refundRepository.save(refund);
    }

    /**
     * ❌ MARK REFUND FAILED
     */
    @Transactional
    public void markRefundFailed(Long refundId) {

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Refund not found")
                );

        refund.setStatus(RefundStatus.FAILED);
        refundRepository.save(refund);
    }
}
