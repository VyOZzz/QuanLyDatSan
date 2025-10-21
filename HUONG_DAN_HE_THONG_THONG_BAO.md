# 📋 HỆ THỐNG THÔNG BÁO VÀ QUẢN LÝ ĐẶT SÂN

## 🎯 TỔNG QUAN

Hệ thống đã được thiết kế với workflow hoàn chỉnh:
1. Người dùng đặt sân → Hiển thị thông tin TK chủ sân + thời hạn thanh toán 15 phút
2. Sân bị KHÓA trong thời gian pending (không ai đặt được)
3. Người dùng chuyển khoản + upload ảnh + confirm → Gửi thông báo cho chủ sân
4. Chủ sân kiểm tra TK ngân hàng → Accept/Reject
5. Accept → Thông báo cho người dùng + Giữ sân
6. Reject hoặc Expired → Giải phóng sân cho người khác đặt

---

## 📁 CÁC FILE ĐÃ TẠO/CẬP NHẬT

### ✅ Entities (đã cập nhật/tạo mới)
- `BookingStatus.java` - Thêm PENDING_PAYMENT, PAYMENT_UPLOADED, REJECTED, EXPIRED
- `Booking.java` - Thêm expireTime, paymentProofUrl, rejectionReason
- `User.java` - Thêm bankName, bankAccountNumber, bankAccountName
- `Venues.java` - Thêm owner (chủ sân) - BẮT BUỘC
- `Notification.java` ✨ MỚI
- `NotificationType.java` ✨ MỚI

### ✅ Repositories
- `BookingRepository.java` - Thêm query tìm booking hết hạn, pending, của venues
- `NotificationRepository.java` ✨ MỚI

### ✅ DTOs
- `BookingResponse.java` - Thêm thông tin expireTime, paymentProof, ownerBankInfo
- `OwnerBankInfoDTO.java` ✨ MỚI
- `PaymentProofRequest.java` ✨ MỚI
- `BookingRejectRequest.java` ✨ MỚI
- `NotificationDTO.java` ✨ MỚI (sử dụng Instant cho createdAt)

### ✅ Services
- `BookingService.java` + `BookingServiceImpl.java` - Thêm confirmPayment, acceptBooking, rejectBooking
- `NotificationService.java` + `NotificationServiceImpl.java` ✨ MỚI
- `BookingExpirationService.java` ✨ MỚI (Scheduled job tự động cancel booking hết hạn)

### ✅ Controllers
- `BookingController.java` - Thêm các endpoint mới
- `NotificationController.java` ✨ MỚI

### ✅ Application
- `QuanLyDatSanApplication.java` - Thêm @EnableScheduling

---

## 🚀 API ENDPOINTS

### 📌 BOOKING APIs

#### 1. Tạo booking mới (USER)
```http
POST /api/bookings
Authorization: Bearer {token}

Request:
{
  "courtId": 1,
  "startTime": "2025-10-21T14:00:00",
  "endTime": "2025-10-21T16:00:00"
}

Response:
{
  "success": true,
  "data": {
    "id": 123,
    "status": "PENDING_PAYMENT",
    "totalPrice": 200000,
    "expireTime": "2025-10-21T14:15:00",
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN A",
      "ownerName": "Nguyễn Văn A"
    }
  },
  "message": "Đặt sân thành công. Vui lòng chuyển khoản trong 15 phút."
}
```

#### 2. Upload chứng minh chuyển khoản + Confirm (USER)
```http
PUT /api/bookings/{id}/confirm-payment
Authorization: Bearer {token}

Request:
{
  "paymentProofUrl": "https://example.com/payment-proof.jpg"
}

Response:
{
  "success": true,
  "data": {
    "id": 123,
    "status": "PAYMENT_UPLOADED",
    "paymentProofUrl": "https://example.com/payment-proof.jpg",
    "paymentProofUploaded": true
  },
  "message": "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận."
}
```

#### 3. Chủ sân accept booking (OWNER)
```http
PUT /api/bookings/{id}/accept
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": {
    "id": 123,
    "status": "CONFIRMED"
  },
  "message": "Đã xác nhận đặt sân thành công."
}
```

#### 4. Chủ sân reject booking (OWNER)
```http
PUT /api/bookings/{id}/reject
Authorization: Bearer {token}

Request:
{
  "rejectionReason": "Chuyển khoản không đủ số tiền"
}

Response:
{
  "success": true,
  "data": {
    "id": 123,
    "status": "REJECTED",
    "rejectionReason": "Chuyển khoản không đủ số tiền"
  },
  "message": "Đã từ chối đặt sân."
}
```

#### 5. Xem booking chờ xác nhận (OWNER)
```http
GET /api/bookings/pending
Authorization: Bearer {token}

Response: Danh sách booking có status PAYMENT_UPLOADED
```

#### 6. Xem tất cả booking của venues (OWNER)
```http
GET /api/bookings/venue/{venueId}
Authorization: Bearer {token}
```

#### 7. Xem booking của tôi (USER)
```http
GET /api/bookings/my-bookings
Authorization: Bearer {token}
```

#### 8. Hủy booking (USER)
```http
PUT /api/bookings/{id}/cancel
Authorization: Bearer {token}

Note: Chỉ có thể hủy booking chưa CONFIRMED hoặc COMPLETED
```

