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
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

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

    private static final String UPLOAD_BUCKET = "images";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Pattern SAFE_FILENAME = Pattern.compile("^[a-zA-Z0-9._-]+$");
    private static final Set<String> BLOCKED_EXTENSIONS = Set.of(
            ".svg", ".html", ".htm", ".js", ".xhtml", ".xml", ".mht", ".mhtml"
    );

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传图片", description = "支持 JPG, PNG, GIF, WebP 图片格式")
    public ResponseEntity<ApiResponse<UploadResult>> uploadImage(
            @RequestParam("file") MultipartFile file) throws IOException {

        // Validate file not empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("GEN-001", "文件不能为空"));
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(ApiResponse.error("GEN-001", "文件大小不能超过10MB"));
        }

        // Validate extension is not dangerous
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        if (BLOCKED_EXTENSIONS.contains(extension.toLowerCase())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("GEN-001", "不支持的文件格式"));
        }

        // Validate magic bytes (file signature)
        byte[] content = file.getBytes();
        if (!isValidImageSignature(content)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("GEN-001", "文件内容与图片格式不匹配"));
        }

        // Generate safe object key
        String objectKey = UUID.randomUUID().toString() + extension;

        // Upload to server-controlled bucket
        String url = storageService.upload(UPLOAD_BUCKET, objectKey, content, "application/octet-stream");

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
                    .header("X-Content-Type-Options", "nosniff")
                    .body(content);
        } catch (Exception e) {
            log.error("Failed to get file: bucket={}", bucket, e);
            return ResponseEntity.notFound().build();
        }
    }

    private String getContentType(String objectKey) {
        String lower = objectKey.toLowerCase();
        // Block dangerous types - force download instead of rendering
        if (lower.endsWith(".svg") || lower.endsWith(".html") || lower.endsWith(".htm")
                || lower.endsWith(".js") || lower.endsWith(".xhtml")) {
            return "application/octet-stream";
        }
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".ico")) return "image/x-icon";
        return "application/octet-stream";
    }

    private boolean isValidImageSignature(byte[] content) {
        if (content.length < 8) return false;
        // PNG: 89 50 4E 47
        if (content[0] == (byte) 0x89 && content[1] == 0x50 && content[2] == 0x4E && content[3] == 0x47) return true;
        // JPEG: FF D8 FF
        if (content[0] == (byte) 0xFF && content[1] == (byte) 0xD8 && content[2] == (byte) 0xFF) return true;
        // GIF: 47 49 46
        if (content[0] == 0x47 && content[1] == 0x49 && content[2] == 0x46) return true;
        // WebP: RIFF....WEBP
        if (content.length >= 12 && content[0] == 0x52 && content[1] == 0x49 && content[2] == 0x46 && content[3] == 0x46
                && content[8] == 0x57 && content[9] == 0x45 && content[10] == 0x42 && content[11] == 0x50) return true;
        return false;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }

    public record UploadResult(String url, String objectKey) {}
}
