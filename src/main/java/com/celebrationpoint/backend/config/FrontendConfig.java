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
 * - Local Dev: Set FRONTEND_URLS env var to http://localhost:5173
 * - Multiple URLs: Set FRONTEND_URLS env var to comma-separated URLs (e.g., https://url1.com,https://url2.com)
 * 
 * DEFAULT FALLBACK:
 * If FRONTEND_URLS env var is not set, defaults to http://localhost:5173
 */
@Component
public class FrontendConfig {

    @Value("${frontend.urls:http://localhost:5173}")
    private String frontendUrls;

    /**
     * Get the list of configured frontend URLs
     * 
     * @return List of Frontend URLs (e.g., [http://localhost:5173, https://example.netlify.app])
     */
    public java.util.List<String> getUrls() {
        return java.util.Arrays.stream(frontendUrls.split(","))
                .map(String::trim)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Get the primary (first) configured frontend URL
     * 
     * @return Primary Frontend URL (e.g., http://localhost:5173)
     */
    public String getUrl() {
        return getUrls().get(0);
    }

    /**
     * Get the frontend callback URL for payment gateways
     * 
     * @param path The callback path (e.g., /paytm-callback)
     * @return Full callback URL using the primary URL (e.g., http://localhost:5173/paytm-callback)
     */
    public String getCallbackUrl(String path) {
        return getUrl() + path;
    }
}
