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
5. [Price Rules APIs](#price-rules-apis)
6. [Booking APIs](#booking-apis)
7. [Notification APIs](#notification-apis)
8. [Review APIs](#review-apis)
9. [File APIs](#file-apis)
10. [Address APIs](#address-apis)

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
  "timestamp": "2025-10-28T15:30:00Z"
}
```

**Response Error (401):**
```json
{
  "success": false,
  "message": "Số điện thoại hoặc mật khẩu không đúng",
  "timestamp": "2025-10-28T15:30:00Z"
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
- `phone`: 8-15 số
- `email`: Phải là email hợp lệ
- `password`: Tối thiểu 6 ký tự
- `confirmPassword`: Phải khớp với password

**Response Success (200):**
```json
{
  "success": true,
  "data": "User registered successfully",
  "message": "Registered",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

**Response Error (400):**
```json
{
  "success": false,
  "message": "Phone is already in use",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

## User Management APIs

### 3. Get Current User Info
**GET** `/users/me`

**Authentication Required:** ✅ Yes (Any authenticated user)

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
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 4. Update Current User Info
**PUT** `/users/me`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Request Body:**
```json
{
  "fullname": "Nguyen Van A Updated",
  "email": "newemail@example.com",
  "phone": "0987654321",
  "bankName": "Vietcombank",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "NGUYEN VAN A"
}
```

**Note:** 
- Tất cả các field đều optional. Chỉ gửi những field cần update.
- **`phone`** có thể được cập nhật, nhưng hệ thống sẽ kiểm tra số điện thoại mới chưa được sử dụng bởi tài khoản khác

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "fullname": "Nguyen Van A Updated",
    "phone": "0987654321",
    "email": "newemail@example.com",
    "roles": ["ROLE_USER"],
    "bankName": "Vietcombank",
    "bankAccountNumber": "1234567890",
    "bankAccountName": "NGUYEN VAN A"
  },
  "message": "Cập nhật thông tin thành công",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

**Response Error (400) - Số điện thoại đã được sử dụng:**
```json
{
  "success": false,
  "message": "Số điện thoại đã được sử dụng bởi tài khoản khác",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 5. Request Owner Role (Trở thành chủ sân)
**POST** `/users/me/request-owner-role`

**Authentication Required:** ✅ Yes (ROLE_USER only)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": "Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập l��i để cập nhật quyền.",
  "message": "Success",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

**Note:** Sau khi gọi API này thành công, user cần đăng nhập lại để nhận role mới.

---

## Venues APIs

### 6. Get All Venues
**GET** `/venues`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Sân bóng ABC",
      "numberOfCourt": 3,
      "address": {
        "id": 1,
        "provinceOrCity": "Hà Nội",
        "district": "Cầu Giấy",
        "detailAddress": "123 Đường ABC"
      },
      "courtsCount": 3
    }
  ],
  "message": "List venues",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 7. Search Venues
**GET** `/venues/search`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Query Parameters:**
- `name` (optional): Tên venue
- `province` (optional): Tỉnh/Thành phố
- `district` (optional): Quận/Huyện
- `detail` (optional): Địa chỉ chi tiết

**Example:**
```
GET /venues/search?name=ABC&province=Hà Nội&district=Cầu Giấy
```

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Sân bóng ABC",
      "numberOfCourt": 3,
      "address": {
        "id": 1,
        "provinceOrCity": "Hà Nội",
        "district": "Cầu Giấy",
        "detailAddress": "123 Đường ABC"
      },
      "courtsCount": 3
    }
  ],
  "message": "Search results",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 8. Get Venue by ID
**GET** `/venues/{id}`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "Sân bóng ABC",
    "numberOfCourt": 3,
    "address": {
      "id": 1,
      "provinceOrCity": "Hà Nội",
      "district": "Cầu Giấy",
      "detailAddress": "123 Đường ABC"
    },
    "courtsCount": 3
  },
  "message": "OK",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 9. Create Venue
**POST** `/venues`

**Authentication Required:** ✅ Yes (ROLE_OWNER required)

**Request Body:**
```json
{
  "name": "Sân bóng XYZ",
  "description": "Sân bóng chất lượng cao",
  "phoneNumber": "0987654321",
  "email": "contact@xyz.com",
  "pricePerHour": 100000,
  "openingTime": "06:00",
  "closingTime": "23:00",
  "numberOfCourt": 5,
  "address": {
    "provinceOrCity": "Hà Nội",
    "district": "Đống Đa",
    "detailAddress": "456 Đường XYZ"
  }
}
```

**Validation Rules:**
- `name`: Bắt buộc, không được để trống
- `phoneNumber`: Bắt buộc
- `email`: Phải là email hợp lệ
- `pricePerHour`: Bắt buộc khi create, giá theo giờ (VND)
- `address`: Bắt buộc
- `numberOfCourt`: Tùy chọn - Nếu có, hệ thống sẽ **TỰ ĐỘNG TẠO** số lượng courts tương ứng

**✨ TÍNH NĂNG MỚI: Tự động tạo courts**
- Nếu `numberOfCourt = 5` → Hệ thống tự động tạo 5 courts với tên "Sân số 1", "Sân số 2"... "Sân số 5"
- Nếu không truyền `numberOfCourt` hoặc = 0 → Không tạo courts, owner có thể thêm sau

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "Sân bóng XYZ",
    "numberOfCourt": 5,
    "pricePerHour": 100000,
    "openingTime": "06:00:00",
    "closingTime": "23:00:00",
    "address": {
      "id": 2,
      "provinceOrCity": "Hà Nội",
      "district": "Đống Đa",
      "detailAddress": "456 Đường XYZ"
    },
    "courtsCount": 5
  },
  "message": "Created",
  "timestamp": "2025-11-04T21:00:00Z"
}
```

**Frontend Example:**
```javascript
// Tạo venue với 5 sân tự động
const createVenue = async () => {
  const response = await fetch('/api/venues', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      name: "Sân bóng ABC",
      phoneNumber: "0123456789",
      email: "abc@gmail.com",
      pricePerHour: 150000,
      openingTime: "06:00",
      closingTime: "23:00",
      numberOfCourt: 5, // ← Tự động tạo 5 courts
      address: {
        provinceOrCity: "Hà Nội",
        district: "Cầu Giấy",
        detailAddress: "123 Đường ABC"
      }
    })
  });
  
  const data = await response.json();
  console.log('Venue created with', data.data.numberOfCourt, 'courts');
};
```

---

### 10. Update Venue
**PUT** `/venues/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu)

**Request Body:**

**Cách 1: Cập nhật chỉ thông tin cơ bản**
```json
{
  "name": "Sân bóng XYZ Updated",
  "description": "Sân bóng chất lượng cao - đã nâng cấp",
  "phoneNumber": "0987654321",
  "email": "contact_new@xyz.com",
  "address": {
    "provinceOrCity": "Hà Nội",
    "district": "Đống Đa",
    "detailAddress": "456 Đường XYZ - Tầng 2"
  }
}
```

**Cách 2: Cập nhật cả số lượng sân (TỰ ĐỘNG TẠO/XÓA COURTS)**
```json
{
  "name": "Sân bóng XYZ Updated",
  "description": "Mở rộng thêm sân",
  "phoneNumber": "0987654321",
  "email": "contact_new@xyz.com",
  "numberOfCourt": 8,
  "address": {
    "provinceOrCity": "Hà Nội",
    "district": "Đống Đa",
    "detailAddress": "456 Đường XYZ - Tầng 2"
  }
}
```

**Cách 3: Cập nhật giá tiền và thời gian hoạt động**
```json
{
  "name": "Sân bóng XYZ Premium",
  "description": "Sân bóng cao cấp với cỏ nhân tạo",
  "phoneNumber": "0987654321",
  "email": "premium@xyz.com",
  "pricePerHour": 200000,
  "openingTime": "05:00",
  "closingTime": "24:00",
  "address": {
    "provinceOrCity": "TP Hồ Chí Minh",
    "district": "Quận 1",
    "detailAddress": "789 Nguyễn Huệ"
  }
}
```

**⚠️ LƯU Ý QUAN TRỌNG VỀ `numberOfCourt`:**
- Field `numberOfCourt` là **OPTIONAL** (không bắt buộc)
- **KHÔNG gửi** `numberOfCourt` → Số lượng sân **GIỮ NGUYÊN**
- **GỬI** `numberOfCourt` với giá trị mới:
  - **Tăng số sân** (VD: từ 5 lên 8) → Hệ thống **TỰ ĐỘNG TẠO** thêm 3 courts mới ("Sân số 6", "Sân số 7", "Sân số 8")
  - **Giảm số sân** (VD: từ 8 xuống 5) → Hệ thống **TỰ ĐỘNG XÓA** 3 courts cuối cùng (courts có ID lớn nhất)
  - **Giữ nguyên** (VD: từ 5 → 5) → Không thay đổi gì

**💡 Ví dụ thực tế:**
```javascript
// Venue hiện tại có 5 sân
// User muốn mở rộng thêm 3 sân → Tổng 8 sân

const response = await fetch(`/api/venues/${venueId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    numberOfCourt: 8  // ← Hệ thống tự động tạo "Sân số 6", "Sân số 7", "Sân số 8"
  })
});
```

**⚠️ Lưu ý khi GIẢM số sân:**
- Hệ thống sẽ xóa các sân có ID lớn nhất (thường là sân được tạo sau cùng)
- **Khuyến nghị:** Trước khi giảm số sân, nên kiểm tra xem các sân đó có booking nào không
- Nếu có booking đang active trên các sân bị xóa → Có thể gây lỗi

**Validation Rules:**
- `name`: Tên venue
- `phoneNumber`: Số điện thoại liên hệ
- `email`: Email hợp lệ
- `pricePerHour`: Giá theo giờ (VND), phải > 0
- `openingTime`: Format "HH:mm" hoặc "HH:mm:ss" (VD: "06:00")
- `closingTime`: Format "HH:mm" hoặc "HH:mm:ss" (VD: "23:00")
- `numberOfCourt`: Số nguyên dương (VD: 5, 8, 10)
- `address`: Object chứa thông tin địa chỉ

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "name": "Sân bóng XYZ Premium",
    "numberOfCourt": 3,
    "address": {
      "id": 2,
      "provinceOrCity": "TP Hồ Chí Minh",
      "district": "Quận 1",
      "detailAddress": "789 Nguyễn Huệ"
    },
    "courtsCount": 3,
    "averageRating": 4.5,
    "totalReviews": 10
  },
  "message": "Updated",
  "timestamp": "2025-10-29T15:30:00Z"
}
```

**💡 UI/UX Flow đề xuất cho Frontend:**

**Khi hiển thị form Update Venue:**

1. **Load thông tin venue hiện tại:**
```javascript
const response = await fetch(`/api/venues/${venueId}`);
const venue = await response.json();
```

2. **Load price rules hiện tại:**
```javascript
const pricesResponse = await fetch(`/api/pricerules/venue/${venueId}`);
const currentPriceRules = await pricesResponse.json();
```

3. **Hiển thị form với 2 options:**
   - ☑️ Checkbox: "Cập nhật giá tiền"
   - Nếu KHÔNG check: Không gửi field `priceRules` → Giữ nguyên giá cũ
   - Nếu CHECK: Hiển thị form nhập price rules → Gửi `priceRules` → Thay thế toàn bộ

4. **Khi user check "Cập nhật giá tiền":**
```javascript
const updatePricesCheckbox = document.getElementById('update-prices');

updatePricesCheckbox.addEventListener('change', (e) => {
  if (e.target.checked) {
    // Hiển thị form price rules
    // Pre-fill với giá cũ để user có thể chỉnh sửa
    priceRulesForm.style.display = 'block';
    priceRulesInput.value = JSON.stringify(currentPriceRules, null, 2);
  } else {
    // Ẩn form price rules
    priceRulesForm.style.display = 'none';
  }
});
```

5. **Khi submit form:**
```javascript
const formData = {
  name: nameInput.value,
  description: descInput.value,
  phoneNumber: phoneInput.value,
  email: emailInput.value,
  address: {
    provinceOrCity: provinceInput.value,
    district: districtInput.value,
    detailAddress: detailInput.value
  }
};

// Chỉ thêm priceRules nếu user check "Cập nhật giá tiền"
if (updatePricesCheckbox.checked) {
  formData.priceRules = priceRulesArray; // Array của price rules mới
}

const response = await fetch(`/api/venues/${venueId}`, {
  method: 'PUT',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(formData)
});
```

**Response Error (403):**
```json
{
  "success": false,
  "message": "Bạn không có quyền cập nhật venue này",
  "timestamp": "2025-10-29T15:30:00Z"
}
```

**Response Error (404):**
```json
{
  "success": false,
  "message": "Venues not found with id=2",
  "timestamp": "2025-10-29T15:30:00Z"
}
```

---

### 11. Delete Venue
**DELETE** `/venues/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu)

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Deleted",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

## Court APIs

### 12. Get All Courts
**GET** `/courts`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
[
  {
    "id": 1,
    "description": "Sân số 1",
    "venues": {
      "id": 1,
      "name": "Sân bóng ABC"
    }
  }
]
```

**Note:** Response này không wrap trong ApiResponse format.

---

### 13. Get Court by ID
**GET** `/courts/{id}`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
{
  "id": 1,
  "description": "Sân số 1",
  "venues": {
    "id": 1,
    "name": "Sân bóng ABC"
  }
}
```

---

### 14. Check Court Availability (QUAN TRỌNG - Dùng trước khi đặt sân)
**GET** `/courts/{id}/availability`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Query Parameters:**
- `startTime` (required): ISO DateTime (VD: `2025-10-28T14:00:00`)
- `endTime` (required): ISO DateTime (VD: `2025-10-28T16:00:00`)

**Example:**
```
GET /courts/1/availability?startTime=2025-10-28T14:00:00&endTime=2025-10-28T16:00:00
```

**Response Success (200):**
```json
{
  "courtId": 1,
  "available": false,
  "bookedSlots": [
    {
      "startTime": "2025-10-28T14:00:00",
      "endTime": "2025-10-28T15:00:00",
      "bookingId": 5
    }
  ]
}
```

**Response khi sân trống:**
```json
{
  "courtId": 1,
  "available": true,
  "bookedSlots": []
}
```

**🎯 Use Case cho Frontend:**

**Bước 1 - Người dùng chọn thời gian:**
```javascript
// User chọn sân, ngày giờ bắt đầu và kết thúc
const courtId = 1;
const startTime = "2025-10-28T14:00:00";
const endTime = "2025-10-28T16:00:00";
```

**Bước 2 - Gọi API kiểm tra trước khi cho đặt:**
```javascript
const response = await fetch(
  `/api/courts/${courtId}/availability?startTime=${startTime}&endTime=${endTime}`,
  {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  }
);

const data = await response.json();

if (data.available) {
  // ✅ Sân trống - Cho phép người dùng đặt sân
  // Hiện nút "Đặt sân" hoặc chuyển sang bước tiếp theo
  enableBookingButton();
} else {
  // ❌ Sân đã có người đặt
  // Hiển thị thông báo lỗi và danh sách các slot đã được đặt
  showError("Sân đã được đặt trong khung giờ này!");
  
  // Có thể hiển thị chi tiết các slot đã đặt
  data.bookedSlots.forEach(slot => {
    console.log(`Đã đặt từ ${slot.startTime} đến ${slot.endTime}`);
  });
  
  // Gợi ý user chọn thời gian khác
  suggestOtherTimeSlots();
}
```

**Bước 3 - Nếu available = true, gọi API Create Booking:**
```javascript
// Chỉ gọi API này sau khi đã check availability
const bookingResponse = await fetch('/api/bookings', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    venueId: 1,
    courtId: courtId,
    startTime: startTime,
    endTime: endTime
  })
});
```

**💡 Lưu ý quan trọng:**
- ⚠️ **BẮT BUỘC** phải gọi API này trước khi cho phép user đặt sân
- ⚠️ Nếu `available = false`, KHÔNG được gọi API Create Booking
- ⚠️ Nên disable nút "Đặt sân" cho đến khi check availability thành công
- ⚠️ Có thể xảy ra race condition: Giữa lúc check và lúc đặt có người khác đặt trước. Backend sẽ validate lại và trả lỗi nếu sân đã được đặt.
- 💡 Khuyến nghị: Hiển thị loading spinner khi đang check availability
- 💡 Có thể cache kết quả trong 10-30 giây để tránh gọi API quá nhiều lần

**UI/UX Flow đề xuất:**

```
1. User chọn venue → Hiển thị danh sách courts
2. User chọn court → Hiển thị calendar/time picker
3. User chọn startTime và endTime
4. Frontend: Disable nút "Đặt sân", hiển thị loading
5. Frontend: Gọi GET /courts/{id}/availability
6. Nếu available = true:
   → Enable nút "Đặt sân"
   → Hiển thị "Sân đang trống, bạn có thể đặt"
7. Nếu available = false:
   → Hiển thị "Sân đã được đặt"
   → Hiển thị danh sách các slot đã đặt (từ bookedSlots)
   → Gợi ý chọn thời gian khác
8. User nhấn "Đặt sân" → Gọi POST /bookings
```

**Response Error (404):**
```json
{
  "success": false,
  "message": "Court not found"
}
```

---

### 15. Create Court
**POST** `/courts`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu)

**Request Body:**
```json
{
  "description": "Sân số 2",
  "venues": {
    "id": 1
  }
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "description": "Sân số 2",
    "venues": {
      "id": 1,
      "name": "Sân bóng ABC"
    }
  },
  "message": "Created",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 16. Update Court
**PUT** `/courts/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu sân)

**Request Body:**
```json
{
  "description": "Sân số 2 - Đã nâng cấp"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "description": "Sân số 2 - Đã nâng cấp",
    "venues": {
      "id": 1,
      "name": "Sân bóng ABC"
    }
  },
  "message": "Updated",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 16.5. Get Courts by Venue ⭐ MỚI
**GET** `/api/venues/{venueId}/courts`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Mục đích:** Lấy danh sách tất cả các courts thuộc một venue cụ thể. Frontend cần API này để biết Court ID trước khi xóa hoặc hiển thị danh sách sân cho user chọn.

**Path Parameters:**
- `venueId` (required): ID của venue cần lấy danh sách courts

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "description": "Sân số 1 - VIP",
      "venues": {
        "id": 10,
        "name": "Sân bóng ABC",
        "numberOfCourt": 3,
        "address": {...}
      }
    },
    {
      "id": 2,
      "description": "Sân số 2 - Thường",
      "venues": {
        "id": 10,
        "name": "Sân bóng ABC",
        "numberOfCourt": 3,
        "address": {...}
      }
    }
  ],
  "message": "Courts of venue",
  "timestamp": "2025-11-04T20:30:00Z"
}
```

**Response Error (404 Not Found):**
```json
{
  "success": false,
  "message": "Venues not found with id=999",
  "timestamp": "2025-11-04T20:30:00Z"
}
```

**Frontend Example:**
```javascript
// Lấy danh sách courts của venue
async function getCourtsByVenue(venueId) {
  const token = localStorage.getItem('token');

  const response = await fetch(`/api/venues/${venueId}/courts`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });

  const data = await response.json();
  return data.data; // Array of courts
}

// Sử dụng
const courts = await getCourtsByVenue(10);
courts.forEach(court => {
  console.log(`Court ID: ${court.id}, Mô tả: ${court.description}`);
});
```

**Use Case:**
```javascript
// Flow: Hiển thị danh sách courts và cho phép xóa
async function manageCourts(venueId) {
  // 1. Lấy danh sách courts
  const courts = await getCourtsByVenue(venueId);

  // 2. Hiển thị với button xóa
  courts.forEach(court => {
    displayCourt(court.id, court.description, () => {
      // 3. Xóa khi click (xem API 17 bên dưới)
      deleteCourt(court.id);
    });
  });
}
```

---

### 17. Delete Court
**DELETE** `/api/courts/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu venue)

**💡 Làm sao biết Court ID?** → Dùng API `GET /api/venues/{venueId}/courts` (xem section 16.5 ở trên)

**Path Parameters:**
- `id` (required): ID của court cần xóa

**Response Success (204 No Content):**
- Không có body
- HTTP Status: 204

**Response Error (404 Not Found):**
```json
{
  "success": false,
  "message": "Court not found",
  "timestamp": "2025-11-04T20:30:00Z"
}
```

**Lưu ý quan trọng:**
- ✅ Backend sẽ **TỰ ĐỘNG giảm** `numberOfCourt` của venue khi xóa court thành công
- ✅ Frontend **KHÔNG CẦN** gọi thêm API update venue
- ⚠️ Nên hiển thị confirm dialog trước khi xóa

**Frontend Example - Flow hoàn chỉnh:**
```javascript
// BƯỚC 1: Lấy danh sách courts (để biết Court ID)
const courts = await fetch(`/api/venues/${venueId}/courts`, {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

console.log(courts.data);
// → [{ id: 1, description: "Sân 1" }, { id: 2, ... }]

// BƯỚC 2: Xóa court theo ID
async function deleteCourt(courtId) {
  if (!confirm('Bạn có chắc muốn xóa sân này?')) return;
  
  const token = localStorage.getItem('token');
  
  const response = await fetch(`/api/courts/${courtId}`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });

  if (response.status === 204) {
    alert('✅ Xóa court thành công!');
    // numberOfCourt của venue đã được tự động giảm
    window.location.reload(); // Refresh danh sách
  }
}

// BƯỚC 3: Sử dụng
deleteCourt(1); // Xóa court có ID = 1
```

**React Example:**
```javascript
function CourtManager({ venueId }) {
  const [courts, setCourts] = useState([]);

  // Load courts
  useEffect(() => {
    fetch(`/api/venues/${venueId}/courts`, {
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    })
    .then(r => r.json())
    .then(data => setCourts(data.data));
  }, [venueId]);

  // Delete court
  const handleDelete = async (courtId) => {
    if (!confirm('Xóa sân này?')) return;

    await fetch(`/api/courts/${courtId}`, {
      method: 'DELETE',
      headers: { 'Authorization': `Bearer ${localStorage.getItem('token')}` }
    });

    // Refresh danh sách
    setCourts(courts.filter(c => c.id !== courtId));
  };

  return (
    <div>
      {courts.map(court => (
        <div key={court.id}>
          <span>Sân #{court.id}: {court.description}</span>
          <button onClick={() => handleDelete(court.id)}>🗑️ Xóa</button>
        </div>
      ))}
    </div>
  );
}
```

---

## Price Rules APIs

### 18. Get All Price Rules
**GET** `/pricerules`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "Giờ cao điểm buổi sáng",
      "startTime": "06:00:00",
      "endTime": "09:00:00",
      "pricePerHour": 200000,
      "active": true,
      "venues": {...}
    },
    {
      "id": 2,
      "name": "Giờ thường",
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "pricePerHour": 150000,
      "active": true,
      "venues": {...}
    }
  ],
  "message": "List price rules",
  "timestamp": "2025-10-28T15:30:00Z"
}
```

---

### 19. Get Price Rules by Venue
**GET** `/pricerules/venue/{venueId}`

**Authentication Required:** ❌ No (Public)

**Response Success (200):**
```json
[
  {
    "id": 1,
    "name": "Giờ cao điểm buổi sáng",
    "startTime": "06:00:00",
    "endTime": "09:00:00",
    "pricePerHour": 200000,
    "active": true,
    "venues": {...}
  },
  {
    "id": 2,
    "name": "Giờ thường",
    "startTime": "09:00:00",
    "endTime": "17:00:00",
    "pricePerHour": 150000,
    "active": true,
    "venues": {...}
  }
]
```

---

### 20. Update Price Rule
**PUT** `/pricerules/{id}`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu venue)

**Request Body:**
```json
{
  "name": "Giờ cao điểm buổi sáng - Updated",
  "startTime": "06:00:00",
  "endTime": "10:00:00",
  "pricePerHour": 250000
}
```

**Note:** Tất cả field đều optional.

**Response Success (200):**
```json
{
  "id": 1,
  "name": "Giờ cao điểm buổi sáng - Updated",
  "startTime": "06:00:00",
  "endTime": "10:00:00",
  "pricePerHour": 250000,
  "active": true,
  "venues": {...}
}
```

---

### 21. Toggle Price Rule (Bật/Tắt)
**PATCH** `/pricerules/{id}/toggle`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu venue)

**Request Body:** None

**Response Success (200):**
```json
{
  "id": 1,
  "name": "Giờ cao điểm buổi sáng",
  "startTime": "06:00:00",
  "endTime": "10:00:00",
  "pricePerHour": 250000,
  "active": false,
  "venues": {...}
}
```

---

## Booking APIs

**⚠️ LƯU Ý QUAN TRỌNG VỀ `totalPrice`:**
- Field `totalPrice` **KHÔNG được lưu trong database** (tuân thủ chuẩn 3NF)
- `totalPrice` được **TÍNH ĐỘNG** từ `SUM(booking_item.price)` 
- Backend tự động tính khi gọi API
- Frontend chỉ cần hiển thị giá trị này, không cần tính toán

---

### 22. Create Booking (Đặt sân)
**POST** `/bookings`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Request Body:**
```json
{
  "venueId": 1,
  "courtId": 1,
  "startTime": "2025-10-28T14:00:00",
  "endTime": "2025-10-28T16:00:00"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 1,
    "userName": "Nguyen Van A",
    "courtId": 1,
    "courtName": "Sân số 1",
    "venuesName": "Sân bóng ABC",
    "startTime": "2025-10-28T14:00:00",
    "endTime": "2025-10-28T16:00:00",
    "totalPrice": 400000,
    "status": "PENDING_PAYMENT",
    "expireTime": "2025-10-28T14:05:00",
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
  "timestamp": "2025-10-28T14:00:00Z"
}
```

**Note:** 
- Trạng thái ban đầu: `PENDING_PAYMENT`
- Có 5 phút để upload ảnh và confirm payment
- `ownerBankInfo` chứa thông tin tài khoản ngân hàng của chủ sân để người dùng chuyển tiền

---

### 23. Upload Payment Proof (Upload ảnh chuyển khoản)
**POST** `/bookings/{id}/upload-payment-proof`

**Authentication Required:** ✅ Yes (ROLE_USER - chỉ người đặt)

**Request Type:** `multipart/form-data`

**Form Data:**
- `file`: File ảnh (jpg, jpeg, png - max 10MB)

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 1,
    "userName": "Nguyen Van A",
    "courtId": 1,
    "courtName": "Sân số 1",
    "venuesName": "Sân bóng ABC",
    "startTime": "2025-10-28T14:00:00",
    "endTime": "2025-10-28T16:00:00",
    "totalPrice": 400000,
    "status": "PENDING_PAYMENT",
    "expireTime": "2025-10-28T14:05:00",
    "paymentProofUploaded": true,
    "paymentProofUrl": "/api/files/payment-proofs/payment_1730102400000_abc123.jpg",
    "paymentProofUploadedAt": "2025-10-28T14:03:00",
    "rejectionReason": null,
    "ownerBankInfo": {...}
  },
  "message": "Đã upload ảnh thành công. Vui lòng nhấn 'Xác nhận thanh toán' để gửi cho chủ sân.",
  "timestamp": "2025-10-28T14:03:00Z"
}
```

---

### 24. Confirm Payment (Xác nhận đã chuyển khoản)
**PUT** `/bookings/{id}/confirm-payment`

**Authentication Required:** ✅ Yes (ROLE_USER - chỉ người đặt)

**Request Body:**
```json
{
  "paymentProofUrl": "/api/files/payment-proofs/payment_1730102400000_abc123.jpg"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 1,
    "userName": "Nguyen Van A",
    "courtId": 1,
    "courtName": "Sân số 1",
    "venuesName": "Sân bóng ABC",
    "startTime": "2025-10-28T14:00:00",
    "endTime": "2025-10-28T16:00:00",
    "totalPrice": 400000,
    "status": "PENDING_CONFIRMATION",
    "expireTime": null,
    "paymentProofUploaded": true,
    "paymentProofUrl": "/api/files/payment-proofs/payment_1730102400000_abc123.jpg",
    "paymentProofUploadedAt": "2025-10-28T14:03:00",
    "rejectionReason": null,
    "ownerBankInfo": {...}
  },
  "message": "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận.",
  "timestamp": "2025-10-28T14:04:00Z"
}
```

**Note:** 
- Trạng thái chuyển sang `PENDING_CONFIRMATION`
- Hệ thống gửi thông báo cho chủ sân

---

### 25. Accept Booking (Chủ sân chấp nhận)
**PUT** `/bookings/{id}/accept`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 1,
    "userName": "Nguyen Van A",
    "courtId": 1,
    "courtName": "Sân số 1",
    "venuesName": "Sân bóng ABC",
    "startTime": "2025-10-28T14:00:00",
    "endTime": "2025-10-28T16:00:00",
    "totalPrice": 400000,
    "status": "COMPLETED",
    "expireTime": null,
    "paymentProofUploaded": true,
    "paymentProofUrl": "/api/files/payment-proofs/payment_1730102400000_abc123.jpg",
    "paymentProofUploadedAt": "2025-10-28T14:03:00",
    "rejectionReason": null,
    "ownerBankInfo": {...}
  },
  "message": "Đã xác nhận đặt sân thành công.",
  "timestamp": "2025-10-28T14:10:00Z"
}
```

