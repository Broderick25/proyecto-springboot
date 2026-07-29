package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.product.CategoryReference;
import com.SpringBoot.domain.product.Product;
import com.SpringBoot.domain.product.ProductImage;
import com.SpringBoot.domain.product.ProductName;
import com.SpringBoot.domain.repository.ProductRepository;
import com.SpringBoot.domain.shared.Money;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Service
public class UpdateProductCommandHandler implements CommandHandler<UpdateProductCommand, Void> {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UpdateProductCommandHandler(ProductRepository productRepository, ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(UpdateProductCommand command) {
        com.SpringBoot.domain.entity.Product entity = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        Product aggregate = ProductMapper.toAggregate(entity);
        aggregate.update(
                ProductName.of(command.name()),
                command.description(),
                Money.of(command.price(), Currency.getInstance("USD")),
                CategoryReference.of(command.categoryId()),
                command.imageUrl() != null ? ProductImage.of(command.imageUrl()) : null);

        ProductMapper.copyToEntity(aggregate, entity);
        productRepository.save(entity);

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
