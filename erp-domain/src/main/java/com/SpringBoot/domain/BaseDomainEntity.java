package com.SpringBoot.domain;

import lombok.Data;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Entidad placeholder para el módulo de dominio
 */
@Entity
@Data
public class BaseDomainEntity {
    
    @Id
    private Long id;
    
    private String name;
}
