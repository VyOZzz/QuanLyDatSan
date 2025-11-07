# 🧪 TEST API BẰNG SWAGGER UI (Thay thế Postman)

## 🚀 MỞ SWAGGER UI

**URL:** `http://localhost:8080/swagger-ui.html`

Hoặc: `http://localhost:8080/swagger-ui/index.html`

---

## 📝 HƯỚNG DẪN TEST TỪNG BƯỚC

### BƯỚC 1: Login để lấy Token

1. Scroll xuống section **`auth-controller`**
2. Click vào **`POST /api/auth/login`**
3. Click nút **"Try it out"** (góc phải)
4. Nhập vào Request body:
   ```json
   {
     "phone": "0123456789",
     "password": "password123"
   }
   ```
5. Click nút **"Execute"** (màu xanh)
6. Xem **Response body**, copy `jwtToken`:
   ```json
   {
     "success": true,
     "data": {
       "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  ← Copy cái này
       "id": 1,
       "phone": "0123456789"
     }
   }
   ```

---

### BƯỚC 2: Thêm Token vào Swagger (Authorize)

1. Scroll lên đầu trang
2. Click nút **"Authorize"** (có icon ổ khóa 🔒)
3. Popup hiện ra, nhập vào ô **Value:**
   ```
   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
   ⚠️ **CHÚ Ý:** Phải có chữ `Bearer` + dấu cách + token

4. Click **"Authorize"**
5. Click **"Close"**

✅ Bây giờ tất cả API đều có token, không cần nhập lại!

---

### BƯỚC 3: Lấy danh sách Courts của Venue

1. Scroll xuống section **`venues-controller`**
2. Click **`GET /api/venues/{venueId}/courts`**
3. Click **"Try it out"**
4. Nhập **venueId:** `14` (hoặc venue ID bạn muốn test)
5. Click **"Execute"**

**Response mong đợi:**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "description": "Sân số 1",
      "isActive": true
    },
    {
      "id": 11,
      "description": "Sân số 2",
      "isActive": true
    }
  ]
}
```

📌 **Lưu lại:** Court ID (ví dụ: `10`)

---

### BƯỚC 4: Kiểm tra Availability (Sân rảnh hay bận)

1. Scroll xuống **`GET /api/venues/{venueId}/courts/availability`**
2. Click **"Try it out"**
3. Nhập:
   - **venueId:** `14`
   - **startTime:** `2025-11-07T14:00:00` (KHÔNG CÓ Z)
   - **endTime:** `2025-11-07T15:00:00` (KHÔNG CÓ Z)
4. Click **"Execute"**

**Response (nếu chưa có booking):**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "description": "Sân số 1",
      "available": true,        ← Sân rảnh
      "bookedSlots": []         ← Chưa có booking
    }
  ]
}
```

---

### BƯỚC 5: Đặt Sân (Create Booking)

1. Scroll xuống section **`booking-controller`**
2. Click **`POST /api/bookings`**
3. Click **"Try it out"**
4. Nhập Request body:
   ```json
   {
     "venueId": 14,
     "courtId": 10,
     "startTime": "2025-11-07T14:00:00",
     "endTime": "2025-11-07T15:00:00"
   }
   ```
5. Click **"Execute"**

**Response mong đợi:**
```json
{
  "success": true,
  "data": {
    "id": 123,                              ← Booking ID
    "venueId": 14,
    "courtId": 10,
    "status": "PENDING",
    "paymentDeadline": "2025-11-07T14:05:00"  ← Còn 5 phút
  },
  "message": "Booking created successfully. Please upload payment proof within 5 minutes."
}
```

📌 **Lưu lại:** Booking ID (ví dụ: `123`)

---

### BƯỚC 6: Kiểm tra lại Availability (Slot đã bị khóa)

1. Lặp lại **BƯỚC 4** (GET availability)
2. Sử dụng cùng thời gian: `14:00:00 - 15:00:00`

**Response (slot bị khóa):**
```json
{
  "data": [
    {
      "id": 10,
      "available": false,       ← ĐÃ BỊ KHÓA!
      "bookedSlots": [
        {
          "bookingId": 123,
          "startTime": "2025-11-07T14:00:00",
          "endTime": "2025-11-07T15:00:00",
          "status": "PENDING"
        }
      ]
    }
  ]
}
```

✅ **THÀNH CÔNG!** Slot đã bị khóa ngay sau khi đặt!

---

### BƯỚC 7: Test đặt trùng slot (Phải bị từ chối)

1. Lặp lại **BƯỚC 5** (POST /api/bookings)
2. Nhập **CÙNG** thời gian: `14:00:00 - 15:00:00`
3. Click **"Execute"**

**Response mong đợi (bị từ chối):**
```json
{
  "success": false,
  "message": "Sân đã được đặt trong khung giờ này. Vui lòng chọn khung giờ khác.",
  "timestamp": "2025-11-07T..."
}
```

**HTTP Status:** `400 Bad Request` hoặc `409 Conflict`

✅ **ĐÚNG!** Hệ thống không cho đặt trùng!

---

### BƯỚC 8: Upload Payment Proof (Ảnh chuyển khoản)

⚠️ **LƯU Ý:** Swagger UI **KHÔNG HỖ TRỢ** upload file tốt. Có 2 cách:

#### Cách 1: Dùng Postman cho bước này
- Import collection: `POSTMAN/BookingCourt_Postman_Collection.json`
- Test API: `PUT /api/bookings/{id}/confirm-payment`

#### Cách 2: Dùng curl
```bash
curl -X PUT "http://localhost:8080/api/bookings/123/confirm-payment" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -F "file=@C:/path/to/image.jpg"
```

#### Cách 3: Skip bước này (test tiếp)
- Nếu chỉ muốn test khóa slot, có thể bỏ qua upload payment
- Slot vẫn bị khóa trong vòng 5 phút

---

### BƯỚC 9: Login Owner (để Accept Booking)

1. Click nút **"Authorize"** → **"Logout"** (xóa token user cũ)
2. Lặp lại **BƯỚC 1** với tài khoản Owner:
   ```json
   {
     "phone": "0987654321",
     "password": "owner123"
   }
   ```
3. Copy token Owner
4. Click **"Authorize"** và nhập token Owner mới

---

### BƯỚC 10: Owner Accept Booking

1. Scroll xuống **`PUT /api/bookings/{id}/accept`**
2. Click **"Try it out"**
3. Nhập **id:** `123` (Booking ID từ BƯỚC 5)
4. Click **"Execute"**

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "status": "CONFIRMED",       ← Đã chuyển sang CONFIRMED
    "confirmedAt": "2025-11-07T14:10:00"
  },
  "message": "Booking accepted successfully"
}
```

