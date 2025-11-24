# Hệ Thống Quản Lý Đặt Sân - API Documentation

## Tổng Quan

Backend API cho hệ thống quản lý đặt sân thể thao, hỗ trợ đặt sân, thanh toán, thống kê doanh thu cho chủ sân.

**Tech Stack:**
- Java 17
- Spring Boot 3.5.6
- Spring Security (JWT)
- MySQL 8.0
- Maven

**Base URL:** `http://localhost:8080`

---

## 🚀 Quick Start

### 1. Cấu hình Database

```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/quan_ly_dat_san
spring.datasource.username=root
spring.datasource.password=your_password
```

### 2. Chạy Application

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

### 3. Swagger UI

Truy cập: http://localhost:8080/swagger-ui.html

---

## 📚 API Endpoints

### Authentication

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Đăng ký tài khoản mới | No |
| POST | `/api/auth/login` | Đăng nhập | No |
| POST | `/api/auth/forgot-password` | Quên mật khẩu | No |
| POST | `/api/auth/reset-password` | Reset mật khẩu | No |
| POST | `/api/auth/change-password` | Đổi mật khẩu | Yes (USER/OWNER) |

### Users

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/users/me/request-owner-role` | Nâng cấp lên chủ sân | Yes (USER) |
| GET | `/api/users/me` | Lấy thông tin user hiện tại | Yes |
| PUT | `/api/users/me` | Cập nhật thông tin user | Yes |

### Venues (Sân)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/venues` | Danh sách tất cả sân | No |
| GET | `/api/venues/{id}` | Chi tiết 1 sân | No |
| GET | `/api/venues/search` | Tìm kiếm sân | No |
| GET | `/api/venues/my` | Danh sách sân của tôi | Yes (OWNER) |
| POST | `/api/venues` | Tạo sân mới | Yes (OWNER) |
| PUT | `/api/venues/{id}` | Cập nhật sân | Yes (OWNER) |
| DELETE | `/api/venues/{id}` | Xóa sân | Yes (OWNER) |
| POST | `/api/venues/{id}/images` | Upload ảnh sân | Yes (OWNER) |
| DELETE | `/api/venues/{id}/images/{imagePath}` | Xóa ảnh sân | Yes (OWNER) |

### Courts (Sân con)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/venues/{venueId}/courts` | Danh sách court của venue | No |
| GET | `/api/venues/{venueId}/courts/{date}` | Court + availability theo ngày | No |
| POST | `/api/courts` | Tạo court mới | Yes (OWNER) |
| PUT | `/api/courts/{id}` | Cập nhật court | Yes (OWNER) |
| DELETE | `/api/courts/{id}` | Xóa court | Yes (OWNER) |

### Bookings (Đặt sân)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/bookings/my` | Danh sách booking của tôi | Yes (USER) |
| GET | `/api/bookings/{id}` | Chi tiết 1 booking | Yes |
| POST | `/api/bookings` | Tạo booking mới | Yes (USER) |
| POST | `/api/bookings/{id}/upload-proof` | Upload ảnh chuyển khoản | Yes (USER) |
| PUT | `/api/bookings/{id}/accept` | Chấp nhận booking | Yes (OWNER) |
| PUT | `/api/bookings/{id}/reject` | Từ chối booking | Yes (OWNER) |
| DELETE | `/api/bookings/{id}/cancel` | Hủy booking | Yes (USER) |
| GET | `/api/bookings/pending` | Booking chờ xác nhận | Yes (OWNER) |
| GET | `/api/bookings/venue/{venueId}` | Booking theo venue | Yes (OWNER) |

### Reviews (Đánh giá)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/reviews/venue/{venueId}` | Danh sách review của sân | No |
| POST | `/api/reviews` | Tạo review mới | Yes (USER) |
| PUT | `/api/reviews/{id}` | Cập nhật review | Yes (USER) |
| DELETE | `/api/reviews/{id}` | Xóa review | Yes (USER) |

### Notifications (Thông báo)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/notifications/my` | Danh sách thông báo của tôi | Yes |
| PUT | `/api/notifications/{id}/read` | Đánh dấu đã đọc | Yes |
| PUT | `/api/notifications/read-all` | Đánh dấu tất cả đã đọc | Yes |
| DELETE | `/api/notifications/{id}` | Xóa thông báo | Yes |

