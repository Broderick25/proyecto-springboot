package com.SpringBoot.infrastructure.audit;

import com.SpringBoot.domain.common.DomainEvent;
import com.SpringBoot.domain.document.AuditLog;
import com.SpringBoot.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

/**
 * Auditoría genérica: registra en {@code audit_logs} (Mongo) cada {@link DomainEvent} publicado
 * por cualquier agregado (Product, Order, los que se agreguen después), sin tener que escribir
 * un listener dedicado por tipo de evento. Spring resuelve este listener contra la interfaz
 * {@code DomainEvent} usando el tipo real del evento publicado en tiempo de ejecución.
 */
@Component
public class DomainEventAuditListener {

    private final AuditLogRepository auditLogRepository;

    public DomainEventAuditListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDomainEvent(DomainEvent event) {
        auditLogRepository.save(AuditLog.builder()
                .className(event.getClass().getSimpleName())
                .timestamp(Instant.now())
                .success(true)
                .build());
    }
}