---

### 📌 NOTIFICATION APIs

#### 1. Lấy danh sách thông báo
```http
GET /api/notifications
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": [
    {
      "id": 1,
      "bookingId": 123,
      "type": "PAYMENT_UPLOADED",
      "title": "Có khách đã chuyển khoản",
      "message": "Khách hàng Nguyễn Văn B đã chuyển khoản...",
      "isRead": false,
      "createdAt": "2025-10-21T07:05:00.000Z",
      "senderName": "Nguyễn Văn B"
    }
  ]
}

Note: createdAt trả về dạng Instant (ISO-8601 UTC timestamp)
```

#### 2. Đếm số thông báo chưa đọc
```http
GET /api/notifications/unread-count
Authorization: Bearer {token}

Response:
{
  "success": true,
  "data": 5
}
```

#### 3. Đánh dấu đã đọc
```http
PUT /api/notifications/{id}/read
Authorization: Bearer {token}
```

#### 4. Đánh dấu tất cả đã đọc
```http
PUT /api/notifications/read-all
Authorization: Bearer {token}
```

#### 5. Xóa thông báo
```http
DELETE /api/notifications/{id}
Authorization: Bearer {token}
```

---

## 📊 WORKFLOW CHI TIẾT

### BƯỚC 1: Người dùng tạo booking
- Status: `PENDING_PAYMENT`
- Sân bị KHÓA ngay lập tức (Court.isBooked = true)
- ExpireTime = now + 15 phút
- Response trả về thông tin TK chủ sân để chuyển khoản

### BƯỚC 2: Hệ thống tự động cancel booking hết h��n
- Job chạy mỗi 1 phút (BookingExpirationService)
- Tìm booking có status PENDING_PAYMENT và expireTime < now
- Đổi status → EXPIRED
- GIẢI PHÓNG SÂN (Court.isBooked = false)
- Gửi thông báo BOOKING_EXPIRED cho người dùng

### BƯỚC 3: Người dùng upload chứng minh + Confirm
- Upload ảnh chuyển khoản (URL)
- Nhấn confirm payment
- Status: PENDING_PAYMENT → PAYMENT_UPLOADED
- Gửi thông báo PAYMENT_UPLOADED cho CHỦ SÂN

### BƯỚC 4a: Chủ sân ACCEPT
- Chủ sân kiểm tra tài khoản ngân hàng (manual)
- Thấy tiền đã về → Accept
- Status: PAYMENT_UPLOADED → CONFIRMED
- Sân vẫn bị khóa
- Gửi thông báo BOOKING_CONFIRMED cho NGƯỜI DÙNG

### BƯỚC 4b: Chủ sân REJECT
- Chủ sân kiểm tra TK → Không thấy tiền hoặc sai số tiền
- Reject với lý do cụ thể
- Status: PAYMENT_UPLOADED → REJECTED
- GIẢI PHÓNG SÂN (Court.isBooked = false)
- Lưu lý do từ chối
- Gửi thông báo BOOKING_REJECTED cho NGƯỜI DÙNG

---

## 🔧 HƯỚNG DẪN CÀI ĐẶT

### 1. Chuẩn bị Database

**Nếu đang có dữ liệu test cũ:**
```sql
-- Xóa và tạo lại database (khuyến nghị cho test)
DROP DATABASE quanlydatsan;
CREATE DATABASE quanlydatsan;
```

**Lý do:** Vì `owner_id` trong bảng `venues` là bắt buộc (NOT NULL), nếu có dữ liệu cũ không có owner_id sẽ bị lỗi.

### 2. Build và chạy ứng dụng
```bash
mvn clean install
mvn spring-boot:run
```

### 3. Hibernate tự động tạo/cập nhật database
Khi app khởi động, Hibernate sẽ tự động:
- ✅ Tạo bảng `notification`
- ✅ Thêm cột `owner_id` vào `venues` (NOT NULL)
- ✅ Thêm cột `bank_name`, `bank_account_number`, `bank_account_name` vào `user`
- ✅ Thêm cột `expire_time`, `payment_proof_uploaded`, `payment_proof_url`, `payment_proof_uploaded_at`, `rejection_reason` vào `booking`
- ✅ Tạo tất cả foreign keys và indexes

Bạn sẽ thấy log:
```
Hibernate: alter table venues add column owner_id bigint not null
Hibernate: create table notification (...)
Hibernate: alter table user add column bank_name varchar(255)
...
```

### 4. Cấu hình Scheduling (đã tự động enable)
- Đã thêm `@EnableScheduling` vào QuanLyDatSanApplication.java
- BookingExpirationService sẽ tự động chạy mỗi 1 phút để cancel booking hết hạn

---

## 🔐 PHÂN QUYỀN

### USER (ROLE_USER)
- ✅ Tạo booking
- ✅ Upload payment proof + confirm
- ✅ Xem booking của mình
- ✅ Hủy booking (chưa confirmed)
- ✅ Xem thông báo của mình

### OWNER (ROLE_OWNER)
- ✅ Tạo venues (tự động là owner)
- ✅ Accept booking
- ✅ Reject booking
- ✅ Xem tất cả booking của venues
- ✅ Xem booking chờ xác nhận
- ✅ Xem thông báo của mình
- ✅ Cập nhật thông tin tài khoản ngân hàng