### ⭐ **Analytics (Thống kê - API CHÍNH)**

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/analytics/owner` | **🔥 API chính theo specs** - Analytics tổng hợp | Yes (OWNER) |
| GET | `/api/owners/me/analytics` | Analytics cho owner hiện tại | Yes (OWNER) |
| GET | `/api/owners/{ownerId}/analytics` | Analytics theo owner ID | Yes (OWNER) |
| GET | `/api/venues/{venueId}/analytics` | Analytics theo venue | Yes (OWNER) |

**Query Parameters cho Analytics:**
- `period`: `DAY` \| `WEEK` \| `MONTH` \| `YEAR` (required, default: MONTH)
- `startDate`: `yyyy-MM-dd` (optional)
- `endDate`: `yyyy-MM-dd` (optional)

**Response Analytics (theo SPECS):**
```json
{
  "success": true,
  "data": {
    "period": "MONTH",
    "startDate": "2024-11-01T00:00:00Z",
    "endDate": "2024-11-30T23:59:59Z",
    "overview": {
      "totalRevenue": 50000000,
      "totalBookings": 150,
      "averageBookingValue": 333333,
      "bookingStats": {
        "totalBookings": 150,
        "pendingCount": 5,
        "confirmedCount": 100,
        "completedCount": 120,
        "rejectedCount": 3,
        "cancelledCount": 7,
        "conversionRate": 85.5
      }
    },
    "revenueByDate": [...],      // Doanh thu theo ngày
    "revenueByWeek": [...],      // Doanh thu theo tuần (có weekLabel, weekRange)
    "revenueByMonth": [...],     // Doanh thu theo tháng (12 tháng)
    "venuePerformance": [...],   // Hiệu suất các sân (có averageRating)
    "topCustomers": [...],       // Top 10 khách hàng (có userPhone, lastBookingDate)
    "timeSlotStats": [...],      // Thống kê theo giờ (0-23, có hourLabel)
    "paymentMethodStats": [...], // Phương thức thanh toán (có methodLabel)
    "insights": {                // PHÂN TÍCH TỰ ĐỘNG
      "peakHour": 19,
      "peakHourLabel": "19:00",
      "peakHourBookings": 25,
      "bestVenue": {
        "venueId": 1,
        "venueName": "Sân bóng A",
        "revenue": 20500000
      },
      "bestDay": {
        "date": "2024-11-15",
        "revenue": 2500000,
        "bookingCount": 10
      },
      "growthRate": 15.5,
      "growthRateLabel": "+15.5%"
    }
  }
}
```

👉 **Chi tiết:** Xem [API_ANALYTICS_GUIDE.md](./API_ANALYTICS_GUIDE.md)

---

## 🔐 Authentication

### JWT Token

API sử dụng JWT Bearer Token:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Roles

- `ROLE_USER`: Người dùng thông thường (đặt sân)
- `ROLE_OWNER`: Chủ sân (quản lý sân, xác nhận booking)

---

## 📊 Booking Flow

### 1. User đặt sân

```
POST /api/bookings
{
  "bookingItems": [
    {
      "courtId": 1,
      "startTime": "2025-11-24T18:00:00",
      "endTime": "2025-11-24T19:00:00"
    }
  ]
}
```

**→ Status: `PENDING_PAYMENT`**

### 2. User upload ảnh chuyển khoản

```
POST /api/bookings/{id}/upload-proof
FormData: file (image)
```

**→ Status: `PAYMENT_UPLOADED`**
**→ Thông báo gửi tới Owner**

### 3. Owner xác nhận

```
PUT /api/bookings/{id}/accept
```

**→ Status: `CONFIRMED`**
**→ Thông báo gửi tới User**

### 4. Hoàn thành

Sau khi hết `endTime`:
**→ Auto chuyển sang: `COMPLETED`**

### Các trạng thái khác:

- `EXPIRED`: Hết 5 phút chưa upload proof
- `REJECTED`: Owner từ chối
- `CANCELLED`: User hủy

---

## 🎯 Venue Availability

### Check sân trống

```
GET /api/venues/{venueId}/courts/{date}
```

Response:
```json
{
  "venue": {...},
  "courts": [
    {
      "court": {...},
      "availability": [
        {
          "startTime": "06:00",
          "endTime": "07:00",
          "available": true,
          "price": 200000
        },
        {
          "startTime": "07:00",
          "endTime": "08:00",
          "available": false,
          "price": 200000
        }
      ]
    }
  ]
}
```

---

## 📁 File Upload

### Upload Ảnh Chuyển Khoản

```
POST /api/bookings/{id}/upload-proof
Content-Type: multipart/form-data

