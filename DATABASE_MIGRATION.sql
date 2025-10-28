-- =====================================================
-- DATABASE MIGRATION SCRIPT - Fix Database Design Issues
-- Ngày: 2025-10-28
-- Mô tả: Sửa các vấn đề thiết kế database theo roadmap
-- =====================================================

-- ============================================
-- BƯỚC 1: Tạo bảng booking_item mới
-- ============================================
CREATE TABLE IF NOT EXISTS booking_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    court_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    price DOUBLE NOT NULL,
    CONSTRAINT fk_booking_item_booking FOREIGN KEY (booking_id) REFERENCES booking(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_item_court FOREIGN KEY (court_id) REFERENCES court(id)
);

-- Tạo index để tăng tốc độ query
CREATE INDEX idx_booking_item_booking_id ON booking_item(booking_id);
CREATE INDEX idx_booking_item_court_id ON booking_item(court_id);
CREATE INDEX idx_booking_item_time ON booking_item(start_time, end_time);

-- ============================================
-- BƯỚC 2: Migrate dữ liệu từ booking sang booking_item
-- ============================================
-- Nếu bảng booking có dữ liệu cũ với court_id, start_time, end_time
INSERT INTO booking_item (booking_id, court_id, start_time, end_time, price)
SELECT
    id as booking_id,
    court_id,
    start_time,
    end_time,
    total_price as price
FROM booking
WHERE court_id IS NOT NULL
  AND start_time IS NOT NULL
  AND end_time IS NOT NULL;

-- ============================================
-- BƯỚC 3: Xóa các cột không cần thiết khỏi booking
-- ============================================
ALTER TABLE booking DROP FOREIGN KEY IF EXISTS booking_ibfk_1;
ALTER TABLE booking DROP COLUMN IF EXISTS court_id;
ALTER TABLE booking DROP COLUMN IF EXISTS start_time;
ALTER TABLE booking DROP COLUMN IF EXISTS end_time;

-- ============================================
-- BƯỚC 4: Xóa cột is_booked khỏi court
-- ============================================
ALTER TABLE court DROP COLUMN IF EXISTS is_booked;

-- ============================================
-- BƯỚC 5: Thêm venue_id vào service
-- ============================================
ALTER TABLE service ADD COLUMN IF NOT EXISTS venue_id BIGINT;

-- Migrate dữ liệu: lấy venue_id từ venues_detail
UPDATE service s
INNER JOIN venues_detail vd ON s.venues_detail_id = vd.id
INNER JOIN venues v ON vd.venues_id = v.id
SET s.venue_id = v.id
WHERE s.venue_id IS NULL;

-- Thêm foreign key
ALTER TABLE service ADD CONSTRAINT fk_service_venue
FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE;

-- Đặt NOT NULL sau khi đã migrate dữ liệu
-- ALTER TABLE service MODIFY venue_id BIGINT NOT NULL;

-- ============================================
-- BƯỚC 6: Gộp venues_detail vào venues
-- ============================================

-- Thêm các cột mới vào venues
ALTER TABLE venues ADD COLUMN IF NOT EXISTS title VARCHAR(255);

-- Tạo bảng mới cho images (thay thế venues_detail_images)
CREATE TABLE IF NOT EXISTS venues_images (
    venue_id BIGINT NOT NULL,
    image VARCHAR(500),
    CONSTRAINT fk_venues_images_venue FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- Migrate dữ liệu từ venues_detail sang venues
UPDATE venues v
INNER JOIN venues_detail vd ON v.id = vd.venues_id
SET v.title = vd.title
WHERE v.title IS NULL;

-- Migrate images từ venues_detail_images sang venues_images
INSERT INTO venues_images (venue_id, image)
SELECT vd.venues_id, vdi.image
FROM venues_detail vd
INNER JOIN venues_detail_images vdi ON vd.id = vdi.venues_detail_id
WHERE NOT EXISTS (
    SELECT 1 FROM venues_images vi
    WHERE vi.venue_id = vd.venues_id AND vi.image = vdi.image
);

-- TÙY CHỌN: Xóa các bảng cũ (CHỈ CHẠY KHI ĐÃ CHẮC CHẮN)
-- DROP TABLE IF EXISTS booked_court;
-- DROP TABLE IF EXISTS venues_detail_images;
-- DROP TABLE IF EXISTS venues_detail;

-- ============================================
-- BƯỚC 7: Tạo view để dễ dàng query
-- ============================================
CREATE OR REPLACE VIEW v_booking_details AS
SELECT
    b.id as booking_id,
    b.user_id,
    b.total_price,
    b.status,
    b.expire_time,
    b.payment_proof_uploaded,
    b.payment_proof_url,
    b.rejection_reason,
    bi.id as booking_item_id,
    bi.court_id,
    bi.start_time,
    bi.end_time,
    bi.price as item_price,
    c.description as court_description,
    v.id as venue_id,
    v.name as venue_name,
    u.id as owner_id,
    u.fullname as owner_name
FROM booking b
LEFT JOIN booking_item bi ON b.id = bi.booking_id
LEFT JOIN court c ON bi.court_id = c.id
LEFT JOIN venues v ON c.venues_id = v.id

