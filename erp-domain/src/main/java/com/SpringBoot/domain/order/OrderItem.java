package com.SpringBoot.domain.order;

import com.SpringBoot.domain.common.Entity;
import com.SpringBoot.domain.product.Product;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.shared.Money;
import com.SpringBoot.domain.shared.Quantity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItem extends Entity<OrderItemId> {

    private OrderItemId id;
    private ProductId productReference;
    private String productName;
    private Quantity quantity;
    private Money unitPrice;
    private Money subtotal;

    public static OrderItem from(Product product, Quantity quantity) {
        if (product == null) {
            throw new IllegalArgumentException("product must not be null");
        }
        if (quantity == null) {
            throw new IllegalArgumentException("quantity must not be null");
        }

        Money unitPrice = product.getPrice();
        Money subtotal = unitPrice.multiply(quantity);

        return new OrderItem(OrderItemId.generate(), product.getId(), product.getName().value(), quantity,
                unitPrice, subtotal);
    }

    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }
}
