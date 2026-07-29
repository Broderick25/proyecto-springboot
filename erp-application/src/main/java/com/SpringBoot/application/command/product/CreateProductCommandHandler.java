package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.domain.document.CatalogTypes;
import com.SpringBoot.domain.document.CategoryNotFoundException;
import com.SpringBoot.domain.entity.DuplicateSkuException;
import com.SpringBoot.domain.product.CategoryReference;
import com.SpringBoot.domain.product.Product;
import com.SpringBoot.domain.product.ProductImage;
import com.SpringBoot.domain.product.ProductName;
import com.SpringBoot.domain.product.SKU;
import com.SpringBoot.domain.product.Stock;
import com.SpringBoot.domain.repository.CatalogRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import com.SpringBoot.domain.shared.Money;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.UUID;

@Service
public class CreateProductCommandHandler implements CommandHandler<CreateProductCommand, UUID> {

    private final ProductRepository productRepository;
    private final CatalogRepository catalogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public CreateProductCommandHandler(ProductRepository productRepository, CatalogRepository catalogRepository,
                                        ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.catalogRepository = catalogRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public UUID handle(CreateProductCommand command) {
        if (productRepository.existsBySku(command.sku())) {
            throw new DuplicateSkuException(command.sku());
        }
        if (!catalogRepository.existsByCatalogTypeAndItemsId(CatalogTypes.PRODUCT_CATEGORIES, command.categoryId())) {
            throw new CategoryNotFoundException(command.categoryId());
        }

        Product aggregate = Product.create(
                SKU.of(command.sku()),
                ProductName.of(command.name()),
                command.description(),
                Money.of(command.price(), Currency.getInstance("USD")),
                Stock.of(command.initialStock()),
                CategoryReference.of(command.categoryId()),
                command.imageUrl() != null ? ProductImage.of(command.imageUrl()) : null,
                command.createdBy());

        productRepository.save(ProductMapper.toNewEntity(aggregate));

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return aggregate.getId().value();
    }
}