### ADMIN (ROLE_ADMIN)
- ✅ Full access

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. Khóa sân trong thời gian pending ⭐
- ✅ Khi booking có status **PENDING_PAYMENT** → Sân BỊ KHÓA
- ✅ Khi booking có status **PAYMENT_UPLOADED** → Sân VẪN BỊ KHÓA
- ✅ Khi booking có status **CONFIRMED** → Sân VẪN BỊ KHÓA
- ❌ Khi booking **EXPIRED, CANCELLED, REJECTED** → Sân ĐƯỢC GIẢI PHÓNG
- 🎯 **Kết quả:** Không ai có thể đặt sân trong khung giờ đang bị khóa

### 2. Thời gian hết hạn thanh toán
- Mặc định: **15 phút**
- Có thể thay đổi tại constant `PAYMENT_EXPIRE_MINUTES` trong `BookingServiceImpl.java`
- Job tự động chạy **mỗi 1 phút** để kiểm tra và cancel booking hết hạn

### 3. Thông tin tài khoản ngân hàng
- Chủ sân cần cập nhật thông tin TK ngân hàng trong profile:
  - `bankName` - Tên ngân hàng (VD: Vietcombank, Techcombank)
  - `bankAccountNumber` - Số tài khoản
  - `bankAccountName` - Tên chủ tài khoản
- Khi tạo Venues, hệ thống tự động gán `owner_id` = user hiện tại
- **owner_id là BẮT BUỘC** - không thể tạo venues mà không có owner

### 4. Upload ảnh chuyển khoản
- API chỉ nhận **URL của ảnh** (String)
- Bạn cần implement riêng service upload file:
  - **Local storage:** Lưu vào thư mục `uploads/` trong server
  - **Cloud storage:** Upload lên AWS S3, Cloudinary, Firebase Storage, etc.
  - Trả về URL sau khi upload thành công
- Frontend upload ảnh → Nhận URL → Gọi API confirm-payment với URL đó

### 5. Kiểu dữ liệu thời gian
- `createdAt`, `updatedAt` trong các entity sử dụng kiểu **Instant** (UTC timestamp)
- Frontend nhận được cần convert sang local timezone nếu cần hiển thị
- Ví dụ convert trong JavaScript:
  ```javascript
  const instant = "2025-10-21T07:05:00.000Z";
  const localTime = new Date(instant).toLocaleString();
  ```

---

## 🧪 TEST WORKFLOW

### Test Case 1: Đặt sân thành công ✅
1. **User** đăng nhập với ROLE_USER
2. **User** tạo booking → Nhận response với thông tin TK chủ sân
   ```
   Status: PENDING_PAYMENT
   Court.isBooked: true ← SÂN BỊ KHÓA
   ```
3. **User** chuyển khoản thực tế theo thông tin TK
4. **User** upload ảnh chứng minh + gọi API confirm-payment
   ```
   Status: PAYMENT_UPLOADED
   Court.isBooked: true ← SÂN VẪN BỊ KHÓA
   ```
5. **Owner** nhận thông báo PAYMENT_UPLOADED
6. **Owner** check TK ngân hàng → Thấy tiền đã về
7. **Owner** gọi API accept booking
   ```
   Status: CONFIRMED
   Court.isBooked: true ← SÂN VẪN BỊ KHÓA
   ```
8. **User** nhận thông báo BOOKING_CONFIRMED → Đặt sân thành công!

### Test Case 2: Hết hạn thanh toán ⏰
1. **User** tạo booking
   ```
   Status: PENDING_PAYMENT
   ExpireTime: now + 15 phút
   Court.isBooked: true ← SÂN BỊ KHÓA
   ```
2. **User** không chuyển khoản trong 15 phút
3. **Hệ thống** (BookingExpirationService) tự động chạy sau 15 phút
   ```
   Status: EXPIRED
   Court.isBooked: false ← SÂN ĐƯỢC GIẢI PHÓNG
   ```
4. **User** nhận thông báo BOOKING_EXPIRED
5. **Người khác** có thể đặt sân trong khung giờ đó

### Test Case 3: Chủ sân từ chối ❌
1. **User** tạo booking + upload proof
   ```
   Status: PAYMENT_UPLOADED
   Court.isBooked: true ← SÂN BỊ KHÓA
   ```
2. **Owner** nhận thông báo
3. **Owner** check TK ngân hàng → Không thấy tiền / Sai số tiền
4. **Owner** gọi API reject với lý do: "Chuyển khoản không đủ số tiền"
   ```
   Status: REJECTED
   Court.isBooked: false ← SÂN ĐƯỢC GIẢI PHÓNG
   ```
5. **User** nhận thông báo BOOKING_REJECTED với lý do từ chối
6. **Người khác** có thể đặt sân trong khung giờ đó

### Test Case 4: User hủy booking 🚫
1. **User** tạo booking
   ```
   Status: PENDING_PAYMENT
   Court.isBooked: true ← SÂN BỊ KHÓA
   ```
2. **User** đổi ý, gọi API cancel booking
   ```
   Status: CANCELLED
   Court.isBooked: false ← SÂN ĐƯỢC GIẢI PHÓNG
   ```
3. **Người khác** có thể đặt sân

