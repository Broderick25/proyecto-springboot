package com.SpringBoot.domain.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "product_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("sku")
    private String sku;

    @TextIndexed
    @Field("name")
    private String name;

    @TextIndexed
    @Field("description")
    private String description;

    @Field(name = "price", targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    @Field("currency")
    private String currency;

    @Field("stock")
    private Integer stock;

    @Indexed
    @Field("categoryId")
    private String categoryId;

    @Field("categoryName")
    private String categoryName;

    @Field("imageUrl")
    private String imageUrl;

    @Indexed
    @Field("active")
    private Boolean active;

    @Builder.Default
    @Field("tags")
    private List<String> tags = new ArrayList<>();

    @Field("specifications")
    private ProductSpecifications specifications;

    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