**Note:** 
- Trạng thái chuyển sang `COMPLETED`
- Hệ thống gửi thông báo cho người đặt sân

---

### 26. Reject Booking (Chủ sân từ chối)
**PUT** `/bookings/{id}/reject`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu)

**Request Body:**
```json
{
  "rejectionReason": "Chưa nhận được tiền chuyển khoản"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 1,
    "userName": "Nguyen Van A",
    "courtId": 1,
    "courtName": "Sân số 1",
    "venuesName": "Sân bóng ABC",
    "startTime": "2025-10-28T14:00:00",
    "endTime": "2025-10-28T16:00:00",
    "totalPrice": 400000,
    "status": "REJECTED",
    "expireTime": null,
    "paymentProofUploaded": true,
    "paymentProofUrl": "/api/files/payment-proofs/payment_1730102400000_abc123.jpg",
    "paymentProofUploadedAt": "2025-10-28T14:03:00",
    "rejectionReason": "Chưa nhận được tiền chuyển khoản",
    "ownerBankInfo": {...}
  },
  "message": "Đã từ chối đặt sân.",
  "timestamp": "2025-10-28T14:10:00Z"
}
```

**Note:** 
- Trạng thái chuyển sang `REJECTED`
- Hệ thống gửi thông báo cho người đặt sân

