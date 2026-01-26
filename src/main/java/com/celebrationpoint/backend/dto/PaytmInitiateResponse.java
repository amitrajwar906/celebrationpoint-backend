package com.celebrationpoint.backend.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * Response DTO for Paytm payment initiation
 * 
 * Contains parameters needed to redirect user to Paytm Secure Gateway
 */
public class PaytmInitiateResponse {
    private Long paymentId;
    private Long orderId;
    private String amount;
    private Map<String, String> paytmParams;  // Parameters to POST to Paytm

    public PaytmInitiateResponse() {
        this.paytmParams = new HashMap<>();
    }

    public PaytmInitiateResponse(Long paymentId, Long orderId, String amount) {
        this();
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Map<String, String> getPaytmParams() {
        return paytmParams;
    }

    public void setPaytmParams(Map<String, String> paytmParams) {
        this.paytmParams = paytmParams;
    }

    public void addParam(String key, String value) {
        this.paytmParams.put(key, value);
    }
}
