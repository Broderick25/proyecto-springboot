package com.SpringBoot.application.command.order;

import com.SpringBoot.application.command.CommandHandler;
import com.SpringBoot.application.port.outbound.MailPort;
import com.SpringBoot.application.port.outbound.OrderConfirmationEmail;
import com.SpringBoot.domain.customer.CustomerNotFoundException;
import com.SpringBoot.domain.customer.CustomerProvider;
import com.SpringBoot.domain.entity.Order;
import com.SpringBoot.domain.entity.OrderNotFoundException;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SendOrderConfirmationCommandHandler implements CommandHandler<SendOrderConfirmationCommand, Void> {

    private final OrderRepository orderRepository;
    private final CustomerProvider customerProvider;
    private final MailPort mailPort;
    // TODO: temporal mientras se valida el envío; reemplazar por customer.email() cuando se confirme el flujo.
    private final String testRecipient;

    public SendOrderConfirmationCommandHandler(OrderRepository orderRepository, CustomerProvider customerProvider,
                                                MailPort mailPort,
                                                @Value("${app.mail.test-recipient}") String testRecipient) {
        this.orderRepository = orderRepository;
        this.customerProvider = customerProvider;
        this.mailPort = mailPort;
        this.testRecipient = testRecipient;
    }

    @Override
    @Transactional(readOnly = true)
    public Void handle(SendOrderConfirmationCommand command) {
        Order order = orderRepository.findByIdWithItems(command.orderId())
                .orElseThrow(() -> new OrderNotFoundException(command.orderId()));

        if (!customerProvider.existsById(order.getCustomerId())) {
            throw new CustomerNotFoundException(order.getCustomerId());
        }

        OrderConfirmationEmail email = new OrderConfirmationEmail(
                testRecipient,
                order.getCustomerName(),
                order.getOrderNumber(),
                order.getId().toString(),
                order.getOrderDate().toLocalDate(),
                order.getOrderProducts().size(),
                order.getCurrency(),
                order.getTotalAmount());

        mailPort.sendOrderConfirmation(email);

        return null;
    }
}
