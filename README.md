# Hệ thống Quản Lý Đặt Sân Thể Thao

Tài liệu này hướng dẫn cách **cài đặt, chạy và sử dụng backend** của project Quản lý đặt sân, dành cho:
- Thành viên nhóm làm đồ án (backend + frontend)
- Người muốn chạy thử API để tích hợp mobile/web frontend

---

## 1. Công nghệ sử dụng

- **Ngôn ngữ:** Java 17  
- **Framework:** Spring Boot 3.5.6  
- **ORM:** Spring Data JPA (Hibernate)  
- **CSDL:** MySQL 8.x  
- **Bảo mật:** Spring Security + JWT  
- **Gửi mail:** Spring Boot Starter Mail (SMTP Gmail)  
- **Tài liệu API:** Springdoc OpenAPI (Swagger UI)  
- **Build tool:** Maven

Các dependency chính đã được khai báo trong `pom.xml`, không cần chỉnh tay trừ khi mở rộng tính năng.

---

## 2. Chuẩn bị môi trường

### 2.1. Cài đặt bắt buộc

- Java 17 (JDK 17)
- Maven 3.x (hoặc dùng `mvnw.cmd` sẵn trong project)
- MySQL 8.x
- IDE khuyến nghị: IntelliJ IDEA / VS Code / Eclipse

### 2.2. Tạo database MySQL

Tạo một database trống, ví dụ:
Làm như này để có thể lưu trữ tiếng Việt có dấu:
```sql
CREATE DATABASE quan_ly_dat_san CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Sau đó cấu hình trong `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/quan_ly_dat_san
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

Các cấu hình khác (DDL auto, mail, JWT secret, ...) đã có sẵn trong project, chỉ cần sửa lại giá trị phù hợp môi trường của bạn nếu cần.

---

## 3. Cách chạy project backend

Tại thư mục gốc project (`QuanLyDatSan`):

### 3.1. Build project

```bash
mvn clean install
```

Hoặc trên Windows có thể dùng wrapper:

```bash
mvnw.cmd clean install
```

### 3.2. Chạy application

```bash
mvn spring-boot:run
```

Hoặc:

```bash
mvnw.cmd spring-boot:run
```

- Mặc định application sẽ chạy tại: `http://localhost:8080`
- Kiểm tra log xem có lỗi kết nối DB hay lỗi cấu hình nào không.

### 3.3. Truy cập tài liệu Swagger

Sau khi app chạy thành công, mở trình duyệt:

- Swagger UI: `http://localhost:8080/swagger-ui.html`  
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Tại Swagger UI, bạn có thể:
- Xem danh sách toàn bộ API
- Thử gọi API trực tiếp (nhớ nhập JWT token vào phần Authorize đối với API cần đăng nhập)

---

## 4. Luồng sử dụng cơ bản (dành cho Frontend / Tester)

### 4.1. Đăng ký & đăng nhập

1. **Đăng ký tài khoản user thường**  
   - `POST /api/auth/register`
   - Body JSON ví dụ: thông tin số điện thoại / email, mật khẩu, họ tên...

2. **Đăng nhập**  
   - `POST /api/auth/login`
   - Gửi số điện thoại/email + mật khẩu
   - Backend trả về **JWT token** trong response

3. **Gắn JWT vào header khi gọi API cần auth**

   ```http
   Authorization: Bearer <JWT_TOKEN_NHẬN_ĐƯỢC_KHI_LOGIN>
   ```

### 4.2. Nâng cấp lên chủ sân (Owner)

1. Đăng nhập bằng user thường để lấy token
2. Gọi API:
   - `POST /api/users/me/request-owner-role`
3. Sau khi được cấp ROLE_OWNER (theo logic của hệ thống), user có thể tạo và quản lý sân.

### 4.3. Quản lý sân (Venues)

Các API chính:

