package com.SpringBoot.application.command.product;

import com.SpringBoot.application.port.outbound.StorageException;
import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.domain.entity.Product;
import com.SpringBoot.domain.entity.ProductNotFoundException;
import com.SpringBoot.domain.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadProductImageCommandHandlerTest {

    @Mock
    private StoragePort storagePort;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private UploadProductImageCommandHandler handler;

    @Test
    void handle_subeLaImagenYActualizaElProducto_cuandoNoHabiaImagenPrevia() throws Exception {
        UUID productId = UUID.randomUUID();
        Product product = Product.builder().id(productId).build();
        byte[] content = "image-bytes".getBytes();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(storagePort.generatePresignedGetUrl(anyString())).thenReturn(new URL("https://example.com/file"));

        String url = handler.handle(new UploadProductImageCommand(productId, content, "image/png"));

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storagePort).upload(keyCaptor.capture(), eq(content), eq("image/png"));
        verify(productRepository).save(product);
        verify(storagePort, never()).delete(anyString());

        assertThat(url).isEqualTo("https://example.com/file");
        assertThat(product.getImageUrl()).isEqualTo(keyCaptor.getValue());
        assertThat(keyCaptor.getValue()).contains("products/" + productId + "/");
    }

    @Test
    void handle_eliminaLaImagenAnterior_cuandoElProductoYaTeniaUna() throws Exception {
        UUID productId = UUID.randomUUID();
        String previousKey = "products/" + productId + "/old.png";
        Product product = Product.builder().id(productId).imageUrl(previousKey).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(storagePort.generatePresignedGetUrl(anyString())).thenReturn(new URL("https://example.com/file"));

        handler.handle(new UploadProductImageCommand(productId, "image-bytes".getBytes(), "image/png"));

        verify(storagePort).delete(previousKey);
    }

    @Test
    void handle_ignoraElFalloAlEliminarLaImagenAnterior() throws Exception {
        UUID productId = UUID.randomUUID();
        String previousKey = "products/" + productId + "/old.png";
        Product product = Product.builder().id(productId).imageUrl(previousKey).build();
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(storagePort.generatePresignedGetUrl(anyString())).thenReturn(new URL("https://example.com/file"));
        doThrow(new StorageException("boom", new RuntimeException()))
                .when(storagePort).delete(previousKey);

        String url = handler.handle(new UploadProductImageCommand(productId, "image-bytes".getBytes(), "image/png"));

        assertThat(url).isEqualTo("https://example.com/file");
    }

    @Test
    void handle_lanzaProductNotFoundException_cuandoElProductoNoExiste() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(new UploadProductImageCommand(productId, "abc".getBytes(), "image/png")))
                .isInstanceOf(ProductNotFoundException.class);

        verify(storagePort, never()).upload(anyString(), any(), anyString());
    }
}
