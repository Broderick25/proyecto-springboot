package com.SpringBoot.application.query.order;

import com.SpringBoot.application.query.Query;

public record ListOrdersQuery(Long customerId, String status, int page, int size) implements Query {
}
