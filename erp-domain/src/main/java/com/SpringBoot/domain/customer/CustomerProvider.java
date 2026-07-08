package com.SpringBoot.domain.customer;

import java.util.Optional;

/**
 *  Port for external service for JSONPlaceholder
 */
public interface CustomerProvider {

    Optional<CustomerInfo> findById(Long id);
    boolean existsById(Long id);
}
