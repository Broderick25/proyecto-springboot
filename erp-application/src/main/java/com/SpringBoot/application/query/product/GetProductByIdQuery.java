package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.Query;

import java.util.UUID;

public record GetProductByIdQuery(UUID productId) implements Query {
}
