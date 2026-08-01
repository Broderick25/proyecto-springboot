package com.SpringBoot.application.query.audit;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.audit.view.AuditLogView;
import com.SpringBoot.domain.document.AuditLog;
import com.SpringBoot.domain.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAuditLogsQueryHandlerTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private ListAuditLogsQueryHandler handler;

    private static AuditLog auditLog(boolean success) {
        return AuditLog.builder()
                .id("audit-1")
                .className("CreateProductCommandHandler")
                .methodName("handle")
                .timestamp(Instant.now())
                .executionTimeMs(12L)
                .success(success)
                .build();
    }

    private static Page<AuditLog> pageOf(Pageable pageable, AuditLog... logs) {
        return new PageImpl<>(List.of(logs), pageable, logs.length);
    }

    @Test
    void handle_llamaFindAll_cuandoNoHayFiltros() {
        Pageable pageable = PageRequest.of(0, 20);
        when(auditLogRepository.findAll(pageable)).thenReturn(pageOf(pageable, auditLog(true)));

        PageView<AuditLogView> result = handler.handle(new ListAuditLogsQuery(null, null, false, 0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).success()).isTrue();
    }

    @Test
    void handle_llamaFindBySuccessFalse_cuandoOnlyFailedEsTrue() {
        Pageable pageable = PageRequest.of(0, 20);
        when(auditLogRepository.findBySuccessFalse(pageable)).thenReturn(pageOf(pageable, auditLog(false)));

        PageView<AuditLogView> result = handler.handle(new ListAuditLogsQuery(null, null, true, 0, 20));

        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).success()).isFalse();
        verify(auditLogRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void handle_llamaFindByTimestampBetween_cuandoSeIndicaRango() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Pageable pageable = PageRequest.of(0, 20);
        when(auditLogRepository.findByTimestampBetween(from, to, pageable))
                .thenReturn(pageOf(pageable, auditLog(true)));

        PageView<AuditLogView> result = handler.handle(new ListAuditLogsQuery(from, to, false, 0, 20));

        assertThat(result.content()).hasSize(1);
        verify(auditLogRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void handle_llamaFindByTimestampBetweenAndSuccessFalse_cuandoSeIndicanAmbos() {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        Pageable pageable = PageRequest.of(0, 20);
        when(auditLogRepository.findByTimestampBetweenAndSuccessFalse(from, to, pageable))
                .thenReturn(pageOf(pageable, auditLog(false)));

        PageView<AuditLogView> result = handler.handle(new ListAuditLogsQuery(from, to, true, 0, 20));

        assertThat(result.content()).hasSize(1);
        verify(auditLogRepository, never()).findByTimestampBetween(any(), any(), any(Pageable.class));
        verify(auditLogRepository, never()).findBySuccessFalse(any(Pageable.class));
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoSoloSeIndicaFrom() {
        assertThatThrownBy(() -> handler.handle(
                new ListAuditLogsQuery(Instant.now(), null, false, 0, 20)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void handle_lanzaIllegalArgumentException_cuandoSizeExcedeElMaximo() {
        assertThatThrownBy(() -> handler.handle(new ListAuditLogsQuery(null, null, false, 0, 101)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