---

### BƯỚC 11: Kiểm tra lại Availability sau khi Accept

1. Login lại User (BƯỚC 1 + 2 với user token)
2. Lặp lại **BƯỚC 4** (GET availability)

**Response:**
```json
{
  "data": [
    {
      "id": 10,
      "available": false,       ← VẪN BỊ KHÓA!
      "bookedSlots": [
        {
          "bookingId": 123,
          "status": "CONFIRMED"  ← Status đã chuyển
        }
      ]
    }
  ]
}
```

✅ **HOÀN THÀNH!** Slot vẫn bị khóa sau khi owner accept!

---

## 🎯 TÓM TẮT TEST CASES

| # | Test Case | API | Kết quả mong đợi |
|---|-----------|-----|------------------|
| 1 | Login User | `POST /api/auth/login` | Nhận token |
| 2 | Authorize | Click "Authorize" | Thêm token |
| 3 | Lấy courts | `GET /api/venues/{id}/courts` | Danh sách courts |
| 4 | Check availability | `GET /api/venues/{id}/courts/availability` | `available: true` |
| 5 | Đặt sân | `POST /api/bookings` | Booking thành công |
| 6 | Check lại availability | Same API | `available: false` ✅ |
| 7 | Đặt trùng slot | `POST /api/bookings` | `400 Bad Request` ✅ |
| 8 | Upload payment | `PUT /api/bookings/{id}/confirm-payment` | (Dùng Postman) |
| 9 | Login Owner | `POST /api/auth/login` | Nhận owner token |
| 10 | Accept booking | `PUT /api/bookings/{id}/accept` | Status → CONFIRMED |
| 11 | Check lại availability | Same API | `available: false` ✅ |

---

## 💡 TIPS KHI DÙNG SWAGGER UI

### 1. Reset Token khi đổi user
- Click **"Authorize"** → **"Logout"**
- Login lại với user mới
- Nhập token mới vào **"Authorize"**

### 2. Format thời gian
- ✅ **ĐÚNG:** `2025-11-07T14:00:00` (KHÔNG CÓ Z)
- ❌ **SAI:** `2025-11-07T14:00:00Z`
- ❌ **SAI:** `2025-11-07 14:00:00`

### 3. Xem Response
- **Response body:** Kết quả JSON
- **Response headers:** Thông tin bổ sung
- **Curl:** Lệnh curl tương đương (có thể copy)

### 4. Schema/Model
- Click vào **"Schemas"** (cuối trang) để xem cấu trúc DTO
- Biết field nào bắt buộc, field nào optional

### 5. Clear cache nếu không thấy API mới
- Ctrl + Shift + Delete (Clear cache)
- Refresh page: F5 hoặc Ctrl + F5

---

## 🔍 DEBUG KHI GẶP LỖI

### Lỗi 401 Unauthorized
- **Nguyên nhân:** Token sai hoặc hết hạn
- **Giải pháp:** Login lại và nhập token mới vào "Authorize"

### Lỗi 403 Forbidden
- **Nguyên nhân:** User không có quyền (VD: User gọi API Owner)
- **Giải pháp:** Login với tài khoản có role phù hợp

### Lỗi 400 Bad Request
- **Nguyên nhân:** Format sai hoặc validation lỗi
- **Giải pháp:** Xem **Response body** để biết field nào sai

### Lỗi 404 Not Found
- **Nguyên nhân:** ID không tồn tại
- **Giải pháp:** Kiểm tra lại venueId, courtId, bookingId

---

## 📊 SO SÁNH SWAGGER UI vs POSTMAN

| Tính năng | Swagger UI | Postman |
|-----------|------------|---------|
| **Không cần cài đặt** | ✅ Mở browser | ❌ Cần cài app |
| **Tự động có docs** | ✅ Có sẵn | ❌ Phải import |
| **Test nhanh** | ✅ Rất nhanh | ⚠️ Hơi chậm |
| **Upload file** | ❌ Không tốt | ✅ Tốt |
| **Save request** | ❌ Không lưu được | ✅ Lưu được |
| **Environment variables** | ❌ Không có | ✅ Có |
| **Test automation** | ❌ Không có | ✅ Có |

**KẾT LUẬN:** 
- **Swagger UI** tốt cho **test nhanh, debug** ✅
- **Postman** tốt cho **test phức tạp, automation** ✅

---

## 🎉 KẾT QUẢ

Bạn đã test thành công bằng Swagger UI:
- ✅ Đặt sân thành công
- ✅ Slot bị khóa ngay sau khi đặt
- ✅ Không cho đặt trùng slot
- ✅ Owner accept được booking
- ✅ Slot vẫn bị khóa sau khi accept

**Swagger UI hoàn toàn có thể thay thế Postman cho việc test API cơ bản!** 🚀

