# 🚀 QUICK START - TEST POSTMAN

## 📦 Cài đặt nhanh

### Bước 1: Import Collection vào Postman
```
1. Mở Postman
2. Click Import
3. Kéo thả file: BookingCourt_Postman_Collection.json
4. Done! ✅
```

### Bước 2: Chạy Backend
```bash
cd QuanLyDatSan
./mvnw spring-boot:run
```

### Bước 3: Test thủ công (GUI)
Mở Postman và chạy theo thứ tự:

#### 3.1. Đăng ký & Đăng nhập USER
```
1. Chạy: 1.1 Register User
2. Chạy: 1.3 Login User
   → Token tự động lưu vào biến {{token}}
```

#### 3.2. Đăng ký & Đăng nhập OWNER
```
1. Chạy: 1.2 Register Owner
2. Chạy: 1.4 Login Owner
   → Token tự động lưu
3. Chạy: 2.3 Request Owner Role
4. Chạy lại: 1.4 Login Owner (để có token mới với OWNER role)
```

#### 3.3. OWNER tạo sân
```
1. Chạy: 3.4 Create Venue
   → venueId tự động lưu
2. Chạy: 4.4 Create Court
   → courtId tự động lưu
3. Chạy: 5.2 Create Price Rule (tùy chọn)
```

#### 3.4. USER đặt sân
```
1. Login lại bằng: 1.3 Login User
2. Chạy: 3.1 Get All Venues (xem danh sách sân)
3. Chạy: 6.1 Create Booking
   → bookingId tự động lưu
4. Chạy: 6.4 Confirm Payment (upload ảnh CK)
```

#### 3.5. OWNER xác nhận booking
```
1. Login lại bằng: 1.4 Login Owner
2. Chạy: 6.5 Get Pending Bookings
3. Chạy: 6.7 Accept Booking
   hoặc: 6.8 Reject Booking
```

#### 3.6. Các tính năng khác
```
- Thông báo: Folder 8. Notifications Management
- Đánh giá: Folder 7. Reviews Management
- Quản lý thông tin: Folder 2. User Management
```

---

## 🤖 Test tự động (CLI với Newman)

### Cài đặt Newman (một lần)
```bash
npm install -g newman
npm install -g newman-reporter-htmlextra
```

### Chạy test tự động
```bash
cd QuanLyDatSan
chmod +x run-postman-tests.sh
./run-postman-tests.sh
```

Hoặc chạy trực tiếp:
```bash
newman run BookingCourt_Postman_Collection.json
```

### Xem report HTML
```bash
# Report sẽ được tạo trong thư mục: test-reports/
# Mở file .html bằng trình duyệt
```

---

## 🎯 Test nhanh các tính năng chính

### ✅ Authentication
```
POST /api/auth/register     # Đăng ký
POST /api/auth/login        # Đăng nhập
POST /api/auth/forgot-password  # Quên mật khẩu
```

### ✅ Quản lý Venue (Sân)
```
GET    /api/venues                # Xem danh sách
GET    /api/venues/search?province=Hanoi  # Tìm kiếm
POST   /api/venues                # Tạo mới (OWNER)
PUT    /api/venues/{id}           # Cập nhật (OWNER)
DELETE /api/venues/{id}           # Xóa (OWNER)
```

### ✅ Quản lý Court (Sân con)
```
GET    /api/courts                # Xem danh sách
GET    /api/courts/{id}/availability?startTime=...&endTime=...  # Kiểm tra lịch
POST   /api/courts               # Tạo mới (OWNER)
```

### ✅ Đặt sân (Booking)
```
POST   /api/bookings             # USER đặt sân
PUT    /api/bookings/{id}/confirm-payment  # USER upload ảnh CK
GET    /api/bookings/pending     # OWNER xem booking chờ confirm
PUT    /api/bookings/{id}/accept # OWNER chấp nhận
PUT    /api/bookings/{id}/reject # OWNER từ chối
```

