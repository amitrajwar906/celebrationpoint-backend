package com.celebrationpoint.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Paytm Payment Gateway Configuration
 * 
 * Merchant credentials are loaded from environment variables for security.
 * NEVER expose merchant key in frontend or logs.
 */
@Component
public class PaytmConfig {

    @Value("${paytm.merchant.id:}")
    private String merchantId;

    @Value("${paytm.merchant.key:}")
    private String merchantKey;

    @Value("${paytm.website:WEBSTAGING}")
    private String website;

    @Value("${paytm.channel.id:WEB}")
    private String channelId;

    @Value("${paytm.industry.type:Retail}")
    private String industryType;

    @Value("${paytm.callback.url:}")
    private String callbackUrl;

    @Value("${paytm.gateway.url:https://securegw-stage.paytm.in}")
    private String gatewayUrl;

    public String getMerchantId() {
        return merchantId;
    }

    public String getMerchantKey() {
        return merchantKey;
    }

    public String getWebsite() {
        return website;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getIndustryType() {
        return industryType;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public boolean isConfigured() {
        return merchantId != null && !merchantId.isEmpty() &&
                merchantKey != null && !merchantKey.isEmpty();
    }
}
