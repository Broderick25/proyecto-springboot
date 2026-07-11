package com.SpringBoot.infrastructure.storage;

import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.infrastructure.config.S3Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Properties properties;

    @InjectMocks
    private S3StorageAdapter adapter;

    @Test
    void shouldRejectUnsupportedContentType() {
        when(properties.allowedContentTypes()).thenReturn(List.of("image/png"));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adapter.upload("product/1.png", "abc".getBytes(), "image/jpeg"));

        assertTrue(ex.getMessage().contains("Content-Type"));
    }

    @Test
    void shouldImplementStoragePort() {
        assertTrue(adapter instanceof StoragePort);
    }
}