---

### 27. Get My Bookings (Lấy danh sách booking của tôi)
**GET** `/bookings/my-bookings`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "userId": 1,
      "userName": "Nguyen Van A",
      "courtId": 1,
      "courtName": "Sân số 1",
      "venuesName": "Sân bóng ABC",
      "startTime": "2025-10-28T14:00:00",
      "endTime": "2025-10-28T16:00:00",
      "totalPrice": 400000,
      "status": "COMPLETED",
      "expireTime": null,
      "paymentProofUploaded": true,
      "paymentProofUrl": "/api/files/payment-proofs/payment_1730102400000_abc123.jpg",
      "paymentProofUploadedAt": "2025-10-28T14:03:00",
      "rejectionReason": null,
      "ownerBankInfo": {...}
    }
  ],
  "message": "My bookings retrieved successfully",
  "timestamp": "2025-10-28T15:00:00Z"
}
```

---

### 28. Get Pending Bookings (Chủ sân xem booking chờ xác nhận)
**GET** `/bookings/pending`

**Authentication Required:** ✅ Yes (ROLE_OWNER)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 11,
      "userId": 2,
      "userName": "Tran Thi B",
      "courtId": 2,
      "courtName": "Sân số 2",
      "venuesName": "Sân bóng ABC",
      "startTime": "2025-10-28T16:00:00",
      "endTime": "2025-10-28T18:00:00",
      "totalPrice": 400000,
      "status": "PENDING_CONFIRMATION",
      "expireTime": null,
      "paymentProofUploaded": true,
      "paymentProofUrl": "/api/files/payment-proofs/payment_1730106000000_xyz789.jpg",
      "paymentProofUploadedAt": "2025-10-28T15:55:00",
      "rejectionReason": null,
      "ownerBankInfo": {...}
    }
  ],
  "message": "Lấy danh sách booking chờ xác nhận thành công.",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 29. Get Venue Bookings (Lấy tất cả booking của một venue)
**GET** `/bookings/venue/{venueId}`

**Authentication Required:** ✅ Yes (ROLE_OWNER - chỉ chủ sở hữu venue)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "userId": 1,
      "userName": "Nguyen Van A",
      "courtId": 1,
      "courtName": "Sân số 1",
      "venuesName": "Sân bóng ABC",
      "startTime": "2025-10-28T14:00:00",
      "endTime": "2025-10-28T16:00:00",
      "totalPrice": 400000,
      "status": "COMPLETED",
      ...
    }
  ],
  "message": "Lấy danh sách booking thành công.",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 30. Get Booking by ID
**GET** `/bookings/{id}`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "userId": 1,
    "userName": "Nguyen Van A",
    "courtId": 1,
    "courtName": "Sân số 1",
    "venuesName": "Sân bóng ABC",
    "startTime": "2025-10-28T14:00:00",
    "endTime": "2025-10-28T16:00:00",
    "totalPrice": 400000,
    "status": "COMPLETED",
    ...
  },
  "message": "Booking retrieved successfully",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 31. Cancel Booking (Người dùng hủy booking)
**PUT** `/bookings/{id}/cancel`

**Authentication Required:** ✅ Yes (ROLE_USER - chỉ người đặt)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 10,
    "status": "CANCELLED",
    ...
  },
  "message": "Booking cancelled successfully",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

## Notification APIs

### 32. Get My Notifications
**GET** `/notifications`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "bookingId": 10,
      "type": "BOOKING_ACCEPTED",
      "title": "Đặt sân thành công",
      "message": "Booking #10 đã được chủ sân xác nhận. Bạn có thể đến sân vào 2025-10-28 14:00:00.",
      "isRead": false,
      "createdAt": "2025-10-28T14:10:00Z",
      "senderName": "Nguyen Van B"
    },
    {
      "id": 2,
      "bookingId": 11,
      "type": "NEW_BOOKING",
      "title": "Có booking mới",
      "message": "Bạn có booking mới #11 từ Tran Thi B. Vui lòng kiểm tra và xác nhận.",
      "isRead": true,
      "createdAt": "2025-10-28T15:56:00Z",
      "senderName": "Tran Thi B"
    }
  ],
  "message": "Lấy danh sách thông báo thành công.",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

**Notification Types:**
- `NEW_BOOKING`: Có booking mới (gửi cho OWNER)
- `BOOKING_ACCEPTED`: Booking được chấp nhận (gửi cho USER)
- `BOOKING_REJECTED`: Booking bị từ chối (gửi cho USER)
- `BOOKING_CANCELLED`: Booking bị hủy (gửi cho OWNER)

---

### 33. Get Unread Count
**GET** `/notifications/unread-count`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Response Success (200):**
```json
{
  "success": true,
  "data": 5,
  "message": "Số thông báo chưa đọc.",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 34. Mark Notification as Read
**PUT** `/notifications/{id}/read`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Đã đánh dấu thông báo là đã đọc.",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 35. Mark All as Read
**PUT** `/notifications/read-all`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Đã đánh dấu tất cả thông báo là đã đọc.",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 36. Delete Notification
**DELETE** `/notifications/{id}`

**Authentication Required:** ✅ Yes (Any authenticated user)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Đã xóa thông báo.",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

## Review APIs

### 37. Create Review (Đánh giá sau khi hoàn thành booking)
**POST** `/bookings/{bookingId}/review`

**Authentication Required:** ✅ Yes (ROLE_USER - chỉ người đã đặt sân)

**Request Body:**
```json
{
  "rating": 5,
  "comment": "Sân rất đẹp, chất lượng tốt. Sẽ quay lại lần sau!"
}
```

**Validation Rules:**
- `rating`: Bắt buộc, từ 1-5
- `comment`: Tùy chọn

**Note:** Chỉ có thể review booking có status = `COMPLETED`

**Response Success (201):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "userFullname": "Nguyen Van A",
    "venueId": 1,
    "venueName": "Sân bóng ABC",
    "bookingId": 10,
    "rating": 5,
    "comment": "Sân rất đẹp, chất lượng tốt. Sẽ quay lại lần sau!",
    "createdAt": "2025-10-28T16:30:00Z",
    "updatedAt": "2025-10-28T16:30:00Z"
  },
  "message": "Review created successfully",
  "timestamp": "2025-10-28T16:30:00Z"
}
```

---

### 38. Get Venue Reviews (Xem tất cả đánh giá của một venue)
**GET** `/venues/{venueId}/reviews`

**Authentication Required:** ❌ No (Public)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "userFullname": "Nguyen Van A",
      "venueId": 1,
      "venueName": "Sân bóng ABC",
      "bookingId": 10,
      "rating": 5,
      "comment": "Sân rất đẹp, chất lượng tốt. Sẽ quay lại lần sau!",
      "createdAt": "2025-10-28T16:30:00Z",
      "updatedAt": "2025-10-28T16:30:00Z"
    },
    {
      "id": 2,
      "userId": 2,
      "userFullname": "Tran Thi B",
      "venueId": 1,
      "venueName": "Sân bóng ABC",
      "bookingId": 12,
      "rating": 4,
      "comment": "Sân ổn, giá hợp lý",
      "createdAt": "2025-10-27T10:00:00Z",
      "updatedAt": "2025-10-27T10:00:00Z"
    }
  ],
  "message": "Reviews retrieved successfully",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 39. Get My Reviews (Xem tất cả đánh giá của tôi)
**GET** `/my-reviews`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "userFullname": "Nguyen Van A",
      "venueId": 1,
      "venueName": "Sân bóng ABC",
      "bookingId": 10,
      "rating": 5,
      "comment": "Sân rất đẹp, chất lượng tốt. Sẽ quay lại lần sau!",
      "createdAt": "2025-10-28T16:30:00Z",
      "updatedAt": "2025-10-28T16:30:00Z"
    }
  ],
  "message": "Your reviews retrieved successfully",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 40. Get Booking Review (Xem review của một booking)
**GET** `/bookings/{bookingId}/review`

**Authentication Required:** ✅ Yes (ROLE_USER)

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 1,
    "userFullname": "Nguyen Van A",
    "venueId": 1,
    "venueName": "Sân bóng ABC",
    "bookingId": 10,
    "rating": 5,
    "comment": "Sân rất đẹp, chất lượng tốt. Sẽ quay lại lần sau!",
    "createdAt": "2025-10-28T16:30:00Z",
    "updatedAt": "2025-10-28T16:30:00Z"
  },
  "message": "Review retrieved successfully",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 41. Delete Review
**DELETE** `/reviews/{reviewId}`

**Authentication Required:** ✅ Yes (ROLE_USER - chỉ người tạo review)

**Request Body:** None

**Response Success (200):**
```json
{
  "success": true,
  "data": null,
  "message": "Review deleted successfully",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

## File APIs

### 42. Get Payment Proof Image
**GET** `/files/payment-proofs/{filename}`

**Authentication Required:** ❌ No (Public - nhưng URL khó đoán)

**Example:**
```
GET /files/payment-proofs/payment_1730102400000_abc123.jpg
```

**Response:** Image file (image/jpeg hoặc image/png)

**Note:** URL này được trả về trong `paymentProofUrl` của BookingResponse. Có thể dùng trực tiếp trong thẻ `<img>` hoặc để hiển thị ảnh.

---

## Address APIs

### 43. Get All Addresses
**GET** `/addresses`

**Authentication Required:** ❌ No (Public)

**Response Success (200):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "provinceOrCity": "Hà Nội",
      "district": "Cầu Giấy",
      "detailAddress": "123 Đường ABC"
    }
  ],
  "message": "List addresses",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 44. Get Address by ID
