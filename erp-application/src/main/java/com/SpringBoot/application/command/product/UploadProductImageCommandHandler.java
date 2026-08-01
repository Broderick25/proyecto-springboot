package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.port.outbound.StorageException;
import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.UUID;

@Service
public class UploadProductImageCommandHandler implements CommandHandler<UploadProductImageCommand, String> {

    private static final Logger log = LoggerFactory.getLogger(UploadProductImageCommandHandler.class);

    private final StoragePort storagePort;
    private final ProductRepository productRepository;

    public UploadProductImageCommandHandler(StoragePort storagePort, ProductRepository productRepository) {
        this.storagePort = storagePort;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public String handle(UploadProductImageCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        String previousKey = product.getImageUrl();

        String newKey = buildKey(command.productId());
        storagePort.upload(newKey, command.content(), command.contentType());

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
