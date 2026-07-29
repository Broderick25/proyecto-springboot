package com.SpringBoot.infrastructure.notification;

import com.SpringBoot.application.port.outbound.MailPort;
import com.SpringBoot.application.port.outbound.OrderCancelledEmail;
import com.SpringBoot.application.port.outbound.OrderDeliveredEmail;
import com.SpringBoot.application.port.outbound.OrderShippedEmail;
import com.SpringBoot.domain.order.events.OrderCancelled;
import com.SpringBoot.domain.order.events.OrderDelivered;
import com.SpringBoot.domain.order.events.OrderShipped;
import com.SpringBoot.domain.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Notificaciones por correo para las transiciones de Order que todavía no tenían aviso al
 * cliente (confirmación ya la maneja {@code SendOrderConfirmationEmailService}). Reutiliza
 * {@link MailPort}/{@code MailAdapter} y, por ahora, el mismo destinatario de pruebas fijo que
 * ya usa el flujo de confirmación (ver TODO en {@code SendOrderConfirmationEmailService}).
 */
@Component
public class OrderNotificationListener {

    private final OrderRepository orderRepository;
    private final MailPort mailPort;
    private final String testRecipient;

    public OrderNotificationListener(OrderRepository orderRepository, MailPort mailPort,
                                      @Value("${app.mail.test-recipient}") String testRecipient) {
        this.orderRepository = orderRepository;
        this.mailPort = mailPort;
        this.testRecipient = testRecipient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderShipped event) {
        orderRepository.findById(event.orderId().value()).ifPresent(order ->
                mailPort.sendOrderShipped(
                        new OrderShippedEmail(testRecipient, order.getCustomerName(), order.getOrderNumber())));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderDelivered event) {
        orderRepository.findById(event.orderId().value()).ifPresent(order ->
                mailPort.sendOrderDelivered(
                        new OrderDeliveredEmail(testRecipient, order.getCustomerName(), order.getOrderNumber())));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(OrderCancelled event) {
        orderRepository.findById(event.orderId().value()).ifPresent(order ->
                mailPort.sendOrderCancelled(new OrderCancelledEmail(testRecipient, order.getCustomerName(),
                        order.getOrderNumber(), event.reason())));
    }
}
