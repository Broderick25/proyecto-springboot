package com.SpringBoot.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.SpringBoot"})
public class ErpApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErpApiApplication.class, args);
    }
}
