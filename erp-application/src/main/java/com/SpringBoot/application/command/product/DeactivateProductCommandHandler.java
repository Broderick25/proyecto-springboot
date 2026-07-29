package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.product.Product;
import com.SpringBoot.domain.repository.ProductRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeactivateProductCommandHandler implements CommandHandler<DeactivateProductCommand, Void> {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public DeactivateProductCommandHandler(ProductRepository productRepository,
                                            ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(DeactivateProductCommand command) {
        com.SpringBoot.domain.entity.Product entity = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        Product aggregate = ProductMapper.toAggregate(entity);
        aggregate.deactivate();

        ProductMapper.copyToEntity(aggregate, entity);
        productRepository.save(entity);

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
