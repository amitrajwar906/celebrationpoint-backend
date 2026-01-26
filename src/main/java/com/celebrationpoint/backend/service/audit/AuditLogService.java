package com.celebrationpoint.backend.service.audit;

import com.celebrationpoint.backend.entity.AuditLog;
import com.celebrationpoint.backend.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Extract client IP address from HttpServletRequest
     * Handles multiple proxy headers for production environments
     */
    public String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return "N/A";
            }
            
            HttpServletRequest request = attributes.getRequest();
            
            // Check various proxy headers (in order of priority)
            String[] ipHeaders = {
                "CF-Connecting-IP",      // Cloudflare
                "X-Forwarded-For",       // Standard proxy header
                "X-Real-IP",             // Nginx
                "X-Client-IP",           // Custom
                "X-Originating-IP",      // Apache
                "Proxy-Client-IP",       // Proxy
                "WL-Proxy-Client-IP"     // WebLogic
            };
            
            for (String header : ipHeaders) {
                String ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty()) {
                    // Handle comma-separated IPs (take the first one)
                    String clientIp = ip.split(",")[0].trim();
                    return normalizeIpAddress(clientIp);
                }
            }
            
            // Fallback to RemoteAddr
            String remoteAddr = request.getRemoteAddr();
            return normalizeIpAddress(remoteAddr);
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * Normalize IPv6 loopback to IPv4 loopback for readability
     */
    private String normalizeIpAddress(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "N/A";
        }
        
        // Convert IPv6 loopback (0:0:0:0:0:0:0:1 or ::1) to 127.0.0.1
        if (ip.equals("0:0:0:0:0:0:0:1") || ip.equals("::1")) {
            return "127.0.0.1 (localhost)";
        }
        
        return ip;
    }

    /**
     * ✅ GENERIC AUDIT LOG CREATOR (WITH AUTO IP CAPTURE)
     * This method automatically extracts IP from request context
     */
    @Transactional
    public void logAction(
            String action,
            String entityType,
            Long entityId,
            String oldValue,
            String newValue,
            String performedBy,
            String role
    ) {
        logAction(action, entityType, entityId, oldValue, newValue, performedBy, role, getClientIpAddress());
    }

    /**
     * ✅ GENERIC AUDIT LOG CREATOR (WITH EXPLICIT IP)
     * This method should be called ONLY from service layer
     */
    @Transactional
    public void logAction(
            String action,
            String entityType,
            Long entityId,
            String oldValue,
            String newValue,
            String performedBy,
            String role,
            String ipAddress
    ) {

        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setPerformedBy(performedBy);
        log.setRole(role);
        log.setIpAddress(ipAddress);
        log.setCreatedAt(LocalDateTime.now());

        auditLogRepository.save(log);
    }
}
