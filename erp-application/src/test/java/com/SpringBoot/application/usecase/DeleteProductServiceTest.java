package com.SpringBoot.application.usecase;

import com.SpringBoot.application.port.outbound.StorageException;
import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductInUseException;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private DeleteProductService service;

    @Test
    void delete_eliminaElProductoYSuImagen_cuandoElProductoTeniaImagen() {
        UUID productId = UUID.randomUUID();
        String imageKey = "products/" + productId + "/foto.png";
        Product product = Product.builder().id(productId).imageUrl(imageKey).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        service.delete(productId);

        verify(productRepository).delete(product);
        verify(storagePort).delete(imageKey);
    }

    @Test
    void delete_noIntentaEliminarImagen_cuandoElProductoNoTeniaImagen() {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        service.delete(productId);

        verify(productRepository).delete(product);
        verify(storagePort, never()).delete(anyString());
    }

    @Test
    void delete_ignoraElFalloAlEliminarLaImagenEnS3() {
        UUID productId = UUID.randomUUID();
        String imageKey = "products/" + productId + "/foto.png";
        Product product = Product.builder().id(productId).imageUrl(imageKey).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doThrow(new StorageException("boom", new RuntimeException()))
                .when(storagePort).delete(imageKey);

        assertThatCode(() -> service.delete(productId)).doesNotThrowAnyException();
    }

    @Test
    void delete_lanzaProductNotFoundException_cuandoElProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(productId))
                .isInstanceOf(ProductNotFoundException.class);

        verify(productRepository, never()).delete(org.mockito.ArgumentMatchers.any());
        verify(storagePort, never()).delete(anyString());
    }

    @Test
    void delete_lanzaProductInUseException_cuandoElProductoTienePedidosAsociados() {
        UUID productId = UUID.randomUUID();
        String imageKey = "products/" + productId + "/foto.png";
        Product product = Product.builder().id(productId).imageUrl(imageKey).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doThrow(new DataIntegrityViolationException("fk violation"))
                .when(productRepository).delete(product);

        assertThatThrownBy(() -> service.delete(productId))
                .isInstanceOf(ProductInUseException.class)
                .hasCauseInstanceOf(DataIntegrityViolationException.class);

        verify(storagePort, never()).delete(anyString());
    }
}
