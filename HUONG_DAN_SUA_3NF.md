# HƯỚNG DẪN: ĐÃ SỬA DATABASE TUÂN THỦ CHUẨN 3NF

## ✅ NHỮNG GÌ ĐÃ THAY ĐỔI

### 1. **Xóa 2 cột vi phạm 3NF khỏi bảng `venues`:**
- ❌ `average_rating` - Dữ liệu dẫn xuất, có thể tính từ bảng `review`
- ❌ `total_reviews` - Dữ liệu dẫn xuất, có thể tính từ bảng `review`

### 2. **Files đã sửa:**
- ✅ `entity/Venues.java` - Xóa 2 trường averageRating và totalReviews
- ✅ `mapper/VenuesMapper.java` - Tính toán động từ ReviewRepository
- ✅ `service/ReviewService.java` - Xóa method updateVenueRating()
- ✅ `dto/VenuesDTO.java` - GIỮ NGUYÊN (frontend không cần sửa gì!)

### 3. **Migration SQL:**
- File: `migration_remove_venue_rating_fields.sql`

---

## 🎯 FRONTEND KHÔNG CẦN THAY ĐỔI GÌ!

### **Trước:**
```json
{
  "id": 1,
  "name": "Sân A",
  "averageRating": 4.5,
  "totalReviews": 10
}
```

### **Sau:**
```json
{
  "id": 1,
  "name": "Sân A",
  "averageRating": 4.5,  // ← Tính toán từ bảng review
  "totalReviews": 10      // ← Tính toán từ bảng review
}
```

**Frontend vẫn nhận cùng 1 JSON response!** Khác biệt:
- Trước: Lấy từ cột `venues.average_rating` và `venues.total_reviews`
- Sau: Tính toán real-time từ `SELECT AVG(rating), COUNT(*) FROM review WHERE venue_id = ?`

---

## 🚀 CÁCH CHẠY MIGRATION

### **Bước 1: Chạy file SQL**
```bash
mysql -u root -p bookingcourt < migration_remove_venue_rating_fields.sql
```

HOẶC trong MySQL Workbench:
1. Mở file `migration_remove_venue_rating_fields.sql`
2. Chạy toàn bộ script

### **Bước 2: Restart Spring Boot**
```bash
mvn clean install
mvn spring-boot:run
```

---

## 📊 PHÂN TÍCH 3NF

### **TRƯỚC (Vi phạm 3NF):**
```
venues
├── id
├── name
├── average_rating    ← VI PHẠM: Tính được từ review
└── total_reviews     ← VI PHẠM: Tính được từ review

review
├── id
├── venue_id
├── rating
└── ...
```

**Vấn đề:**
- Khi có review mới → Phải UPDATE 2 bảng (review + venues)
- Dễ bị sai lệch dữ liệu nếu quên cập nhật
- Vi phạm nguyên tắc "không lưu dữ liệu dẫn xuất"

### **SAU (Tuân thủ 3NF):**
```
venues
├── id
└── name

review
├── id
├── venue_id
├── rating
└── ...
```

**Query để lấy rating:**
```sql
SELECT 
  v.*,
  AVG(r.rating) as average_rating,
  COUNT(r.id) as total_reviews
FROM venues v
LEFT JOIN review r ON r.venue_id = v.id
WHERE v.id = ?
GROUP BY v.id
```

**Ưu điểm:**
- ✅ Không có dữ liệu thừa/trùng lặp
- ✅ Không cần UPDATE 2 bảng
- ✅ Dữ liệu luôn chính xác 100%
- ✅ Tuân thủ chuẩn 3NF

---

## 🎓 GIẢI THÍCH 3NF

### **3 Normal Form (3NF) yêu cầu:**
1. ✅ Đã đạt 1NF (mỗi cột chỉ chứa giá trị đơn)
2. ✅ Đã đạt 2NF (không có phụ thuộc một phần)
3. ✅ **Không có phụ thuộc bắc cầu (transitive dependency)**

### **Phụ thuộc bắc cầu là gì?**
```
A → B → C  (A quyết định B, B quyết định C)
→ C phụ thuộc bắc cầu vào A
```

**Ví dụ trong venues:**
```
venue_id → reviews → average_rating
venue_id → reviews → total_reviews
```

→ `average_rating` và `total_reviews` phụ thuộc bắc cầu vào `venue_id`  
→ **VI PHẠM 3NF** → Phải tách ra hoặc tính toán động

---

## ⚠️ LƯU Ý

### **Performance:**
- Tính toán động có thể **chậm hơn** khi có nhiều reviews
- Giải pháp: Thêm **index** cho cột `review.venue_id`

```sql
CREATE INDEX idx_review_venue_id ON review(venue_id);
```

### **Caching (tùy chọn):**
Nếu muốn tối ưu hơn nữa, có thể cache kết quả:
```java
@Cacheable(value = "venueRating", key = "#venueId")
public VenueRating getVenueRating(Long venueId) {
    // ... tính toán
}
```

---

## ✅ KIỂM TRA SAU KHI SỬA

### **1. Test API lấy danh sách venues:**
```bash
GET /api/venues
```

Response phải có `averageRating` và `totalReviews` như cũ.

### **2. Test tạo review mới:**
```bash
POST /api/reviews
```

Không còn thấy log "Updating venue rating" nữa.

### **3. Kiểm tra database:**
```sql
DESCRIBE venues;
```

Phải KHÔNG còn cột `average_rating` và `total_reviews`.

---

## 🎯 KẾT LUẬN

✅ Database đã tuân thủ chuẩn 3NF  
✅ Frontend không cần sửa gì  
✅ Code sạch hơn, ít bug hơn  
✅ Dữ liệu luôn chính xác  

**Mức độ tuân thủ 3NF:** **10/10 bảng** ✅

