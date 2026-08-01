package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    // findByUserId se quitó: CommandHandlerAuditAspect todavía no captura userId (no hay
    // mecanismo de identidad del actor en el proyecto) — se puede reintroducir cuando exista.
    Page<AuditLog> findByTimestampBetween(Instant start, Instant end, Pageable pageable);
    Page<AuditLog> findBySuccessFalse(Pageable pageable);
    Page<AuditLog> findByTimestampBetweenAndSuccessFalse(Instant start, Instant end, Pageable pageable);
}
