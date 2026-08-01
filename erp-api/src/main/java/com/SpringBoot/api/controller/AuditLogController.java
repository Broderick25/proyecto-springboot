package com.SpringBoot.api.controller;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.audit.ListAuditLogsQuery;
import com.SpringBoot.application.query.audit.ListAuditLogsQueryHandler;
import com.SpringBoot.application.query.audit.view.AuditLogView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/audit-logs")
public class AuditLogController {

    private final ListAuditLogsQueryHandler listAuditLogsQueryHandler;

    public AuditLogController(ListAuditLogsQueryHandler listAuditLogsQueryHandler) {
        this.listAuditLogsQueryHandler = listAuditLogsQueryHandler;
    }

    @GetMapping
    public ResponseEntity<PageView<AuditLogView>> list(
            @RequestParam(name = "from", required = false) Instant from,
            @RequestParam(name = "to", required = false) Instant to,
            @RequestParam(name = "onlyFailed", defaultValue = "false") boolean onlyFailed,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(
                listAuditLogsQueryHandler.handle(new ListAuditLogsQuery(from, to, onlyFailed, page, size)));
    }
}
