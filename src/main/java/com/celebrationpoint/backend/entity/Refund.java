package com.celebrationpoint.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ One refund per payment
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    @JsonIgnore
    private Payment payment;

    // ✅ Refund amount
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    // ✅ Refund status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status;

    // ✅ Gateway refund id (optional)
    private String providerRefundId;

    // ✅ Timestamp
    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ===============================
    // 🔧 CONSTRUCTORS
    // ===============================
    public Refund() {}

    public Refund(Payment payment, BigDecimal amount, RefundStatus status) {
        this.payment = payment;
        this.amount = amount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // ===============================
    // 🔄 JPA LIFECYCLE
    // ===============================
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ===============================
    // 📥 GETTERS & SETTERS
    // ===============================
    public Long getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public String getProviderRefundId() {
        return providerRefundId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
    }

    public void setProviderRefundId(String providerRefundId) {
        this.providerRefundId = providerRefundId;
    }
}
