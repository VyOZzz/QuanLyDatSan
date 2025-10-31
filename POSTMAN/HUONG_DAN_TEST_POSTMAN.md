# HƯỚNG DẪN TEST API VỚI POSTMAN

## 📋 Mục lục
1. [Cài đặt và Import Collection](#1-cài-đặt-và-import-collection)
2. [Cấu hình Environment](#2-cấu-hình-environment)
3. [Test Flow - Kịch bản đầy đủ](#3-test-flow---kịch-bản-đầy-đủ)
4. [Các API theo từng module](#4-các-api-theo-từng-module)
5. [Tính năng tự động trong Collection](#5-tính-năng-tự-động-trong-collection)
6. [Troubleshooting](#6-troubleshooting)

---

## 1. Cài đặt và Import Collection

### Bước 1: Cài đặt Postman
- Tải Postman: https://www.postman.com/downloads/
- Hoặc sử dụng Postman Web: https://web.postman.com/

### Bước 2: Import Collection
1. Mở Postman
2. Click **Import** (góc trái trên)
3. Kéo thả file `BookingCourt_Postman_Collection.json` vào
4. Hoặc click **Upload Files** và chọn file

### Bước 3: Kiểm tra Import thành công
- Bạn sẽ thấy collection **"Booking Court API - Complete Collection"**
- Collection có 9 folder chính:
  - 1. Authentication
  - 2. User Management
  - 3. Venues Management
  - 4. Courts Management
  - 5. Price Rules Management
  - 6. Bookings Management
  - 7. Reviews Management
  - 8. Notifications Management
  - 9. File Management

---

## 2. Cấu hình Environment

### Collection Variables (Tự động)
Collection đã được cấu hình sẵn các biến:

| Biến | Giá trị mặc định | Mô tả |
|------|------------------|-------|
| `baseUrl` | `http://localhost:8080` | URL backend server |
| `token` | (auto) | JWT token - tự động lưu sau login |
| `userId` | (auto) | ID user - tự động lưu sau login |
| `venueId` | (auto) | ID venue - tự động lưu sau tạo venue |
| `courtId` | (auto) | ID court - tự động lưu sau tạo court |
| `bookingId` | (auto) | ID booking - tự động lưu sau tạo booking |
| `priceRuleId` | (auto) | ID price rule - tự động lưu |
| `userPhone` | `0123456789` | SĐT user test |
| `userPassword` | `password123` | Mật khẩu user test |
| `ownerPhone` | `0987654321` | SĐT owner test |
| `ownerPassword` | `owner123` | Mật khẩu owner test |

### Thay đổi cấu hình (nếu cần)
1. Click vào collection **"Booking Court API"**
2. Chọn tab **Variables**
3. Sửa giá trị trong cột **Current Value**
4. Click **Save**

**Lưu ý:** Nếu backend chạy ở port khác hoặc remote server, hãy đổi `baseUrl`

---

## 3. Test Flow - Kịch bản đầy đủ

### 🎯 Kịch bản 1: Test flow User đặt sân

#### Bước 1: Đăng ký tài khoản USER
```
Request: 1.1 Register User
```
- Tự động lưu số điện thoại vào biến `userPhone`

#### Bước 2: Đăng nhập USER
```
Request: 1.3 Login User
```
- Tự động lưu JWT token vào biến `token`
- Tất cả request sau này sẽ dùng token này

#### Bước 3: Xem thông tin cá nhân
```
Request: 2.1 Get Current User Info
```
- Kiểm tra thông tin user vừa đăng nhập

#### Bước 4: Tìm sân để đặt
```
Request: 3.1 Get All Venues
```
- Xem danh sách tất cả sân
- Tự động lưu ID sân đầu tiên vào `venueId`

Hoặc:
```
Request: 3.2 Search Venues
```
- Tìm theo địa chỉ: `?province=Hanoi`

#### Bước 5: Xem chi tiết sân
```
Request: 3.3 Get Venue By ID
```
- Xem thông tin chi tiết: giờ mở cửa, địa chỉ, giá...

#### Bước 6: Xem các sân con
```
Request: 4.1 Get All Courts
```
- Tự động lưu ID sân con đầu tiên vào `courtId`

#### Bước 7: Kiểm tra lịch trống
```
Request: 4.3 Check Court Availability
```
- Kiểm tra sân có trống trong khoảng thời gian không

#### Bước 8: Đặt sân
```
Request: 6.1 Create Booking
```
- Tự động tạo booking cho ngày mai 10:00-12:00
- Tự động lưu `bookingId`
- Status: PENDING

#### Bước 9: Upload ảnh chuyển khoản
```
Request: 6.4 Confirm Payment (Upload Proof)
```
- Gửi link ảnh chứng minh đã chuyển khoản
- Status: PAYMENT_CONFIRMED

#### Bước 10: Chờ Owner xác nhận
- Owner sẽ accept hoặc reject (xem kịch bản 2)

#### Bước 11: Sau khi hoàn thành - Đánh giá
```
Request: 7.1 Create Review
```
- Chỉ được đánh giá khi booking COMPLETED
- Rating: 1-5 sao

---

### 🏢 Kịch bản 2: Test flow Owner quản lý sân

#### Bước 1: Đăng ký tài khoản
```
Request: 1.2 Register Owner
```

#### Bước 2: Đăng nhập
```
Request: 1.4 Login Owner
```

#### Bước 3: Nâng cấp lên OWNER
```
Request: 2.3 Request Owner Role
```
- Sau đó phải **login lại** để có token với role OWNER

#### Bước 4: Login lại
```
Request: 1.4 Login Owner
```
- Token mới có role OWNER

#### Bước 5: Cập nhật thông tin ngân hàng
```
Request: 2.2 Update User Info
```
- Thêm bankName, bankAccountNumber, bankAccountName

#### Bước 6: Tạo venue (sân)
```
Request: 3.4 Create Venue
```
- Tự động lưu `venueId`

#### Bước 7: Tạo court (sân con)
```
Request: 4.4 Create Court
```
- Tự động lưu `courtId`
- Có thể tạo nhiều sân: Sân 1, Sân 2, Sân 3...

#### Bước 8: Tạo quy tắc giá
```
Request: 5.2 Create Price Rule
```
- Giá theo giờ: 06:00-09:00 → 250k/h
- Tạo nhiều rule cho các khung giờ khác nhau

#### Bước 9: Xem booking chờ xác nhận
```
Request: 6.5 Get Pending Bookings (Owner)
```

#### Bước 10: Xác nhận hoặc từ chối booking
**Accept:**
```
Request: 6.7 Accept Booking (Owner)
```
- Status: CONFIRMED

**Reject:**
```
Request: 6.8 Reject Booking (Owner)
```
- Status: REJECTED
- Phải ghi lý do reject

#### Bước 11: Xem tất cả booking của venue
```
Request: 6.6 Get Venue Bookings (Owner)
```

---

### 🔔 Kịch bản 3: Test hệ thống thông báo

#### Bước 1: Xem thông báo
```
Request: 8.1 Get My Notifications
```

#### Bước 2: Xem số thông báo chưa đọc
```
Request: 8.2 Get Unread Count
```

#### Bước 3: Đánh dấu đã đọc
```
Request: 8.3 Mark As Read
```
Hoặc đánh dấu tất cả:
```
Request: 8.4 Mark All As Read
```

#### Bước 4: Xóa thông báo
```
Request: 8.5 Delete Notification
```

---

## 4. Các API theo từng module

### 📌 Module 1: Authentication

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Register User | POST | `/api/auth/register` | No | Đăng ký USER |
| Register Owner | POST | `/api/auth/register` | No | Đăng ký OWNER |
| Login User | POST | `/api/auth/login` | No | Đăng nhập USER |
| Login Owner | POST | `/api/auth/login` | No | Đăng nhập OWNER |
| Forgot Password | POST | `/api/auth/forgot-password` | No | Quên mật khẩu - Nhận mã OTP 8 chữ số |
| Reset Password | POST | `/api/auth/reset-password` | No | Đặt lại mật khẩu với mã OTP |

**Request body mẫu - Register:**
```json
{
    "name": "Nguyễn Văn A",
    "phone": "0123456789",
    "password": "password123",
    "email": "user@example.com",
    "address": "123 Đường ABC, Hà Nội"
}
```

**Request body mẫu - Login:**
```json
{
    "phone": "0123456789",
    "password": "password123"
}
```

**Request body mẫu - Forgot Password:**
```json
{
    "email": "user@example.com"
}
```
**Response:**
```json
{
    "success": true,
    "message": "Nếu email hợp lệ, mã đặt lại đã được gửi",
    "data": "Sent"
}
```
**Email nhận được:**
```
Subject: Mã Xác Nhận Đặt Lại Mật Khẩu

Mã xác nhận đặt lại mật khẩu của bạn là: 12345678

Mã này có hiệu lực trong 15 phút.

Vui lòng không chia sẻ mã này với bất kỳ ai.
```

**Request body mẫu - Reset Password:**
```json
{
    "token": "12345678",
    "newPassword": "newPassword123"
}
```
**Lưu ý:**
- Token là **mã OTP 8 chữ số** (00000000 - 99999999)
- Mã có hiệu lực **15 phút**
- Mỗi mã chỉ dùng được **1 lần**
- Sau khi reset thành công, cần login lại với mật khẩu mới

---

### 📌 Module 2: User Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Get Current User | GET | `/api/users/me` | Yes | Xem thông tin cá nhân |
| Update User | PUT | `/api/users/me` | Yes | Cập nhật thông tin |
| Request Owner Role | POST | `/api/users/me/request-owner-role` | USER | Nâng cấp lên OWNER |

**Request body mẫu - Update:**
```json
{
    "name": "Nguyễn Văn A Updated",
    "email": "newemail@example.com",
    "address": "Địa chỉ mới",
    "bankName": "Vietcombank",
    "bankAccountNumber": "1234567890",
    "bankAccountName": "NGUYEN VAN A"
}
```

---

### 📌 Module 3: Venues Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Get All Venues | GET | `/api/venues` | No | Danh sách tất cả venue |
| Search Venues | GET | `/api/venues/search?province=...` | No | Tìm kiếm venue |
| Get Venue By ID | GET | `/api/venues/{id}` | No | Chi tiết venue |
| Create Venue | POST | `/api/venues` | OWNER | Tạo venue mới |
| Update Venue | PUT | `/api/venues/{id}` | OWNER | Cập nhật venue |
| Delete Venue | DELETE | `/api/venues/{id}` | OWNER | Xóa venue |

**Request body mẫu - Create Venue:**
```json
{
    "name": "Sân Bóng ABC",
    "description": "Sân bóng đá chất lượng cao, có mái che",
    "phoneNumber": "0901234567",
    "openTime": "06:00",
    "closeTime": "22:00",
    "address": {
        "provinceOrCity": "Hà Nội",
        "district": "Cầu Giấy",
        "wardOrCommune": "Dịch Vọng",
        "detail": "123 Đường Cầu Giấy"
    }
}
```

---

### 📌 Module 4: Courts Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Get All Courts | GET | `/api/courts` | No | Danh sách tất cả court |
| Get Court By ID | GET | `/api/courts/{id}` | No | Chi tiết court |
| Check Availability | GET | `/api/courts/{id}/availability?startTime=...&endTime=...` | No | Kiểm tra lịch trống |
| Create Court | POST | `/api/courts` | OWNER | Tạo court mới |
| Update Court | PUT | `/api/courts/{id}` | OWNER | Cập nhật court |
| Delete Court | DELETE | `/api/courts/{id}` | OWNER | Xóa court |

**Court Types:**
- `FOOTBALL_5` - Bóng đá 5 người
- `FOOTBALL_7` - Bóng đá 7 người
- `FOOTBALL_11` - Bóng đá 11 người
- `BADMINTON` - Cầu lông
- `TENNIS` - Quần vợt
- `BASKETBALL` - Bóng rổ
- `VOLLEYBALL` - Bóng chuyền

**Request body mẫu - Create Court:**
```json
{
    "venueId": 1,
    "name": "Sân 1",
    "courtType": "FOOTBALL_5",
    "pricePerHour": 300000
}
```

---

### 📌 Module 5: Price Rules Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Get Price Rules | GET | `/api/pricerules/venue/{venueId}` | No | Quy tắc giá của venue |
| Create Price Rule | POST | `/api/pricerules` | OWNER | Tạo quy tắc giá |
| Update Price Rule | PUT | `/api/pricerules/{id}` | OWNER | Cập nhật quy tắc |
| Delete Price Rule | DELETE | `/api/pricerules/{id}` | OWNER | Xóa quy tắc |

**Request body mẫu - Create Price Rule:**
```json
{
    "venueId": 1,
    "name": "Giờ vàng buổi sáng",
    "startTime": "06:00",
    "endTime": "09:00",
    "pricePerHour": 250000
}
```

**Ví dụ cấu hình giá:**
- 06:00-09:00: 250,000đ/h (giờ sáng rẻ)
- 09:00-17:00: 300,000đ/h (giờ hành chính)
- 17:00-22:00: 400,000đ/h (giờ vàng)

---

### 📌 Module 6: Bookings Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Create Booking | POST | `/api/bookings` | USER | Tạo booking mới |
| Get Booking By ID | GET | `/api/bookings/{id}` | Auth | Chi tiết booking |
| Get My Bookings | GET | `/api/bookings/my-bookings` | USER | Booking của tôi |
| Confirm Payment | PUT | `/api/bookings/{id}/confirm-payment` | USER | Upload ảnh CK |
| Get Pending Bookings | GET | `/api/bookings/pending` | OWNER | Booking chờ confirm |
| Get Venue Bookings | GET | `/api/bookings/venue/{venueId}` | OWNER | Booking của venue |
| Accept Booking | PUT | `/api/bookings/{id}/accept` | OWNER | Chấp nhận booking |
| Reject Booking | PUT | `/api/bookings/{id}/reject` | OWNER | Từ chối booking |
| Cancel Booking | PUT | `/api/bookings/{id}/cancel` | USER | Hủy booking |

**Booking Status Flow:**
```
PENDING → PAYMENT_CONFIRMED → CONFIRMED → COMPLETED
                ↓                  ↓
            CANCELLED          REJECTED
```

**Request body mẫu - Create Booking:**
```json
{
    "venueId": 1,
    "bookingItems": [
        {
            "courtId": 1,
            "startTime": "2025-11-01T10:00:00",
            "endTime": "2025-11-01T12:00:00"
        },
        {
            "courtId": 2,
            "startTime": "2025-11-01T10:00:00",
            "endTime": "2025-11-01T12:00:00"
        }
    ],
    "note": "Đặt sân cho đội bóng công ty"
}
```

**Request body mẫu - Confirm Payment:**
```json
{
    "paymentProofUrl": "https://example.com/payment.jpg",
    "note": "Đã chuyển khoản lúc 10:30"
}
```

**Request body mẫu - Reject Booking:**
```json
{
    "reason": "Sân đã được đặt trước"
}
```

---

### 📌 Module 7: Reviews Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Create Review | POST | `/api/bookings/{bookingId}/review` | USER | Tạo đánh giá |
| Get Venue Reviews | GET | `/api/venues/{venueId}/reviews` | No | Xem đánh giá venue |
| Get My Reviews | GET | `/api/my-reviews` | USER | Đánh giá của tôi |

**Request body mẫu - Create Review:**
```json
{
    "rating": 5,
    "comment": "Sân rất đẹp, chất lượng tốt, phục vụ nhiệt tình!"
}
```

**Lưu ý:**
- Rating: 1-5 sao
- Chỉ review được khi booking COMPLETED
- Mỗi booking chỉ review 1 lần

---

### 📌 Module 8: Notifications Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Get My Notifications | GET | `/api/notifications` | Auth | Danh sách thông báo |
| Get Unread Count | GET | `/api/notifications/unread-count` | Auth | Số thông báo chưa đọc |
| Mark As Read | PUT | `/api/notifications/{id}/read` | Auth | Đánh dấu đã đọc |
| Mark All As Read | PUT | `/api/notifications/read-all` | Auth | Đánh dấu tất cả |
| Delete Notification | DELETE | `/api/notifications/{id}` | Auth | Xóa thông báo |

**Notification Types:**
- `BOOKING_CREATED` - Đặt sân thành công
- `BOOKING_CONFIRMED` - Owner xác nhận
- `BOOKING_REJECTED` - Owner từ chối
- `BOOKING_CANCELLED` - User hủy
- `PAYMENT_RECEIVED` - Nhận được thanh toán
- `REVIEW_RECEIVED` - Nhận được đánh giá

---

### 📌 Module 9: File Management

| API | Method | Endpoint | Auth | Mô tả |
|-----|--------|----------|------|-------|
| Get Payment Proof | GET | `/api/files/payment-proofs/{filename}` | No | Xem ảnh chuyển khoản |

**Ví dụ:** `GET /api/files/payment-proofs/payment_123456.jpg`

---

## 5. Tính năng tự động trong Collection

### ✅ Auto-save Token
Sau khi login, token tự động được lưu vào biến `{{token}}` và được sử dụng cho tất cả request tiếp theo.

**Script trong request Login:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    if (jsonData.success && jsonData.data.token) {
        pm.collectionVariables.set('token', jsonData.data.token);
        console.log('✅ Token saved');
    }
}
```

### ✅ Auto-save IDs
Các ID quan trọng tự động được lưu:
- `venueId` - sau khi tạo hoặc lấy danh sách venue
- `courtId` - sau khi tạo hoặc lấy danh sách court
- `bookingId` - sau khi tạo booking
- `priceRuleId` - sau khi tạo price rule
- `userId` - sau khi login

### ✅ Auto-generate Time
Request "Create Booking" tự động tạo thời gian booking cho ngày mai:
```javascript
var now = new Date();
var tomorrow = new Date(now.getTime() + 24 * 60 * 60 * 1000);
var startTime = new Date(tomorrow);
startTime.setHours(10, 0, 0, 0);
```

### ✅ Validation Tests
Mỗi request quan trọng đều có test tự động:
```javascript
pm.test('Login successful', function() {
    pm.expect(jsonData.success).to.be.true;
    pm.expect(jsonData.data.token).to.exist;
});
```

### ✅ Console Logging
Tất cả request đều log ra console để dễ debug:
```javascript
console.log('✅ User logged in successfully');
console.log('Token saved:', jsonData.data.token.substring(0, 20) + '...');
```

---

## 6. Troubleshooting

### ❌ Lỗi: Connection refused
**Nguyên nhân:** Backend chưa chạy

**Giải pháp:**
```bash
cd QuanLyDatSan
./mvnw spring-boot:run
```

### ❌ Lỗi: 401 Unauthorized
**Nguyên nhân:** Token hết hạn hoặc chưa login

**Giải pháp:**
1. Chạy lại request Login
2. Token sẽ tự động được refresh

### ❌ Lỗi: 403 Forbidden
**Nguyên nhân:** Không có quyền (ví dụ: USER gọi API của OWNER)

**Giải pháp:**
1. Kiểm tra role hiện tại: `GET /api/users/me`
2. Login với tài khoản đúng role
3. Nếu cần OWNER role: `POST /api/users/me/request-owner-role` → Login lại

### ❌ Lỗi: 404 Not Found
**Nguyên nhân:** ID không tồn tại

**Giải pháp:**
1. Kiểm tra biến: `{{venueId}}`, `{{courtId}}`, etc.
2. Chạy lại request để tạo mới entity
3. Kiểm tra baseUrl có đúng không

### ❌ Lỗi: 400 Bad Request - Validation Error
**Nguyên nhân:** Dữ liệu request không hợp lệ

**Giải pháp:**
1. Kiểm tra format thời gian: `2025-11-01T10:00:00`
2. Kiểm tra required fields
3. Xem response body để biết field nào bị lỗi

### ❌ Booking bị reject với lý do "Sân đã được đặt"
**Nguyên nhân:** Sân đã có booking trong khung giờ đó

**Giải pháp:**
1. Kiểm tra lịch trống: `GET /api/courts/{id}/availability`
2. Chọn khung giờ khác
3. Hoặc đặt sân khác

### ❌ Không tạo được review
**Nguyên nhân:** Booking chưa COMPLETED

**Giải pháp:**
1. Chỉ review được khi booking đã hoàn thành
2. Test bằng cách manually update booking status trong DB

---

## 📚 Tips & Best Practices

### 1. Sử dụng Console
- Mở **Postman Console** (Ctrl+Alt+C / Cmd+Alt+C)
- Xem log chi tiết của mỗi request
- Debug script errors

### 2. Organize Collections
- Sắp xếp request theo thứ tự workflow
- Đặt tên rõ ràng, có số thứ tự
- Group theo module

### 3. Test theo thứ tự
Với lần test đầu tiên, chạy theo thứ tự:
1. Register → Login
2. Tạo Venue → Tạo Court → Tạo Price Rule
3. Booking → Payment → Accept → Complete → Review

### 4. Save requests
- Sau khi sửa request, nhớ Save (Ctrl+S)
- Sync với Postman Cloud để backup

### 5. Export Collection
- File → Export → Collection v2.1
- Chia sẻ cho team member

### 6. Environment cho nhiều server
Tạo environment riêng cho:
- Local: `http://localhost:8080`
- Dev: `https://dev-api.example.com`
- Production: `https://api.example.com`

---

## 🎯 Checklist Test đầy đủ

### Authentication ✓
- [ ] Đăng ký USER thành công
- [ ] Đăng ký OWNER thành công
- [ ] Login USER thành công
- [ ] Login OWNER thành công
- [ ] Token được lưu tự động
- [ ] Forgot password gửi email với mã OTP 8 chữ số
- [ ] Reset password với mã OTP thành công
- [ ] Mã OTP hết hạn sau 15 phút
- [ ] Mã OTP chỉ dùng được 1 lần

### User Management ✓
- [ ] Xem thông tin user
- [ ] Cập nhật thông tin user
- [ ] Cập nhật thông tin ngân hàng
- [ ] Nâng cấp USER → OWNER
- [ ] Login lại sau nâng cấp role

### Venues ✓
- [ ] Xem danh sách venues
- [ ] Tìm kiếm venues
- [ ] Xem chi tiết venue
- [ ] OWNER tạo venue
- [ ] OWNER cập nhật venue
- [ ] OWNER xóa venue

### Courts ✓
- [ ] Xem danh sách courts
- [ ] Xem chi tiết court
- [ ] Kiểm tra lịch trống
- [ ] OWNER tạo court
- [ ] OWNER cập nhật court
- [ ] OWNER xóa court

### Price Rules ✓
- [ ] Xem quy tắc giá của venue
- [ ] OWNER tạo quy tắc giá
- [ ] OWNER cập nhật quy tắc giá
- [ ] OWNER xóa quy tắc giá
- [ ] Tính giá đúng theo khung giờ

### Bookings ✓
- [ ] USER tạo booking
- [ ] Tính tổng tiền đúng
- [ ] USER upload ảnh chuyển khoản
- [ ] OWNER xem pending bookings
- [ ] OWNER accept booking
- [ ] OWNER reject booking
- [ ] USER xem my bookings
- [ ] USER cancel booking

### Reviews ✓
- [ ] USER tạo review sau khi completed
- [ ] Xem reviews của venue
- [ ] USER xem my reviews
- [ ] Tính trung bình rating đúng

### Notifications ✓
- [ ] Nhận thông báo khi booking created
- [ ] Nhận thông báo khi booking confirmed
- [ ] Nhận thông báo khi booking rejected
- [ ] Xem số thông báo chưa đọc
- [ ] Đánh dấu đã đọc
- [ ] Xóa thông báo

---

## 📞 Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra Console log trong Postman
2. Kiểm tra log của Backend
3. Xem Swagger UI: http://localhost:8080/swagger-ui.html
4. Xem API docs: http://localhost:8080/api-docs

---

## 🎉 Kết luận

Collection Postman này bao gồm:
- ✅ **83+ API requests** đầy đủ
- ✅ **Auto-save tokens & IDs**
- ✅ **Pre-request scripts** tự động tạo data
- ✅ **Test scripts** validate response
- ✅ **Console logging** dễ debug
- ✅ **Variables** linh hoạt
- ✅ **Organized folders** dễ tìm

**Bạn chỉ cần:**
1. Import file JSON
2. Start backend
3. Chạy các request theo thứ tự
4. Tất cả sẽ hoạt động tự động! 🚀

Good luck testing! 💪

