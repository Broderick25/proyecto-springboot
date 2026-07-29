package com.SpringBoot.application.port.outbound;

public interface MailPort {

    void sendOrderConfirmation(OrderConfirmationEmail email);

    void sendOrderShipped(OrderShippedEmail email);

    void sendOrderDelivered(OrderDeliveredEmail email);

    void sendOrderCancelled(OrderCancelledEmail email);
}
