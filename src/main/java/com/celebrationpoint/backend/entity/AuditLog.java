package com.celebrationpoint.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Action type
    // e.g. ORDER_STATUS_CHANGED, PAYMENT_SUCCESS, ORDER_CANCELLED
    @Column(nullable = false)
    private String action;

    // Entity type
    // e.g. ORDER, PAYMENT
    @Column(nullable = false)
    private String entityType;

    // ID of the entity (orderId, paymentId, etc.)
    @Column(nullable = false)
    private Long entityId;

    // Old value (status before change)
    @Column(length = 100)
    private String oldValue;

    // New value (status after change)
    @Column(length = 100)
    private String newValue;

    // Email of user/admin who performed the action
    @Column(nullable = false)
    private String performedBy;

    // Role of performer (USER / ADMIN)
    @Column(nullable = false)
    private String role;

    // IP Address of the user performing the action
    @Column(length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // ======================
    // CONSTRUCTORS
    // ======================

    public AuditLog() {
    }

    // ======================
    // GETTERS & SETTERS
    // ======================

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getPerformedBy() {
        return performedBy;
    }

    public void setPerformedBy(String performedBy) {
        this.performedBy = performedBy;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
