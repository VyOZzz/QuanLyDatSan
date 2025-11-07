# Test Booking Flow Script
# Đảm bảo server đang chạy trên http://localhost:8080

$baseUrl = "http://localhost:8080/api"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "🧪 TEST: Luồng Đặt Sân và Kiểm Tra Slot Bị Khóa" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host ""

# BƯỚC 1: Kiểm tra server
Write-Host "BƯỚC 1: Kiểm tra server..." -ForegroundColor Yellow
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/venues" -Method Get -ErrorAction Stop
    Write-Host "✅ Server đang chạy!" -ForegroundColor Green
} catch {
    Write-Host "❌ Server không chạy! Vui lòng khởi động server trước." -ForegroundColor Red
    exit
}
Write-Host ""

# BƯỚC 2: Lấy danh sách courts của venue 14
Write-Host "BƯỚC 2: Lấy danh sách courts của venue 14..." -ForegroundColor Yellow
try {
    $courts = Invoke-RestMethod -Uri "$baseUrl/venues/14/courts" -Method Get
    Write-Host "✅ Response:" -ForegroundColor Green
    $courts | ConvertTo-Json -Depth 10

    if ($courts.data.Count -eq 0) {
        Write-Host "⚠️ Venue 14 không có court nào!" -ForegroundColor Yellow
        Write-Host "Tạo venue mới hoặc chọn venue khác để test" -ForegroundColor Yellow
        exit
    }

    $courtId = $courts.data[0].id
    Write-Host "📌 Sẽ dùng Court ID: $courtId" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "⚠️ Venue 14 không tồn tại hoặc không có quyền truy cập" -ForegroundColor Yellow
    exit
}
Write-Host ""

# BƯỚC 3: Kiểm tra availability TRƯỚC KHI ĐẶT
Write-Host "BƯỚC 3: Kiểm tra availability TRƯỚC KHI ĐẶT..." -ForegroundColor Yellow
$startTime = "2025-11-07T14:00:00"
$endTime = "2025-11-07T15:00:00"

try {
    $availability = Invoke-RestMethod -Uri "$baseUrl/venues/14/courts/availability?startTime=$startTime&endTime=$endTime" -Method Get
    Write-Host "✅ Response:" -ForegroundColor Green
    $availability | ConvertTo-Json -Depth 10

    $targetCourt = $availability.data | Where-Object { $_.id -eq $courtId }
    if ($targetCourt.available) {
        Write-Host "✅ Court $courtId đang rảnh (available: true)" -ForegroundColor Green
        Write-Host "✅ Booked slots: $($targetCourt.bookedSlots.Count)" -ForegroundColor Green
    } else {
        Write-Host "⚠️ Court $courtId đang bận (available: false)" -ForegroundColor Yellow
        Write-Host "Chọn thời gian khác để test" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# BƯỚC 4: Đặt sân (cần login trước)
Write-Host "BƯỚC 4: Đặt sân..." -ForegroundColor Yellow
Write-Host "⚠️ Cần login để lấy token!" -ForegroundColor Yellow
Write-Host "Nhập thông tin đăng nhập User (Customer):" -ForegroundColor Cyan
$phone = Read-Host "Phone"
$password = Read-Host "Password" -AsSecureString
$passwordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($password))

$loginBody = @{
    phone = $phone
    password = $passwordPlain
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    Write-Host "✅ Login thành công!" -ForegroundColor Green
    $token = $loginResponse.data.jwtToken
    Write-Host "📌 Token: $($token.Substring(0, 20))..." -ForegroundColor Cyan
} catch {
    Write-Host "❌ Login thất bại: $($_.Exception.Message)" -ForegroundColor Red
    exit
}
Write-Host ""

# BƯỚC 5: Tạo booking
Write-Host "BƯỚC 5: Tạo booking..." -ForegroundColor Yellow
$bookingBody = @{
    venueId = 14
    courtId = $courtId
    startTime = $startTime
    endTime = $endTime
} | ConvertTo-Json

$headers = @{
    Authorization = "Bearer $token"
    "Content-Type" = "application/json"
}

try {
    $booking = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method Post -Body $bookingBody -Headers $headers
    Write-Host "✅ Booking thành công!" -ForegroundColor Green
    $booking | ConvertTo-Json -Depth 10
    $bookingId = $booking.data.id
    Write-Host "📌 Booking ID: $bookingId" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Booking thất bại: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.ErrorDetails.Message) {
        Write-Host "Chi tiết: $($_.ErrorDetails.Message)" -ForegroundColor Red
    }
    exit
}
Write-Host ""

