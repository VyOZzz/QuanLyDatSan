package com.codewithvy.quanlydatsan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO cho Swagger UI để hiển thị form upload file
 */
@Schema(description = "Request upload file ảnh chuyển khoản")
public class FileUploadRequest {

    @Schema(description = "File ảnh chuyển khoản (jpg, jpeg, png)",
            type = "string",
            format = "binary",
            required = true)
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}