### Test Case 5: Không thể đặt sân đang bị khóa 🔒
1. **User A** tạo booking cho sân X từ 14:00-16:00
   ```
   Status: PENDING_PAYMENT
   Court X.isBooked: true
   ```
2. **User B** cố đặt sân X từ 14:00-16:00
   ```
   → API trả về lỗi: "Sân đã được đặt trong khung giờ này"
   ```
3. Chỉ khi User A hết hạn/cancel/rejected, User B mới đặt được

---

## 📮 HƯỚNG DẪN TEST TRÊN POSTMAN

### 🔧 CHUẨN BỊ TRƯỚC KHI TEST

#### 1. Chạy ứng dụng
```bash
mvn spring-boot:run
```
Đảm bảo server đang chạy tại `http://localhost:8080`

#### 2. Tạo Collection mới trong Postman
- Collection name: `QuanLyDatSan - Notification System`
- Base URL: `http://localhost:8080`

#### 3. Tạo Environment trong Postman
Tạo environment với các biến:
- `baseUrl`: `http://localhost:8080`
- `userToken`: (sẽ lưu sau khi đăng nhập USER)
- `ownerToken`: (sẽ lưu sau khi đăng nhập OWNER)
- `bookingId`: (sẽ lưu sau khi tạo booking)
- `venueId`: (sẽ lưu sau khi tạo venue)
- `courtId`: (sẽ lưu sau khi tạo court)

---

### 📝 BƯỚC 1: ĐĂNG KÝ VÀ ĐĂNG NHẬP

#### 1.1. Đăng ký tài khoản mới (tự động là USER)
```
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

Body (raw JSON):
{
  "fullname": "Nguyễn Văn User",
  "email": "user@test.com",
  "phone": "0901234567",
  "password": "123456",
  "confirmPassword": "123456"
}

✅ Expected Response (200):
{
  "success": true,
  "data": "User registered successfully",
  "message": "Registered"
}

📌 NOTE: Tất cả tài khoản mới đều tự động có role ROLE_USER
```

#### 1.2. Đăng nhập
```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

Body (raw JSON):
{
  "phone": "0901234567",
  "password": "123456"
}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 1,
    "phone": "0901234567",
    "roles": ["ROLE_USER"]
  },
  "message": "Login success"
}

📌 ACTION: Copy token và lưu vào Environment variable `userToken`
```

**Cách lưu token tự động vào Environment:**
- Tab **Tests** trong request, thêm script:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("userToken", jsonData.data.jwtToken);
}
```

#### 1.3. Nâng cấp tài khoản lên OWNER (chỉ khi đã đăng nhập)
```
POST {{baseUrl}}/api/users/me/request-owner-role
Authorization: Bearer {{userToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": "Success",
  "message": "Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập lại để cập nhật quyền."
}

📌 NOTE: 
- Phải đăng nhập với tài khoản USER trước
- Không cần body request
- Sau khi nâng cấp, PHẢI ĐĂNG NHẬP LẠI để token có role mới
- Sau khi đăng nhập lại, tài khoản có cả 2 role: ROLE_USER và ROLE_OWNER
```

#### 1.4. Đăng nhập lại sau khi nâng cấp lên OWNER
```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

Body (raw JSON):
{
  "phone": "0901234567",
  "password": "123456"
}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 1,
    "phone": "0901234567",
    "roles": ["ROLE_USER", "ROLE_OWNER"]  ← Có cả 2 role
  },
  "message": "Login success"
}

📌 ACTION: Copy token mới và lưu vào Environment variable `ownerToken`
```

**Script tự động lưu:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("ownerToken", jsonData.data.token);
}
```

#### 1.5. Xem thông tin tài khoản hiện tại
```
GET {{baseUrl}}/api/users/me
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "phone": "0901234567",
    "fullname": "Nguyễn Văn User",
    "email": "user@test.com",
    "roles": [
      {"id": 1, "name": "ROLE_USER"},
      {"id": 2, "name": "ROLE_OWNER"}
    ],
    "bankName": null,
    "bankAccountNumber": null,
    "bankAccountName": null
  },
  "message": "Success"
}
```

#### 1.6. Đăng ký tài khoản thứ 2 để test (USER khác)
```
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

Body (raw JSON):
{
  "fullname": "Nguyễn Văn B",
  "email": "userb@test.com",
  "phone": "0909999999",
  "password": "123456",
  "confirmPassword": "123456"
}

✅ Expected Response (200):
{
  "success": true,
  "data": "User registered successfully",
  "message": "Registered"
}
```

---

### 🏦 BƯỚC 2: CẬP NHẬT THÔNG TIN NGÂN HÀNG (CHỦ SÂN)

⚠️ **LƯU Ý:** Trước khi tạo venue, chủ sân PHẢI cập nhật thông tin ngân hàng để nhận thanh toán từ khách đặt sân.

```
PUT {{baseUrl}}/api/users/me
Authorization: Bearer {{ownerToken}}
Content-Type: application/json

Body (raw JSON):
{
  "bankName": "Vietcombank",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "NGUYEN VAN A"
}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "fullname": "Nguyễn Văn User",
    "phone": "0901234567",
    "email": "user@test.com",
    "roles": ["ROLE_USER", "ROLE_OWNER"],
    "bankName": "Vietcombank",
    "bankAccountNumber": "1234567890",
    "bankAccountName": "NGUYEN VAN A"
  },
  "message": "Cập nhật thông tin thành công"
}

📌 NOTE: Chỉ tài khoản có ROLE_OWNER mới cần thông tin ngân hàng
```

