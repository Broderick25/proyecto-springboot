package com.SpringBoot.application.query.product;

import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.QueryHandler;
import com.SpringBoot.application.query.product.view.ProductOrderHistoryView;
import com.SpringBoot.domain.entity.OrderProduct;
import com.SpringBoot.domain.repository.OrderProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class GetOrdersByProductQueryHandler
        implements QueryHandler<GetOrdersByProductQuery, PageView<ProductOrderHistoryView>> {

    private static final int MAX_PAGE_SIZE = 100;

    private final OrderProductRepository orderProductRepository;

    public GetOrdersByProductQueryHandler(OrderProductRepository orderProductRepository) {
        this.orderProductRepository = orderProductRepository;
    }

    @Override
    public PageView<ProductOrderHistoryView> handle(GetOrdersByProductQuery query) {
        if (query.size() > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size must not exceed " + MAX_PAGE_SIZE);
        }

        Pageable pageable = PageRequest.of(query.page(), query.size());
        Page<OrderProduct> page = orderProductRepository.findByProductIdWithOrder(query.productId(), pageable);

        return new PageView<>(
                page.getContent().stream().map(GetOrdersByProductQueryHandler::toView).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    private static ProductOrderHistoryView toView(OrderProduct orderProduct) {
        return new ProductOrderHistoryView(
                orderProduct.getOrder().getId(),
                orderProduct.getOrder().getOrderNumber(),
                orderProduct.getOrder().getOrderDate(),
                orderProduct.getOrder().getStatus().name(),
                orderProduct.getQuantity(),
                orderProduct.getUnitPrice(),
                orderProduct.getSubtotal());
    }
}
