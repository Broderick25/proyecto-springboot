package com.SpringBoot.domain.shared;

import java.time.Instant;

public record AuditInfo(String createdBy, Instant createdAt, Instant updatedAt) {

    public AuditInfo {
        if (createdBy == null || createdBy.isBlank()) {
            throw new IllegalArgumentException("AuditInfo createdBy must not be null or blank");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("AuditInfo createdAt must not be null");
        }
        if (updatedAt == null) {
            throw new IllegalArgumentException("AuditInfo updatedAt must not be null");
        }
    }

    public static AuditInfo create(String createdBy, Instant timestamp) {
        return new AuditInfo(createdBy, timestamp, timestamp);
    }

    public AuditInfo updateTimestamp() {
        return new AuditInfo(this.createdBy, this.createdAt, Instant.now());
    }
}
