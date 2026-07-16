package com.SpringBoot.application.usecase;

import com.SpringBoot.application.port.outbound.StorageException;
import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.UUID;

@Service
public class UploadProductImageService {

    private static final Logger log = LoggerFactory.getLogger(UploadProductImageService.class);

    private final StoragePort storagePort;
    private final ProductRepository productRepository;

    public UploadProductImageService(StoragePort storagePort, ProductRepository productRepository) {
        this.storagePort = storagePort;
        this.productRepository = productRepository;
    }

    public String upload(UUID productId, byte[] content, String contentType) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        String previousKey = product.getImageUrl();

        String newKey = buildKey(productId);
        storagePort.upload(newKey, content, contentType);

        product.setImageUrl(newKey);
        productRepository.save(product);

        if (previousKey != null && !previousKey.isBlank()) {
            deleteQuietly(previousKey);
        }

        URL presignedUrl = storagePort.generatePresignedGetUrl(newKey);
        return presignedUrl.toExternalForm();
    }

    private void deleteQuietly(String key) {
        try {
            storagePort.delete(key);
        } catch (StorageException e) {
            log.warn("No se pudo eliminar la imagen anterior {} (se ignora, la nueva imagen ya quedó activa)", key, e);
        }
    }

    private String buildKey(UUID productId) {
        return "products/" + productId + "/" + UUID.randomUUID() + ".png";
    }
}