---

### 🏟️ BƯỚC 3: CHỦ SÂN TẠO VENUE VÀ COURT

#### 3.1. Tạo Venue (Sân bóng)
```
POST {{baseUrl}}/api/venues
Authorization: Bearer {{ownerToken}}
Content-Type: application/json

Body (raw JSON):
{
  "name": "Sân bóng ABC",
  "description": "Sân bóng đá mini chất lượng cao, đầy đủ tiện nghi",
  "phoneNumber": "0901234567",
  "email": "contact@sanabac.com",
  "address": {
    "detailAddress": "123 Đường Lê Lợi",
    "district": "Quận 1",
    "provinceOrCity": "TP. Hồ Chí Minh"
  }
}

✅ Expected Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân bóng ABC",
    "description": "Sân bóng đá mini chất lượng cao, đầy đủ tiện nghi",
    "phoneNumber": "0901234567",
    "email": "contact@sanabac.com",
    "address": {
      "detailAddress": "123 Đường Lê Lợi",
      "district": "Quận 1",
      "provinceOrCity": "TP. Hồ Chí Minh"
    },
    "owner": {
      "id": 1,
      "fullname": "Nguyễn Văn User"
    },
    "createdAt": "2025-10-21T04:00:00.000Z"
  },
  "message": "Tạo venue thành công"
}

📌 ACTION: Lưu `id` (venueId) vào Environment variable `venueId`
```

**Script tự động lưu venueId:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("venueId", jsonData.data.id);
}
```

#### 3.2. Tạo Court (Sân con) trong Venue
```
POST {{baseUrl}}/api/courts
Authorization: Bearer {{ownerToken}}
Content-Type: application/json

Body (raw JSON):
{
  "venueId": {{venueId}},
  "name": "Sân số 1",
  "type": "FOOTBALL_5",
  "description": "Sân 5 người có mái che, cỏ nhân tạo cao cấp"
}

✅ Expected Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân số 1",
    "type": "FOOTBALL_5",
    "description": "Sân 5 người có mái che, cỏ nhân tạo cao cấp",
    "venueId": 1,
    "isBooked": false,
    "createdAt": "2025-10-21T04:05:00.000Z"
  },
  "message": "Tạo court thành công"
}

📌 ACTION: Lưu `id` (courtId) vào Environment variable `courtId`
```

**Script tự động lưu courtId:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("courtId", jsonData.data.id);
}
```

#### 3.3. Tạo PriceRule cho Court (Quy định giá theo khung giờ)
```
POST {{baseUrl}}/api/price-rules
Authorization: Bearer {{ownerToken}}
Content-Type: application/json

Body (raw JSON):
{
  "venueId": {{venueId}},
  "courtId": {{courtId}},
  "dayOfWeek": "ALL",
  "startTime": "06:00:00",
  "endTime": "17:59:59",
  "pricePerHour": 200000,
  "name": "Giờ bình thường (sáng - chiều)"
}

✅ Expected Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Giờ bình thường (sáng - chiều)",
    "dayOfWeek": "ALL",
    "startTime": "06:00:00",
    "endTime": "17:59:59",
    "pricePerHour": 200000.0,
    "courtId": 1,
    "venueId": 1
  },
  "message": "Tạo price rule thành công"
}

📌 NOTE: Tạo thêm price rule cho giờ vàng (18:00-22:00) với giá cao hơn
```

#### 3.4. Tạo thêm PriceRule cho giờ vàng
```
POST {{baseUrl}}/api/price-rules
Authorization: Bearer {{ownerToken}}
Content-Type: application/json

Body (raw JSON):
{
  "venueId": {{venueId}},
  "courtId": {{courtId}},
  "dayOfWeek": "ALL",
  "startTime": "18:00:00",
  "endTime": "22:00:00",
  "pricePerHour": 300000,
  "name": "Giờ vàng (tối)"
}

✅ Expected Response (201):
{
  "success": true,
  "data": {
    "id": 2,
    "name": "Giờ vàng (tối)",
    "dayOfWeek": "ALL",
    "startTime": "18:00:00",
    "endTime": "22:00:00",
    "pricePerHour": 300000.0,
    "courtId": 1,
    "venueId": 1
  },
  "message": "Tạo price rule thành công"
}
```

---

### ⚽ BƯỚC 4: NGƯỜI DÙNG XEM VÀ ĐẶT SÂN

#### 4.1. Xem danh sách Venues (Không cần đăng nhập)
```
GET {{baseUrl}}/api/venues

✅ Expected Response (200):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Sân bóng ABC",
      "description": "Sân bóng đá mini chất lượng cao, đầy đủ tiện nghi",
      "address": {
        "detailAddress": "123 Đường Lê Lợi",
        "district": "Quận 1",
        "provinceOrCity": "TP. Hồ Chí Minh"
      },
      "phoneNumber": "0901234567"
    }
  ],
  "message": "Success"
}
```

