package com.celebrationpoint.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Centralized Frontend URL Configuration
 * 
 * PURPOSE:
 * - Single source of truth for frontend URL across the entire application
 * - Supports environment-specific configuration (dev, staging, production)
 * - Eliminates hardcoded localhost URLs in Java code
 * 
 * USAGE:
 * Inject this component into any service/controller that needs the frontend URL:
 * 
 *   @Autowired
 *   private FrontendConfig frontendConfig;
 *   
 *   String url = frontendConfig.getUrl();
 * 
 * CONFIGURATION:
 * - Local Dev: Set FRONTEND_URL env var to http://localhost:5173
 * - Netlify: Set FRONTEND_URL env var to your Netlify domain
 * - Railway: Set FRONTEND_URL env var to your Railway domain
 * 
 * DEFAULT FALLBACK:
 * If FRONTEND_URL env var is not set, defaults to http://localhost:5173
 */
@Component
public class FrontendConfig {

    @Value("${frontend.url:https://celebrationpointdevtestforclient.netlify.app}")
    private String frontendUrl;

    /**
     * Get the configured frontend URL
     * 
     * @return Frontend URL (e.g., http://localhost:5173 or https://example.netlify.app)
     */
    public String getUrl() {
        return frontendUrl;
    }

    /**
     * Get the frontend callback URL for payment gateways
     * 
     * @param path The callback path (e.g., /paytm-callback)
     * @return Full callback URL (e.g., http://localhost:5173/paytm-callback)
     */
    public String getCallbackUrl(String path) {
        return frontendUrl + path;
    }
}
