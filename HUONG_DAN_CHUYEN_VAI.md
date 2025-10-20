# 🎯 HƯỚNG DẪN LUỒNG "CHUYỂN VAI" - USER ↔ OWNER

## 📋 Các thay đổi đã thực hiện:

### 1. ✅ Entity `User.java`
- ❌ **Đã xóa** field `username`
- ✅ **Đã thêm** `@JsonIgnore` cho `password`
- ✅ Login bằng `phone`, Forgot password bằng `email`

### 2. ✅ Service `UserService.java` (MỚI)
- Method `getCurrentUser()`: Lấy user hiện tại từ SecurityContext
- Method `addOwnerRole()`: Thêm ROLE_OWNER cho user (KHÔNG xóa ROLE_USER)

### 3. ✅ Controller `UserController.java` (MỚI)
- `POST /api/users/me/request-owner-role`: API "Trở thành chủ sân"
- `GET /api/users/me`: API lấy thông tin user hiện tại

### 4. ✅ Controller `AuthController.java`
- **Đã sửa** method `register()`: 
  - Mặc định TẤT CẢ user mới đều là `ROLE_USER`
  - Bỏ logic chọn accountType khi đăng ký

---

## 🔄 LUỒNG HOẠT ĐỘNG

### Bước 1️⃣: Đăng ký tài khoản
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "fullname": "Nguyễn Văn A",
  "email": "nguyenvana@gmail.com",
  "phone": "0123456789",
  "password": "123456",
  "confirmPassword": "123456",
  "accountType": "USER"  // ← KHÔNG CÒN QUAN TRỌNG, luôn tạo USER
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": "Registered"
}
```

---

### Bước 2️⃣: Đăng nhập
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "phone": "0123456789",
  "password": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login success",
  "data": {
    "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "phone": "0123456789",
    "roles": ["ROLE_USER"]  // ← Chỉ có 1 role
  }
}
```

---

### Bước 3️⃣: Lấy thông tin user hiện tại
```http
GET http://localhost:8080/api/users/me
Authorization: Bearer {JWT_TOKEN}
```

**⚠️ CHÚ Ý:** URL là `/api/users/me` KHÔNG PHẢI `/api/auth/users/me`

**📝 CÁCH ĐIỀN AUTHORIZATION TRONG POSTMAN:**

**Cách 1: Dùng tab "Authorization" (Khuyến nghị ⭐)**
1. Chọn tab **"Authorization"**
2. **Type**: Chọn `Bearer Token`
3. **Token**: Paste JWT token từ bước login (KHÔNG cần gõ "Bearer")
4. Nhấn **Send**

**Cách 2: Dùng tab "Headers" thủ công**
1. Chọn tab **"Headers"**
2. Thêm header:
   - **Key**: `Authorization`
   - **Value**: `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (có từ "Bearer" + space + token)
3. Nhấn **Send**

**⚠️ LƯU Ý:** 
- Phải COPY token từ response login ở Bước 2
- Token có dạng: `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (rất dài)
- Token có thời hạn (thường 24h), hết hạn phải login lại

**Response:**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "fullname": "Nguyễn Văn A",
    "phone": "0123456789",
    "email": "nguyenvana@gmail.com",
    "roles": [
      {
        "id": 1,
        "name": "ROLE_USER"
      }
    ]
  }
}
```

---

### Bước 4️⃣: Nâng cấp thành chủ sân (Trong màn hình "Cài đặt")
```http
POST http://localhost:8080/api/users/me/request-owner-role
Authorization: Bearer {JWT_TOKEN}
```

**⚠️ CHÚ Ý:** URL là `/api/users/me/request-owner-role` KHÔNG PHẢI `/api/auth/users/me/request-owner-role`

**Response:**
```json
{
  "success": true,
  "message": "Đã nâng cấp thành chủ sân thành công! Vui lòng đăng nhập lại để cập nhật quyền.",
  "data": "Success"
}
```

---

### Bước 5️⃣: Đăng nhập lại để có JWT mới
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "phone": "0123456789",
  "password": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login success",
  "data": {
    "jwtToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "id": 1,
    "phone": "0123456789",
    "roles": ["ROLE_USER", "ROLE_OWNER"]  // ← BÂY GIỜ CÓ 2 ROLES!
  }
}
```

---

## 🎨 PHÍA FRONTEND CẦN LÀM GÌ?

