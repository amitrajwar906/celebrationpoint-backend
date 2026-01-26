package com.celebrationpoint.backend.controller.admin;

import com.celebrationpoint.backend.entity.AuditLog;
import com.celebrationpoint.backend.repository.AuditLogRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/audit-logs")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // =================================================
    // 📜 GET ALL AUDIT LOGS (PAGINATED)
    // =================================================
    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                auditLogRepository.findAll(pageable)
        );
    }

    // =================================================
    // 🔍 FILTER BY ENTITY TYPE (PAGINATED)
    // =================================================
    @GetMapping("/entity/{entityType}")
    public ResponseEntity<Page<AuditLog>> getLogsByEntityType(
            @PathVariable String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                auditLogRepository.findByEntityType(entityType, pageable)
        );
    }

    // =================================================
    // 🔍 FILTER BY ENTITY TYPE + ENTITY ID
    // =================================================
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<Page<AuditLog>> getLogsByEntityId(
            @PathVariable String entityType,
            @PathVariable Long entityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                auditLogRepository.findByEntityTypeAndEntityId(
                        entityType,
                        entityId,
                        pageable
                )
        );
    }

    // =================================================
    // 👤 FILTER BY USER / ADMIN EMAIL (PAGINATED)
    // =================================================
    @GetMapping("/user/{email}")
    public ResponseEntity<Page<AuditLog>> getLogsByUser(
            @PathVariable String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return ResponseEntity.ok(
                auditLogRepository.findByPerformedBy(email, pageable)
        );
    }
}
