package com.SpringBoot.domain.customer;

/**
 * Thrown when the {@link CustomerProvider} port fails to reach or read from the external
 * customer service for a reason other than "not found" (e.g. timeout, connectivity error,
 * or a non-2xx response).
 */
public class CustomerLookupException extends RuntimeException {

    public CustomerLookupException(String message, Throwable cause) {
        super(message, cause);
    }
}
