package com.SpringBoot.infrastructure.mail;

import com.SpringBoot.application.port.outbound.MailException;
import com.SpringBoot.application.port.outbound.MailPort;
import com.SpringBoot.application.port.outbound.OrderConfirmationEmail;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateProcessingException;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class MailAdapter implements MailPort {

    private static final Logger log = LoggerFactory.getLogger(MailAdapter.class);

    private static final String TEMPLATE_NAME = "email-order-confirm-template";
    private static final DateTimeFormatter ORDER_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.of("es", "ES"));

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;

    public MailAdapter(JavaMailSender mailSender, TemplateEngine templateEngine, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.mailProperties = mailProperties;
    }

    @Override
    public void sendOrderConfirmation(OrderConfirmationEmail email) {
        try {
            String html = templateEngine.process(TEMPLATE_NAME, buildContext(email));

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(email.to());
            helper.setFrom(mailProperties.getUsername());
            helper.setSubject("Confirmación de compra - " + email.orderNumber());
            helper.setText(html, true);

            mailSender.send(mimeMessage);
        } catch (TemplateProcessingException e) {
            log.error("No se pudo procesar la plantilla del correo de confirmación para la orden {}", email.orderNumber(), e);
            throw new MailException("No se pudo procesar la plantilla del correo de confirmación para la orden " + email.orderNumber(), e);
        } catch (MessagingException e) {
            log.error("No se pudo construir el correo de confirmación para la orden {}", email.orderNumber(), e);
            throw new MailException("No se pudo construir el correo de confirmación para la orden " + email.orderNumber(), e);
        } catch (org.springframework.mail.MailException e) {
            log.error("Fallo al enviar el correo de confirmación para la orden {}", email.orderNumber(), e);
            throw new MailException("No se pudo enviar el correo de confirmación para la orden " + email.orderNumber(), e);
        }
    }

    private Context buildContext(OrderConfirmationEmail email) {
        Context context = new Context();
        context.setVariable("customerName", email.customerName());
        context.setVariable("orderNumber", email.orderNumber());
        context.setVariable("orderId", email.orderId());
        context.setVariable("orderDate", email.orderDate().format(ORDER_DATE_FORMATTER));
        context.setVariable("itemsCount", email.itemsCount());
        context.setVariable("currency", email.currency());
        context.setVariable("totalAmount", email.totalAmount().toPlainString());
        return context;
    }
}