- `GET /api/venues` – Lấy danh sách tất cả sân (cho khách xem)
- `GET /api/venues/{id}` – Xem chi tiết 1 sân
- `GET /api/venues/search` – Tìm kiếm sân theo tên/địa chỉ
- `GET /api/venues/my` – Lấy danh sách sân thuộc **chủ sân hiện tại** (cần ROLE_OWNER)
- `POST /api/venues` – Tạo sân mới (ROLE_OWNER)
- `PUT /api/venues/{id}` – Cập nhật sân (ROLE_OWNER, chỉ owner của sân đó)
- `DELETE /api/venues/{id}` – Xóa sân (ROLE_OWNER, chỉ owner)
- `POST /api/venues/{id}/images` – Upload ảnh cho sân
- `DELETE /api/venues/{id}/images/{imagePath}` – Xóa ảnh của sân

**Lưu ý cập nhật venue:**
- Có thể cập nhật: tên, mô tả, số điện thoại, email, địa chỉ, **giá theo giờ**, thời gian mở cửa/đóng cửa, số lượng sân con, ảnh.
- Khi tăng/giảm số lượng sân con, backend sẽ tự tạo/xóa các `Court` tương ứng theo đúng logic hiện tại.

### 4.4. Quản lý sân con (Courts)

- `GET /api/venues/{venueId}/courts` – Danh sách court của một venue
- `GET /api/venues/{venueId}/courts/{date}` – Lấy danh sách court và tình trạng trống/đã đặt theo ngày (dùng để hiển thị lịch cho FE)
- `POST /api/courts` – Tạo thêm court (ROLE_OWNER)
- `PUT /api/courts/{id}` – Cập nhật court (ROLE_OWNER)
- `DELETE /api/courts/{id}` – Xóa court (ROLE_OWNER)

Để **“khóa” một court** (không cho đặt nữa), FE có thể dùng API update court và set trạng thái `isActive = false` (xem chi tiết trong entity/DTO tương ứng).

### 4.5. Đặt sân (Bookings)

Luồng cơ bản phía user:

1. **Chọn sân + sân con + khung giờ**
   - Client gọi `GET /api/venues/{venueId}/courts/{date}` để xem slot trống.

2. **Tạo booking**
   - `POST /api/bookings`
   - Body ví dụ (nhiều item):

     ```json
     {
       "bookingItems": [
         {
           "courtId": 1,
           "startTime": "2025-11-24T18:00:00",
           "endTime": "2025-11-24T19:00:00"
         },
         {
           "courtId": 1,
           "startTime": "2025-11-24T19:00:00",
           "endTime": "2025-11-24T20:00:00"
         }
       ]
     }
     ```

   - Khi tạo xong:
     - `status = PENDING_PAYMENT`
     - `expireTime = now + 5 phút` (thời gian cho phép upload chứng từ thanh toán)

3. **User upload ảnh chuyển khoản**
   - `POST /api/bookings/{id}/upload-proof` (multipart/form-data)
   - Sau khi upload và xác nhận, booking sẽ chuyển về trạng thái **`PAYMENT_UPLOADED`** nếu chưa hết hạn.

4. **Chủ sân duyệt / từ chối**
   - `PUT /api/bookings/{id}/accept` – chấp nhận (ROLE_OWNER, đúng owner của venue)
   - `PUT /api/bookings/{id}/reject` – từ chối, kèm lý do

5. **Auto hoàn thành**
   - Sau khi qua thời gian kết thúc (`endTime`) và booking ở trạng thái `CONFIRMED`, scheduler sẽ tự chuyển sang `COMPLETED`.

6. **User hủy booking** (nếu còn được phép)
   - `DELETE /api/bookings/{id}/cancel`

### 4.6. Trạng thái booking & xử lý tự động

Các trạng thái chính:

- `PENDING_PAYMENT`: Vừa tạo, chờ user thanh toán
- `PAYMENT_UPLOADED`: User đã upload chứng từ thanh toán
- `CONFIRMED`: Chủ sân đã xác nhận
- `COMPLETED`: Hệ thống tự chuyển sau khi kết thúc giờ chơi
- `EXPIRED`: Quá hạn thanh toán (sau 5 phút, hoặc startTime < now theo logic mở rộng)
- `REJECTED`: Chủ sân từ chối
- `CANCELLED`: User hủy

**Scheduled tasks hiện có:**

- Mỗi 1 phút:
  - Tìm booking `PENDING_PAYMENT` đã quá `expireTime` (quá 5 phút)  
  - Chuyển sang `EXPIRED` và gửi thông báo cho user

- Mỗi 5 phút:
  - Tìm booking `CONFIRMED` đã qua `endTime`  
  - Chuyển sang `COMPLETED`

Ngoài ra, project đã sẵn cấu trúc để có thể mở rộng thêm logic:
- Nếu `startTime < currentTime` mà booking chưa được xác nhận thì tự chuyển sang hết hạn.

### 4.7. Review (Đánh giá sân)

- `GET /api/reviews/venue/{venueId}` – danh sách review của một sân
- `POST /api/reviews` – tạo review mới (ROLE_USER)
- `PUT /api/reviews/{id}` – cập nhật review (chủ review)
- `DELETE /api/reviews/{id}` – xóa review (chủ review)

**Ràng buộc:**
- Chỉ user có **booking đã hoàn thành** tại sân đó mới được review (dựa trên trạng thái booking `COMPLETED`).
- Mỗi user chỉ được review **1 lần / 1 venue** (theo logic hiện tại).

### 4.8. Notifications (Thông báo)

Backend đã có:
- Entity `Notification`
- API REST để lấy và đánh dấu đã đọc:
  - `GET /api/notifications/my`
  - `PUT /api/notifications/{id}/read`
  - `PUT /api/notifications/read-all`
  - `DELETE /api/notifications/{id}`

Ngoài ra, có tài liệu `WEBSOCKET_NOTIFICATION_GUIDE.md` và cấu trúc sẵn để mở rộng **WebSocket notification realtime** trong tương lai.  
Tuy nhiên **frontend hiện tại không triển khai trung tâm notification dạng “chuông”**, mà chỉ sử dụng các phản hồi JSON bình thường trong luồng nghiệp vụ.

---

## 5. API Thống kê (Analytics)

Dùng cho chủ sân (OWNER) để xem các số liệu tổng quan.

Các endpoint chính:

- `GET /api/analytics/owner` – API tổng hợp (theo specs chính)
- `GET /api/owners/me/analytics` – analytics cho owner hiện tại
- `GET /api/owners/{ownerId}/analytics` – analytics theo owner cụ thể
- `GET /api/venues/{venueId}/analytics` – analytics theo từng venue

**Query params phổ biến:**
- `period`: `DAY` \| `WEEK` \| `MONTH` \| `YEAR`
- `startDate`, `endDate`: định dạng `yyyy-MM-dd`

**Dữ liệu trả về** bao gồm:
- Tổng doanh thu, số lượng booking, tỷ lệ chuyển đổi
- Doanh thu theo ngày / tuần / tháng
- Hiệu suất từng sân (có rating trung bình nếu có)
- Top khách hàng
- Thống kê theo khung giờ

Chi tiết hơn xem trong file `API_ANALYTICS_GUIDE.md` (nếu có trong project).

---

## 6. File upload

### 6.1. Ảnh chuyển khoản (payment proof)

- API: `POST /api/bookings/{id}/upload-proof`
- Loại: `multipart/form-data`
- Field: `file` (ảnh)
- File được lưu trong thư mục: `uploads/payment-proofs/`

### 6.2. Ảnh sân (venue images)

- API: `POST /api/venues/{id}/images`
- Loại: `multipart/form-data`
- Field: `files` (nhiều ảnh)
- File được lưu trong thư mục: `uploads/venue-images/`

---
