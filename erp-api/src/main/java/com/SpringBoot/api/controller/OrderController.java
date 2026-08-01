package com.SpringBoot.api.controller;

import com.SpringBoot.application.command.order.CancelOrderCommand;
import com.SpringBoot.application.command.order.CancelOrderCommandHandler;
import com.SpringBoot.application.command.order.ConfirmOrderCommand;
import com.SpringBoot.application.command.order.ConfirmOrderCommandHandler;
import com.SpringBoot.application.command.order.CreateOrderCommand;
import com.SpringBoot.application.command.order.CreateOrderCommandHandler;
import com.SpringBoot.application.command.order.DeliverOrderCommand;
import com.SpringBoot.application.command.order.DeliverOrderCommandHandler;
import com.SpringBoot.application.command.order.SendOrderConfirmationCommand;
import com.SpringBoot.application.command.order.SendOrderConfirmationCommandHandler;
import com.SpringBoot.application.command.order.ShipOrderCommand;
import com.SpringBoot.application.command.order.ShipOrderCommandHandler;
import com.SpringBoot.application.command.order.UpdateOrderCommand;
import com.SpringBoot.application.command.order.UpdateOrderCommandHandler;
import com.SpringBoot.application.query.PageView;
import com.SpringBoot.application.query.order.GetOrderByIdQuery;
import com.SpringBoot.application.query.order.GetOrderByIdQueryHandler;
import com.SpringBoot.application.query.order.ListOrdersQuery;
import com.SpringBoot.application.query.order.ListOrdersQueryHandler;
import com.SpringBoot.application.query.order.view.OrderSummaryView;
import com.SpringBoot.application.query.order.view.OrderView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final SendOrderConfirmationCommandHandler sendOrderConfirmationCommandHandler;
    private final CreateOrderCommandHandler createOrderCommandHandler;
    private final UpdateOrderCommandHandler updateOrderCommandHandler;
    private final ConfirmOrderCommandHandler confirmOrderCommandHandler;
    private final ShipOrderCommandHandler shipOrderCommandHandler;
    private final DeliverOrderCommandHandler deliverOrderCommandHandler;
    private final CancelOrderCommandHandler cancelOrderCommandHandler;
    private final GetOrderByIdQueryHandler getOrderByIdQueryHandler;
    private final ListOrdersQueryHandler listOrdersQueryHandler;

    public OrderController(SendOrderConfirmationCommandHandler sendOrderConfirmationCommandHandler,
                            CreateOrderCommandHandler createOrderCommandHandler,
                            UpdateOrderCommandHandler updateOrderCommandHandler,
                            ConfirmOrderCommandHandler confirmOrderCommandHandler,
                            ShipOrderCommandHandler shipOrderCommandHandler,
                            DeliverOrderCommandHandler deliverOrderCommandHandler,
                            CancelOrderCommandHandler cancelOrderCommandHandler,
                            GetOrderByIdQueryHandler getOrderByIdQueryHandler,
                            ListOrdersQueryHandler listOrdersQueryHandler) {
        this.sendOrderConfirmationCommandHandler = sendOrderConfirmationCommandHandler;
        this.createOrderCommandHandler = createOrderCommandHandler;
        this.updateOrderCommandHandler = updateOrderCommandHandler;
        this.confirmOrderCommandHandler = confirmOrderCommandHandler;
        this.shipOrderCommandHandler = shipOrderCommandHandler;
        this.deliverOrderCommandHandler = deliverOrderCommandHandler;
        this.cancelOrderCommandHandler = cancelOrderCommandHandler;
        this.getOrderByIdQueryHandler = getOrderByIdQueryHandler;
        this.listOrdersQueryHandler = listOrdersQueryHandler;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderView> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(getOrderByIdQueryHandler.handle(new GetOrderByIdQuery(id)));
    }

    @GetMapping
    public ResponseEntity<PageView<OrderSummaryView>> list(
            @RequestParam(name = "customerId", required = false) Long customerId,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(listOrdersQueryHandler.handle(new ListOrdersQuery(customerId, status, page, size)));
    }

    @PostMapping
    public ResponseEntity<UUID> create(@RequestBody CreateOrderCommand command) {
        UUID orderId = createOrderCommandHandler.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @RequestBody UpdateOrderRequest request) {
        updateOrderCommandHandler.handle(new UpdateOrderCommand(id, request.items()));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/send-confirmation")
    public ResponseEntity<Void> sendConfirmation(@PathVariable UUID id) {
        sendOrderConfirmationCommandHandler.handle(new SendOrderConfirmationCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<Void> confirm(@PathVariable UUID id) {
        confirmOrderCommandHandler.handle(new ConfirmOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/ship")
    public ResponseEntity<Void> ship(@PathVariable UUID id) {
        shipOrderCommandHandler.handle(new ShipOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/deliver")
    public ResponseEntity<Void> deliver(@PathVariable UUID id) {
        deliverOrderCommandHandler.handle(new DeliverOrderCommand(id));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable UUID id, @RequestBody CancelOrderRequest request) {
        cancelOrderCommandHandler.handle(new CancelOrderCommand(id, request.reason()));
        return ResponseEntity.ok().build();
    }

    public record CancelOrderRequest(String reason) {
    }

    public record UpdateOrderRequest(List<UpdateOrderCommand.OrderLine> items) {
    }
}
