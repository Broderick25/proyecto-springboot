package com.SpringBoot.infrastructure.projection;

import com.SpringBoot.domain.document.CatalogItem;
import com.SpringBoot.domain.document.CatalogTypes;
import com.SpringBoot.domain.document.ProductDocument;
import com.SpringBoot.domain.product.ProductId;
import com.SpringBoot.domain.product.events.ProductCreated;
import com.SpringBoot.domain.product.events.ProductDeactivated;
import com.SpringBoot.domain.product.events.ProductUpdated;
import com.SpringBoot.domain.product.events.StockChanged;
import com.SpringBoot.domain.repository.CatalogRepository;
import com.SpringBoot.domain.repository.ProductDocumentRepository;
import com.SpringBoot.domain.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Lado de lectura del CQRS para Product: mantiene {@link ProductDocument} (Mongo) sincronizado
 * con la entidad JPA (Postgres, la fuente de verdad de escritura).
 *
 * <p>Ninguno de los eventos ({@code ProductUpdated}, {@code StockChanged},
 * {@code ProductDeactivated}) trae el estado completo del producto — solo el id y, en algunos
 * casos, el dato puntual que cambió. En vez de aplicar parches campo por campo por tipo de
 * evento, cada listener simplemente vuelve a leer el producto completo desde Postgres y
 * reemplaza el documento de Mongo — más simple y no se desincroniza si se agregan campos nuevos.
 *
 * <p>{@code AFTER_COMMIT}: si la transacción de escritura hace rollback, este listener nunca se
 * ejecuta — evita que el modelo de lectura quede con datos que nunca se confirmaron.
 */
@Component
public class ProductProjection {

    private static final Logger log = LoggerFactory.getLogger(ProductProjection.class);

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;
    private final CatalogRepository catalogRepository;

    public ProductProjection(ProductRepository productRepository,
                              ProductDocumentRepository productDocumentRepository,
                              CatalogRepository catalogRepository) {
        this.productRepository = productRepository;
        this.productDocumentRepository = productDocumentRepository;
        this.catalogRepository = catalogRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProductCreated event) {
        resync(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProductUpdated event) {
        resync(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(StockChanged event) {
        resync(event.productId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(ProductDeactivated event) {
        resync(event.productId());
    }

    private void resync(ProductId productId) {
        productRepository.findById(productId.value()).ifPresentOrElse(entity -> {
            ProductDocument document = productDocumentRepository.findById(entity.getId().toString())
                    .orElseGet(() -> ProductDocument.builder().id(entity.getId().toString()).build());

            document.setSku(entity.getSku());
            document.setName(entity.getName());
            document.setDescription(entity.getDescription());
            document.setPrice(entity.getPrice());
            document.setCurrency("USD");
            document.setStock(entity.getStock());
            document.setCategoryId(entity.getCategoryId());
            document.setCategoryName(resolveCategoryName(entity.getCategoryId()));
            document.setImageUrl(entity.getImageUrl());
            document.setActive(entity.getActive());

            productDocumentRepository.save(document);
        }, () -> log.warn("ProductProjection: no se encontró el producto {} para sincronizar con Mongo", productId));
    }

    private String resolveCategoryName(String categoryId) {
        if (categoryId == null) {
            return null;
        }
        return catalogRepository.findByCatalogType(CatalogTypes.PRODUCT_CATEGORIES)
                .flatMap(catalog -> catalog.getItems().stream()
                        .filter(item -> categoryId.equals(item.id()))
                        .findFirst())
                .map(CatalogItem::value)
                .orElse(null);
    }
}
