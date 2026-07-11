package com.SpringBoot.application.usecase;

import com.SpringBoot.application.port.outbound.StoragePort;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.UUID;

@Service
public class UploadProductImageService {

    private final StoragePort storagePort;

    public UploadProductImageService(StoragePort storagePort) {
        this.storagePort = storagePort;
    }

    public String upload(String productId, byte[] content, String contentType) {
        String key = buildKey(productId);
        storagePort.upload(key, content, contentType);
        URL presignedUrl = storagePort.generatePresignedGetUrl(key);
        return presignedUrl.toExternalForm();
    }

    private String buildKey(String productId) {
        return "products/" + productId + "/" + UUID.randomUUID() + ".png";
    }
}
