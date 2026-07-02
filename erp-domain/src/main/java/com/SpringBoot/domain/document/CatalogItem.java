package com.SpringBoot.domain.document;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

public record CatalogItem(

        @Field("id")
        String id,

        @Field("code")
        String code,

        @Field("value")
        String value,

        @Field("description")
        String description,

        @Field("displayOrder")
        Integer displayOrder,

        @Field("metadata")
        Map<String, Object> metadata
) {}
