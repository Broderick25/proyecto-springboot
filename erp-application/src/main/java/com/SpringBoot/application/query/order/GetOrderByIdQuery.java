package com.SpringBoot.application.query.order;

import com.SpringBoot.application.query.Query;

import java.util.UUID;

public record GetOrderByIdQuery(UUID orderId) implements Query {
}
