package com.celebrationpoint.backend.service.payment;

import com.celebrationpoint.backend.config.PaytmConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.*;

/**
 * Paytm Payment Service
 * 
 * Handles Paytm-specific operations:
 * - Checksum generation (for secure transactions)
 * - Checksum verification (for webhook callback validation)
 * 
 * SECURITY NOTE: All cryptographic operations happen ONLY on backend.
 * Merchant key is NEVER exposed to frontend.
 */
@Service
public class PaytmService {

    @Autowired
    private PaytmConfig paytmConfig;

    private static final String ALGORITHM = "HmacSHA256";
    private static final String ENCODING = "UTF-8";

    /**
     * Generate Paytm Checksum
     * Used to sign transaction parameters before redirecting to Paytm gateway
     * 
     * @param params Transaction parameters (sorted by key)
     * @return Base64 encoded checksum string
     */
    public String generateChecksum(Map<String, String> params) {
        if (!paytmConfig.isConfigured()) {
            throw new RuntimeException("Paytm configuration is missing. Check merchant key and ID.");
        }

        try {
            String merchantKey = paytmConfig.getMerchantKey();
            
            // Create string from parameters (sorted)
            TreeMap<String, String> sortedParams = new TreeMap<>(params);
            StringBuilder checkSumString = new StringBuilder();
            
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (!entry.getKey().equals("CHECKSUMHASH")) {
                    if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                        checkSumString.append(entry.getValue()).append("|");
                    }
                }
            }

            // Initialize HMAC
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(merchantKey.getBytes(ENCODING), 0, merchantKey.getBytes(ENCODING).length, ALGORITHM);
            mac.init(secretKey);

            // Generate checksum hash
            byte[] rawHmac = mac.doFinal(checkSumString.toString().getBytes(ENCODING));
            String checksum = Base64.getEncoder().encodeToString(rawHmac);

            return checksum;
        } catch (Exception e) {
            throw new RuntimeException("Checksum generation failed: " + e.getMessage());
        }
    }

    /**
     * Verify Paytm Checksum (from callback)
     * Validates that the callback is authentic from Paytm
     * 
     * @param params Transaction parameters received from Paytm
     * @param checksum Checksum received in callback
     * @return true if checksum is valid, false otherwise
     */
    public boolean verifyChecksum(Map<String, String> params, String checksum) {
        if (!paytmConfig.isConfigured()) {
            throw new RuntimeException("Paytm configuration is missing");
        }

        try {
            String merchantKey = paytmConfig.getMerchantKey();
            
            // Create string from parameters (sorted, excluding CHECKSUMHASH)
            TreeMap<String, String> sortedParams = new TreeMap<>(params);
            StringBuilder checkSumString = new StringBuilder();
            
            for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                if (!entry.getKey().equals("CHECKSUMHASH")) {
                    if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                        checkSumString.append(entry.getValue()).append("|");
                    }
                }
            }

            // Initialize HMAC
            Mac mac = Mac.getInstance(ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(merchantKey.getBytes(ENCODING), 0, merchantKey.getBytes(ENCODING).length, ALGORITHM);
            mac.init(secretKey);

            // Generate expected checksum hash
            byte[] rawHmac = mac.doFinal(checkSumString.toString().getBytes(ENCODING));
            String expectedChecksum = Base64.getEncoder().encodeToString(rawHmac);

            // Compare with received checksum
            return expectedChecksum.equals(checksum);
        } catch (Exception e) {
            throw new RuntimeException("Checksum verification failed: " + e.getMessage());
        }
    }

    /**
     * Generate Paytm Transaction Parameters
     * Used before redirecting user to Paytm Secure Gateway
     * 
     * @param orderId Order ID
     * @param amount Payment amount
     * @param userEmail Customer email
     * @param userPhone Customer phone
     * @return Map of parameters to be POSTed to Paytm
     */
    public Map<String, String> generateTransactionParams(
            Long orderId,
            String amount,
            String userEmail,
            String userPhone
    ) {
        Map<String, String> params = new HashMap<>();
        
        // Mandatory parameters
        params.put("MID", paytmConfig.getMerchantId());
        params.put("ORDER_ID", "ORDER-" + orderId + "-" + System.currentTimeMillis());  // Unique ID
        params.put("CUST_ID", orderId.toString());  // Customer ID
        params.put("TXN_AMOUNT", amount);
        params.put("CHANNEL_ID", paytmConfig.getChannelId());
        params.put("WEBSITE", paytmConfig.getWebsite());
        params.put("INDUSTRY_TYPE_ID", paytmConfig.getIndustryType());
        
        // Callback URL for Paytm to notify on transaction completion
        params.put("CALLBACK_URL", paytmConfig.getCallbackUrl());
        
        // Customer details
        params.put("EMAIL", userEmail);
        params.put("MOBILE_NO", userPhone);

        // Generate and add checksum
        String checksum = generateChecksum(params);
        params.put("CHECKSUMHASH", checksum);

        return params;
    }
}
