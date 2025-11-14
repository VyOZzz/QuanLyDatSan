# 📋 API MỚI: Lấy Pending Booking Theo Venue

## ✅ ĐÃ HOÀN THÀNH

API mới đã được tạo để lấy danh sách booking chờ xác nhận (PAYMENT_UPLOADED) theo venue cụ thể.

---

## 🔧 CÁC THAY ĐỔI ĐÃ THỰC HIỆN

### 1️⃣ **BookingController.java**
Thêm endpoint mới:
```java
@GetMapping("/venue/{venueId}/pending")
@PreAuthorize("hasRole('OWNER')")
@Operation(summary = "Lấy danh sách booking chờ xác nhận theo venue",
           description = "Trả về các booking có status PAYMENT_UPLOADED của một venue cụ thể")
public ResponseEntity<ApiResponse<List<BookingResponse>>> getPendingBookingsByVenue(@PathVariable Long venueId) {
    List<BookingResponse> bookings = bookingService.getPendingBookingsByVenue(venueId);
    return ResponseEntity.ok(ApiResponse.ok(bookings, "Lấy danh sách booking chờ xác nhận của venue thành công."));
}
```

### 2️⃣ **BookingService.java**
Thêm method interface:
```java
List<BookingResponse> getPendingBookingsByVenue(Long venueId);
```

### 3️⃣ **BookingServiceImpl.java**
Thêm implementation với kiểm tra quyền sở hữu:
```java
@Override
public List<BookingResponse> getPendingBookingsByVenue(Long venueId) {
    User currentUser = getCurrentUser();
    // Kiểm tra xem venue có thuộc sở hữu của owner không
    Venues venue = venuesRepository.findById(venueId)
            .orElseThrow(() -> new ResourceNotFoundException("Venue not found"));
    
    if (!venue.getOwner().getId().equals(currentUser.getId())) {
        throw new UnauthorizedException("Bạn không có quyền truy cập venue này");
    }
    
    List<Booking> bookings = bookingRepository.findPendingBookingsByVenue(venueId);
    return bookings.stream()
            .map(this::mapToBookingResponse)
            .collect(Collectors.toList());
}
```

### 4️⃣ **BookingRepository.java**
Thêm query method:
```java
@Query("SELECT DISTINCT b FROM Booking b JOIN b.bookingItems bi WHERE bi.court.venues.id = :venueId AND b.status = 'PAYMENT_UPLOADED' ORDER BY b.paymentProofUploadedAt DESC")
List<Booking> findPendingBookingsByVenue(@Param("venueId") Long venueId);
```

### 5️⃣ **UnauthorizedException.java** (Mới)
Tạo exception mới cho trường hợp không có quyền:
```java
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
```

### 6️⃣ **GlobalExceptionHandler.java**
Thêm handler cho UnauthorizedException:
```java
@ExceptionHandler(UnauthorizedException.class)
public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException ex){
    log.error("UnauthorizedException: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail(ex.getMessage()));
}
```

---

## 🎯 API ENDPOINT MỚI

### **GET /api/bookings/venue/{venueId}/pending**

**Authorization:** Bearer Token (OWNER role required)

**Description:** Lấy danh sách booking chờ xác nhận (status = PAYMENT_UPLOADED) của một venue cụ thể. API sẽ kiểm tra xem venue có thuộc sở hữu của owner hiện tại hay không.

---

## 📝 CÁCH SỬ DỤNG

### **Request**
```http
GET /api/bookings/venue/1/pending
Authorization: Bearer {owner_token}
```

