package com.celebrationpoint.backend.entity;

public enum PaymentStatus {

    INITIATED,        // Online payment started
    SUCCESS,          // Online payment success
    FAILED,           // Online payment failed

    COD_PENDING,      // COD order placed, payment pending
    COD_PAID,         // COD amount collected

    REFUNDED          // Online payment refunded
}
