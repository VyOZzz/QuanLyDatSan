Frontend – Booking API (tài liệu dành cho FE)

Mục đích
- Tài liệu này mô tả các endpoint liên quan đến chức năng đặt sân (booking) trong backend để FE có thể gọi đúng API, truyền đúng payload và xử lý các trạng thái.

Tệp bạn nên gửi cho FE (tham khảo / đính kèm):
- `src/main/java/com/codewithvy/quanlydatsan/controller/BookingController.java`
- `src/main/java/com/codewithvy/quanlydatsan/dto/BookingRequest.java`
- `src/main/java/com/codewithvy/quanlydatsan/dto/BookingResponse.java`
- `src/main/java/com/codewithvy/quanlydatsan/dto/PaymentProofRequest.java`
- `src/main/java/com/codewithvy/quanlydatsan/dto/BookingRejectRequest.java`
- `src/main/java/com/codewithvy/quanlydatsan/dto/OwnerBankInfoDTO.java`
- `src/main/java/com/codewithvy/quanlydatsan/dto/ApiResponse.java`
- `src/main/java/com/codewithvy/quanlydatsan/service/impl/BookingServiceImpl.java` (để biết business rules)
- `src/main/java/com/codewithvy/quanlydatsan/service/BookingExpirationService.java` (để biết logic expire/complete tự động)
- `src/main/java/com/codewithvy/quanlydatsan/repository/BookingItemRepository.java` (nếu FE cần danh sách slot đã booked)

Chung
- Base path: /api/bookings
- Mọi request cần header Authorization: Bearer <JWT_TOKEN> (tùy backend config). Một số endpoint yêu cầu role cụ thể (USER hoặc OWNER).
- Tất cả response được bọc trong `ApiResponse<T>` với các trường: success, data, message, timestamp.
- Backend sử dụng `LocalDateTime` cho thời gian (không chứa timezone). FE nên gửi datetime ở định dạng ISO_LOCAL_DATE_TIME: "YYYY-MM-DDTHH:mm:ss".

Endpoints (chi tiết)

1) Tạo booking
- POST /api/bookings
- Role: USER
- Request (application/json) — BookingRequest:
  {
    "venueId": Long,
    "courtId": Long,
    "startTime": "2025-11-02T18:00:00",
    "endTime": "2025-11-02T19:00:00"
  }
- Response data: BookingResponse
- Behavior: Tạo booking mới với status = PENDING_PAYMENT, expireTime = now + PAYMENT_EXPIRE_MINUTES (mặc định 5 phút). Tạo kèm BookingItem.
- Notes FE: hiển thị expireTime cho user; nếu user không confirm/upload trước thời hạn thì hệ thống sẽ auto set status = EXPIRED.

2) Upload ảnh chứng minh chuyển khoản
- POST /api/bookings/{id}/upload-payment-proof
- Role: USER
- Request: multipart/form-data, field `file` (jpg|jpeg|png). Tham khảo swagger: max 10MB (FE nên validate trước upload).
- Response data: BookingResponse (bao gồm `paymentProofUrl`, `paymentProofUploaded`, `paymentProofUploadedAt`)
- Behavior: Lưu file, cập nhật paymentProofUrl + paymentProofUploaded=true. Không đổi status tại bước này.
- Example curl (Windows cmd):
  curl -X POST "https://your-api.example.com/api/bookings/123/upload-payment-proof" -H "Authorization: Bearer <TOKEN>" -F "file=@C:\\path\\to\\proof.jpg"

3) Xác nhận đã chuyển (user nhấn "Xác nhận thanh toán")
- PUT /api/bookings/{id}/confirm-payment
- Role: USER
- Request (application/json): PaymentProofRequest
  { "paymentProofUrl": "https://.../uploads/...png" }
- Behavior: Kiểm tra booking có status == PENDING_PAYMENT, booking.paymentProofUploaded == true và expireTime chưa qua -> set status = PAYMENT_UPLOADED, gửi notification cho owner.
- Response data: BookingResponse
- Notes FE: backend bắt buộc phải upload trước khi confirm; nếu chưa upload, API trả lỗi; nếu expireTime đã qua, booking bị set EXPIRED và API trả lỗi.

4) Chủ sân lấy booking cần xử lý (dành cho OWNER)
- GET /api/bookings/pending
- Role: OWNER
- Response data: List<BookingResponse> (status = PAYMENT_UPLOADED)

5) Chủ sân accept booking
- PUT /api/bookings/{id}/accept
- Role: OWNER
- Request: none
- Precondition: booking.status == PAYMENT_UPLOADED
- Behavior: set status = CONFIRMED, notify user
- Response data: BookingResponse

6) Chủ sân reject booking
- PUT /api/bookings/{id}/reject
- Role: OWNER
- Request (application/json): BookingRejectRequest
  { "rejectionReason": "Lý do từ chối" }
- Behavior: set status = REJECTED, set rejectionReason, notify user
- Response data: BookingResponse

7) Lấy booking theo venue (OWNER)
- GET /api/bookings/venue/{venueId}
- Role: OWNER
- Response: List<BookingResponse>
- Implemented bằng: BookingRepository.findByVenueId

8) Lấy booking của user hiện tại
- GET /api/bookings/my-bookings
- Role: USER
- Response: List<BookingResponse>

9) Lấy booking by id
- GET /api/bookings/{id}
- Role: isAuthenticated()
- Response: BookingResponse