### 1. Màn hình "Cài đặt" / "Tài khoản"
```javascript
// Kiểm tra user hiện tại có role OWNER chưa
const hasOwnerRole = user.roles.includes("ROLE_OWNER");

// Nếu chưa có → hiển thị nút "Trở thành chủ sân"
if (!hasOwnerRole) {
  return <Button onClick={requestOwnerRole}>🏟️ Trở thành chủ sân</Button>;
}

// Hàm gọi API
async function requestOwnerRole() {
  const response = await fetch("/api/users/me/request-owner-role", {
    method: "POST",
    headers: {
      "Authorization": `Bearer ${token}`
    }
  });
  
  if (response.ok) {
    alert("Đã nâng cấp thành công! Vui lòng đăng nhập lại.");
    // Đăng xuất v�� yêu cầu login lại
    logout();
  }
}
```

---

### 2. Màn hình chính (Home)
```javascript
// Sau khi login, kiểm tra user có nhiều hơn 1 role không
const userRoles = jwtResponse.data.roles;

if (userRoles.includes("ROLE_USER") && userRoles.includes("ROLE_OWNER")) {
  // Hiển thị nút "Chuyển chế độ"
  return (
    <div>
      <button onClick={() => setMode("USER")}>👤 Chế độ Đặt sân</button>
      <button onClick={() => setMode("OWNER")}>🏢 Chế độ Quản lý</button>
    </div>
  );
}
```

---

### 3. Logic "Chuyển chế độ"
```javascript
const [currentMode, setCurrentMode] = useState("USER"); // Mặc định là USER

// Khi ở chế độ USER
if (currentMode === "USER") {
  return <UserHomePage />; // Màn hình tìm kiếm, đặt sân
}

// Khi ở chế độ OWNER
if (currentMode === "OWNER") {
  return <OwnerDashboard />; // Màn hình quản lý sân
}
```

**LƯU Ý:** JWT token đã chứa cả 2 quyền, nên dù ở chế độ nào, user vẫn gọi được cả API USER và OWNER. Frontend chỉ cần điều khiển GIỮ LIỆU hiển thị thôi.

---

## ✅ CHECKLIST

- [x] User đăng ký → Mặc định là ROLE_USER
- [x] User login → JWT có roles: ["ROLE_USER"]
- [x] User vào cài đặt → Gọi API "Trở thành chủ sân"
- [x] User login lại → JWT có roles: ["ROLE_USER", "ROLE_OWNER"]
- [x] Frontend hiển thị nút "Chuyển chế độ"
- [x] User có thể chuyển qua lại giữa 2 chế độ

---

## 🧪 TEST TRÊN POSTMAN

### Test Case 1: Đăng ký user mới
1. Gọi `POST /api/auth/register`
2. Kiểm tra response: `"User registered successfully"`

### Test Case 2: Login
1. Gọi `POST /api/auth/login`
2. Kiểm tra response: `roles: ["ROLE_USER"]`
3. Lưu JWT token

### Test Case 3: Xem thông tin user
1. Gọi `GET /api/users/me` với Bearer token
2. Kiểm tra response có field `roles`

### Test Case 4: Nâng cấp thành chủ sân
1. Gọi `POST /api/users/me/request-owner-role` với Bearer token
2. Kiểm tra response: `"Đã nâng cấp thành công"`

### Test Case 5: Login lại
1. Gọi `POST /api/auth/login`
2. Kiểm tra response: `roles: ["ROLE_USER", "ROLE_OWNER"]`

### Test Case 6: Thử nâng cấp lần 2 (phải báo lỗi)
1. Gọi lại `POST /api/users/me/request-owner-role`
2. Kiểm tra response: `"Bạn đã là chủ sân rồi!"`

---

## 📝 GHI CHÚ

- **Không cần đăng xuất/đăng nhập** nếu Frontend lưu roles trong state và tự update sau khi nâng cấp
- **Khuyến nghị:** Sau khi nâng cấp, bắt user login lại để có JWT mới chứa cả 2 roles
- **Bảo mật:** API `request-owner-role` chỉ cho phép user có `ROLE_USER` gọi (kiểm tra bằng `@PreAuthorize`)

---

## 🚀 CHẠY ỨNG DỤNG

```bash
# Build lại project
mvnw clean install

# Chạy ứng dụng
mvnw spring-boot:run
```

Sau đó test trên Postman theo các test case ở trên!
