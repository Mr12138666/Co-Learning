package com.colearning.common.storage;

import com.colearning.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * REST controller for file storage operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/storage")
@RequiredArgsConstructor
@Tag(name = "文件存储", description = "文件上传下载接口")
public class StorageController {

    private final StorageService storageService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传图片", description = "支持 JPG, PNG, GIF 等图片格式")
    public ResponseEntity<ApiResponse<UploadResult>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "images") String bucket) throws IOException {
        
        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "文件不能为空"));
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "只支持图片格式"));
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String objectKey = UUID.randomUUID().toString() + extension;
        
        // Upload to storage
        String url = storageService.upload(bucket, objectKey, file.getBytes(), contentType);
        
        return ResponseEntity.ok(ApiResponse.ok(new UploadResult(url, objectKey)));
    }

    @GetMapping("/url")
    @Operation(summary = "获取文件URL", description = "根据bucket和key获取文件访问地址")
    public ResponseEntity<ApiResponse<String>> getUrl(
            @RequestParam String bucket,
            @RequestParam String objectKey) {
        String url = storageService.getUrl(bucket, objectKey);
        return ResponseEntity.ok(ApiResponse.ok(url));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除文件", description = "根据bucket和key删除文件")
    public ResponseEntity<ApiResponse<Void>> delete(
            @RequestParam String bucket,
            @RequestParam String objectKey) {
        storageService.delete(bucket, objectKey);
        return ResponseEntity.ok(ApiResponse.message("文件已删除"));
    }

    @GetMapping("/proxy/{bucket}/**")
    @Operation(summary = "获取文件内容", description = "代理获取MinIO中的文件内容")
    public ResponseEntity<byte[]> getFile(
            @PathVariable String bucket,
            jakarta.servlet.http.HttpServletRequest request) {
        try {
            String prefix = "/api/storage/proxy/" + bucket + "/";
            String objectKey = request.getRequestURI().substring(
                    request.getRequestURI().indexOf(prefix) + prefix.length());
            byte[] content = ((MinioStorageService) storageService).getFile(bucket, objectKey);
            String contentType = getContentType(objectKey);
            return ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (Exception e) {
            log.error("Failed to get file: bucket={}", bucket, e);
            return ResponseEntity.notFound().build();
        }
    }

    private String getContentType(String objectKey) {
        if (objectKey.endsWith(".png")) return "image/png";
        if (objectKey.endsWith(".jpg") || objectKey.endsWith(".jpeg")) return "image/jpeg";
        if (objectKey.endsWith(".gif")) return "image/gif";
        if (objectKey.endsWith(".webp")) return "image/webp";
        if (objectKey.endsWith(".svg")) return "image/svg+xml";
        if (objectKey.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    public record UploadResult(String url, String objectKey) {}
}
