package com.SpringBoot.application.usecase;

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

import java.util.UUID;

@Service
public class SendOrderConfirmationEmailService {

    private final OrderRepository orderRepository;
    private final CustomerProvider customerProvider;
    private final MailPort mailPort;
    // TODO: temporal mientras se valida el envío; reemplazar por customer.email() cuando se confirme el flujo.
    private final String testRecipient;

    public SendOrderConfirmationEmailService(OrderRepository orderRepository, CustomerProvider customerProvider,
                                              MailPort mailPort,
                                              @Value("${app.mail.test-recipient}") String testRecipient) {
        this.orderRepository = orderRepository;
        this.customerProvider = customerProvider;
        this.mailPort = mailPort;
        this.testRecipient = testRecipient;
    }

    @Transactional(readOnly = true)
    public void sendConfirmation(UUID orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

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
    }
}
