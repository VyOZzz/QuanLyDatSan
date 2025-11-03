package com.codewithvy.quanlydatsan.controller;

import com.codewithvy.quanlydatsan.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller để serve các file tĩnh (ảnh chuyển khoản)
 */
@RestController
@RequestMapping("/api/files")
@Tag(name = "Files", description = "API quản lý file")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/payment-proofs/{filename:.+}")
    @Operation(
        summary = "Xem ảnh chứng minh chuyển khoản",
        description = "Endpoint để load và hiển thị ảnh chuyển khoản"
    )
    public ResponseEntity<Resource> getPaymentProofImage(@PathVariable String filename) {
        Resource resource = fileStorageService.loadFileAsResource(filename);

        // Xác định content type
        String contentType = "image/jpeg";
        if (filename.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/venue-images/{filename:.+}")
    @Operation(
        summary = "Xem ảnh venue",
        description = "Endpoint để load và hiển thị ảnh venue"
    )
    public ResponseEntity<Resource> getVenueImage(@PathVariable String filename) {
        Resource resource = fileStorageService.loadVenueImageAsResource(filename);

        // Xác định content type
        String contentType = "image/jpeg";
        if (filename.toLowerCase().endsWith(".png")) {
            contentType = "image/png";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
