# HƯỚNG DẪN API QUẢN LÝ GIÁ (PRICE RULES)

## Tổng quan
- **PriceRule** áp dụng cho cả **Venues** (không phải từng Court riêng)
- **Giá tính theo giờ** và chủ sân có thể tự cài đặt
- Chủ sân có thể tạo nhiều khung giờ với giá khác nhau (giờ cao điểm, giờ thường...)
- Hỗ trợ **bật/tắt** quy tắc giá mà không cần xóa

---

## 1. Tạo quy tắc giá mới (Chỉ OWNER)

**POST** `/api/pricerules`

**Headers:**
```
Authorization: Bearer {owner_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "venueId": 1,
  "name": "Giờ cao điểm buổi sáng",
  "startTime": "06:00",
  "endTime": "09:00",
  "pricePerHour": 200000
}
```

**Giải thích các trường:**
- `venueId`: ID của venues (bắt buộc)
- `name`: Tên khung giờ (bắt buộc)
- `startTime`: Giờ bắt đầu áp dụng giá (format: "HH:mm")
- `endTime`: Giờ kết thúc áp dụng giá (format: "HH:mm")
- `pricePerHour`: Giá theo giờ (VND)

**Response Success (200):**
```json
{
  "id": 1,
  "name": "Giờ cao điểm buổi sáng",
  "startTime": "06:00",
  "endTime": "09:00",
  "pricePerHour": 200000.0,
  "active": true,
  "venues": {
    "id": 1,
    "name": "Sân bóng ABC"
  }
}
```

**Ví dụ khác:**

**Giờ thường:**
```json
{
  "venueId": 1,
  "name": "Giờ thường",
  "startTime": "09:00",
  "endTime": "17:00",
  "pricePerHour": 150000
}
```

**Giờ cao điểm buổi tối:**
```json
{
  "venueId": 1,
  "name": "Giờ cao điểm buổi tối",
  "startTime": "17:00",
  "endTime": "22:00",
  "pricePerHour": 300000
}
```

---

## 2. Xem danh sách quy tắc giá của một venues

**GET** `/api/pricerules/venue/{venueId}`

**Ví dụ:** `GET /api/pricerules/venue/1`

**Response Success (200):**
```json
[
  {
    "id": 1,
    "name": "Giờ cao điểm buổi sáng",
    "startTime": "06:00",
    "endTime": "09:00",
    "pricePerHour": 200000.0,
    "active": true
  },
  {
    "id": 2,
    "name": "Giờ thường",
    "startTime": "09:00",
    "endTime": "17:00",
    "pricePerHour": 150000.0,
    "active": true
  },
  {
    "id": 3,
    "name": "Giờ cao điểm buổi tối",
    "startTime": "17:00",
    "endTime": "22:00",
    "pricePerHour": 300000.0,
    "active": false
  }
]
```

---

## 3. Cập nhật quy tắc giá (Chỉ OWNER)

**PUT** `/api/pricerules/{id}`

**Headers:**
```
Authorization: Bearer {owner_token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Giờ cao điểm buổi sáng (Cập nhật)",
  "pricePerHour": 250000
}
```

**Lưu ý:** Chỉ cần gửi các trường muốn cập nhật, không cần gửi tất cả.

**Response Success (200):**
```json
{
  "id": 1,
  "name": "Giờ cao điểm buổi sáng (Cập nhật)",
  "startTime": "06:00",
  "endTime": "09:00",
  "pricePerHour": 250000.0,
  "active": true
}
```

---

## 4. Bật/Tắt quy tắc giá (Chỉ OWNER)

**PATCH** `/api/pricerules/{id}/toggle`

**Headers:**
```
Authorization: Bearer {owner_token}
```

**Ví dụ:** `PATCH /api/pricerules/3/toggle`

**Response Success (200):**
```json
{
  "success": true,
  "message": "Price rule deactivated",
  "data": {
    "id": 3,
    "name": "Giờ cao điểm buổi tối",
    "active": false
  }
}
```

**Lưu ý:** API này sẽ **toggle** (đảo ngược) trạng thái:
- Nếu đang `active: true` → chuyển thành `active: false`
- Nếu đang `active: false` → chuyển thành `active: true`

---

## 5. Xóa quy tắc giá (Chỉ OWNER)

**DELETE** `/api/pricerules/{id}`

**Headers:**
```
Authorization: Bearer {owner_token}
```

**Ví dụ:** `DELETE /api/pricerules/3`

**Response Success (200):**
```json
{
  "success": true,
  "message": "Price rule deleted successfully"
}
```

---

## Lưu ý quan trọng

### 1. Phân quyền
- **Chỉ chủ sân (OWNER)** mới có thể:
  - Tạo quy tắc giá
  - Cập nhật quy tắc giá
  - Bật/tắt quy tắc giá
  - Xóa quy tắc giá
- Tất cả người dùng đều có thể **xem** danh sách quy tắc giá

### 2. Cách tính giá khi booking
- Hệ thống sẽ tìm quy tắc giá **phù hợp** với:
  - Thời gian đặt sân
  - Trạng thái `active = true`
- Tổng tiền = `số giờ × pricePerHour`

### 3. Ví dụ cấu hình giá cho một venues

```json
[
  {
    "name": "Giờ sáng sớm",
    "startTime": "06:00",
    "endTime": "09:00",
    "pricePerHour": 150000
  },
  {
    "name": "Giờ hành chính",
    "startTime": "09:00",
    "endTime": "17:00",
    "pricePerHour": 200000
  },
  {
    "name": "Giờ cao điểm tối",
    "startTime": "17:00",
    "endTime": "22:00",
    "pricePerHour": 300000
  }
]
```

### 4. Xử lý xung đột
- Nếu có nhiều quy tắc giá **cùng thời gian**, hệ thống sẽ ưu tiên quy tắc được tạo sau cùng

---

## Error Responses

**401 Unauthorized:**
```json
{
  "success": false,
  "message": "Token is invalid or expired"
}
```

**403 Forbidden:**
```json
{
  "success": false,
  "message": "You are not the owner of this venue"
}
```

**400 Bad Request:**
```json
{
  "success": false,
  "message": "venueId is required"
}
```

**404 Not Found:**
```json
{
  "success": false,
  "message": "Venue not found"
}
```
