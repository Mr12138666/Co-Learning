package com.colearning.common.storage;

/**
 * Storage service interface for object storage (S3/MinIO compatible).
 */
public interface StorageService {

    /**
     * Uploads a file and returns the public URL.
     */
    String upload(String bucket, String objectKey, byte[] content, String contentType);

    /**
     * Generates the public URL for a stored object.
     */
    String getUrl(String bucket, String objectKey);

    /**
     * Deletes a stored object.
     */
    void delete(String bucket, String objectKey);

    /**
     * Generates a default avatar URL using DiceBear API.
     */
    String generateDefaultAvatar(String seed);
}
