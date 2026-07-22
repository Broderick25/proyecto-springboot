package com.SpringBoot.infrastructure.mail;

import com.SpringBoot.application.port.outbound.MailException;
import com.SpringBoot.application.port.outbound.MailPort;
import com.SpringBoot.application.port.outbound.OrderConfirmationEmail;
import jakarta.mail.Session;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailAdapterTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MailProperties mailProperties;

    private MailAdapter adapter;

    private OrderConfirmationEmail email;

    @BeforeEach
    void setUp() {
        adapter = new MailAdapter(mailSender, templateEngine, mailProperties);
        lenient().when(mailProperties.getUsername()).thenReturn("ventas@erp-lite.com");

        email = new OrderConfirmationEmail(
                "cliente@example.com",
                "Juan Pérez",
                "ORD-1001",
                "a1b2c3",
                LocalDate.of(2026, 7, 17),
                3,
                "USD",
                new BigDecimal("129.90"));
    }

    @Test
    void shouldImplementMailPort() {
        assertThat(adapter).isInstanceOf(MailPort.class);
    }

    @Test
    void sendOrderConfirmation_enviaElCorreo_cuandoTodoResponde() throws Exception {
        MimeMessage mimeMessage = newMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email-order-confirm-template"), any(Context.class)))
                .thenReturn("<html><body>Hola Juan</body></html>");

        adapter.sendOrderConfirmation(email);

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Confirmación de compra - ORD-1001");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).contains("cliente@example.com");
    }

    @Test
    void sendOrderConfirmation_lanzaMailException_cuandoFallaElRenderDeLaPlantilla() {
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new TemplateProcessingException("error de plantilla"));

        assertThatThrownBy(() -> adapter.sendOrderConfirmation(email))
                .isInstanceOf(MailException.class)
                .hasCauseInstanceOf(TemplateProcessingException.class);
    }

    @Test
    void sendOrderConfirmation_lanzaMailException_cuandoFallaElEnvio() throws Exception {
        MimeMessage mimeMessage = newMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html><body>Hola Juan</body></html>");
        doThrow(new MailSendException("SMTP no disponible")).when(mailSender).send(any(MimeMessage.class));

        assertThatThrownBy(() -> adapter.sendOrderConfirmation(email))
                .isInstanceOf(MailException.class)
                .hasCauseInstanceOf(MailSendException.class);
    }

    @Test
    void sendOrderConfirmation_lanzaMailException_cuandoElDestinatarioEsInvalido() {
        when(mailSender.createMimeMessage()).thenReturn(newMimeMessage());
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html><body>Hola Juan</body></html>");

        OrderConfirmationEmail invalidRecipient = new OrderConfirmationEmail(
                "Cliente <cliente@example.com", // falta el '>' de cierre: dirección inválida
                email.customerName(), email.orderNumber(), email.orderId(),
                email.orderDate(), email.itemsCount(), email.currency(), email.totalAmount());

        assertThatThrownBy(() -> adapter.sendOrderConfirmation(invalidRecipient))
                .isInstanceOf(MailException.class)
                .hasCauseInstanceOf(AddressException.class);
    }

    private static MimeMessage newMimeMessage() {
        return new MimeMessage(Session.getDefaultInstance(new Properties()));
    }
}
