# API Documentation for Frontend Development

## Base URL
```
http://localhost:8080/api
```

## Authentication
Hầu hết các API yêu cầu JWT token. Sau khi đăng nhập, thêm token vào header:
```
Authorization: Bearer {your_jwt_token}
```

---

## 📋 Table of Contents
1. [Authentication APIs](#authentication-apis)
2. [User Management APIs](#user-management-apis)
3. [Venues APIs](#venues-apis)
4. [Court APIs](#court-apis)
5. [Booking APIs](#booking-apis)
6. [Review APIs](#review-apis)
7. [Notification APIs](#notification-apis)
8. [Address APIs](#address-apis)

---

## Authentication APIs

### 1. Login
**POST** `/auth/login`

**Authentication Required:** ❌ No

**Request Body:**
```json
{
  "phone": "0123456789",
  "password": "password123"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "phone": "0123456789",
    "roles": ["ROLE_USER"]
  },
  "message": "Login success",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Response Error (401):**
```json
{
  "success": false,
  "message": "Số điện thoại hoặc mật khẩu không đúng",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 2. Register
**POST** `/auth/register`

**Authentication Required:** ❌ No

**Request Body:**
```json
{
  "fullname": "Nguyen Van A",
  "phone": "0123456789",
  "email": "nguyenvana@example.com",
  "password": "password123",
  "confirmPassword": "password123"
}
```

**Validation Rules:**
- `fullname`: 2-100 ký tự
- `phone`: 8-15 số, không được trùng
- `email`: Phải là email hợp lệ, không được trùng
- `password`: Tối thiểu 6 ký tự
- `confirmPassword`: Phải khớp với password

**Response Success (200):**
```json
{
  "success": true,
  "data": "User registered successfully",
  "message": "Registered",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Response Error (400):**
```json
{
  "success": false,
  "message": "Phone number already exists",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

## User Management APIs

### 1. Get Current User Profile
**GET** `/users/me`

**Authentication Required:** ✅ Yes (any authenticated user)

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "fullname": "Nguyen Van A",
    "phone": "0123456789",
    "email": "nguyenvana@example.com",
    "roles": ["ROLE_USER"],
    "bankName": null,
    "bankAccountNumber": null,
    "bankAccountName": null
  },
  "message": "Success",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 2. Request Owner Role (Trở thành chủ sân)
**POST** `/users/me/request-owner-role`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": "Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập lại để cập nhật quyền.",
  "message": "Success",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Note:** User cần đăng nhập lại sau khi nâng cấp để JWT token có role mới.

---

### 3. Update User Profile
**PUT** `/users/me`

**Authentication Required:** ✅ Yes

**Request Body:**
```json
{
  "fullname": "Nguyen Van A Updated",
  "email": "newemail@example.com",
  "bankName": "Vietcombank",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "NGUYEN VAN A"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "fullname": "Nguyen Van A Updated",
    "phone": "0123456789",
    "email": "newemail@example.com",
    "roles": ["ROLE_USER", "ROLE_OWNER"],
    "bankName": "Vietcombank",
    "bankAccountNumber": "1234567890",
    "bankAccountName": "NGUYEN VAN A"
  },
  "message": "Cập nhật thông tin thành công",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

## Venues APIs

### 1. Get All Venues
**GET** `/venues`

**Authentication Required:** ✅ Yes

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Sân bóng đá ABC",
      "numberOfCourt": 5,
      "address": {
        "id": 1,
        "province": "Hà Nội",
        "district": "Cầu Giấy",
        "detail": "123 Đường ABC"
      },
      "courtsCount": 5,
      "pricePerHour": 200000.0,
      "averageRating": 4.5,
      "totalReviews": 25,
      "openingTime": "06:00:00",
      "closingTime": "22:00:00",
      "images": [
        "http://localhost:8080/uploads/venue-images/image1.jpg",
        "http://localhost:8080/uploads/venue-images/image2.jpg"
      ]
    }
  ],
  "message": "List venues",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 2. Get My Venues (Owner only)
**GET** `/venues/my-venues`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Response:** Giống như Get All Venues nhưng chỉ trả về venues thuộc sở hữu của owner đang đăng nhập.

---

### 3. Search Venues
**GET** `/venues/search`

**Authentication Required:** ✅ Yes

**Query Parameters:**
- `name` (optional): Tên venue
- `province` (optional): Tỉnh/Thành phố
- `district` (optional): Quận/Huyện
- `detail` (optional): Địa chỉ chi tiết

**Example:**
```
GET /api/venues/search?name=ABC&province=Hà Nội
```

**Response Success (200):** Giống Get All Venues

---

### 4. Get Venue by ID
**GET** `/venues/{id}`

**Authentication Required:** ✅ Yes

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân bóng đá ABC",
    "numberOfCourt": 5,
    "address": {
      "id": 1,
      "province": "Hà Nội",
      "district": "Cầu Giấy",
      "detail": "123 Đường ABC"
    },
    "courtsCount": 5,
    "pricePerHour": 200000.0,
    "averageRating": 4.5,
    "totalReviews": 25,
    "openingTime": "06:00:00",
    "closingTime": "22:00:00",
    "images": [
      "http://localhost:8080/uploads/venue-images/image1.jpg"
    ]
  },
  "message": null,
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 5. Get Courts with Availability Status
**GET** `/venues/{venueId}/courts/availability`

**Authentication Required:** ✅ Yes

**Query Parameters:**
- `startTime` (required): ISO DateTime format (VD: 2025-11-15T08:00:00)
- `endTime` (required): ISO DateTime format (VD: 2025-11-15T10:00:00)

**Example:**
```
GET /api/venues/1/courts/availability?startTime=2025-11-15T08:00:00&endTime=2025-11-15T10:00:00
```

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "description": "Sân số 1",
      "available": true,
      "bookedSlots": []
    },
    {
      "id": 2,
      "description": "Sân số 2",
      "available": false,
      "bookedSlots": [
        {
          "startTime": "2025-11-15T08:00:00",
          "endTime": "2025-11-15T09:00:00",
          "bookingId": 45
        }
      ]
    }
  ],
  "message": "Courts availability retrieved",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 6. Create Venue (Owner only)
**POST** `/venues`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Request Body:**
```json
{
  "name": "Sân bóng đá XYZ",
  "numberOfCourt": 3,
  "addressRequest": {
    "province": "Hà Nội",
    "district": "Hoàn Kiếm",
    "detail": "456 Đường XYZ"
  },
  "pricePerHour": 250000.0,
  "openingTime": "06:00:00",
  "closingTime": "23:00:00"
}
```

**Response Success (200):** Trả về VenuesDTO của venue vừa tạo.

---

### 7. Update Venue
**PUT** `/venues/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Request Body:** Giống Create Venue

---

### 8. Upload Venue Images
**POST** `/venues/{id}/upload-images`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Request:** `multipart/form-data` với field `files` (array of images)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    "http://localhost:8080/uploads/venue-images/image1.jpg",
    "http://localhost:8080/uploads/venue-images/image2.jpg"
  ],
  "message": "Upload images successfully",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 9. Delete Venue
**DELETE** `/venues/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Response Success (204):** No Content

---

## Court APIs

### 1. Get All Courts
**GET** `/courts`

**Authentication Required:** ✅ Yes

**Response Success (200):** Danh sách tất cả courts trong hệ thống.

---

### 2. Get Court by ID
**GET** `/courts/{id}`

**Authentication Required:** ✅ Yes

**Response Success (200):**
```json
{
  "id": 1,
  "description": "Sân số 1",
  "venues": {
    "id": 1,
    "name": "Sân bóng đá ABC"
  }
}
```

---

### 3. Check Court Availability
**GET** `/courts/{id}/availability`

**Authentication Required:** ✅ Yes

**Query Parameters:**
- `startTime` (required): ISO DateTime format
- `endTime` (required): ISO DateTime format

**Example:**
```
GET /api/courts/1/availability?startTime=2025-11-15T08:00:00&endTime=2025-11-15T10:00:00
```

**Response Success (200):**
```json
{
  "courtId": 1,
  "available": false,
  "bookedSlots": [
    {
      "startTime": "2025-11-15T08:00:00",
      "endTime": "2025-11-15T09:00:00",
      "bookingId": 45
    }
  ]
}
```

---

### 4. Create Court (Owner only)
**POST** `/courts`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Request Body:**
```json
{
  "venueId": 1,
  "description": "Sân số 6"
}
```

**Response Success (200):** Trả về Court object vừa tạo.

---

### 5. Update Court
**PUT** `/courts/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Request Body:** Giống Create Court

---

### 6. Delete Court
**DELETE** `/courts/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Response Success (204):** No Content

---

## Booking APIs

### 1. Create Booking
**POST** `/bookings`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Request Body:**
```json
{
  "venueId": 1,
  "courtId": 2,
  "startTime": "2025-11-15T18:00:00",
  "endTime": "2025-11-15T19:00:00"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "userId": 1,
    "userName": "Nguyen Van A",
    "courtId": 2,
    "courtName": "Sân số 2",
    "venuesName": "Sân bóng đá ABC",
    "startTime": "2025-11-15T18:00:00",
    "endTime": "2025-11-15T19:00:00",
    "totalPrice": 200000.0,
    "status": "PENDING_PAYMENT",
    "expireTime": "2025-11-15T17:05:00",
    "paymentProofUploaded": false,
    "paymentProofUrl": null,
    "paymentProofUploadedAt": null,
    "rejectionReason": null,
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN B",
      "ownerName": "Nguyen Van B"
    }
  },
  "message": "Đặt sân thành công. Vui lòng chuyển khoản trong 5 phút.",
  "timestamp": "2025-11-05T17:00:00Z"
}
```

**Note:** 
- `expireTime` là thời gian hết hạn thanh toán (5 phút sau khi tạo booking)
- `ownerBankInfo` chứa thông tin tài khoản ngân hàng của chủ sân để user chuyển khoản

**Booking Status Flow:**
1. `PENDING_PAYMENT` - Vừa tạo, chờ upload chứng minh chuyển khoản
2. `PAYMENT_UPLOADED` - Đã upload và xác nhận, chờ owner xác nhận
3. `CONFIRMED` - Owner chấp nhận
4. `COMPLETED` - Hoàn thành (tự động sau khi hết giờ đặt)
5. `REJECTED` - Owner từ chối
6. `CANCELLED` - User hủy
7. `EXPIRED` - Hết hạn thanh toán (quá 5 phút không upload)

**Status Transition Rules:**

| Current Status | User Can | Owner Can | Auto-System |
|---------------|----------|-----------|-------------|
| `PENDING_PAYMENT` | ✅ Cancel<br>✅ Upload proof<br>✅ Confirm payment | ❌ Cannot act yet | ⏰ Auto → `EXPIRED` after 5 min |
| `PAYMENT_UPLOADED` | ❌ Cannot cancel | ✅ Accept → `CONFIRMED`<br>✅ Reject → `REJECTED` | - |
| `CONFIRMED` | ❌ Cannot cancel | ❌ Cannot change | ⏰ Auto → `COMPLETED` after endTime |
| `COMPLETED` | ❌ Cannot change<br>✅ Can review | ❌ Cannot change | - |
| `REJECTED` | ❌ Cannot change | ❌ Cannot change | - |
| `CANCELLED` | ❌ Cannot change | ❌ Cannot change | - |
| `EXPIRED` | ❌ Cannot change | ❌ Cannot change | - |

**Important Notes:**
- ⚠️ User **CANNOT** cancel booking once it reaches `PAYMENT_UPLOADED` status (already paid)
- ⚠️ Owner can **ONLY** accept/reject bookings in `PAYMENT_UPLOADED` status
- ⚠️ Once booking is `CONFIRMED`, neither user nor owner can cancel it
- ✅ User can cancel anytime during `PENDING_PAYMENT` (before uploading payment proof)

**Error Handling:**
```json
// Example: User tries to cancel CONFIRMED booking
{
  "success": false,
  "message": "Cannot cancel confirmed or completed booking",
  "timestamp": "2025-11-05T17:00:00Z"
}
```

```json
// Example: Owner tries to accept booking not in PAYMENT_UPLOADED status
{
  "success": false,
  "message": "Booking must be in PAYMENT_UPLOADED status to accept",
  "timestamp": "2025-11-05T17:00:00Z"
}
```

---

### 2. Upload Payment Proof
**POST** `/bookings/{id}/upload-payment-proof`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Request:** `multipart/form-data` với field `file` (jpg, jpeg, png - max 10MB)

**Example (Windows CMD):**
```bash
curl -X POST "http://localhost:8080/api/bookings/123/upload-payment-proof" -H "Authorization: Bearer <TOKEN>" -F "file=@C:\path\to\proof.jpg"
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "paymentProofUploaded": true,
    "paymentProofUrl": "http://localhost:8080/uploads/payment-proofs/proof123.jpg",
    "paymentProofUploadedAt": "2025-11-15T17:02:00",
    "status": "PENDING_PAYMENT"
  },
  "message": "Đã upload ảnh thành công. Vui lòng nhấn 'Xác nhận thanh toán' để gửi cho chủ sân.",
  "timestamp": "2025-11-05T17:02:00Z"
}
```

**Note:** Sau khi upload thành công, user cần gọi API "Confirm Payment" để chuyển status sang `PAYMENT_UPLOADED`.

---

### 3. Confirm Payment (Xác nhận đã chuyển khoản)
**PUT** `/bookings/{id}/confirm-payment`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Request Body:**
```json
{
  "paymentProofUrl": "http://localhost:8080/uploads/payment-proofs/proof123.jpg"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "status": "PAYMENT_UPLOADED",
    "paymentProofUploaded": true,
    "paymentProofUrl": "http://localhost:8080/uploads/payment-proofs/proof123.jpg"
  },
  "message": "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận.",
  "timestamp": "2025-11-05T17:03:00Z"
}
```

---

### 4. Get My Bookings
**GET** `/bookings/my-bookings`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "userId": 1,
      "userName": "Nguyen Van A",
      "courtId": 2,
      "courtName": "Sân số 2",
      "venuesName": "Sân bóng đá ABC",
      "startTime": "2025-11-15T18:00:00",
      "endTime": "2025-11-15T19:00:00",
      "totalPrice": 200000.0,
      "status": "CONFIRMED",
      "expireTime": null,
      "paymentProofUploaded": true,
      "paymentProofUrl": "http://localhost:8080/uploads/payment-proofs/proof123.jpg",
      "paymentProofUploadedAt": "2025-11-15T17:02:00",
      "rejectionReason": null,
      "ownerBankInfo": {
        "bankName": "Vietcombank",
        "bankAccountNumber": "1234567890",
        "bankAccountName": "NGUYEN VAN B",
        "ownerName": "Nguyen Van B"
      }
    }
  ],
  "message": "My bookings retrieved successfully",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 5. Get Booking by ID
**GET** `/bookings/{id}`

**Authentication Required:** ✅ Yes

**Response Success (200):** Giống format trong Get My Bookings

---

### 6. Get Pending Bookings (Owner only)
**GET** `/bookings/pending`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Description:** 
- Lấy danh sách **TẤT CẢ** bookings chờ xác nhận (`PAYMENT_UPLOADED`) từ **TẤT CẢ venues** thuộc sở hữu của owner
- Nếu owner có nhiều venue, API này sẽ trả về pending bookings của **TẤT CẢ venues**
- Hữu ích cho dashboard tổng quan của owner

**Response Success (200):** Array of BookingResponse

**Example Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "venuesName": "Sân bóng đá ABC",
      "status": "PAYMENT_UPLOADED",
      "paymentProofUploaded": true
    },
    {
      "id": 124,
      "venuesName": "Sân bóng rổ XYZ",
      "status": "PAYMENT_UPLOADED",
      "paymentProofUploaded": true
    }
  ],
  "message": "Lấy danh sách booking chờ xác nhận thành công.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 6.1. Get Pending Bookings by Venue (Owner only) 🆕
**GET** `/bookings/venue/{venueId}/pending`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Description:** 
- Lấy danh sách bookings chờ xác nhận (`PAYMENT_UPLOADED`) của **MỘT venue cụ thể**
- Owner phải là chủ sở hữu của venue đó, nếu không sẽ trả về lỗi 403
- Hữu ích khi owner muốn xem pending bookings của từng venue riêng biệt

**Path Parameters:**
- `venueId` (Long): ID của venue cần lấy pending bookings

**Response Success (200):** Array of BookingResponse

**Example Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "venuesName": "Sân bóng đá ABC",
      "status": "PAYMENT_UPLOADED",
      "paymentProofUploaded": true
    }
  ],
  "message": "Lấy danh sách booking chờ xác nhận thành công.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Error Response (403):**
```json
{
  "success": false,
  "message": "You are not authorized to view bookings for this venue",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 7. Get All Bookings for Owner
**GET** `/bookings/owner/all-bookings`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Description:** Lấy tất cả bookings của các venues thuộc sở hữu (tất cả trạng thái)

**Response Success (200):** Array of BookingResponse

---

### 8. Get Venue Bookings (Owner only)
**GET** `/bookings/venue/{venueId}`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Description:** Lấy tất cả bookings của một venue cụ thể

**Response Success (200):** Array of BookingResponse

---

### 9. Accept Booking (Owner only)
**PUT** `/bookings/{id}/accept`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "status": "CONFIRMED"
  },
  "message": "Đã xác nhận đặt sân thành công.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Note:** Chỉ có thể accept booking có status = `PAYMENT_UPLOADED`

---

### 10. Reject Booking (Owner only)
**PUT** `/bookings/{id}/reject`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Request Body:**
```json
{
  "rejectionReason": "Sân đang bảo trì"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "status": "REJECTED",
    "rejectionReason": "Sân đang bảo trì"
  },
  "message": "Đã từ chối đặt sân.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 11. Cancel Booking (User)
**PUT** `/bookings/{id}/cancel`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "status": "CANCELLED"
  },
  "message": "Booking cancelled successfully",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Note:** Chỉ có thể cancel booking chưa CONFIRMED hoặc COMPLETED

---

## Review APIs

### 1. Create Review
**POST** `/bookings/{bookingId}/review`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Request Body:**
```json
{
  "rating": 5,
  "comment": "Sân đẹp, dịch vụ tốt"
}
```

**Validation:**
- `rating`: 1-5 (required)
- `comment`: 10-500 ký tự (required)

**Response Success (201):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "bookingId": 123,
    "venueId": 1,
    "venueName": "Sân bóng đá ABC",
    "userId": 1,
    "userName": "Nguyen Van A",
    "rating": 5,
    "comment": "Sân đẹp, dịch vụ tốt",
    "createdAt": "2025-11-05T15:30:00"
  },
  "message": "Review created successfully",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Note:** Chỉ có thể review booking đã COMPLETED và chưa có review

---

### 2. Get Venue Reviews
**GET** `/venues/{venueId}/reviews`

**Authentication Required:** ❌ No (Public API)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "bookingId": 123,
      "venueId": 1,
      "venueName": "Sân bóng đá ABC",
      "userId": 1,
      "userName": "Nguyen Van A",
      "rating": 5,
      "comment": "Sân đẹp, dịch vụ tốt",
      "createdAt": "2025-11-05T15:30:00"
    }
  ],
  "message": "Reviews retrieved successfully",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 3. Get My Reviews
**GET** `/my-reviews`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Response Success (200):** Array of ReviewDTO

---

### 4. Get Booking Review
**GET** `/bookings/{bookingId}/review`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Response Success (200):** ReviewDTO object

---

## Notification APIs

### 1. Get My Notifications
**GET** `/notifications`

**Authentication Required:** ✅ Yes

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "title": "Booking được xác nhận",
      "message": "Booking #123 đã được chủ sân xác nhận",
      "type": "BOOKING_CONFIRMED",
      "isRead": false,
      "createdAt": "2025-11-05T15:30:00"
    }
  ],
  "message": "Lấy danh sách thông báo thành công.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Notification Types:**
- `BOOKING_CONFIRMED` - Booking được xác nhận
- `BOOKING_REJECTED` - Booking bị từ chối
- `PAYMENT_UPLOADED` - User đã upload chứng minh (cho owner)
- `BOOKING_EXPIRED` - Booking hết hạn
- `BOOKING_COMPLETED` - Booking hoàn thành

---

### 2. Get Unread Count
**GET** `/notifications/unread-count`

**Authentication Required:** ✅ Yes

**Response Success (200):**
```json
{
  "success": true,
  "data": 5,
  "message": "Số thông báo chưa đọc.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 3. Mark as Read
**PUT** `/notifications/{id}/read`

**Authentication Required:** ✅ Yes

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Đã đánh dấu thông báo là đã đọc.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 4. Mark All as Read
**PUT** `/notifications/read-all`

**Authentication Required:** ✅ Yes

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Đã đánh dấu tất cả thông báo là đã đọc.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

### 5. Delete Notification
**DELETE** `/notifications/{id}`

**Authentication Required:** ✅ Yes

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa thông báo.",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

## Address APIs

### 1. Get All Addresses
**GET** `/addresses`

**Authentication Required:** ✅ Yes

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "province": "Hà Nội",
      "district": "Cầu Giấy",
      "detail": "123 Đường ABC"
    }
  ],
  "message": "List of addresses",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

---

## Error Responses

All error responses follow this format:

```json
{
  "success": false,
  "message": "Error message here",
  "timestamp": "2025-11-05T15:30:00Z"
}
```

**Common HTTP Status Codes:**
- `200` - OK
- `201` - Created
- `204` - No Content
- `400` - Bad Request (validation error)
- `401` - Unauthorized (not logged in or token invalid)
- `403` - Forbidden (không có quyền)
- `404` - Not Found
- `500` - Internal Server Error

---

## Notes for Frontend Developers

### Date/Time Format
- **Request:** Sử dụng ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
- **Example:** `2025-11-15T18:00:00`
- **Timezone:** Server sử dụng LocalDateTime (không có timezone)

### File Upload
- **Payment Proof:** Max 10MB, formats: jpg, jpeg, png
- **Venue Images:** Multiple files upload supported
- **Content-Type:** `multipart/form-data`

### Booking Flow (User)
1. Tìm kiếm và chọn venue
2. Kiểm tra tính khả dụng của courts (`/venues/{id}/courts/availability`)
3. Tạo booking (`POST /bookings`)
4. Nhận thông tin tài khoản ngân hàng chủ sân từ response
5. Chuyển khoản và chụp ảnh
6. Upload ảnh chứng minh (`POST /bookings/{id}/upload-payment-proof`)
7. Xác nhận thanh toán (`PUT /bookings/{id}/confirm-payment`)
8. Đợi owner xác nhận
9. Nhận thông báo khi được xác nhận/từ chối

### Booking Flow (Owner)
1. Xem danh sách booking chờ xác nhận (`GET /bookings/pending`)
2. Xem ảnh chứng minh chuyển khoản
3. Accept hoặc Reject booking
4. User nhận thông báo

### Auto-Processing
- Booking tự động chuyển sang `EXPIRED` nếu không upload proof trong 5 phút
- Booking tự động chuyển sang `COMPLETED` sau khi hết giờ đặt

---

## Postman Collection

Để test API, sử dụng Postman collection tại: `POSTMAN/BookingCourt_Postman_Collection.json`

Import collection này vào Postman và làm theo hướng dẫn trong `POSTMAN/QUICK_START_POSTMAN.md`

---

**Last Updated:** November 5, 2025
**API Version:** 1.0
**Base URL:** http://localhost:8080/api
