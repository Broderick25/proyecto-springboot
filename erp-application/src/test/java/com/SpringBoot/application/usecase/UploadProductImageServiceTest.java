package com.SpringBoot.application.usecase;

import com.SpringBoot.application.port.outbound.StoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UploadProductImageServiceTest {

    @Mock
    private StoragePort storagePort;

    @InjectMocks
    private UploadProductImageService service;

    @Test
    void shouldUploadImageAndReturnPresignedUrl() throws Exception {
        byte[] content = "image-bytes".getBytes();
        when(storagePort.generatePresignedGetUrl(anyString())).thenReturn(new URL("https://example.com/file"));

        String url = service.upload("prod-1", content, "image/png");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(storagePort).upload(keyCaptor.capture(), eq(content), eq("image/png"));
        verify(storagePort).generatePresignedGetUrl(keyCaptor.getValue());

        assertEquals("https://example.com/file", url);
        assertTrue(keyCaptor.getValue().contains("products/prod-1/"));
    }
}
