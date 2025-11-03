# Hướng dẫn Upload Ảnh Venue

## Tổng quan
Mỗi venue có thể có nhiều ảnh (lưu dưới dạng danh sách URL).

**Lưu ý:** JPA/Hibernate đã tự động tạo bảng `venues_images` khi khởi động application (do annotation `@ElementCollection` trong entity `Venues`). Bạn **KHÔNG CẦN** chạy migration thủ công.

## Luồng hoạt động

### 1. Upload ảnh lên server
Sử dụng endpoint có sẵn:
```
POST /api/files/upload
Content-Type: multipart/form-data
Body: file=<chọn file ảnh>
```

Response:
```json
{
  "success": true,
  "message": "File uploaded successfully",
  "data": {
    "fileName": "abc123.jpg",
    "fileUrl": "http://localhost:8080/uploads/abc123.jpg"
  }
}
```

### 2. Tạo venue với ảnh
```
POST /api/venues
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Sân Bóng ABC",
  "description": "Mô tả sân",
  "phoneNumber": "0123456789",
  "email": "san@example.com",
  "address": {
    "provinceOrCity": "Hà Nội",
    "district": "Hà Đông",
    "detailAddress": "123 Nguyễn Trãi"
  },
  "pricePerHour": 150000,
  "openingTime": "06:00:00",
  "closingTime": "23:00:00",
  "images": [
    "http://localhost:8080/uploads/abc123.jpg",
    "http://localhost:8080/uploads/def456.jpg"
  ]
}
```

### 3. Cập nhật ảnh venue
```
PUT /api/venues/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "images": [
    "http://localhost:8080/uploads/new1.jpg",
    "http://localhost:8080/uploads/new2.jpg",
    "http://localhost:8080/uploads/new3.jpg"
  ]
}
```

**Lưu ý:** Khi update, danh sách ảnh cũ sẽ bị xóa và thay thế hoàn toàn bởi danh sách mới.

### 4. Lấy thông tin venue (có kèm ảnh)
```
GET /api/venues/{id}
```

Response:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân Bóng ABC",
    "address": {...},
    "pricePerHour": 150000,
    "images": [
      "http://localhost:8080/uploads/abc123.jpg",
      "http://localhost:8080/uploads/def456.jpg"
    ],
    "averageRating": 4.5,
    "totalReviews": 10
  }
}
```

## Ghi chú
- **Không cần ALTER TABLE venues**: Entity đã có sẵn field `images` với `@ElementCollection`, JPA sẽ tự động tạo bảng `venues_images`.
- **Dữ liệu cũ không bị mất**: Chỉ thêm bảng mới, venues hiện tại vẫn giữ nguyên (chỉ không có ảnh).
- **Giới hạn**: Mỗi URL ảnh tối đa 500 ký tự.
- **Khuyến nghị**: Upload ảnh lên cloud storage (AWS S3, Cloudinary) để tránh tải server.

## Test với Postman
1. Upload 2-3 ảnh bằng `/api/files/upload`
2. Copy các `fileUrl` trả về
3. Tạo venue mới và paste các URL vào mảng `images`
4. GET venue để xem kết quả