**GET** `/addresses/{id}`

**Authentication Required:** ❌ No (Public)

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "provinceOrCity": "Hà Nội",
    "district": "Cầu Giấy",
    "detailAddress": "123 Đường ABC"
  },
  "message": "OK",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

### 45. Create Address
**POST** `/addresses`

**Authentication Required:** ❌ No (nhưng nên bảo mật lại)

**Request Body:**
```json
{
  "provinceOrCity": "Hồ Chí Minh",
  "district": "Quận 1",
  "detailAddress": "789 Đường XYZ"
}
```

**Response Success (200):**
```json
{
  "success": true,
  "data": {
    "id": 2,
    "provinceOrCity": "Hồ Chí Minh",
    "district": "Quận 1",
    "detailAddress": "789 Đường XYZ"
  },
  "message": "Created",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

## Booking Status Flow (Luồng trạng thái đặt sân)

```
1. USER tạo booking
   ↓
   Status: PENDING_PAYMENT
   ↓ (có 5 phút)
   
2. USER upload ảnh chuyển khoản
   ↓
   Status: vẫn PENDING_PAYMENT
   ↓
   
3. USER confirm payment
   ↓
   Status: PENDING_CONFIRMATION
   ↓ (gửi thông báo cho OWNER)
   
4a. OWNER accept          hoặc     4b. OWNER reject
    ↓                                   ↓
    Status: COMPLETED                   Status: REJECTED
    ↓                                   ↓
    (gửi thông báo cho USER)           (gửi thông báo cho USER)
    ↓
    USER có thể review
