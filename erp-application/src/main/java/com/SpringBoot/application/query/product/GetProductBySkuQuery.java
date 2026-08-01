package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.Query;

public record GetProductBySkuQuery(String sku) implements Query {
}
