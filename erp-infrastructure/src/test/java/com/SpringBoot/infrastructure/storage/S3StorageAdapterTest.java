package com.SpringBoot.infrastructure.storage;

import com.SpringBoot.application.port.outbound.StorageException;
import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.infrastructure.config.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageAdapterTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private S3Properties properties;

    private S3StorageAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new S3StorageAdapter(s3Client, s3Presigner, properties);
        lenient().when(properties.bucketName()).thenReturn("erp-products-images");
        lenient().when(properties.allowedContentTypes()).thenReturn(List.of("image/png"));
        lenient().when(properties.maxFileSizeMb()).thenReturn(5);
        lenient().when(properties.sseAlgorithm()).thenReturn("AES256");
        lenient().when(properties.publicRead()).thenReturn(false);
        lenient().when(properties.presignedUrlExpirationMinutes()).thenReturn(15);
    }

    @Test
    void shouldRejectUnsupportedContentType() {
        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> adapter.upload("product/1.png", "abc".getBytes(), "image/jpeg"));

        assertThat(ex.getMessage()).contains("Content-Type");
    }

    @Test
    void shouldRejectFileExceedingMaxSize() {
        byte[] oversized = new byte[6 * 1024 * 1024];

        IllegalArgumentException ex = org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> adapter.upload("product/1.png", oversized, "image/png"));

        assertThat(ex.getMessage()).contains("tamaño máximo");
    }

    @Test
    void shouldImplementStoragePort() {
        assertThat(adapter).isInstanceOf(StoragePort.class);
    }

    @Test
    void upload_subeElArchivo_cuandoS3Responde() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        adapter.upload("product/1.png", "abc".getBytes(), "image/png");
    }

    @Test
    void upload_lanzaStorageException_cuandoS3RespondeConError() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("Access Denied").statusCode(403).build());

        assertThatThrownBy(() -> adapter.upload("product/1.png", "abc".getBytes(), "image/png"))
                .isInstanceOf(StorageException.class)
                .hasCauseInstanceOf(S3Exception.class);
    }

    @Test
    void upload_lanzaStorageException_cuandoHayErrorDeConexion() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(SdkClientException.create("connection refused"));

        assertThatThrownBy(() -> adapter.upload("product/1.png", "abc".getBytes(), "image/png"))
                .isInstanceOf(StorageException.class)
                .hasCauseInstanceOf(SdkClientException.class);
    }

    @Test
    void delete_eliminaElArchivo_cuandoS3Responde() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        adapter.delete("product/1.png");
    }

    @Test
    void delete_lanzaStorageException_cuandoS3RespondeConError() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("No such key").statusCode(404).build());

        assertThatThrownBy(() -> adapter.delete("product/1.png"))
                .isInstanceOf(StorageException.class)
                .hasCauseInstanceOf(S3Exception.class);
    }

    @Test
    void delete_lanzaStorageException_cuandoHayErrorDeConexion() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(SdkClientException.create("timeout"));

        assertThatThrownBy(() -> adapter.delete("product/1.png"))
                .isInstanceOf(StorageException.class)
                .hasCauseInstanceOf(SdkClientException.class);
    }

    @Test
    void generatePresignedGetUrl_devuelveLaUrl_cuandoElPresignerResponde() throws Exception {
        URL expectedUrl = new URL("https://erp-products-images.s3.amazonaws.com/product/1.png");
        PresignedGetObjectRequest presignedRequest = mockPresignedRequest(expectedUrl);
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class))).thenReturn(presignedRequest);

        URL result = adapter.generatePresignedGetUrl("product/1.png");

        assertThat(result).isEqualTo(expectedUrl);
    }

    @Test
    void generatePresignedGetUrl_lanzaStorageException_cuandoFallaLaResolucionDeCredenciales() {
        when(s3Presigner.presignGetObject(any(GetObjectPresignRequest.class)))
                .thenThrow(SdkClientException.create("unable to load credentials"));

        assertThatThrownBy(() -> adapter.generatePresignedGetUrl("product/1.png"))
                .isInstanceOf(StorageException.class)
                .hasCauseInstanceOf(SdkClientException.class);
    }

    private static PresignedGetObjectRequest mockPresignedRequest(URL url) {
        PresignedGetObjectRequest presigned = org.mockito.Mockito.mock(PresignedGetObjectRequest.class);
        lenient().when(presigned.url()).thenReturn(url);
        return presigned;
    }
}
