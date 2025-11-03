-- =========================================
-- SCRIPT SỬA LỖI: Field 'end_time' doesn't have a default value
-- =========================================
-- TẠI SAO PHẢI XÓA CÁC CỘT NÀY?
-- - Trước: booking có court_id, start_time, end_time → Chỉ đặt 1 sân, 1 giờ
-- - Bây giờ: booking có booking_items[] → Đặt NHIỀU sân, NHIỀU giờ trong 1 booking!
-- - Thông tin court_id, start_time, end_time đã chuyển sang bảng booking_item
--
-- CÁCH DÙNG:
-- 1. Copy toàn bộ script này
-- 2. Paste vào MySQL Workbench
-- 3. Nhấn Execute (Ctrl+Enter)
--
-- LƯU Ý: Nếu gặp lỗi "Unknown column" hoặc "Can't DROP",
--         bỏ qua và chạy tiếp! (Nghĩa là đã xóa rồi)
-- =========================================

USE quanlydatsan;

-- Xem cấu trúc hiện tại của bảng booking
DESCRIBE booking;

-- Bước 1: Xóa các cột start_time và end_time (không có foreign key)
ALTER TABLE booking DROP COLUMN start_time;
ALTER TABLE booking DROP COLUMN end_time;

-- Bước 2: Xóa foreign key constraint của court_id trước
-- (Tên constraint này do Hibernate tự generate, có thể khác nhau)
-- Nếu gặp lỗi, chạy query này để tìm tên đúng:
-- SELECT CONSTRAINT_NAME FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
-- WHERE TABLE_NAME = 'booking' AND COLUMN_NAME = 'court_id' AND TABLE_SCHEMA = 'quanlydatsan';
ALTER TABLE booking DROP FOREIGN KEY FK8086mrvdgwdllb70v0cylusv2;

-- Bước 3: Sau đó mới xóa cột court_id
ALTER TABLE booking DROP COLUMN court_id;

-- Xem cấu trúc sau khi xóa
DESCRIBE booking;

-- =========================================
-- KẾT QUẢ MONG ĐỢI
-- =========================================
-- Bảng booking chỉ còn các cột:
-- - id
-- - user_id
-- - total_price
-- - status
-- - expire_time
-- - payment_proof_uploaded
-- - payment_proof_url
-- - payment_proof_uploaded_at
-- - rejection_reason
-- - created_at (nếu có)
-- - updated_at (nếu có)
-- =========================================

SELECT 'Migration completed successfully!' as Status;

