-- =========================================
-- SIMPLE VERSION: Xóa cột total_price
-- Chạy file này nếu bạn CHẮC CHẮN cột total_price tồn tại
-- =========================================

USE quanlydatsan;

-- Tắt safe mode
SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa cột total_price (không kiểm tra)
ALTER TABLE booking DROP COLUMN total_price;

-- Bật lại safe mode
SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

SELECT '✅ Đã xóa cột total_price từ bảng booking' AS 'Status';

-- Kiểm tra lại cấu trúc bảng
DESCRIBE booking;

