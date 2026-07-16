package com.SpringBoot.infrastructure.storage;

import com.SpringBoot.application.port.outbound.StorageException;
import com.SpringBoot.application.port.outbound.StoragePort;
import com.SpringBoot.infrastructure.config.S3Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.ServerSideEncryption;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.net.URL;
import java.time.Duration;

@Service
public class S3StorageAdapter implements StoragePort {

    private static final Logger log = LoggerFactory.getLogger(S3StorageAdapter.class);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties properties;

    public S3StorageAdapter(S3Client s3Client, S3Presigner s3Presigner, S3Properties properties) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.properties = properties;
    }

    @Override
    public void upload(String key, byte[] content, String contentType) {
        validateContentType(contentType);
        validateSize(content.length);

        PutObjectRequest.Builder request = PutObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .contentType(contentType)
                .serverSideEncryption(ServerSideEncryption.fromValue(properties.sseAlgorithm()));

        if (properties.publicRead()) {
            request.acl(ObjectCannedACL.PUBLIC_READ);
        }

        try {
            s3Client.putObject(request.build(), RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            log.error("S3 respondió con error al subir el archivo {}", key, e);
            throw new StorageException("El servicio de almacenamiento respondió con error al subir " + key, e);
        } catch (SdkClientException e) {
            log.error("Fallo de conexión con S3 al subir el archivo {}", key, e);
            throw new StorageException("No se pudo contactar al servicio de almacenamiento para subir " + key, e);
        }
    }

    @Override
    public URL generatePresignedGetUrl(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(properties.presignedUrlExpirationMinutes()))
                .getObjectRequest(getObjectRequest)
                .build();

        try {
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return presigned.url();
        } catch (SdkClientException e) {
            log.error("Fallo al resolver credenciales/firmar la URL prefirmada para {}", key, e);
            throw new StorageException("No se pudo generar la URL prefirmada para " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.bucketName())
                .key(key)
                .build();

        try {
            s3Client.deleteObject(request);
        } catch (S3Exception e) {
            log.error("S3 respondió con error al eliminar el archivo {}", key, e);
            throw new StorageException("El servicio de almacenamiento respondió con error al eliminar " + key, e);
        } catch (SdkClientException e) {
            log.error("Fallo de conexión con S3 al eliminar el archivo {}", key, e);
            throw new StorageException("No se pudo contactar al servicio de almacenamiento para eliminar " + key, e);
        }
    }

    private void validateContentType(String contentType) {
        if (contentType == null || !properties.allowedContentTypes().contains(contentType)) {
            throw new IllegalArgumentException(
                    "Content-Type no permitido: " + contentType + ". Permitidos: " + properties.allowedContentTypes());
        }
    }

    private void validateSize(int sizeInBytes) {
        long maxBytes = properties.maxFileSizeMb() * 1024L * 1024L;
        if (sizeInBytes > maxBytes) {
            throw new IllegalArgumentException(
                    "El archivo supera el tamaño máximo permitido de " + properties.maxFileSizeMb() + "MB");
        }
    }
}
