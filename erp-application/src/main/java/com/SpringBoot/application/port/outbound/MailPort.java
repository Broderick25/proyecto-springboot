package com.SpringBoot.application.port.outbound;

public interface MailPort {

    void sendOrderConfirmation(OrderConfirmationEmail email);
}
