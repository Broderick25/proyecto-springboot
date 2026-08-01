package com.SpringBoot.application.query.audit;

import com.SpringBoot.application.query.Query;

import java.time.Instant;

public record ListAuditLogsQuery(Instant from, Instant to, boolean onlyFailed, int page, int size)
        implements Query {
}