### **Response Success (200 OK)**
```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "userId": 5,
      "userName": "Nguyen Van A",
      "userPhone": "0123456789",
      "courtId": 1,
      "courtName": "Sân số 1",
      "venuesId": 1,
      "venuesName": "Sân bóng ABC",
      "bookingItems": [
        {
          "courtId": 1,
          "courtName": "Sân số 1",
          "startTime": "2025-11-15T14:00:00",
          "endTime": "2025-11-15T16:00:00",
          "price": 400000
        }
      ],
      "totalPrice": 400000,
      "status": "PAYMENT_UPLOADED",
      "paymentProofUrl": "/api/files/payment-proofs/payment_1700044980000_abc123.jpg",
      "paymentProofUploadedAt": "2025-11-15T13:55:00",
      "createdAt": "2025-11-15T13:50:00"
    },
    {
      "id": 15,
      "userId": 8,
      "userName": "Tran Thi B",
      "userPhone": "0987654321",
      "courtId": 2,
      "courtName": "Sân số 2",
      "venuesId": 1,
      "venuesName": "Sân bóng ABC",
      "bookingItems": [
        {
          "courtId": 2,
          "courtName": "Sân số 2",
          "startTime": "2025-11-15T16:00:00",
          "endTime": "2025-11-15T18:00:00",
          "price": 500000
        }
      ],
      "totalPrice": 500000,
      "status": "PAYMENT_UPLOADED",
      "paymentProofUrl": "/api/files/payment-proofs/payment_1700045100000_xyz789.jpg",
      "paymentProofUploadedAt": "2025-11-15T14:10:00",
      "createdAt": "2025-11-15T14:05:00"
    }
  ],
  "message": "Lấy danh sách booking chờ xác nhận của venue thành công."
}
```

### **Response Error (404 Not Found)**
```json
{
  "success": false,
  "data": null,
  "message": "Venue not found"
}
```

### **Response Error (403 Forbidden)**
```json
{
  "success": false,
  "data": null,
  "message": "Bạn không có quyền truy cập venue này"
}
```

---

## 🔄 SO SÁNH CÁC API

| API | Endpoint | Mô tả | Lọc theo venue? |
|-----|----------|-------|-----------------|
| **Pending (All)** | `GET /api/bookings/pending` | Lấy TẤT CẢ pending booking của owner từ TẤT CẢ venues | ❌ Không |
| **Pending (Venue)** ⭐ NEW | `GET /api/bookings/venue/{venueId}/pending` | Lấy pending booking của MỘT venue cụ thể | ✅ Có |
| **All Bookings (Venue)** | `GET /api/bookings/venue/{venueId}` | Lấy TẤT CẢ booking (mọi status) của một venue | ✅ Có (nhưng không lọc status) |

---

## 💡 USE CASES

### **Use Case 1: Owner có nhiều venue**
```javascript
// Owner có 3 venues: A, B, C
// Muốn xem pending booking chỉ của venue A

const venueId = 1; // Venue A
const response = await fetch(`/api/bookings/venue/${venueId}/pending`, {
  headers: {
    'Authorization': `Bearer ${ownerToken}`
  }
});

// Kết quả: Chỉ pending booking của venue A
```

### **Use Case 2: Dashboard với filter**
```javascript
// UI có dropdown chọn venue
const [selectedVenue, setSelectedVenue] = useState('all');

const fetchPendingBookings = async () => {
  const endpoint = selectedVenue === 'all' 
    ? '/api/bookings/pending'  // Tất cả venues
    : `/api/bookings/venue/${selectedVenue}/pending`;  // Venue cụ thể
    
  const response = await fetch(endpoint, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  
  return response.json();
};
```

---

## ✅ BẢO MẬT

API có 2 lớp bảo mật:

1. **Spring Security:** `@PreAuthorize("hasRole('OWNER')")` - Chỉ owner mới gọi được
2. **Business Logic:** Kiểm tra xem venue có thuộc sở hữu của owner hiện tại không

```java
if (!venue.getOwner().getId().equals(currentUser.getId())) {
    throw new UnauthorizedException("Bạn không có quyền truy cập venue này");
}
```

---

## 🎉 KẾT QUẢ

✅ **Build thành công:** Maven compile không có lỗi  
✅ **API sẵn sàng sử dụng:** Có thể test ngay với Postman  
✅ **Bảo mật:** Kiểm tra quyền sở hữu venue  
✅ **Documentation:** Swagger tự động generate docs  

---

## 📌 GHI CHÚ

- API này **CHỈ** trả về booking có status = `PAYMENT_UPLOADED` (đã upload ảnh chuyển khoản, chờ xác nhận)
- Sắp xếp theo thời gian upload chứng minh thanh toán (mới nhất trước)
- Owner chỉ xem được pending booking của venue mình sở hữu
- Nếu muốn xem tất cả booking (mọi status) của venue, dùng API: `GET /api/bookings/venue/{venueId}`

