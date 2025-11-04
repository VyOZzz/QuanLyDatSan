# HƯỚNG DẪN CHẠY MIGRATION SQL

## 🚀 CÁCH 1: Chạy trực tiếp trong Terminal

```bash
cd /Users/phammanh/Documents/JavaProject/QuanLyDatSan
mysql -u root -p bookingcourt < migration_remove_venue_rating_fields.sql
```

Khi được hỏi password, nhập password MySQL của bạn.

---

## 🚀 CÁCH 2: Chạy trong MySQL Workbench (KHUYẾN KHÍCH)

1. Mở **MySQL Workbench**
2. Kết nối vào database `bookingcourt`
3. Mở file: `migration_remove_venue_rating_fields.sql`
4. Nhấn nút **Execute** (⚡ hoặc Ctrl+Shift+Enter)
5. Xem kết quả trong Output

---

## 🚀 CÁCH 3: Chạy script tự động

```bash
cd /Users/phammanh/Documents/JavaProject/QuanLyDatSan
chmod +x run_migration.sh
./run_migration.sh
```

Nhập password MySQL khi được hỏi.

---

## ✅ KIỂM TRA SAU KHI CHẠY

### Kiểm tra cấu trúc bảng venues:
```bash
mysql -u root -p -e "DESCRIBE bookingcourt.venues;"
```

**Kết quả mong đợi:** KHÔNG còn thấy 2 cột:
- ❌ `average_rating`
- ❌ `total_reviews`

### Hoặc chạy query:
```sql
SHOW COLUMNS FROM bookingcourt.venues;
```

---

## 🔄 SAU KHI CHẠY MIGRATION

### Rebuild và restart Spring Boot:
```bash
cd /Users/phammanh/Documents/JavaProject/QuanLyDatSan
mvn clean install
mvn spring-boot:run
```

---

## 🧪 TEST API

### Test lấy danh sách venues:
```bash
GET http://localhost:8080/api/venues
```

Response vẫn phải có `averageRating` và `totalReviews` (tính toán từ bảng review).

---

## ⚠️ NẾU GẶP LỖI

### Lỗi: "Column not found"
→ Cột đã bị xóa rồi, không sao cả.

### Lỗi: "Access denied"
→ Kiểm tra lại username/password MySQL.

### Lỗi khi restart Spring Boot
→ Chạy: `mvn clean install` trước khi `mvn spring-boot:run`

---

## 📝 GHI CHÚ

Migration này **AN TOÀN** vì:
- ✅ Chỉ XÓA 2 cột không cần thiết
- ✅ KHÔNG XÓA dữ liệu quan trọng
- ✅ Backend đã được sửa để tính toán động
- ✅ Frontend không cần thay đổi gì

**Nếu muốn rollback:** Không thể rollback tự động, nhưng có thể thêm lại 2 cột bằng:
```sql
ALTER TABLE venues ADD COLUMN average_rating DOUBLE DEFAULT 0.0;
ALTER TABLE venues ADD COLUMN total_reviews INT DEFAULT 0;
```

