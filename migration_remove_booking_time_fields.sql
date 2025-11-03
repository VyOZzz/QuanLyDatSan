-- =========================================
-- Migration Script: Xóa start_time và end_time khỏi bảng booking
-- Các thông tin này đã được chuyển sang bảng booking_item
-- =========================================

-- Bước 1: Kiểm tra xem các cột có tồn tại không
-- MySQL sẽ báo lỗi nếu cột không tồn tại, nên cần kiểm tra trước

-- Bước 2: Xóa các cột start_time, end_time, và court_id khỏi bảng booking
-- (Vì thông tin này giờ nằm trong booking_item)

ALTER TABLE booking
DROP COLUMN IF EXISTS start_time;

ALTER TABLE booking
DROP COLUMN IF EXISTS end_time;

ALTER TABLE booking
DROP COLUMN IF EXISTS court_id;

-- =========================================
-- HOÀN THÀNH!
-- =========================================
-- Từ giờ thông tin về court, start_time, end_time
-- được lưu trong bảng booking_item
-- Một booking có thể có nhiều booking_item
-- =========================================

