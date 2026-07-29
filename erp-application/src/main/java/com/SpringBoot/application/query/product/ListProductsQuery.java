package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.Query;

public record ListProductsQuery(boolean onlyActive) implements Query {
}
