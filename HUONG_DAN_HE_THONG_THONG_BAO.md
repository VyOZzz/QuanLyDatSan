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

### BƯỚC 2: Hệ thống tự động cancel booking hết hạn
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

## 📞 HỖ TRỢ & TROUBLESHOOTING

### Lỗi thường gặp:

#### 1. Lỗi compile: "incompatible types: Instant cannot be converted to LocalDateTime"
**Nguyên nhân:** Sử dụng sai kiểu dữ liệu cho `createdAt`

**Giải pháp:** `NotificationDTO` phải dùng `Instant`, không phải `LocalDateTime`

#### 2. Lỗi khi run app: "Column 'owner_id' cannot be null"
**Nguyên nhân:** Database có dữ liệu venues cũ không có owner_id

**Giải pháp:**
```sql
DROP DATABASE quanlydatsan;
CREATE DATABASE quanlydatsan;
```
Sau đó run app lại.

#### 3. Booking không tự động cancel sau 15 phút
**Kiểm tra:**
- ✅ `@EnableScheduling` đã được thêm vào `QuanLyDatSanApplication.java`?
- ✅ `BookingExpirationService` có annotation `@Service` và `@Scheduled`?
- ✅ Xem log có message "Found X expired bookings to cancel"?

#### 4. Thông báo không được tạo
**Kiểm tra:**
- ✅ Bảng `notification` đã được tạo?
- ✅ `NotificationService` đã được inject vào `BookingServiceImpl`?
- ✅ Xem log có lỗi gì không?

#### 5. API trả về 403 Forbidden
**Nguyên nhân:** User không có quyền (role) phù hợp

**Giải pháp:** Kiểm tra:
- User có role USER/OWNER đúng không?
- Token JWT có hợp lệ không?

---

## 🎉 HOÀN THÀNH!

Hệ thống đã sẵn sàng sử dụng với đầy đủ chức năng:
- ✅ Đặt sân với thời hạn thanh toán 15 phút
- ✅ **Khóa sân trong thời gian pending** - Không ai đặt được cùng lúc
- ✅ Upload chứng minh chuyển khoản
- ✅ Hệ thống thông báo real-time
- ✅ Accept/Reject từ chủ sân
- ✅ **Tự động cancel booking hết hạn** (chạy mỗi 1 phút)
- ✅ **Tự động giải phóng sân** khi expired/cancelled/rejected
- ✅ Phân quyền rõ ràng giữa USER và OWNER
- ✅ Không cần migration SQL thủ công - Hibernate tự động tạo tất cả

**Chúc bạn phát triển app thành công!** 🚀
