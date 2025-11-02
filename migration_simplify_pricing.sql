-- =========================================
-- Migration Script: Đơn giản hóa hệ thống giá
-- Xóa hệ thống PriceRules, chuyển sang giá cố định
-- =========================================

-- Bước 1: Thêm column price_per_hour vào table venues
ALTER TABLE venues
ADD COLUMN price_per_hour DOUBLE NOT NULL DEFAULT 100000.0;

-- Bước 2: Migrate dữ liệu từ price_rules sang venues (nếu có)
-- Lấy giá trung bình của tất cả price rules cho mỗi venue
UPDATE venues v
SET v.price_per_hour = (
    SELECT AVG(pr.price_per_hour)
    FROM price_rules pr
    WHERE pr.venues_id = v.id
)
WHERE EXISTS (
    SELECT 1 FROM price_rules pr WHERE pr.venues_id = v.id
);

-- Bước 3: Xóa table price_rules
DROP TABLE IF EXISTS price_rules;

-- =========================================
-- HOÀN THÀNH!
-- =========================================
-- Từ giờ mỗi venue chỉ có 1 giá cố định: price_per_hour
-- Không còn khung giờ phức tạp nữa
-- =========================================

