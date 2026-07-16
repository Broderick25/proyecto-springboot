package com.SpringBoot.infrastructure.config;

import com.SpringBoot.infrastructure.adapter.customer.JsonPlaceholderClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "jsonplaceholder",
        types = JsonPlaceholderClient.class,
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
public class CustomerHttpClientConfig {
}
