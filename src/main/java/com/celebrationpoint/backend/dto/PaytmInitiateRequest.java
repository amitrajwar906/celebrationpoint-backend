package com.celebrationpoint.backend.dto;

/**
 * Request DTO for Paytm payment initiation
 */
public class PaytmInitiateRequest {
    private Long orderId;

    public PaytmInitiateRequest() {}

    public PaytmInitiateRequest(Long orderId) {
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
}
