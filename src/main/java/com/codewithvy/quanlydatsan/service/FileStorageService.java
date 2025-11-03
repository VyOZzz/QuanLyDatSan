package com.codewithvy.quanlydatsan.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * Service xử lý lưu trữ và quản lý file upload (ảnh chuyển khoản)
 */
@Service
public class FileStorageService {

    private final Path paymentProofStorageLocation;
    private final Path venueImageStorageLocation;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public FileStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
        this.paymentProofStorageLocation = Paths.get(uploadDir, "payment-proofs").toAbsolutePath().normalize();
        this.venueImageStorageLocation = Paths.get(uploadDir, "venue-images").toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.paymentProofStorageLocation);
            Files.createDirectories(this.venueImageStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory!", ex);
        }
    }

    /**
     * Lưu file ảnh chuyển khoản cho booking
     */
    public String savePaymentProofImage(MultipartFile file, Long bookingId) {
        // Validate file không null và không rỗng
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa là 10MB");
        }

        // Lấy tên file gốc và extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);

        // Validate extension
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh định dạng: jpg, jpeg, png");
        }

        // Tạo tên file mới: booking-{id}-{timestamp}.{extension}
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String newFilename = String.format("booking-%d-%s.%s", bookingId, timestamp, fileExtension);

        try {
            // Copy file vào thư mục lưu trữ
            Path targetLocation = this.paymentProofStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL
            return "/api/files/payment-proofs/" + newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file: " + newFilename, ex);
        }
    }

    /**
     * Load file để serve cho client
     */
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.paymentProofStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File không tồn tại: " + filename);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File không tồn tại: " + filename, ex);
        }
    }

    /**
     * Xóa file ảnh chuyển khoản
     */
    public void deletePaymentProofImage(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.paymentProofStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log error nhưng không throw exception
            System.err.println("Không thể xóa file: " + fileUrl);
        }
    }

    /**
     * Ly extension ca file
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Lưu ảnh venue
     */
    public String saveVenueImage(MultipartFile file, Long venueId) {
        // Validate file không null và không rỗng
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Validate kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File quá lớn. Kích thước tối đa là 10MB");
        }

        // Lấy tên file gốc và extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);

        // Validate extension
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh định dạng: jpg, jpeg, png");
        }

        // Tạo tên file mới: venue-{id}-{timestamp}.{extension}
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String newFilename = String.format("venue-%d-%s.%s", venueId, timestamp, fileExtension);

        try {
            // Copy file vào thư mục lưu trữ
            Path targetLocation = this.venueImageStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative URL
            return "/api/files/venue-images/" + newFilename;
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu file: " + newFilename, ex);
        }
    }

    /**
     * Load ảnh venue để serve cho client
     */
    public Resource loadVenueImageAsResource(String filename) {
        try {
            Path filePath = this.venueImageStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File không tồn tại: " + filename);
            }
        } catch (Exception ex) {
            throw new RuntimeException("File không tồn tại: " + filename, ex);
        }
    }

    /**
     * Xóa ảnh venue
     */
    public void deleteVenueImage(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            // Extract filename from URL
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.venueImageStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log error nhưng không throw exception
            System.err.println("Không thể xóa file: " + fileUrl);
        }
    }
}

