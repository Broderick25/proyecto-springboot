package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.CommandHandler;
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
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteProductCommandHandler implements CommandHandler<DeleteProductCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(DeleteProductCommandHandler.class);

    private final ProductRepository productRepository;
    private final StoragePort storagePort;

    public DeleteProductCommandHandler(ProductRepository productRepository, StoragePort storagePort) {
        this.productRepository = productRepository;
        this.storagePort = storagePort;
    }

    @Override
    @Transactional
    public Void handle(DeleteProductCommand command) {
        Product product = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        String imageKey = product.getImageUrl();

        try {
            productRepository.delete(product);
        } catch (DataIntegrityViolationException e) {
            throw new ProductInUseException(command.productId(), e);
        }

        if (imageKey != null && !imageKey.isBlank()) {
            deleteQuietly(imageKey);
        }

        return null;
    }

    private void deleteQuietly(String key) {
        try {
            storagePort.delete(key);
        } catch (StorageException e) {
            log.warn("No se pudo eliminar la imagen {} del producto eliminado (se ignora)", key, e);
        }
    }
}
