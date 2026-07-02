package com.SpringBoot.domain.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document(collection = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    private String id;

    @Field("className")
    private String className;

    @Field("methodName")
    private String methodName;

    @Indexed
    @Field("userId")
    private String userId;

    @Indexed
    @Field("timestamp")
    private Instant timestamp;

    @Field("executionTimeMs")
    private Long executionTimeMs;

    @Field("success")
    private Boolean success;

    @Field("errorMessage")
    private String errorMessage;

    @Field("ipAddress")
    private String ipAddress;

    @Field("endpoint")
    private String endpoint;
}