10) Hủy booking (user)
- PUT /api/bookings/{id}/cancel
- Role: USER
- Behavior: Chỉ cho hủy nếu không phải CONFIRMED hoặc COMPLETED. Set status = CANCELLED.
- Response: BookingResponse

Các trường quan trọng trong BookingResponse (FE cần hiển thị/quan tâm)
- id, userId, userName
- courtId, courtName, venuesName
- startTime, endTime
- totalPrice
- status (PENDING_PAYMENT | PAYMENT_UPLOADED | CONFIRMED | REJECTED | CANCELLED | EXPIRED | COMPLETED)
- expireTime (nếu status == PENDING_PAYMENT show countdown)
- paymentProofUploaded (boolean)
- paymentProofUrl
- paymentProofUploadedAt
- rejectionReason
- ownerBankInfo (được trả khi booking đang PENDING_PAYMENT để cho user biết tài khoản chủ sân)

Business rules / edge cases FE nên xử lý
- Thời hạn thanh toán: booking được cấp expireTime (mặc định 5 phút). Sau expireTime, scheduler backend (BookingExpirationService) sẽ tự đổi status sang EXPIRED và gửi notification. FE nên hiển thị countdown và disable nút confirm sau expire.
- Upload trước confirm: bắt buộc. Nếu user gọi confirm mà chưa upload ảnh, API trả lỗi.
- Chỉ chủ sân (OWNER) mới gọi accept/reject. FE owner cần token có role OWNER.
- Quá trình là: create -> upload -> confirm -> owner accept/reject -> (system sẽ complete sau khi kết thúc thời gian trận đấu)
- Các concurrent booking: backend kiểm tra trùng slot qua `BookingItemRepository.existsConflictingBooking`. Nếu có trùng, create booking sẽ ném IllegalStateException (FE nên show error message và yêu cầu chọn khung giờ khác).
- Timezones: backend dùng LocalDateTime (no timezone). FE và BE cần thống nhất timezone (thường là cùng timezone server). Khuyến nghị FE gửi server-local datetime; hoặc convert tuỳ yêu cầu dự án.

Errors / status codes (chung)
- 200 OK: thành công (ApiResponse.success = true)
- 400/422: validation error (missing fields, invalid time range)
- 401: unauthorized (token missing/invalid)
- 403: forbidden (role thiếu quyền)
- 404: resource not found (booking/court/user not found)
- 409/400: business error (conflict slot, invalid status transition); backend thường ném IllegalStateException, SecurityException, ResourceNotFoundException => translate sang HTTP tương ứng (check FE behavior)

Gợi ý UI flow & UX
- Sau tạo booking, show modal với thông tin tài khoản owner (ownerBankInfo) và countdown expireTime.
- Bật form upload file; sau upload thành công, show preview ảnh và enable nút "Xác nhận thanh toán".
- Khi gọi confirm, show loading, sau success show message "Đã gửi chứng minh chuyển khoản. Chờ chủ sân xác nhận." và redirect user sang trang My Bookings.
- Chủ sân: có trang danh sách Pending (GET /api/bookings/pending) với ảnh proof, chấp nhận hoặc từ chối kèm lý do.

Tham khảo code backend (để FE hiểu logic)
- `BookingServiceImpl.createBooking` => validates start/end time, kiểm tra conflict slot, tính giá bằng PriceService, tạo Booking + BookingItem, set expireTime = now + PAYMENT_EXPIRE_MINUTES
- `BookingServiceImpl.uploadPaymentProof` => save file via FileStorageService, cập nhật paymentProofUrl và paymentProofUploaded=true (không đổi status)
- `BookingServiceImpl.confirmPayment` => kiểm tra paymentProofUploaded và expireTime, set status = PAYMENT_UPLOADED, gửi notification owner
- `BookingExpirationService` => scheduled job, chạy mỗi phút để set EXPIRED cho PENDING_PAYMENT quá thời hạn, và chạy mỗi 5 phút để set COMPLETED cho CONFIRMED đã kết thúc

Nếu FE cần thêm
- Muốn Postman collection: tôi có thể tạo export nhanh cho các endpoint chính (POST create, POST upload, PUT confirm, PUT accept/reject, GET pending/my-bookings/get-by-id). Hãy trả lời "Postman" và tôi sẽ tạo file `POSTMAN/Booking_API.postman_collection.json`.
- Muốn endpoint trả danh sách booked slots (booking items) cho một court trong khoảng thời gian để hiển thị availability: backend đã có `BookingItemRepository.findBookedSlots(...)` nhưng chưa có controller wrapper — tôi có thể thêm endpoint `/api/bookings/slots?courtId=...&startTime=...&endTime=...` nếu bạn muốn (tôi có thể implement và test nhanh).

---
Ghi chú: log SQL kiểu `select ... from booking ... where status=? and expire_time<?` là query mà backend chạy (scheduler) để tìm booking PENDING_PAYMENT đã quá hạn; đây không phải lỗi mà là hành động mong đợi của hệ thống (xem `BookingExpirationService`).

Kết thúc tài liệu. Nếu bạn muốn tôi:
- tạo Postman collection (gõ: Postman)
- hoặc thêm endpoint để lấy booked slots cho calendar (gõ: slots)
chọn 1 trong 2 và tôi sẽ thực hiện tiếp.
