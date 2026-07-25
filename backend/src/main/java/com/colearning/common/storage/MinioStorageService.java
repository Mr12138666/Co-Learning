package com.colearning.common.storage;

import com.colearning.common.config.AppProperties;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * MinIO implementation of {@link StorageService}.
 * Uses S3-compatible API for object storage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final AppProperties appProperties;
    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        this.minioClient = MinioClient.builder()
                .endpoint(appProperties.getStorage().getEndpoint())
                .credentials(
                        appProperties.getStorage().getAccessKey(),
                        appProperties.getStorage().getSecretKey())
                .build();
        log.info("MinIO client initialized: endpoint={}", appProperties.getStorage().getEndpoint());
    }

    @Override
    public String upload(String bucket, String objectKey, byte[] content, String contentType) {
        try {
            ensureBucketExists(bucket);
            minioClient.putObject(
                    io.minio.PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(content), content.length, -1)
                            .contentType(contentType)
                            .build());
            log.info("Uploaded object: bucket={}, key={}, size={}", bucket, objectKey, content.length);
            return getUrl(bucket, objectKey);
        } catch (Exception e) {
            log.error("Failed to upload to MinIO: bucket={}, key={}", bucket, objectKey, e);
            throw new RuntimeException("Storage upload failed", e);
        }
    }

    @Override
    public String getUrl(String bucket, String objectKey) {
        return appProperties.getStorage().getEndpoint() + "/" + bucket + "/" + objectKey;
    }

    @Override
    public void delete(String bucket, String objectKey) {
        try {
            minioClient.removeObject(
                    io.minio.RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build());
            log.info("Deleted object: bucket={}, key={}", bucket, objectKey);
        } catch (Exception e) {
            log.error("Failed to delete from MinIO: bucket={}, key={}", bucket, objectKey, e);
        }
    }

    @Override
    public String generateDefaultAvatar(String seed) {
        // DiceBear API: generates a random avatar based on a seed
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + seed;
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(
                    io.minio.BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                minioClient.makeBucket(
                        io.minio.MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Created bucket: {}", bucket);
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists: {}", bucket, e);
        }
    }
}
