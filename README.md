# Quản Lý Đặt Sân

## Tổng Quan
Quản Lý Đặt Sân là một dự án dựa trên Java được thiết kế để quản lý việc đặt sân và sân chơi. Dự án cung cấp các tính năng cho người dùng đặt sân, tải lên bằng chứng thanh toán và quản lý trạng thái đặt sân. Ngoài ra, dự án còn bao gồm cơ chế thông báo và các API để tích hợp với ứng dụng frontend.

## Tính Năng
- Quản lý đặt sân và sân chơi.
- Tải lên và xác minh bằng chứng thanh toán.
- Hệ thống thông báo cho người dùng và chủ sân.
- Các endpoint API để tích hợp với frontend.
- Hỗ trợ nhiều mục đặt sân với các khung giờ khác nhau.

## Cấu Trúc Dự Án
```
QuanLyDatSan/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── codewithvy/
│   │   │           └── quanlydatsan/
│   │   │               ├── controller/
│   │   │               ├── service/
│   │   │               ├── entity/
│   │   │               ├── repository/
│   │   │               └── ...
│   └── resources/
│       ├── application.properties
│       └── ...
├── test/
├── uploads/
├── pom.xml
└── ...
```

## Yêu Cầu
- Java 17 trở lên
- Maven
- Cơ sở dữ liệu (ví dụ: MySQL, PostgreSQL) được cấu hình trong `application.properties`

## Cài Đặt
1. Clone repository:
   ```
   git clone <repository-url>
   ```
2. Di chuyển vào thư mục dự án:
   ```
   cd QuanLyDatSan
   ```
3. Build dự án:
   ```
   mvn clean install
   ```
4. Chạy ứng dụng:
   ```
   mvn spring-boot:run
   ```

## Tài Liệu API
Tài liệu API có sẵn trong các file sau:
- `API_DOCUMENTATION_FOR_FRONTEND.md`
- `API_MULTI_COURT_BOOKING.md`
- `API_PENDING_BOOKING_BY_VENUE.md`
- `API_UPDATE_VENUE_GUIDE.md`

## Thông Báo
Hệ thống thông báo được triển khai trong package `service`. Nó xử lý việc gửi thông báo đến người dùng và chủ sân dựa trên các thay đổi trạng thái đặt sân.

## Đóng Góp
1. Fork repository.
2. Tạo một branch mới:
   ```
   git checkout -b feature/ten-tinh-nang-cua-ban
   ```
3. Commit thay đổi của bạn:
   ```
   git commit -m "Thêm tính năng mới"
   ```
4. Push lên branch:
   ```
   git push origin feature/ten-tinh-nang-cua-ban
   ```
5. Tạo pull request.

## Giấy Phép
Dự án này được cấp phép theo giấy phép MIT. Xem file LICENSE để biết thêm chi tiết.

## Liên Hệ
Mọi thắc mắc hoặc vấn đề, vui lòng liên hệ người duy trì dự án qua email [email@example.com].