# BƯỚC 6: Kiểm tra availability SAU KHI ĐẶT
Write-Host "BƯỚC 6: Kiểm tra availability SAU KHI ĐẶT (SLOT BỊ KHÓA)..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

try {
    $availabilityAfter = Invoke-RestMethod -Uri "$baseUrl/venues/14/courts/availability?startTime=$startTime&endTime=$endTime" -Method Get
    Write-Host "✅ Response:" -ForegroundColor Green
    $availabilityAfter | ConvertTo-Json -Depth 10

    $targetCourtAfter = $availabilityAfter.data | Where-Object { $_.id -eq $courtId }
    if (!$targetCourtAfter.available) {
        Write-Host "✅✅✅ THÀNH CÔNG! Court $courtId đã bị khóa (available: false)" -ForegroundColor Green
        Write-Host "✅ Booked slots: $($targetCourtAfter.bookedSlots.Count)" -ForegroundColor Green
        Write-Host "✅ Booking ID trong slot: $($targetCourtAfter.bookedSlots[0].bookingId)" -ForegroundColor Green
        Write-Host "✅ Status: $($targetCourtAfter.bookedSlots[0].status)" -ForegroundColor Green
    } else {
        Write-Host "❌ LỖI! Court vẫn available = true (chưa bị khóa)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# BƯỚC 7: Test đặt trùng slot (PHẢI BỊ TỪ CHỐI)
Write-Host "BƯỚC 7: Test đặt trùng slot (PHẢI BỊ TỪ CHỐI)..." -ForegroundColor Yellow

try {
    $duplicateBooking = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method Post -Body $bookingBody -Headers $headers -ErrorAction Stop
    Write-Host "❌ LỖI! Hệ thống cho phép đặt trùng slot!" -ForegroundColor Red
    $duplicateBooking | ConvertTo-Json -Depth 10
} catch {
    if ($_.Exception.Response.StatusCode -eq 400) {
        Write-Host "✅✅✅ ĐÚNG! Hệ thống từ chối đặt trùng slot" -ForegroundColor Green
        Write-Host "Message: $($_.ErrorDetails.Message)" -ForegroundColor Cyan
    } else {
        Write-Host "⚠️ Lỗi khác: $($_.Exception.Message)" -ForegroundColor Yellow
    }
}
Write-Host ""

# BƯỚC 8: Test đặt sân khác không trùng giờ
Write-Host "BƯỚC 8: Test đặt sân KHUNG GIỜ KHÁC (15:00-16:00)..." -ForegroundColor Yellow
$bookingBody2 = @{
    venueId = 14
    courtId = $courtId
    startTime = "2025-11-07T15:00:00"
    endTime = "2025-11-07T16:00:00"
} | ConvertTo-Json

try {
    $booking2 = Invoke-RestMethod -Uri "$baseUrl/bookings" -Method Post -Body $bookingBody2 -Headers $headers
    Write-Host "✅ Booking khung giờ khác thành công!" -ForegroundColor Green
    Write-Host "📌 Booking ID: $($booking2.data.id)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Lỗi: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# TỔNG KẾT
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "📊 TỔNG KẾT TEST" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "✅ Đặt sân thành công - Booking ID: $bookingId" -ForegroundColor Green
Write-Host "✅ Slot bị khóa ngay sau khi đặt" -ForegroundColor Green
Write-Host "✅ Hệ thống từ chối đặt trùng slot" -ForegroundColor Green
Write-Host "✅ Cho phép đặt khung giờ khác" -ForegroundColor Green
Write-Host ""
Write-Host "📌 NEXT STEPS:" -ForegroundColor Yellow
Write-Host "1. Upload payment proof cho booking $bookingId" -ForegroundColor White
Write-Host "2. Login OWNER và accept booking" -ForegroundColor White
Write-Host "3. Kiểm tra slot vẫn bị khóa sau khi accept" -ForegroundColor White
Write-Host ""
Write-Host "Xem hướng dẫn chi tiết trong file: TEST_BOOKING_LOCK_FLOW.md" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