```

**Các trạng thái:**
- `PENDING_PAYMENT`: Chờ user upload ảnh và confirm
- `PENDING_CONFIRMATION`: Chờ owner xác nhận
- `COMPLETED`: Đã hoàn thành
- `REJECTED`: Bị từ chối
- `CANCELLED`: Bị hủy (do user hoặc hệ thống)
- `EXPIRED`: Hết hạn (quá 5 phút không upload ảnh)

---

## Common Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation error message",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "message": "You don't have permission to access this resource",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "Resource not found",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Internal server error",
  "timestamp": "2025-10-28T16:00:00Z"
}
```

---

## Notes for Frontend Developers

### 1. DateTime Format
- Tất cả datetime đều dùng ISO 8601 format
- Example: `2025-10-28T14:00:00`
- Timestamp trong response dùng ISO 8601 với timezone: `2025-10-28T14:00:00Z`

### 2. Authorization Header
```javascript
headers: {
  'Authorization': `Bearer ${token}`,
  'Content-Type': 'application/json'
}
```

### 3. File Upload
```javascript
const formData = new FormData();
formData.append('file', fileObject);

fetch('/api/bookings/{id}/upload-payment-proof', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`
    // Không set Content-Type khi upload file
  },
  body: formData
});
```

### 4. Polling for Notifications
Nên poll API `/notifications/unread-count` mỗi 30-60 giây để cập nhật số thông báo chưa đọc.

### 5. Image Display
Payment proof images có thể hiển thị trực tiếp:
```html
<img src="http://localhost:8080/api/files/payment-proofs/payment_xxx.jpg" />
```

### 6. Role-based UI
- Kiểm tra `roles` trong JWT response để hiển thị UI phù hợp
- `ROLE_USER`: Chức năng đặt sân, xem booking của mình, review
- `ROLE_OWNER`: Tạo venue, court, price rules, quản lý booking

---

## Testing với Postman

### 1. Login và lấy token
```
POST http://localhost:8080/api/auth/login
Body: {"phone": "0123456789", "password": "password123"}
→ Copy jwtToken từ response
```

### 2. Set Authorization
```
Authorization: Bearer {jwtToken}
```

### 3. Test các API theo thứ tự
1. Register user
2. Login
3. Request owner role (nếu cần)
4. Login lại để lấy role mới
5. Create venue (với OWNER role)
6. Create court
7. Create price rules
8. Create booking (với USER role)
9. Upload payment proof
10. Confirm payment
11. Accept/Reject booking (với OWNER role)
12. Create review
13. Get notifications

---

## Swagger UI
Truy cập Swagger UI để test API tương tác:
```
http://localhost:8080/swagger-ui/index.html
```

---

**Last Updated:** October 28, 2025
**Version:** 1.0
**Backend Developer:** CodeWithVy Team
