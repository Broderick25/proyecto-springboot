package com.SpringBoot.domain.repository;

import com.SpringBoot.domain.document.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {

    List<AuditLog> findByUserId(String userId);
    List<AuditLog> findByTimestampBetween(Instant start, Instant end);
    List<AuditLog> findBySuccessFalse();
}
