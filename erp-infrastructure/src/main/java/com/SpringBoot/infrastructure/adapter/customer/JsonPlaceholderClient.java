package com.SpringBoot.infrastructure.adapter.customer;

import com.SpringBoot.infrastructure.adapter.customer.dto.UserDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface JsonPlaceholderClient {

    @GetExchange("/users/{id}")
    UserDTO findById(@PathVariable Long id);
}
