# HƯỚNG DẪN CHẠY MIGRATION - Thêm pricePerHour vào Booking

## 📋 TÓM TẮT THAY ĐỔI ĐÃ THỰC HIỆN:

### ✅ 1. Cập nhật Backend Code (ĐÃ XONG)
- ✅ Thêm field `pricePerHour` vào `Booking` entity
- ✅ Thêm field `pricePerHour` vào `BookingResponse` DTO
- ✅ Cập nhật `createBooking()` để lưu `pricePerHour` từ venue
- ✅ Cập nhật `mapToBookingResponse()` để map field mới

### 🔧 2. Chạy Migration Database (CẦN LÀM)

**File migration:** `migration_add_price_per_hour.sql`

**Cách chạy:**

#### Option 1: Sử dụng MySQL Workbench
1. Mở MySQL Workbench
2. Kết nối tới database của project
3. Mở file `migration_add_price_per_hour.sql`
4. Chạy script (Execute SQL Script)

#### Option 2: Sử dụng MySQL Command Line
```bash
# Mở CMD/PowerShell tại folder project
cd D:\Code\QuanLyDatSan

# Chạy migration (thay username/password/database_name)
mysql -u root -p your_database_name < migration_add_price_per_hour.sql
```

#### Option 3: Chạy từ IntelliJ IDEA
1. Mở Database tool window (View → Tool Windows → Database)
2. Kết nối tới MySQL database
3. Right-click database → Run SQL Script
4. Chọn file `migration_add_price_per_hour.sql`

---

## 🧪 KIỂM TRA SAU KHI MIGRATION:

### 1. Verify cấu trúc table:
```sql
DESCRIBE booking;
-- Phải thấy column mới: price_per_hour (BIGINT)
```

### 2. Kiểm tra dữ liệu cũ:
```sql
SELECT id, total_price, price_per_hour, status
FROM booking
ORDER BY id DESC
LIMIT 10;
-- Các booking cũ phải có price_per_hour được tính tự động
```

---

## 🚀 TEST API SAU KHI MIGRATION:

### Test 1: Tạo booking mới
```bash
POST http://localhost:8080/api/bookings
Content-Type: application/json
Authorization: Bearer YOUR_TOKEN

{
  "venueId": 1,
  "courtId": 1,
  "startTime": "2025-11-06T14:00:00",
  "endTime": "2025-11-06T16:00:00"
}

# Response phải có:
{
  "success": true,
  "data": {
    "id": 123,
    "pricePerHour": 200000,  // ✅ Field mới!
    "totalPrice": 400000.0,
    "status": "PENDING_PAYMENT",
    ...
  }
}
```

### Test 2: Lấy danh sách booking
```bash
GET http://localhost:8080/api/bookings/my-bookings
Authorization: Bearer YOUR_TOKEN

# Response phải có pricePerHour trong mỗi booking:
{
  "data": [
    {
      "id": 123,
      "pricePerHour": 200000,  // ✅ Có field này
      "totalPrice": 400000.0,
      ...
    }
  ]
}
```

---

## 📊 KẾT QUẢ MONG ĐỢI:

### Frontend sẽ nhận được:
```json
{
  "id": 123,
  "courtName": "Sân số 2",
  "venuesName": "Star Club",
  "startTime": "2025-11-06T14:00:00",
  "endTime": "2025-11-06T16:00:00",
  "pricePerHour": 200000,      // ✅ Giá gốc từ venue
  "totalPrice": 400000.0,      // ✅ Tổng giá đã tính
  "status": "PAYMENT_UPLOADED"
}
```

### Frontend tính toán:
```kotlin
// Tính số giờ
val hours = Duration.between(startTime, endTime).toMinutes() / 60.0

// Hiển thị giá chi tiết
val displayPrice = pricePerHour * hours
// Ví dụ: 200,000 × 2 = 400,000 VNĐ
```

---

## ⚠️ LƯU Ý QUAN TRỌNG:

1. **Backup database trước khi chạy migration**:
   ```bash
   mysqldump -u root -p your_database_name > backup_before_migration.sql
   ```

2. **Không cần restart server**: Spring Boot sẽ tự động nhận entity mới

3. **Nếu có lỗi "column already exists"**: Column đã được tạo rồi, skip migration

4. **Dữ liệu cũ**: Migration sẽ tự động tính `pricePerHour` cho các booking cũ dựa trên `totalPrice`

---

## 🔄 ROLLBACK (Nếu cần quay lại):

```sql
-- Xóa column nếu cần rollback
ALTER TABLE booking DROP COLUMN price_per_hour;
```

---

## ✅ CHECKLIST HOÀN THÀNH:

- [x] ✅ Thêm field vào `Booking` entity
- [x] ✅ Thêm field vào `BookingResponse` DTO  
- [x] ✅ Cập nhật `createBooking()` lưu `pricePerHour`
- [x] ✅ Cập nhật mapper `mapToBookingResponse()`
- [x] ✅ Tạo migration SQL script
- [ ] ⏳ Chạy migration trên database
- [ ] ⏳ Test API endpoint
- [ ] ⏳ Frontend test hiển thị giá

---

## 🆘 TROUBLESHOOTING:

### Lỗi: "Unknown column 'price_per_hour'"
→ Migration chưa chạy, hãy chạy `migration_add_price_per_hour.sql`

### Lỗi: "column already exists"
→ Column đã được tạo rồi, bỏ qua migration

### Response không có field `pricePerHour`
→ Kiểm tra xem đã compile lại project chưa (Build → Rebuild Project)

### Frontend vẫn hiển thị giá sai
→ Kiểm tra:
1. Backend đã trả về `pricePerHour` chưa?
2. Frontend đã parse field mới chưa?
3. Clear cache browser/app

---

**Sau khi chạy migration, hãy test ngay để đảm bảo mọi thứ hoạt động!** 🚀

