package com.SpringBoot.infrastructure.config;

import com.SpringBoot.infrastructure.adapter.customer.JsonPlaceholderClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.support.WebClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroup;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(
        group = "jsonplaceholder",
        types = JsonPlaceholderClient.class,
        clientType = HttpServiceGroup.ClientType.WEB_CLIENT
)
public class CustomerHttpClientConfig {

    // Spring Boot 4.1 no aplica automáticamente spring.http.serviceclient.<group>.base-url
    // a los grupos declarados con @ImportHttpServices; hay que aplicarlo manualmente aquí.
    @Bean
    WebClientHttpServiceGroupConfigurer jsonPlaceholderGroupConfigurer(
            @Value("${spring.http.serviceclient.jsonplaceholder.base-url}") String baseUrl) {
        return groups -> groups.filterByName("jsonplaceholder")
                .forEachClient((group, clientBuilder) -> clientBuilder.baseUrl(baseUrl));
    }
}
