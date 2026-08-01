package com.SpringBoot.domain.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.List;

@Document(collection = "catalogs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Catalog {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("catalogType")
    private String catalogType;

    @Field("name")
    private String name;

    @Field("description")
    private String description;

    @Field("active")
    private Boolean active;

    @Field("items")
    private List<CatalogItem> items;

    // Bloqueo optimista: Create/Update/DeleteCategoryCommandHandler hacen un read-modify-write
    // del documento completo (leen el catálogo, mutan la lista de items en memoria, guardan de
    // nuevo). Sin este campo, dos escrituras concurrentes se pisan en silencio — con @Version,
    // Spring Data Mongo hace save() condicional al version leído y lanza
    // OptimisticLockingFailureException si alguien más ya guardó primero.
    @Version
    private Long version;

    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
