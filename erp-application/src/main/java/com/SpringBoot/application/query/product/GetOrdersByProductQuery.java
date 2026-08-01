package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.Query;

import java.util.UUID;

public record GetOrdersByProductQuery(UUID productId, int page, int size) implements Query {
}
