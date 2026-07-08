package com.SpringBoot.domain;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * erp-domain no tiene una clase @SpringBootApplication propia (esa vive en
 * erp-api). Los slices de test (@DataJpaTest, @DataMongoTest) la buscan
 * subiendo por los paquetes desde la clase de test, así que este módulo
 * necesita una configuración mínima propia solo para test.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
public class TestApplication {
}
