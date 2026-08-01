package com.SpringBoot.application.query.audit.view;

import java.time.Instant;

public record AuditLogView(
        String id,
        String className,
        String methodName,
        Instant timestamp,
        Long executionTimeMs,
        boolean success,
        String errorMessage
) {
}
