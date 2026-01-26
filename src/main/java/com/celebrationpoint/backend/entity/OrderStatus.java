package com.celebrationpoint.backend.entity;

public enum OrderStatus {

    PENDING,        // Order created
    CONFIRMED,      // COD confirmed / payment initiated
    SHIPPED,        // Admin shipped
    DELIVERED,      // Delivered to customer
    COMPLETED,      // Closed order
    CANCELLED       // Cancelled before shipping
, PAID
}
