package com.celebrationpoint.backend.dto;

import com.celebrationpoint.backend.entity.PaymentMethod;
import com.celebrationpoint.backend.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String provider;
    private LocalDateTime createdAt;

    public PaymentResponse(
            Long paymentId,
            Long orderId,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            String provider,
            LocalDateTime createdAt
    ) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.provider = provider;
        this.createdAt = createdAt;
    }

    // getters only (no setters needed)
    public Long getPaymentId() { return paymentId; }
    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public String getProvider() { return provider; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