#### 4.2. Xem chi tiết Venue và danh sách Court
```
GET {{baseUrl}}/api/venues/{{venueId}}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân bóng ABC",
    "description": "Sân bóng đá mini chất lượng cao, đầy đủ tiện nghi",
    "courts": [
      {
        "id": 1,
        "name": "Sân số 1",
        "type": "FOOTBALL_5",
        "isBooked": false
      }
    ],
    "priceRules": [
      {
        "id": 1,
        "name": "Giờ bình thường (sáng - chiều)",
        "pricePerHour": 200000.0,
        "startTime": "06:00:00",
        "endTime": "17:59:59"
      },
      {
        "id": 2,
        "name": "Giờ vàng (tối)",
        "pricePerHour": 300000.0,
        "startTime": "18:00:00",
        "endTime": "22:00:00"
      }
    ]
  },
  "message": "Success"
}
```

#### 4.3. Đăng ký tài khoản USER thứ 2 (Người đặt sân)
```
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

Body (raw JSON):
{
  "fullname": "Nguyễn Văn Khách",
  "email": "khach@test.com",
  "phone": "0909999999",
  "password": "123456",
  "confirmPassword": "123456"
}

✅ Expected Response (200):
{
  "success": true,
  "data": "User registered successfully",
  "message": "Registered"
}
```

#### 4.4. Đăng nhập USER thứ 2
```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

Body (raw JSON):
{
  "phone": "0909999999",
  "password": "123456"
}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "id": 2,
    "phone": "0909999999",
    "roles": ["ROLE_USER"]
  },
  "message": "Login success"
}

📌 ACTION: Copy token và lưu vào Environment variable `userBToken`
```

#### 4.5. USER tạo booking
```
POST {{baseUrl}}/api/bookings
Authorization: Bearer {{userBToken}}
Content-Type: application/json

Body (raw JSON):
{
  "courtId": {{courtId}},
  "startTime": "2025-10-22T19:00:00",
  "endTime": "2025-10-22T21:00:00"
}

✅ Expected Response (201):
{
  "success": true,
  "data": {
    "id": 1,
    "courtId": 1,
    "courtName": "Sân số 1",
    "venueName": "Sân bóng ABC",
    "userId": 2,
    "userName": "Nguyễn Văn Khách",
    "startTime": "2025-10-22T19:00:00",
    "endTime": "2025-10-22T21:00:00",
    "totalPrice": 600000.0,
    "status": "PENDING_PAYMENT",
    "expireTime": "2025-10-22T19:15:00",
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN A",
      "ownerName": "Nguyễn Văn User"
    },
    "paymentProofUploaded": false
  },
  "message": "Đặt sân thành công. Vui lòng chuyển khoản trong 15 phút."
}

📌 ACTION: 
- Lưu `id` (bookingId) vào Environment variable `bookingId`
- User thấy thông tin TK ngân hàng của chủ sân
- User có 15 phút để chuyển khoản (trước expireTime)
- Sân đã bị KHÓA (isBooked = true)
```

**Script tự động lưu bookingId:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("bookingId", jsonData.data.id);
}
```

---

### 💰 BƯỚC 5: NGƯỜI DÙNG CHUYỂN KHOẢN VÀ XÁC NHẬN

#### 5.1. USER xem lại thông tin booking
```
GET {{baseUrl}}/api/bookings/{{bookingId}}
Authorization: Bearer {{userBToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "status": "PENDING_PAYMENT",
    "totalPrice": 600000.0,
    "expireTime": "2025-10-22T19:15:00",
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN A",
      "ownerName": "Nguyễn Văn User"
    }
  },
  "message": "Success"
}
```

#### 5.2. USER chuyển khoản (ngoài hệ thống)
💡 **Thực hiện chuyển khoản thật:**
- Mở app ngân hàng
- Chuyển khoản đến: **1234567890 - NGUYEN VAN A - Vietcombank**
- Số tiền: **600.000 VNĐ**
- Nội dung: **Dat san #1 - Nguyen Van Khach**
- Chụp màn hình/screenshot giao dịch thành công

#### 5.3. USER upload ảnh chứng minh chuyển khoản

⚠️ **LƯU Ý:** Bạn cần upload ảnh lên một dịch vụ (Imgur, Cloudinary, Firebase...) để có URL. 
Trong ví dụ này tôi dùng URL giả:

```
PUT {{baseUrl}}/api/bookings/{{bookingId}}/confirm-payment
Authorization: Bearer {{userBToken}}
Content-Type: application/json

Body (raw JSON):
{
  "paymentProofUrl": "https://i.imgur.com/abc123def.jpg"
}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "status": "PAYMENT_UPLOADED",
    "paymentProofUrl": "https://i.imgur.com/abc123def.jpg",
    "paymentProofUploaded": true,
    "paymentProofUploadedAt": "2025-10-22T19:10:00.000Z",
    "totalPrice": 600000.0
  },
  "message": "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận."
}

📌 NOTE: 
- Status đổi từ PENDING_PAYMENT → PAYMENT_UPLOADED
- Hệ thống TỰ ĐỘNG gửi thông báo cho OWNER
- Sân vẫn bị KHÓA
```

---

### 🔔 BƯỚC 6: CHỦ SÂN NHẬN THÔNG BÁO VÀ XỬ LÝ

#### 6.1. OWNER xem thông báo
```
GET {{baseUrl}}/api/notifications
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "bookingId": 1,
      "type": "PAYMENT_UPLOADED",
      "title": "Có khách đã chuyển khoản",
      "message": "Khách hàng Nguyễn Văn Khách đã chuyển khoản cho booking #1 (Sân số 1, 22/10/2025 19:00-21:00). Vui lòng kiểm tra tài khoản ngân hàng và xác nhận.",
      "isRead": false,
      "createdAt": "2025-10-22T19:10:00.000Z",
      "senderName": "Nguyễn Văn Khách"
    }
  ],
  "message": "Success"
}

