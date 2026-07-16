package com.SpringBoot.application.usecase;

import com.SpringBoot.application.port.outbound.StorageException;
import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductInUseException;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class DeleteProductService {

    private static final Logger log = LoggerFactory.getLogger(DeleteProductService.class);

    private final ProductRepository productRepository;
    private final StoragePort storagePort;

    public DeleteProductService(ProductRepository productRepository, StoragePort storagePort) {
        this.productRepository = productRepository;
        this.storagePort = storagePort;
    }

    public void delete(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        String imageKey = product.getImageUrl();

        try {
            productRepository.delete(product);
        } catch (DataIntegrityViolationException e) {
            throw new ProductInUseException(productId, e);
        }

        if (imageKey != null && !imageKey.isBlank()) {
            deleteQuietly(imageKey);
        }
    }

    private void deleteQuietly(String key) {
        try {
            storagePort.delete(key);
        } catch (StorageException e) {
            log.warn("No se pudo eliminar la imagen {} del producto eliminado (se ignora)", key, e);
        }
    }
}
