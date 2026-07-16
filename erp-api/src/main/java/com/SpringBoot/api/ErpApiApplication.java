package com.SpringBoot.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.SpringBoot"})
@EntityScan(basePackages = "com.SpringBoot.domain.entity")
@EnableJpaRepositories(basePackages = "com.SpringBoot.domain.repository")
public class ErpApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApiApplication.class, args);
    }
}