📌 ACTION: Owner thấy có thông báo mới (isRead = false)
```

#### 6.2. OWNER kiểm tra số lượng thông báo chưa đ���c
```
GET {{baseUrl}}/api/notifications/unread-count
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": 1,
  "message": "Success"
}
```

#### 6.3. OWNER xem chi tiết booking
```
GET {{baseUrl}}/api/bookings/{{bookingId}}
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "courtName": "Sân số 1",
    "userName": "Nguyễn Văn Khách",
    "userPhone": "0909999999",
    "startTime": "2025-10-22T19:00:00",
    "endTime": "2025-10-22T21:00:00",
    "totalPrice": 600000.0,
    "status": "PAYMENT_UPLOADED",
    "paymentProofUrl": "https://i.imgur.com/abc123def.jpg",
    "paymentProofUploaded": true,
    "paymentProofUploadedAt": "2025-10-22T19:10:00.000Z"
  },
  "message": "Success"
}

📌 ACTION: Owner xem ảnh chứng minh chuyển khoản tại URL: https://i.imgur.com/abc123def.jpg
```

#### 6.4. OWNER xem danh sách booking chờ xác nhận
```
GET {{baseUrl}}/api/bookings/pending
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courtName": "Sân số 1",
      "userName": "Nguyễn Văn Khách",
      "userPhone": "0909999999",
      "totalPrice": 600000.0,
      "status": "PAYMENT_UPLOADED",
      "paymentProofUrl": "https://i.imgur.com/abc123def.jpg"
    }
  ],
  "message": "Success"
}
```

#### 6.5. OWNER kiểm tra tài khoản ngân hàng (ngoài hệ thống)
💡 **Thực hiện kiểm tra:**
- Mở app ngân hàng Vietcombank
- Kiểm tra tài khoản **1234567890**
- Xem có giao dịch **+600.000 VNĐ** từ khách hàng không
- So sánh với ảnh chứng minh

---

### ✅ BƯỚC 7: CHỦ SÂN CHẤP NHẬN ĐẶT SÂN

#### 7.1. OWNER accept booking (nếu tiền đã về)
```
PUT {{baseUrl}}/api/bookings/{{bookingId}}/accept
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "status": "CONFIRMED",
    "courtName": "Sân số 1",
    "userName": "Nguyễn Văn Khách",
    "startTime": "2025-10-22T19:00:00",
    "endTime": "2025-10-22T21:00:00",
    "totalPrice": 600000.0
  },
  "message": "Đã xác nhận đặt sân thành công."
}

📌 NOTE: 
- Status đổi từ PAYMENT_UPLOADED → CONFIRMED
- Hệ thống TỰ ĐỘNG gửi thông báo cho USER
- Sân vẫn bị KHÓA (giữ cho khách này)
```

#### 7.2. OWNER đánh dấu đã đọc thông báo
```
PUT {{baseUrl}}/api/notifications/1/read
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "message": "Đã đánh dấu đã đọc"
}
```

---

### 🎉 BƯỚC 8: NGƯỜI DÙNG NHẬN THÔNG BÁO THÀNH CÔNG

#### 8.1. USER xem thông báo
```
GET {{baseUrl}}/api/notifications
Authorization: Bearer {{userBToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": [
    {
      "id": 2,
      "bookingId": 1,
      "type": "BOOKING_CONFIRMED",
      "title": "Đặt sân thành công",
      "message": "Booking #1 của bạn đã được chủ sân xác nhận. Sân số 1 - Sân bóng ABC. Thời gian: 22/10/2025 19:00-21:00. Hẹn gặp bạn!",
      "isRead": false,
      "createdAt": "2025-10-22T19:12:00.000Z",
      "senderName": "Nguyễn Văn User"
    }
  ],
  "message": "Success"
}

📌 NOTE: User nhận được thông báo booking đã được xác nhận
```

#### 8.2. USER xem lại booking của mình
```
GET {{baseUrl}}/api/bookings/my-bookings
Authorization: Bearer {{userBToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courtName": "Sân số 1",
      "venueName": "Sân bóng ABC",
      "startTime": "2025-10-22T19:00:00",
      "endTime": "2025-10-22T21:00:00",
      "totalPrice": 600000.0,
      "status": "CONFIRMED",
      "venueAddress": {
        "detailAddress": "123 Đường Lê Lợi",
        "district": "Quận 1",
        "provinceOrCity": "TP. Hồ Chí Minh"
      },
      "venuePhone": "0901234567"
    }
  ],
  "message": "Success"
}

✅ ĐẶT SÂN THÀNH CÔNG! User có thể đến chơi sân vào đúng thời gian đã đặt.
```

---

### ❌ BƯỚC 7B: CHỦ SÂN TỪ CHỐI ĐẶT SÂN (TRƯỜNG HỢP THAY THẾ)

Nếu owner kiểm tra TK ngân hàng và **KHÔNG thấy tiền** hoặc **sai số tiền**:

#### 7B.1. OWNER reject booking
```
PUT {{baseUrl}}/api/bookings/{{bookingId}}/reject
Authorization: Bearer {{ownerToken}}
Content-Type: application/json