### ✅ Đánh giá (Review)
```
POST   /api/bookings/{bookingId}/review  # Tạo review
GET    /api/venues/{venueId}/reviews     # Xem review của venue
```

### ✅ Thông báo (Notification)
```
GET    /api/notifications        # Xem danh sách
GET    /api/notifications/unread-count  # Số thông báo chưa đọc
PUT    /api/notifications/{id}/read     # Đánh dấu đã đọc
```

---

## 📊 Variables tự động

Collection tự động lưu các biến sau:

| Biến | Khi nào được lưu | Dùng để làm gì |
|------|------------------|----------------|
| `token` | Sau khi login | Authentication cho tất cả API |
| `venueId` | Sau khi tạo/lấy venue | Test các API liên quan venue |
| `courtId` | Sau khi tạo/lấy court | Test các API liên quan court |
| `bookingId` | Sau khi tạo booking | Test confirm, accept, reject |
| `priceRuleId` | Sau khi tạo price rule | Test update, delete price rule |
| `userId` | Sau khi login | Reference user |

**Bạn không cần làm gì**, tất cả tự động! ✨

---

## ❗ Lỗi thường gặp

### 1. Connection refused
```
❌ Lỗi: connect ECONNREFUSED 127.0.0.1:8080

✅ Giải pháp: Khởi động backend
cd QuanLyDatSan && ./mvnw spring-boot:run
```

### 2. 401 Unauthorized
```
❌ Lỗi: 401 Unauthorized

✅ Giải pháp: Login lại
Chạy: 1.3 Login User hoặc 1.4 Login Owner
```

### 3. 403 Forbidden
```
❌ Lỗi: 403 Forbidden

✅ Giải pháp: Sai role
- USER API → Login bằng USER
- OWNER API → Login bằng OWNER
```

### 4. 404 Not Found
```
❌ Lỗi: Venue/Court/Booking not found

✅ Giải pháp: Tạo entity trước
- Chạy Create Venue → Create Court → Create Booking
```

---

## 🎓 Tips hay

### 1. Xem Console Log
- Bấm `Ctrl+Alt+C` (Windows) hoặc `Cmd+Alt+C` (Mac)
- Xem log chi tiết của mỗi request

### 2. Chạy toàn bộ Folder
- Click vào folder (ví dụ: "1. Authentication")
- Click "Run" → Chạy tất cả request trong folder

### 3. Environment cho nhiều server
```
Dev:  baseUrl = http://localhost:8080
Prod: baseUrl = https://api.production.com
```

### 4. Export Collection
- Click vào collection → Export → v2.1
- Share cho team

---

## 📁 File Structure

```
QuanLyDatSan/
├── BookingCourt_Postman_Collection.json   ← File collection (import vào Postman)
├── HUONG_DAN_TEST_POSTMAN.md             ← Hướng dẫn chi tiết đầy đủ
├── QUICK_START_POSTMAN.md                ← File này (hướng dẫn nhanh)
├── run-postman-tests.sh                  ← Script chạy test tự động
└── test-reports/                         ← Folder chứa report (tự động tạo)
```

---

## ✅ Checklist

- [ ] Import collection vào Postman
- [ ] Chạy backend (port 8080)
- [ ] Test Register + Login
- [ ] Test Create Venue + Court (OWNER role)
- [ ] Test Create Booking (USER role)
- [ ] Test Accept/Reject Booking (OWNER role)
- [ ] Test Review (USER role)
- [ ] Test Notifications

---

## 🎉 Done!

Bây giờ bạn có thể:
- ✅ Test thủ công trên Postman GUI
- ✅ Test tự động với Newman CLI
- ✅ Xem report HTML chi tiết
- ✅ Share collection cho team

**Chúc bạn test thành công!** 🚀

---

## 📞 Tài liệu đầy đủ

Xem file: `HUONG_DAN_TEST_POSTMAN.md` để biết chi tiết:
- Tất cả API endpoints
- Request/Response mẫu
- Workflow chi tiết
- Troubleshooting đầy đủ

