package com.SpringBoot.infrastructure.adapter.customer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CompanyDTO(
        String name,
        @JsonProperty("catchPhrase") String catchPhrase,
        String bs
) {}