Body (raw JSON):
{
  "rejectionReason": "Số tiền chuyển khoản không đúng. Vui lòng chuyển đúng 600.000 VNĐ và đặt lại."
}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "status": "REJECTED",
    "rejectionReason": "Số tiền chuyển khoản không đúng. Vui lòng chuyển đúng 600.000 VNĐ và đặt lại.",
    "courtName": "Sân số 1"
  },
  "message": "Đã từ chối đặt sân."
}

📌 NOTE: 
- Status đổi từ PAYMENT_UPLOADED → REJECTED
- Hệ thống TỰ ĐỘNG gửi thông báo cho USER kèm lý do
- Sân được GIẢI PHÓNG (isBooked = false) - người khác có thể đặt
```

#### 7B.2. USER nhận thông báo bị từ chối
```
GET {{baseUrl}}/api/notifications
Authorization: Bearer {{userBToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": [
    {
      "id": 2,
      "bookingId": 1,
      "type": "BOOKING_REJECTED",
      "title": "Đặt sân bị từ chối",
      "message": "Booking #1 của bạn đã bị từ chối. Lý do: Số tiền chuyển khoản không đúng. Vui lòng chuyển đúng 600.000 VNĐ và đặt lại.",
      "isRead": false,
      "createdAt": "2025-10-22T19:12:00.000Z",
      "senderName": "Nguyễn Văn User"
    }
  ],
  "message": "Success"
}

📌 NOTE: User thấy lý do từ chối và có thể đặt lại sân
```

---

### 📊 BƯỚC 9: KIỂM TRA TRẠNG THÁI SÂN

#### 9.1. Kiểm tra sân sau khi CONFIRMED
```
GET {{baseUrl}}/api/courts/{{courtId}}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân số 1",
    "type": "FOOTBALL_5",
    "isBooked": true,  ← SÂN VẪN BỊ KHÓA
    "venueId": 1
  },
  "message": "Success"
}
```

#### 9.2. Kiểm tra sân sau khi REJECTED
```
GET {{baseUrl}}/api/courts/{{courtId}}

✅ Expected Response (200):
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân số 1",
    "type": "FOOTBALL_5",
    "isBooked": false,  ← SÂN ĐÃ ĐƯỢC GIẢI PHÓNG
    "venueId": 1
  },
  "message": "Success"
}

📌 NOTE: Người khác có thể đặt sân này
```

---

### 🔄 BƯỚC 10: OWNER XEM TẤT CẢ BOOKING CỦA VENUE

```
GET {{baseUrl}}/api/bookings/venue/{{venueId}}
Authorization: Bearer {{ownerToken}}

✅ Expected Response (200):
{
  "success": true,
  "data": [
    {
      "id": 1,
      "courtName": "Sân số 1",
      "userName": "Nguyễn Văn Khách",
      "userPhone": "0909999999",
      "startTime": "2025-10-22T19:00:00",
      "endTime": "2025-10-22T21:00:00",
      "totalPrice": 600000.0,
      "status": "CONFIRMED",
      "createdAt": "2025-10-22T19:05:00.000Z"
    }
  ],
  "message": "Success"
}

📌 NOTE: Owner có thể xem tất cả booking của tất cả sân trong venue
```

---

## 🎯 TÓM TẮT LUỒNG HOÀN CHỈNH

### ✅ Luồng THÀNH CÔNG:
1. **OWNER** đăng ký → Nâng cấp lên OWNER → Cập nhật bank info
2. **OWNER** tạo Venue → Tạo Court → Tạo PriceRule
3. **USER** xem danh sách venue → Chọn sân
4. **USER** đăng ký/đăng nh���p → Tạo booking
   - Status: **PENDING_PAYMENT**
   - Sân: **BỊ KHÓA**
   - Nhận thông tin TK chủ sân
5. **USER** chuyển khoản (thật) → Upload ảnh chứng minh → Confirm payment
   - Status: **PAYMENT_UPLOADED**
   - **OWNER nhận thông báo tự động**
6. **OWNER** kiểm tra TK ngân hàng → Thấy tiền → **ACCEPT**
   - Status: **CONFIRMED**
   - **USER nhận thông báo tự động**
   - Sân: **VẪN BỊ KHÓA** (giữ cho khách)
7. **USER** đến sân chơi đúng giờ ✅

### ❌ Luồng TỪ CHỐI:
1-5. Giống luồng thành công
6. **OWNER** kiểm tra TK → Không thấy tiền/sai số tiền → **REJECT** với l�� do
   - Status: **REJECTED**
   - **USER nhận thông báo + lý do từ chối**
   - Sân: **GIẢI PHÓNG** (người khác có thể đặt)
7. **USER** có thể đặt lại sân hoặc chọn sân khác

### ⏰ Luồng HẾT HẠN:
1-4. Giống luồng thành công
5. **USER** không chuyển khoản trong 15 phút
6. **Hệ thống** tự động cancel booking sau 15 phút
   - Status: **EXPIRED**
   - **USER nhận thông báo**
   - Sân: **GIẢI PHÓNG**

---
