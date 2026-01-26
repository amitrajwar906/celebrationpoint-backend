package com.celebrationpoint.backend.repository;

import com.celebrationpoint.backend.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByEntityType(String entityType, Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityId(
            String entityType,
            Long entityId,
            Pageable pageable
    );

    Page<AuditLog> findByPerformedBy(String performedBy, Pageable pageable);
}
