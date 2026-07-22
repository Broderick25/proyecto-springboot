package com.SpringBoot.domain.customer;

public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(Long customerId) {
        super("No existe un cliente con id " + customerId);
    }
}
