package com.SpringBoot.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "aws.s3")
public record S3Properties(

        @NotBlank(message = "aws.s3.endpoint no puede estar vacío")
        String endpoint,

        @NotBlank(message = "aws.s3.region no puede estar vacío")
        String region,

        @NotBlank(message = "aws.s3.access-key no puede estar vacío")
        String accessKey,

        @NotBlank(message = "aws.s3.secret-key no puede estar vacío")
        String secretKey,

        @NotBlank(message = "aws.s3.bucket-name no puede estar vacío")
        String bucketName,

        @NotNull(message = "aws.s3.path-style-enabled debe estar definido (true/false)")
        Boolean pathStyleEnabled,

        @NotNull(message = "aws.s3.local-environment debe estar definido (true/false)")
        Boolean localEnvironment,

        @NotBlank(message = "aws.s3.sse-algorithm no puede estar vacío")
        String sseAlgorithm,

        @NotNull(message = "aws.s3.public-read debe estar definido (true/false)")
        Boolean publicRead,

        @NotNull(message = "aws.s3.presigned-url-expiration-minutes debe estar definido")
        @Min(value = 1, message = "aws.s3.presigned-url-expiration-minutes debe ser mayor a 0")
        Integer presignedUrlExpirationMinutes,

        @NotEmpty(message = "aws.s3.allowed-content-types no puede estar vacío")
        List<String> allowedContentTypes,

        @NotNull(message = "aws.s3.max-file-size-mb debe estar definido")
        @Min(value = 1, message = "aws.s3.max-file-size-mb debe ser mayor a 0")
        Integer maxFileSizeMb
) {}