file: [image file]
```

**File lưu tại:** `uploads/payment-proofs/booking-{id}-{timestamp}.{ext}`

### Upload Ảnh Sân

```
POST /api/venues/{id}/images
Content-Type: multipart/form-data

files: [image files] (multiple)
```

**File lưu tại:** `uploads/venue-images/venue-{id}-{timestamp}.{ext}`

### Lấy Ảnh

```
GET /api/files/{filename}
```

---

## 🔔 Notifications

Hệ thống tự động tạo thông báo cho:

### User nhận:
- ✅ Owner chấp nhận booking → `BOOKING_ACCEPTED`
- ❌ Owner từ chối booking → `BOOKING_REJECTED`
- ⏰ Booking sắp tới (1 giờ trước) → `BOOKING_REMINDER`
- 🏁 Booking hoàn thành → `BOOKING_COMPLETED`

### Owner nhận:
- 📝 User upload ảnh chuyển khoản → `NEW_BOOKING_PENDING`
- 🆕 User tạo booking mới → `NEW_BOOKING`
- ⭐ User để lại review → `NEW_REVIEW`

---

## ⚙️ Scheduled Tasks

### 1. Auto-expire bookings
**Cron:** Mỗi phút
- Kiểm tra booking `PENDING_PAYMENT` quá 5 phút
- Chuyển sang `EXPIRED`
- Giải phóng slot cho người khác đặt

### 2. Auto-complete bookings
**Cron:** Mỗi 5 phút
- Kiểm tra booking `CONFIRMED` đã qua `endTime`
- Chuyển sang `COMPLETED`
- Gửi thông báo hoàn thành

---

## 🗄️ Database Schema

### Core Tables

```sql
-- Users & Roles
user
role
user_roles

-- Venues & Courts
venues
address
court
price_by_time_of_day

-- Bookings
booking
booking_item

-- Others
review
notification
```

### Booking Status Flow

```
PENDING_PAYMENT → [Upload proof] → PAYMENT_UPLOADED
                                          ↓
                                    [Owner accept]
                                          ↓
                                      CONFIRMED
                                          ↓
                                    [Auto after endTime]
                                          ↓
                                      COMPLETED

[Expired 5min]  → EXPIRED
[Owner reject]  → REJECTED
[User cancel]   → CANCELLED
```

---

## 🧪 Testing

### Postman Collection

Import collection từ: `/postman/QuanLyDatSan.postman_collection.json`

### Test Accounts

```
# User Account
Phone: 0123456789
Password: password123

# Owner Account  
Phone: 0987654321
Password: password123
```

---

## 📝 Notes

### 1. Price Calculation

Giá sân có thể khác nhau theo giờ:
- `price_by_time_of_day` table lưu giá theo khung giờ
- Nếu không có → dùng `default_price` từ `court`

### 2. Booking Items

Một booking có thể có nhiều items:
- Cùng venue, cùng court hoặc khác court
- Cùng ngày hoặc khác ngày
- `totalPrice` = tổng price của tất cả items

### 3. Expired Bookings

- Booking `PENDING_PAYMENT` có 5 phút để upload proof
- Sau 5 phút → auto chuyển `EXPIRED`
- Court slot được giải phóng

### 4. Review Constraints

- Chỉ user có booking `COMPLETED` mới được review
- Mỗi user chỉ review 1 lần/venue

---

## 🚧 Known Issues & TODO

### Current Issues:
- [ ] TimeUtil.java không được sử dụng → cân nhắc xóa

### TODO:
- [ ] Add pagination cho danh sách bookings
- [ ] Add filtering cho analytics
- [ ] Cache analytics data
- [ ] Add email notifications
- [ ] Add payment gateway integration

---

## 📞 Contact

Developer: [@codewithvy](https://github.com/codewithvy)

---

## 📄 License

MIT License

