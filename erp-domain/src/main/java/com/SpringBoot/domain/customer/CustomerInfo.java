package com.SpringBoot.domain.customer;

/**
 * Value object immutable for JSONPlaceholder API
 */
public record CustomerInfo(
        Long id,
        String name,
        String email,
        String phone,
        String address,
        String city,
        String zipcode,
        String companyName
) {

    public CustomerInfo {

        if (id == null) {
            throw new IllegalArgumentException("id is not present");
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is not present");
        }
    }
}
