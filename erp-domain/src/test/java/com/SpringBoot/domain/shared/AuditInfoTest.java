package com.SpringBoot.domain.shared;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuditInfoTest {

    @Test
    void create_rechazaCreatedByBlank() {
        assertThatThrownBy(() -> AuditInfo.create("  ", Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateTimestamp_devuelveCopiaConUpdatedAtRefrescado() {
        Instant t0 = Instant.now();
        AuditInfo original = AuditInfo.create("admin", t0);

        AuditInfo updated = original.updateTimestamp();

        assertThat(original.updatedAt()).isEqualTo(original.createdAt()); // no mutado
        assertThat(updated.updatedAt()).isAfterOrEqualTo(t0);
    }
}
