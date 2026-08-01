package com.SpringBoot.application.query.audit;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.audit.view.AuditLogView;
import com.SpringBoot.domain.document.AuditLog;
import com.SpringBoot.domain.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ListAuditLogsQueryHandler implements QueryHandler<ListAuditLogsQuery, PageView<AuditLogView>> {

    private static final int MAX_PAGE_SIZE = 100;

    private final AuditLogRepository auditLogRepository;

    public ListAuditLogsQueryHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public PageView<AuditLogView> handle(ListAuditLogsQuery query) {
        if (query.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must not exceed " + MAX_PAGE_SIZE);
        }
        if ((query.from() == null) != (query.to() == null)) {
            throw new IllegalArgumentException("from and to must both be provided together");
        }

        Pageable pageable = PageRequest.of(query.page(), query.size());
        boolean hasRange = query.from() != null;

        Page<AuditLog> page = hasRange
                ? query.onlyFailed()
                        ? auditLogRepository.findByTimestampBetweenAndSuccessFalse(query.from(), query.to(), pageable)
                        : auditLogRepository.findByTimestampBetween(query.from(), query.to(), pageable)
                : query.onlyFailed()
                        ? auditLogRepository.findBySuccessFalse(pageable)
                        : auditLogRepository.findAll(pageable);

        return new PageView<>(
                page.getContent().stream().map(ListAuditLogsQueryHandler::toView).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private static AuditLogView toView(AuditLog log) {
        return new AuditLogView(
                log.getId(),
                log.getClassName(),
                log.getMethodName(),
                log.getTimestamp(),
                log.getExecutionTimeMs(),
                Boolean.TRUE.equals(log.getSuccess()),
                log.getErrorMessage());
    }
}
