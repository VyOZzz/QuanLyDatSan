-- =========================================
-- Migration Script: Xóa averageRating và totalReviews khỏi bảng venues
-- Tuân thủ chuẩn 3NF - không lưu dữ liệu dẫn xuất
-- Các giá trị này sẽ được tính toán động từ bảng review
-- =========================================

-- Bước 1: Xóa cột average_rating
ALTER TABLE venues
DROP COLUMN average_rating;

-- Bước 2: Xóa cột total_reviews
ALTER TABLE venues
DROP COLUMN total_reviews;

-- =========================================
-- HOÀN THÀNH!
-- =========================================
-- Từ giờ averageRating và totalReviews sẽ được tính toán động
-- khi frontend gọi API lấy thông tin venues
-- Backend sẽ query bảng review để tính:
-- - averageRating = AVG(rating) FROM review WHERE venue_id = ?
-- - totalReviews = COUNT(*) FROM review WHERE venue_id = ?
-- =========================================
