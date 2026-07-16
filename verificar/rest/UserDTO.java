package com.erp.infrastructure.adapter.customer.dto;
public record UserDTO(
    Long id,
    String name,
    String username,
    String email,
    AddressDTO address,
    String phone,
    String website,
    CompanyDTO company
) {}