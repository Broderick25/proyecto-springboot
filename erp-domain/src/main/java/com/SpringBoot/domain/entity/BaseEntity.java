package com.SpringBoot.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Implementa {@link Persistable} porque los agregados DDD generan su propio id ANTES de
 * persistir (ej. {@code Product.create()} llama a {@code ProductId.generate()}), y ese mismo
 * id se usa en los eventos de dominio que ya se publicaron. {@code isNew()} usa
 * {@code createdAt} (poblado solo por la BD al insertar) como señal confiable, en vez de basarse
 * en el id.
 *
 * <p>Spring Data respeta {@code isNew()} para decidir {@code persist()} vs {@code merge()}, pero
 * Hibernate hace además su propia verificación interna que no conoce {@link Persistable} — por
 * eso {@code JpaConfig} (erp-infrastructure) registra un {@code Interceptor} que también la
 * consulta, cerrando el otro lado del problema.
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity implements Persistable<UUID> {

    @CreationTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "created_at", nullable = false, updatable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Setter(AccessLevel.NONE)
    @Column(name = "updated_at", nullable = false,
            columnDefinition = "timestamp without time zone")
    private LocalDateTime updatedAt;

    // Bloqueo optimista: evita que dos writers concurrentes (ej. dos IncrementProductStockCommand
    // sobre el mismo producto) se pisen sin darse cuenta (lost update).
    @Version
    @Setter(AccessLevel.NONE)
    @Column(name = "version", nullable = false)
    private Long version;

    @Override
    public boolean isNew() {
        return getCreatedAt() == null;
    }
}
