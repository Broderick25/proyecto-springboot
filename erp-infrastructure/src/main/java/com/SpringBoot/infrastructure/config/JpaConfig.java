package com.SpringBoot.infrastructure.config;

import org.hibernate.Interceptor;
import org.hibernate.cfg.SessionEventSettings;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Persistable;

/**
 * Puente entre {@link Persistable} (que ya usan {@code Product}/{@code Order} vía
 * {@code BaseEntity}) y la detección interna de Hibernate. Spring Data respeta
 * {@code Persistable.isNew()} para decidir si llamar {@code persist()} o {@code merge()}, pero
 * una vez dentro de {@code entityManager.persist()}, Hibernate hace su PROPIA verificación
 * ({@code AbstractEntityPersister.isTransient()}) que no conoce {@link Persistable} — y con un
 * id ya asignado por la app (el agregado DDD genera su propio id antes de persistir) más
 * {@code @Version}, esa verificación es ambigua en cualquier dirección: falla con
 * "uninitialized version" si version es null, o con "Detached entity passed to persist" si no
 * lo es. Este {@code Interceptor} le dice a Hibernate que confíe en {@code Persistable.isNew()}
 * en vez de adivinar por id/version.
 */
@Configuration
public class JpaConfig {

    @Bean
    public HibernatePropertiesCustomizer persistableAwareInterceptorCustomizer() {
        return properties -> properties.put(SessionEventSettings.INTERCEPTOR, new Interceptor() {
            @Override
            public Boolean isTransient(Object entity) {
                if (entity instanceof Persistable<?> persistable) {
                    return persistable.isNew();
                }
                return null;
            }
        });
    }
}
