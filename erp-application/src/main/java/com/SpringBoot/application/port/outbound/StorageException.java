package com.SpringBoot.application.port.outbound;

/**
 * Thrown when the {@link StoragePort} fails to reach or read from the underlying storage
 * provider for a reason other than validation (e.g. connectivity error or a service-side error).
 */
public class StorageException extends RuntimeException {

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
