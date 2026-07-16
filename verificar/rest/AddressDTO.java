package com.erp.infrastructure.adapter.customer.dto;
public record AddressDTO(
    String street,
    String suite,
    String city,
    String zipcode,
    GeoDTO geo
) {}