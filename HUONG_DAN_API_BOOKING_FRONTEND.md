# HƯỚNG DẪN API BOOKING CHO FRONTEND

## Tổng quan
Tài liệu này hướng dẫn chi tiết cách sử dụng API để đặt sân, bao gồm:
- Chọn ngày giờ và sân
- Tính giá theo khung giờ (giá thay đổi theo thời gian)
- Quy trình thanh toán và xác nhận
- **Hiển thị thông tin ngân hàng chủ sân sau khi booking**

---

## MỤC LỤC
1. [Kiểm tra tính khả dụng của sân](#1-kiểm-tra-tính-khả-dụng-của-sân)
2. [Xem bảng giá theo khung giờ](#2-xem-bảng-giá-theo-khung-giờ)
3. [Tạo booking mới](#3-tạo-booking-mới)
4. [Upload chứng minh chuyển khoản](#4-upload-chứng-minh-chuyển-khoản)
5. [Xem danh sách booking của tôi](#5-xem-danh-sách-booking-của-tôi)
6. [Xem chi tiết một booking](#6-xem-chi-tiết-một-booking)
7. [Hủy booking](#7-hủy-booking)
8. [Trạng thái booking](#8-trạng-thái-booking-bookingstatus)
9. [Quy trình booking hoàn chỉnh](#9-quy-trình-booking-hoàn-chỉnh)
10. [Tính giá động theo khung giờ](#10-tính-giá-động-theo-khung-giờ)
11. [Lưu ý quan trọng cho frontend](#11-lưu-ý-quan-trọng-cho-frontend)
12. [Mã lỗi thường gặp](#12-mã-lỗi-thường-gặp)
13. [Demo flow code frontend](#13-demo-flow-code-frontend-reactvue)
14. [**LUỒNG HIỂN THỊ THÔNG TIN NGÂN HÀNG CHỦ SÂN**](#14-luồng-hiển-thị-thông-tin-ngân-hàng-chủ-sân) ⭐ MỚI

---

## 1. KIỂM TRA TÍNH KHẢ DỤNG CỦA SÂN

### Endpoint
```
GET /api/courts/{courtId}/availability
```

### Mô tả
Kiểm tra xem sân có trống trong khung giờ cụ thể hay không.

### Headers
```
Authorization: Bearer {JWT_TOKEN}
```

### Query Parameters
| Tham số | Kiểu | Bắt buộc | Mô tả |
|---------|------|----------|-------|
| startTime | ISO DateTime | Có | Thời gian bắt đầu (VD: 2024-11-15T08:00:00) |
| endTime | ISO DateTime | Có | Thời gian kết thúc (VD: 2024-11-15T10:00:00) |

### Request Example
```http
GET /api/courts/1/availability?startTime=2024-11-15T08:00:00&endTime=2024-11-15T10:00:00
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response Success (200 OK)
```json
{
  "courtId": 1,
  "available": true,
  "bookedSlots": []
}
```

### Response - Sân đã có người đặt
```json
{
  "courtId": 1,
  "available": false,
  "bookedSlots": [
    {
      "startTime": "2024-11-15T08:00:00",
      "endTime": "2024-11-15T09:00:00",
      "bookingId": 45
    },
    {
      "startTime": "2024-11-15T09:30:00",
      "endTime": "2024-11-15T10:00:00",
      "bookingId": 48
    }
  ]
}
```

---

## 2. XEM BẢNG GIÁ THEO KHUNG GIỜ

### Endpoint
```
GET /api/venues/{venueId}/price-rules
```

### Mô tả
Lấy danh sách các khung giờ và giá tương ứng của một sân. Frontend dùng để hiển thị bảng giá cho người dùng.

### Headers
```
Authorization: Bearer {JWT_TOKEN}
```

### Request Example
```http
GET /api/venues/1/price-rules
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response Success (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Giờ sáng (Thứ 2-6)",
      "startTime": "06:00:00",
      "endTime": "10:00:00",
      "pricePerHour": 150000.0,
      "active": true
    },
    {
      "id": 2,
      "name": "Giờ trưa",
      "startTime": "10:00:00",
      "endTime": "17:00:00",
      "pricePerHour": 100000.0,
      "active": true
    },
    {
      "id": 3,
      "name": "Giờ cao điểm tối",
      "startTime": "17:00:00",
      "endTime": "22:00:00",
      "pricePerHour": 200000.0,
      "active": true
    }
  ],
  "message": "Lấy danh sách khung giá thành công",
  "timestamp": "2024-11-15T10:30:00Z"
}
```

### Lưu ý về giá
- **pricePerHour**: Giá theo giờ (VND)
- Hệ thống tính giá theo **khối 30 phút**
- Giá 30 phút = pricePerHour / 2
- Ví dụ: Đặt từ 08:00-10:00 (2 giờ) với giá 150,000/giờ = **300,000 VND**
- Ví dụ: Đặt từ 08:00-08:30 (30 phút) với giá 150,000/giờ = **75,000 VND**

---

## 3. TẠO BOOKING MỚI

### Endpoint
```
POST /api/bookings
```

### Mô tả
Tạo một booking mới. Hệ thống sẽ:
1. Kiểm tra sân có trống không
2. Tính giá dựa trên khung giờ
3. Tạo booking với trạng thái **PENDING_PAYMENT**
4. Trả về thông tin tài khoản ngân hàng của chủ sân để chuyển khoản
5. Booking sẽ **tự động hủy sau 5 phút** nếu không upload chứng minh thanh toán

### Headers
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

### Request Body
```json
{
  "venueId": 1,
  "courtId": 3,
  "startTime": "2024-11-15T08:00:00",
  "endTime": "2024-11-15T10:00:00"
}
```

### Request Body Schema
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| venueId | Long | Yes | ID của khu sân (venues) |
| courtId | Long | Yes | ID của sân cụ thể trong venues |
| startTime | ISO DateTime | Yes | Thời gian bắt đầu (phải là bội số của 30 phút) |
| endTime | ISO DateTime | Yes | Thời gian kết thúc (phải là bội số của 30 phút) |

### Response Success (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 123,
    "userId": 45,
    "userName": "Nguyễn Văn A",
    "courtId": 3,
    "courtName": "Sân số 3",
    "venuesName": "Sân Bóng Đá Mini ABC",
    "startTime": "2024-11-15T08:00:00",
    "endTime": "2024-11-15T10:00:00",
    "totalPrice": 300000.0,
    "status": "PENDING_PAYMENT",
    "expireTime": "2024-11-15T08:05:00",
    "paymentProofUploaded": false,
    "paymentProofUrl": null,
    "paymentProofUploadedAt": null,
    "rejectionReason": null,
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN OWNER",
      "ownerName": "Nguyễn Văn Owner"
    }
  },
  "message": "Đặt sân thành công. Vui lòng chuyển khoản trong 5 phút.",
  "timestamp": "2024-11-15T08:00:00Z"
}
```

### Response Error - Sân đã được đặt
```json
{
  "success": false,
  "data": null,
  "message": "Sân đã được đặt trong khung giờ này. Vui lòng chọn khung giờ khác.",
  "timestamp": "2024-11-15T08:00:00Z"
}
```

### Response Error - Thời gian không hợp lệ
```json
{
  "success": false,
  "data": null,
  "message": "Thời gian đặt phải là bội số của 30 phút",
  "timestamp": "2024-11-15T08:00:00Z"
}
```

---

## 4. UPLOAD CHỨNG MINH CHUYỂN KHOẢN

### Endpoint
```
PUT /api/bookings/{bookingId}/confirm-payment
```

### Mô tả
Sau khi chuyển khoản, người dùng upload ảnh chứng minh đã chuyển khoản. Booking chuyển sang trạng thái **PAYMENT_UPLOADED** và chờ chủ sân xác nhận.

### Headers
```
Authorization: Bearer {JWT_TOKEN}
Content-Type: application/json
```

### Request Body
```json
{
  "paymentProofUrl": "https://example.com/uploads/payment-proofs/payment_123.jpg"
}
```

### Request Body Schema
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| paymentProofUrl | String | Yes | URL của ảnh chứng minh chuyển khoản (đã upload lên server) |

### Response Success (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 123,
    "userId": 45,
    "userName": "Nguyễn Văn A",
    "courtId": 3,
    "courtName": "Sân số 3",
    "venuesName": "Sân Bóng Đá Mini ABC",
    "startTime": "2024-11-15T08:00:00",
    "endTime": "2024-11-15T10:00:00",
    "totalPrice": 300000.0,
    "status": "PAYMENT_UPLOADED",
    "expireTime": "2024-11-15T08:05:00",
    "paymentProofUploaded": true,
    "paymentProofUrl": "https://example.com/uploads/payment-proofs/payment_123.jpg",
    "paymentProofUploadedAt": "2024-11-15T08:02:00",
    "rejectionReason": null,
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN OWNER",
      "ownerName": "Nguyễn Văn Owner"
    }
  },
  "message": "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận.",
  "timestamp": "2024-11-15T08:02:00Z"
}
```

### Lưu ý
- Phải upload ảnh lên server trước (sử dụng API upload file riêng)
- Chỉ có thể upload khi booking ở trạng thái **PENDING_PAYMENT**

---

## 5. XEM DANH SÁCH BOOKING CỦA TÔI

### Endpoint
```
GET /api/bookings/my-bookings
```

### Mô tả
Lấy tất cả các booking của người dùng hiện tại.

### Headers
```
Authorization: Bearer {JWT_TOKEN}
```

### Request Example
```http
GET /api/bookings/my-bookings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response Success (200 OK)
```json
{
  "success": true,
  "data": [
    {
      "id": 123,
      "userId": 45,
      "userName": "Nguyễn Văn A",
      "courtId": 3,
      "courtName": "Sân số 3",
      "venuesName": "Sân Bóng Đá Mini ABC",
      "startTime": "2024-11-15T08:00:00",
      "endTime": "2024-11-15T10:00:00",
      "totalPrice": 300000.0,
      "status": "CONFIRMED",
      "expireTime": "2024-11-15T08:05:00",
      "paymentProofUploaded": true,
      "paymentProofUrl": "https://example.com/uploads/payment-proofs/payment_123.jpg",
      "paymentProofUploadedAt": "2024-11-15T08:02:00",
      "rejectionReason": null,
      "ownerBankInfo": {
        "bankName": "Vietcombank",
        "bankAccountNumber": "1234567890",
        "bankAccountName": "NGUYEN VAN OWNER",
        "ownerName": "Nguyễn Văn Owner"
      }
    },
    {
      "id": 124,
      "userId": 45,
      "userName": "Nguyễn Văn A",
      "courtId": 5,
      "courtName": "Sân số 5",
      "venuesName": "Sân Bóng Đá Mini XYZ",
      "startTime": "2024-11-16T17:00:00",
      "endTime": "2024-11-16T18:30:00",
      "totalPrice": 300000.0,
      "status": "PENDING_PAYMENT",
      "expireTime": "2024-11-16T17:05:00",
      "paymentProofUploaded": false,
      "paymentProofUrl": null,
      "paymentProofUploadedAt": null,
      "rejectionReason": null,
      "ownerBankInfo": {
        "bankName": "Techcombank",
        "bankAccountNumber": "9876543210",
        "bankAccountName": "TRAN THI OWNER",
        "ownerName": "Trần Thị Owner"
      }
    }
  ],
  "message": "My bookings retrieved successfully",
  "timestamp": "2024-11-15T10:30:00Z"
}
```

---

## 6. XEM CHI TIẾT MỘT BOOKING

### Endpoint
```
GET /api/bookings/{bookingId}
```

### Mô tả
Lấy thông tin chi tiết của một booking cụ thể.

### Headers
```
Authorization: Bearer {JWT_TOKEN}
```

### Request Example
```http
GET /api/bookings/123
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response Success (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 123,
    "userId": 45,
    "userName": "Nguyễn Văn A",
    "courtId": 3,
    "courtName": "Sân số 3",
    "venuesName": "Sân Bóng Đá Mini ABC",
    "startTime": "2024-11-15T08:00:00",
    "endTime": "2024-11-15T10:00:00",
    "totalPrice": 300000.0,
    "status": "CONFIRMED",
    "expireTime": "2024-11-15T08:05:00",
    "paymentProofUploaded": true,
    "paymentProofUrl": "https://example.com/uploads/payment-proofs/payment_123.jpg",
    "paymentProofUploadedAt": "2024-11-15T08:02:00",
    "rejectionReason": null,
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN OWNER",
      "ownerName": "Nguyễn Văn Owner"
    }
  },
  "message": "Booking retrieved successfully",
  "timestamp": "2024-11-15T10:30:00Z"
}
```

---

## 7. HUỶ BOOKING

### Endpoint
```
PUT /api/bookings/{bookingId}/cancel
```

### Mô tả
Người dùng hủy booking của mình (chỉ hủy được khi chưa được xác nhận).

### Headers
```
Authorization: Bearer {JWT_TOKEN}
```

### Request Example
```http
PUT /api/bookings/123/cancel
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Response Success (200 OK)
```json
{
  "success": true,
  "data": {
    "id": 123,
    "userId": 45,
    "userName": "Nguyễn Văn A",
    "courtId": 3,
    "courtName": "Sân số 3",
    "venuesName": "Sân Bóng Đá Mini ABC",
    "startTime": "2024-11-15T08:00:00",
    "endTime": "2024-11-15T10:00:00",
    "totalPrice": 300000.0,
    "status": "CANCELLED",
    "expireTime": "2024-11-15T08:05:00",
    "paymentProofUploaded": false,
    "paymentProofUrl": null,
    "paymentProofUploadedAt": null,
    "rejectionReason": null,
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN OWNER",
      "ownerName": "Nguyễn Văn Owner"
    }
  },
  "message": "Booking cancelled successfully",
  "timestamp": "2024-11-15T10:30:00Z"
}
```

---

## 8. TRẠNG THÁI BOOKING (BookingStatus)

| Status | Mô tả | Người dùng có thể làm gì |
|--------|-------|--------------------------|
| **PENDING_PAYMENT** | Chờ chuyển khoản (mới tạo) | Upload chứng minh thanh toán hoặc hủy |
| **PAYMENT_UPLOADED** | Đã upload chứng minh chuyển khoản | Chờ chủ sân xác nhận |
| **CONFIRMED** | Chủ sân đã xác nhận | Không thể hủy (liên hệ chủ sân) |
| **REJECTED** | Chủ sân từ chối | Xem lý do từ chối |
| **CANCELLED** | Người dùng đã hủy | Không thể thao tác |
| **EXPIRED** | Hết hạn thanh toán (quá 5 phút) | Không thể thao tác |
| **COMPLETED** | Hoàn thành | Có thể đánh giá |

---

## 9. QUY TRÌNH BOOKING HOÀN CHỈNH

### Bước 1: Chọn sân và xem giá
1. Frontend hiển thị danh sách venues và courts
2. Gọi API `GET /api/venues/{venueId}/price-rules` để hiển thị bảng giá
3. Người dùng chọn ngày giờ muốn đặt

### Bước 2: Kiểm tra tính khả dụng
1. Gọi API `GET /api/courts/{courtId}/availability?startTime=...&endTime=...`
2. Nếu `available = false`, hiển thị thông báo sân đã được đặt
3. Nếu `available = true`, cho phép đặt sân

### Bước 3: Tạo booking
1. Gọi API `POST /api/bookings` với thông tin đã chọn
2. Backend tự động tính giá dựa trên khung giờ
3. Nhận response với `totalPrice` đã được tính
4. Hiển thị thông tin tài khoản ngân hàng (`ownerBankInfo`)
5. **Hiển thị đếm ngược 5 phút** dựa trên `expireTime`

### Bước 4: Chuyển khoản và upload chứng minh
1. Người dùng chuyển khoản theo thông tin `ownerBankInfo`
2. Upload ảnh chứng minh lên server (API riêng)
3. Gọi API `PUT /api/bookings/{id}/confirm-payment` với URL ảnh
4. Booking chuyển sang trạng thái `PAYMENT_UPLOADED`

### Bước 5: Chờ xác nhận
1. Chủ sân kiểm tra và xác nhận hoặc từ chối
2. Nếu được xác nhận: `status = CONFIRMED`
3. Nếu bị từ chối: `status = REJECTED` (xem `rejectionReason`)

---

## 10. TÍNH GIÁ ĐỘNG THEO KHUNG GIỜ

### Cơ chế tính giá
Hệ thống tính giá dựa trên **từng khối 30 phút** trong khoảng thời gian đặt sân.

### Ví dụ 1: Đặt trong cùng một khung giờ
```
Khung giờ: 06:00-10:00, giá 150,000 VND/giờ
Đặt từ: 08:00 đến 10:00 (2 giờ = 4 khối 30 phút)

Tính:
- 08:00-08:30: 150,000 / 2 = 75,000 VND
- 08:30-09:00: 150,000 / 2 = 75,000 VND
- 09:00-09:30: 150,000 / 2 = 75,000 VND
- 09:30-10:00: 150,000 / 2 = 75,000 VND

Tổng: 300,000 VND
```

### Ví dụ 2: Đặt qua nhiều khung giờ
```
Khung giờ 1: 06:00-10:00, giá 150,000 VND/giờ
Khung giờ 2: 10:00-17:00, giá 100,000 VND/giờ
Đặt từ: 09:00 đến 11:00 (2 giờ)

Tính:
- 09:00-09:30: 150,000 / 2 = 75,000 VND (khung 1)
- 09:30-10:00: 150,000 / 2 = 75,000 VND (khung 1)
- 10:00-10:30: 100,000 / 2 = 50,000 VND (khung 2)
- 10:30-11:00: 100,000 / 2 = 50,000 VND (khung 2)

Tổng: 250,000 VND
```

### Ví dụ 3: Giờ cao điểm
```
Khung giờ: 17:00-22:00, giá 200,000 VND/giờ (giờ cao điểm)
Đặt từ: 18:00 đến 19:30 (1.5 giờ)

Tính:
- 18:00-18:30: 200,000 / 2 = 100,000 VND
- 18:30-19:00: 200,000 / 2 = 100,000 VND
- 19:00-19:30: 200,000 / 2 = 100,000 VND

Tổng: 300,000 VND
```

---

## 11. LƯU Ý QUAN TRỌNG CHO FRONTEND

### Validation thời gian
- Thời gian đặt phải là **bội số của 30 phút**
- Ví dụ hợp lệ: 08:00, 08:30, 09:00, 09:30...
- Ví dụ không hợp lệ: 08:15, 08:45, 09:20...

### Hiển thị giá trước khi đặt
- Frontend NÊN tính giá trước và hiển thị cho người dùng
- Dùng bảng giá từ API `/api/venues/{venueId}/price-rules`
- Tính theo công thức trên để người dùng biết trước giá

### Đếm ngược thời gian thanh toán
- Hiển thị đếm ngược dựa trên field `expireTime`
- Khi hết thời gian, booking tự động chuyển sang `EXPIRED`
- Khuyến khích người dùng upload chứng minh ngay

### Xử lý lỗi
- Luôn kiểm tra field `success` trong response
- Hiển thị `message` từ backend cho người dùng
- Xử lý trường hợp sân đã được đặt bởi người khác

### Real-time updates (Khuyến nghị)
- Sử dụng WebSocket hoặc polling để cập nhật trạng thái booking
- Thông báo khi booking được chủ sân xác nhận/từ chối
- Cảnh báo khi sắp hết hạn thanh toán

---

## 12. MÃ LỖI THƯỜNG GẶP

| Status Code | Message | Giải pháp |
|-------------|---------|-----------|
| 400 | Thời gian đặt phải là bội số của 30 phút | Kiểm tra lại startTime và endTime |
| 400 | Sân đã được đặt trong khung giờ này | Chọn khung giờ khác hoặc sân khác |
| 401 | Unauthorized | Token hết hạn, yêu cầu đăng nhập lại |
| 403 | Bạn không có quyền confirm booking này | Người dùng cố xác nhận booking của người khác |
| 404 | Court not found | ID sân không tồn tại |
| 404 | Booking not found | ID booking không tồn tại |
| 500 | Internal Server Error | Lỗi server, thử lại sau |

---

## 13. DEMO FLOW CODE FRONTEND (React/Vue)

### Bước 1: Hiển thị bảng giá
```javascript
// Lấy bảng giá
const fetchPriceRules = async (venueId) => {
  const response = await fetch(`/api/venues/${venueId}/price-rules`, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  const result = await response.json();
  
  if (result.success) {
    setPriceRules(result.data);
    // Hiển thị bảng giá cho người dùng
  }
};
```

### Bước 2: Tính giá trước khi đặt (Frontend)
```javascript
const calculateEstimatedPrice = (startTime, endTime, priceRules) => {
  let total = 0;
  let current = new Date(startTime);
  const end = new Date(endTime);
  
  while (current < end) {
    const timeOfDay = current.toTimeString().substring(0, 5); // "HH:MM"
    
    // Tìm khung giá áp dụng
    const rule = priceRules.find(r => 
      timeOfDay >= r.startTime && timeOfDay < r.endTime
    );
    
    if (rule) {
      total += rule.pricePerHour / 2; // Giá cho 30 phút
    }
    
    // Cộng 30 phút
    current = new Date(current.getTime() + 30 * 60 * 1000);
  }
  
  return total;
};
```

### Bước 3: Kiểm tra và đặt sân
```javascript
const bookCourt = async (courtId, venueId, startTime, endTime) => {
  // 1. Kiểm tra tính khả dụng
  const availResponse = await fetch(
    `/api/courts/${courtId}/availability?startTime=${startTime}&endTime=${endTime}`,
    {
      headers: { 'Authorization': `Bearer ${token}` }
    }
  );
  const availData = await availResponse.json();
  
  if (!availData.available) {
    alert('Sân đã được đặt trong khung giờ này!');
    return;
  }
  
  // 2. Tạo booking
  const bookResponse = await fetch('/api/bookings', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      venueId,
      courtId,
      startTime,
      endTime
    })
  });
  
  const bookResult = await bookResponse.json();
  
  if (bookResult.success) {
    const booking = bookResult.data;
    
    // Hiển thị thông tin thanh toán
    showPaymentInfo(booking.ownerBankInfo, booking.totalPrice);
    
    // Bắt đầu đếm ngược
    startCountdown(booking.expireTime);
    
    // Lưu bookingId để upload chứng minh sau
    setCurrentBookingId(booking.id);
  }
};
```

### Bước 4: Upload chứng minh thanh toán
```javascript
const uploadPaymentProof = async (bookingId, imageFile) => {
  // 1. Upload ảnh lên server
  const formData = new FormData();
  formData.append('file', imageFile);
  
  const uploadResponse = await fetch('/api/files/upload', {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` },
    body: formData
  });
  const uploadResult = await uploadResponse.json();
  const imageUrl = uploadResult.data.url;
  
  // 2. Confirm payment với URL ảnh
  const confirmResponse = await fetch(`/api/bookings/${bookingId}/confirm-payment`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      paymentProofUrl: imageUrl
    })
  });
  
  const confirmResult = await confirmResponse.json();
  
  if (confirmResult.success) {
    alert('Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận.');
    // Chuyển sang màn hình theo dõi trạng thái
  }
};
```

---

## 14. LUỒNG HIỂN THỊ THÔNG TIN NGÂN HÀNG CHỦ SÂN

### 📌 TỔNG QUAN LUỒNG

#### Luồng API để lấy và hiển thị thông tin ngân hàng:
1. **GET** `/api/venues/{venueId}/price-rules` - Xem bảng giá (tùy chọn, để hiển thị trước)
2. **GET** `/api/courts/{courtId}/availability?startTime=...&endTime=...` - Kiểm tra sân trống
3. **POST** `/api/bookings` - Tạo booking → **Response trả về `ownerBankInfo`** ⭐
4. Hiển thị thông tin ngân hàng ngay lập tức trên UI
5. Người dùng chuyển khoản
6. **POST** `/api/files/upload` - Upload ảnh chứng minh
7. **PUT** `/api/bookings/{id}/confirm-payment` - Xác nhận đã thanh toán

#### Fallback (nếu cần lấy lại thông tin):
- **GET** `/api/bookings/{bookingId}` - Lấy chi tiết booking → có `ownerBankInfo`
- **GET** `/api/bookings/my-bookings` - Lấy danh sách booking → mỗi booking có `ownerBankInfo`

---

### 📋 CẤU TRÚC `ownerBankInfo`

```json
{
  "bankName": "Vietcombank",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "NGUYEN VAN OWNER",
  "ownerName": "Nguyễn Văn Owner"
}
```

| Field | Type | Description |
|-------|------|-------------|
| bankName | String | Tên ngân hàng (VD: Vietcombank, Techcombank, BIDV) |
| bankAccountNumber | String | Số tài khoản ngân hàng (STK) |
| bankAccountName | String | Tên tài khoản ngân hàng (IN HOA, không dấu) |
| ownerName | String | Tên đầy đủ của chủ sân (có dấu, dễ đọc) |

---

### 🔄 LUỒNG CHI TIẾT - STEP BY STEP

#### BƯỚC 1: Kiểm tra availability (GET)
```javascript
const checkAvailability = async (courtId, startTime, endTime, token) => {
  try {
    const response = await fetch(
      `/api/courts/${courtId}/availability?startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`,
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );
    
    const data = await response.json();
    
    if (!data.available) {
      throw new Error('Sân đã được đặt trong khung giờ này');
    }
    
    return data; // { courtId, available: true, bookedSlots: [] }
  } catch (error) {
    console.error('Error checking availability:', error);
    throw error;
  }
};
```

**Request:**
```http
GET /api/courts/3/availability?startTime=2024-11-15T08:00:00&endTime=2024-11-15T10:00:00
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response Success:**
```json
{
  "courtId": 3,
  "available": true,
  "bookedSlots": []
}
```

---

#### BƯỚC 2: Tạo booking (POST) - NHẬN `ownerBankInfo`
```javascript
const createBooking = async (venueId, courtId, startTime, endTime, token) => {
  try {
    const response = await fetch('/api/bookings', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        venueId,
        courtId,
        startTime,
        endTime
      })
    });
    
    const result = await response.json();
    
    if (!result.success) {
      throw new Error(result.message || 'Không thể tạo booking');
    }
    
    return result.data; // Booking object có ownerBankInfo
  } catch (error) {
    console.error('Error creating booking:', error);
    throw error;
  }
};
```

**Request:**
```http
POST /api/bookings
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "venueId": 1,
  "courtId": 3,
  "startTime": "2024-11-15T08:00:00",
  "endTime": "2024-11-15T10:00:00"
}
```

**Response Success (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "userId": 45,
    "userName": "Nguyễn Văn A",
    "courtId": 3,
    "courtName": "Sân số 3",
    "venuesName": "Sân Bóng Đá Mini ABC",
    "startTime": "2024-11-15T08:00:00",
    "endTime": "2024-11-15T10:00:00",
    "totalPrice": 300000.0,
    "status": "PENDING_PAYMENT",
    "expireTime": "2024-11-15T08:05:00",
    "ownerBankInfo": {
      "bankName": "Vietcombank",
      "bankAccountNumber": "1234567890",
      "bankAccountName": "NGUYEN VAN OWNER",
      "ownerName": "Nguyễn Văn Owner"
    }
  },
  "message": "Đặt sân thành công. Vui lòng chuyển khoản trong 5 phút.",
  "timestamp": "2024-11-15T08:00:00Z"
}
```

---

#### BƯỚC 3: Hiển thị thông tin ngân hàng ngay lập tức

```javascript
const displayBankInfo = (booking) => {
  const { ownerBankInfo, totalPrice, expireTime } = booking;
  
  // Kiểm tra ownerBankInfo có tồn tại không
  if (!ownerBankInfo) {
    console.error('Không có thông tin ngân hàng');
    return;
  }
  
  // Hiển thị thông tin
  console.log('=== THÔNG TIN CHUYỂN KHOẢN ===');
  console.log('Ngân hàng:', ownerBankInfo.bankName);
  console.log('Số tài khoản:', ownerBankInfo.bankAccountNumber);
  console.log('Tên tài khoản:', ownerBankInfo.bankAccountName);
  console.log('Chủ sân:', ownerBankInfo.ownerName);
  console.log('Số tiền:', totalPrice.toLocaleString('vi-VN'), 'VND');
  console.log('Hết hạn lúc:', new Date(expireTime).toLocaleString('vi-VN'));
  
  // Return object để render UI
  return {
    bankName: ownerBankInfo.bankName,
    accountNumber: ownerBankInfo.bankAccountNumber,
    accountName: ownerBankInfo.bankAccountName,
    ownerName: ownerBankInfo.ownerName,
    amount: totalPrice,
    expireTime: expireTime
  };
};
```

---

#### BƯỚC 4: Upload chứng minh chuyển khoản

```javascript
const uploadPaymentProof = async (bookingId, imageFile, token) => {
  try {
    // 1. Upload file
    const formData = new FormData();
    formData.append('file', imageFile);
    
    const uploadResponse = await fetch('/api/files/upload', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });
    
    const uploadResult = await uploadResponse.json();
    
    if (!uploadResult.success) {
      throw new Error(uploadResult.message || 'Upload file thất bại');
    }
    
    const imageUrl = uploadResult.data.url;
    
    // 2. Confirm payment
    const confirmResponse = await fetch(`/api/bookings/${bookingId}/confirm-payment`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        paymentProofUrl: imageUrl
      })
    });
    
    const confirmResult = await confirmResponse.json();
    
    if (!confirmResult.success) {
      throw new Error(confirmResult.message || 'Xác nhận thanh toán thất bại');
    }
    
    return confirmResult.data;
  } catch (error) {
    console.error('Error uploading payment proof:', error);
    throw error;
  }
};
```

---

### 🎨 COMPONENT REACT - FULL EXAMPLE

```jsx
import React, { useState, useEffect } from 'react';

const BookingPayment = ({ venueId, courtId, startTime, endTime, token }) => {
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [countdown, setCountdown] = useState(0);
  const [selectedFile, setSelectedFile] = useState(null);
  const [showFullAccount, setShowFullAccount] = useState(false);

  // Hàm tạo booking
  const handleCreateBooking = async () => {
    setLoading(true);
    setError(null);
    
    try {
      // 1. Kiểm tra availability
      const availResponse = await fetch(
        `/api/courts/${courtId}/availability?startTime=${encodeURIComponent(startTime)}&endTime=${encodeURIComponent(endTime)}`,
        {
          headers: { 'Authorization': `Bearer ${token}` }
        }
      );
      const availData = await availResponse.json();
      
      if (!availData.available) {
        throw new Error('Sân đã được đặt trong khung giờ này');
      }
      
      // 2. Tạo booking
      const bookResponse = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          venueId,
          courtId,
          startTime,
          endTime
        })
      });
      
      const bookResult = await bookResponse.json();
      
      if (!bookResult.success) {
        throw new Error(bookResult.message || 'Không thể tạo booking');
      }
      
      // 3. Lưu booking và hiển thị thông tin bank
      setBooking(bookResult.data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Countdown timer
  useEffect(() => {
    if (!booking) return;
    
    const calculateCountdown = () => {
      const now = new Date().getTime();
      const expire = new Date(booking.expireTime).getTime();
      const diff = Math.floor((expire - now) / 1000);
      setCountdown(diff > 0 ? diff : 0);
    };
    
    calculateCountdown();
    const interval = setInterval(calculateCountdown, 1000);
    
    return () => clearInterval(interval);
  }, [booking]);

  // Format số tài khoản (mask)
  const maskAccountNumber = (number) => {
    if (!number) return '';
    if (showFullAccount) return number;
    const len = number.length;
    return number.slice(0, 4) + '****' + number.slice(-3);
  };

  // Format countdown
  const formatCountdown = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  // Upload payment proof
  const handleUploadProof = async () => {
    if (!selectedFile) {
      alert('Vui lòng chọn ảnh chứng minh chuyển khoản');
      return;
    }
    
    setLoading(true);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);
      
      const uploadResponse = await fetch('/api/files/upload', {
        method: 'POST',
        headers: { 'Authorization': `Bearer ${token}` },
        body: formData
      });
      const uploadResult = await uploadResponse.json();
      
      if (!uploadResult.success) {
        throw new Error('Upload ảnh thất bại');
      }
      
      const confirmResponse = await fetch(`/api/bookings/${booking.id}/confirm-payment`, {
        method: 'PUT',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          paymentProofUrl: uploadResult.data.url
        })
      });
      
      const confirmResult = await confirmResponse.json();
      
      if (confirmResult.success) {
        alert('✅ Đã gửi chứng minh thanh toán. Chờ chủ sân xác nhận.');
        setBooking(confirmResult.data);
      }
    } catch (err) {
      alert('❌ ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Render
  if (!booking) {
    return (
      <div className="booking-form">
        <h2>Đặt Sân</h2>
        <button onClick={handleCreateBooking} disabled={loading}>
          {loading ? 'Đang xử lý...' : 'Xác nhận đặt sân'}
        </button>
        {error && <div className="error">{error}</div>}
      </div>
    );
  }

  const { ownerBankInfo, totalPrice, status } = booking;

  return (
    <div className="payment-info">
      <h2>🎉 Đặt Sân Thành Công!</h2>
      
      {/* Countdown */}
      {status === 'PENDING_PAYMENT' && countdown > 0 && (
        <div className="countdown-warning">
          ⏰ Vui lòng chuyển khoản trong: <strong>{formatCountdown(countdown)}</strong>
        </div>
      )}
      
      {/* Thông tin ngân hàng */}
      <div className="bank-info-card">
        <h3>💳 Thông Tin Chuyển Khoản</h3>
        
        <div className="info-row">
          <span className="label">Ngân hàng:</span>
          <strong>{ownerBankInfo.bankName}</strong>
        </div>
        
        <div className="info-row">
          <span className="label">Số tài khoản:</span>
          <div>
            <strong>{maskAccountNumber(ownerBankInfo.bankAccountNumber)}</strong>
            <button 
              onClick={() => setShowFullAccount(!showFullAccount)}
              className="toggle-btn"
            >
              {showFullAccount ? '🙈 Ẩn' : '👁 Hiện'}
            </button>
            <button 
              onClick={() => navigator.clipboard.writeText(ownerBankInfo.bankAccountNumber)}
              className="copy-btn"
            >
              📋 Copy
            </button>
          </div>
        </div>
        
        <div className="info-row">
          <span className="label">Tên tài khoản:</span>
          <strong>{ownerBankInfo.bankAccountName}</strong>
        </div>
        
        <div className="info-row">
          <span className="label">Chủ sân:</span>
          <strong>{ownerBankInfo.ownerName}</strong>
        </div>
        
        <div className="info-row total">
          <span className="label">Số tiền:</span>
          <strong className="amount">
            {totalPrice.toLocaleString('vi-VN')} VND
          </strong>
        </div>
        
        <div className="transfer-note">
          <p>📝 <strong>Nội dung chuyển khoản:</strong></p>
          <code>DATSANBONG {booking.id}</code>
          <button 
            onClick={() => navigator.clipboard.writeText(`DATSANBONG ${booking.id}`)}
            className="copy-btn"
          >
            📋 Copy
          </button>
        </div>
      </div>
      
      {/* Upload chứng minh */}
      {status === 'PENDING_PAYMENT' && (
        <div className="upload-section">
          <h3>📸 Upload Chứng Minh Chuyển Khoản</h3>
          <input 
            type="file" 
            accept="image/*"
            onChange={(e) => setSelectedFile(e.target.files[0])}
          />
          <button 
            onClick={handleUploadProof}
            disabled={!selectedFile || loading}
          >
            {loading ? 'Đang upload...' : 'Gửi chứng minh'}
          </button>
        </div>
      )}
      
      {status === 'PAYMENT_UPLOADED' && (
        <div className="status-pending">
          ⏳ Đã gửi chứng minh. Chờ chủ sân xác nhận...
        </div>
      )}
      
      {status === 'CONFIRMED' && (
        <div className="status-confirmed">
          ✅ Booking đã được xác nhận! Chúc bạn chơi vui vẻ!
        </div>
      )}
    </div>
  );
};

export default BookingPayment;
```

---

### 🛡️ XỬ LÝ LỖI VÀ EDGE CASES

#### 1. Xử lý khi không có `ownerBankInfo`
```javascript
const getBookingWithBankInfo = async (bookingId, token) => {
  try {
    // Fallback: Gọi GET /api/bookings/{id} để lấy lại thông tin
    const response = await fetch(`/api/bookings/${bookingId}`, {
      headers: { 'Authorization': `Bearer ${token}` }
    });
    
    const result = await response.json();
    
    if (!result.success) {
      throw new Error('Không thể lấy thông tin booking');
    }
    
    if (!result.data.ownerBankInfo) {
      throw new Error('Không có thông tin ngân hàng chủ sân');
    }
    
    return result.data;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
```

#### 2. Xử lý token expired (401)
```javascript
const handleApiCall = async (url, options) => {
  try {
    const response = await fetch(url, options);
    
    if (response.status === 401) {
      // Token expired -> redirect to login
      alert('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.');
      window.location.href = '/login';
      return;
    }
    
    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
};
```

#### 3. Xử lý sân đã được đặt (race condition)
```javascript
const createBookingWithRetry = async (venueId, courtId, startTime, endTime, token) => {
  try {
    const response = await fetch('/api/bookings', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ venueId, courtId, startTime, endTime })
    });
    
    const result = await response.json();
    
    if (!result.success && result.message.includes('đã được đặt')) {
      // Sân bị người khác đặt trong lúc đó
      alert('❌ Sân đã được đặt. Vui lòng chọn khung giờ khác.');
      return null;
    }
    
    return result.data;
  } catch (error) {
    console.error('Error:', error);
    throw error;
  }
};
```

#### 4. Validate thời gian trước khi gọi API
```javascript
const validateBookingTime = (startTime, endTime) => {
  const start = new Date(startTime);
  const end = new Date(endTime);
  
  // Kiểm tra thời gian phải là bội số 30 phút
  const startMinutes = start.getMinutes();
  const endMinutes = end.getMinutes();
  
  if (startMinutes !== 0 && startMinutes !== 30) {
    throw new Error('Thời gian bắt đầu phải là :00 hoặc :30');
  }
  
  if (endMinutes !== 0 && endMinutes !== 30) {
    throw new Error('Thời gian kết thúc phải là :00 hoặc :30');
  }
  
  // Kiểm tra endTime > startTime
  if (end <= start) {
    throw new Error('Thời gian kết thúc phải sau thời gian bắt đầu');
  }
  
  return true;
};
```

---

### 💡 GỢI Ý UI/UX

#### 1. Mask số tài khoản để bảo mật
```javascript
// Hiển thị: 1234****890
const maskAccount = (account) => {
  if (!account || account.length < 7) return account;
  return account.slice(0, 4) + '****' + account.slice(-3);
};

// Nút "Hiện số tài khoản đầy đủ" khi người dùng click
```

#### 2. Copy to clipboard
```javascript
const copyToClipboard = (text) => {
  navigator.clipboard.writeText(text)
    .then(() => alert('✅ Đã copy!'))
    .catch(err => console.error('Copy failed:', err));
};
```

#### 3. QR Code thanh toán (nếu có)
```javascript
// Sử dụng thư viện như qrcode.react
import QRCode from 'qrcode.react';

const PaymentQR = ({ bankInfo, amount, bookingId }) => {
  // Format: bank|account|amount|content
  const qrValue = `${bankInfo.bankName}|${bankInfo.bankAccountNumber}|${amount}|DATSANBONG ${bookingId}`;
  
  return <QRCode value={qrValue} size={200} />;
};
```

#### 4. Countdown với cảnh báo màu
```css
.countdown-warning {
  padding: 12px;
  border-radius: 8px;
  text-align: center;
  font-size: 18px;
}

.countdown-warning.danger {
  background-color: #fee;
  color: #c00;
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  25%, 75% { opacity: 0.5; }
}
```

---

### 📱 RESPONSIVE DESIGN

```css
/* Mobile first */
.bank-info-card {
  background: #f9f9f9;
  border: 1px solid #ddd;
  border-radius: 12px;
  padding: 20px;
  margin: 20px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
}

.info-row .label {
  color: #666;
  font-size: 14px;
}

.info-row strong {
  font-size: 16px;
  color: #333;
}

.info-row.total {
  border-bottom: none;
  margin-top: 10px;
  padding-top: 20px;
  border-top: 2px solid #333;
}

.amount {
  color: #e53935;
  font-size: 24px !important;
}

.copy-btn {
  margin-left: 8px;
  padding: 4px 8px;
  font-size: 12px;
  background: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.copy-btn:hover {
  background: #45a049;
}
```

---

### 🔔 NOTIFICATION KHI STATUS THAY ĐỔI

```javascript
// Polling để check status
const pollBookingStatus = async (bookingId, token, onStatusChange) => {
  const interval = setInterval(async () => {
    try {
      const response = await fetch(`/api/bookings/${bookingId}`, {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      
      const result = await response.json();
      
      if (result.success) {
        onStatusChange(result.data.status);
        
        // Dừng polling khi status là CONFIRMED hoặc REJECTED
        if (['CONFIRMED', 'REJECTED', 'CANCELLED', 'EXPIRED'].includes(result.data.status)) {
          clearInterval(interval);
        }
      }
    } catch (error) {
      console.error('Polling error:', error);
    }
  }, 5000); // Check mỗi 5 giây
  
  return interval;
};

// Sử dụng
const intervalId = pollBookingStatus(bookingId, token, (status) => {
  if (status === 'CONFIRMED') {
    showNotification('✅ Booking đã được xác nhận!');
  } else if (status === 'REJECTED') {
    showNotification('❌ Booking bị từ chối. Vui lòng kiểm tra lý do.');
  }
});

// Cleanup khi component unmount
useEffect(() => {
  return () => clearInterval(intervalId);
}, [intervalId]);
```

---

### ✅ CHECKLIST CHO FRONTEND

- [ ] Validate thời gian (bội số 30 phút) trước khi gọi API
- [ ] Kiểm tra availability trước khi tạo booking
- [ ] Hiển thị thông tin ngân hàng ngay sau khi POST thành công
- [ ] Implement countdown timer dựa trên `expireTime`
- [ ] Mask số tài khoản, có nút "Hiện/Ẩn"
- [ ] Nút copy số tài khoản và nội dung chuyển khoản
- [ ] Xử lý lỗi 401 (token expired) → redirect login
- [ ] Xử lý lỗi 400 (sân đã được đặt) → cho chọn lại
- [ ] Upload file và confirm payment
- [ ] Polling hoặc WebSocket để update status real-time
- [ ] Hiển thị notification khi status thay đổi
- [ ] Responsive design cho mobile
- [ ] Loading state cho các API call
- [ ] Error handling và hiển thị message từ backend

---

## KẾT LUẬN

Tài liệu này cung cấp đầy đủ thông tin về API Booking bao gồm:
- ✅ Chọn ngày giờ và sân
- ✅ Kiểm tra tính khả dụng
- ✅ Tính giá động theo khung giờ (30 phút)
- ✅ Quy trình thanh toán hoàn chỉnh
- ✅ **Luồng GET/POST để hiển thị thông tin ngân hàng chủ sân** ⭐
- ✅ JSON request/response chi tiết
- ✅ Ví dụ code frontend (React component đầy đủ)
- ✅ Xử lý lỗi và edge cases
- ✅ UI/UX best practices

Nếu có thắc mắc, vui lòng liên hệ team Backend.
