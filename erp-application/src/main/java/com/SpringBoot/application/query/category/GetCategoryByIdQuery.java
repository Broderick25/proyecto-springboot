package com.SpringBoot.application.query.category;

import com.SpringBoot.application.query.Query;

public record GetCategoryByIdQuery(String id) implements Query {
}
