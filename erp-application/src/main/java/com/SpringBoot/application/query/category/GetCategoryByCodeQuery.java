package com.SpringBoot.application.query.category;

import com.SpringBoot.application.query.Query;

public record GetCategoryByCodeQuery(String code) implements Query {
}
