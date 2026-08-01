package com.SpringBoot.application.command.product;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.domain.entity.ProductInactiveException;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.product.Product;
import com.SpringBoot.domain.repository.ProductRepository;
import com.SpringBoot.domain.shared.Money;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;

@Service
public class ChangeProductPriceCommandHandler implements CommandHandler<ChangeProductPriceCommand, Void> {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ChangeProductPriceCommandHandler(ProductRepository productRepository,
                                             ApplicationEventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public Void handle(ChangeProductPriceCommand command) {
        com.SpringBoot.domain.entity.Product entity = productRepository.findById(command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));

        Product aggregate = ProductMapper.toAggregate(entity);
        if (!aggregate.isActive()) {
            throw new ProductInactiveException(command.productId());
        }
        aggregate.changePrice(Money.of(command.newPrice(), Currency.getInstance("USD")));

        ProductMapper.copyToEntity(aggregate, entity);
        productRepository.save(entity);

        aggregate.getDomainEvents().forEach(eventPublisher::publishEvent);
        aggregate.clearDomainEvents();

        return null;
    }
}
