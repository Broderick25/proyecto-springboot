package com.SpringBoot.application.port.outbound;

/**
 * Thrown when the {@link MailPort} fails to build or send an email
 * (e.g. connectivity error with the SMTP server or an invalid template).
 */
public class MailException extends RuntimeException {

    public MailException(String message, Throwable cause) {
        super(message, cause);
    }
}
