package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.Query;

public record SearchProductsQuery(String text, int page, int size) implements Query {
}
