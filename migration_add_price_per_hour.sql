-- Migration: Add price_per_hour column to booking table
-- Date: 2025-11-05
-- Purpose: Store venue's price per hour at booking time for accurate price display

-- Add price_per_hour column
ALTER TABLE booking
ADD COLUMN price_per_hour BIGINT;

-- Update existing bookings: Calculate price_per_hour from totalPrice and duration
-- This is a best-effort update for existing data
UPDATE booking b
SET price_per_hour = (
    SELECT ROUND(b.total_price /
           GREATEST(1, TIMESTAMPDIFF(HOUR, bi.start_time, bi.end_time)))
    FROM booking_item bi
    WHERE bi.booking_id = b.id
    LIMIT 1
)
WHERE b.price_per_hour IS NULL;

-- For any bookings without booking_items, set a default value
UPDATE booking
SET price_per_hour = 100000
WHERE price_per_hour IS NULL;

-- Optional: Make it NOT NULL after data migration
-- ALTER TABLE booking MODIFY COLUMN price_per_hour BIGINT NOT NULL;

-- Verify the migration
SELECT id, total_price, price_per_hour, status
FROM booking
ORDER BY id DESC
LIMIT 10;

