package com.SpringBoot.infrastructure.storage;

import com.SpringBoot.infrastructure.config.S3Properties;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Deprecated
public class S3StorageService extends S3StorageAdapter {

    public S3StorageService(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties) {
        super(s3Client, s3Presigner, properties);
    }
}
