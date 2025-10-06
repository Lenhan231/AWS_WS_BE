    password = "Password123"
    firstName = "John"
    lastName = "Doe"
    role = "CLIENT_USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json"
```

### **2. Đăng nhập**

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "Password123"
}
```

**Response Success (200):**
```json
{
  "message": "Login successful",
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+84901234567",
    "role": "CLIENT_USER",
    "active": true
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Test với cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"john.doe@example.com\",\"password\":\"Password123\"}"
```

### **3. Get Current User (cần token)**

**Endpoint:** `GET /api/v1/auth/me`

**Headers:**
```
Authorization: Bearer <token_từ_login_hoặc_register>
```

**Response Success (200):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+84901234567",
  "role": "CLIENT_USER",
  "active": true,
  "profileImageUrl": null
}
```

**Test với cURL:**
```bash
curl http://localhost:8080/api/v1/auth/me ^
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### **4. Update Profile (cần token)**

**Endpoint:** `PUT /api/v1/auth/me`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "firstName": "Johnny",
  "phoneNumber": "+84909876543"
}
```

**Response Success (200):**
```json
{
  "id": 1,
  "email": "john.doe@example.com",
  "firstName": "Johnny",
  "lastName": "Doe",
  "phoneNumber": "+84909876543",
  "role": "CLIENT_USER"
}
```

### **5. Change Password (cần token)**

**Endpoint:** `POST /api/v1/auth/change-password`

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "currentPassword": "Password123",
  "newPassword": "NewPassword456"
}
```

**Response Success (200):**
```json
{
  "message": "Password changed successfully"
}
```

---

## 🔐 Password Requirements

Để đảm bảo bảo mật, password phải:
- ✅ Tối thiểu 8 ký tự
- ✅ Có ít nhất 1 chữ hoa (A-Z)
- ✅ Có ít nhất 1 chữ thường (a-z)
- ✅ Có ít nhất 1 số (0-9)

**Ví dụ password hợp lệ:**
- `Password123`
- `MySecure@Pass1`
- `Test1234`

**Password không hợp lệ:**
- `password` (thiếu chữ hoa và số)
- `Pass12` (quá ngắn)
- `PASSWORD123` (thiếu chữ thường)

---

## 📱 Test với Postman

### **Setup Collection**

1. **Create New Collection:** `Easy Body Auth`

2. **Add Environment Variables:**
```
base_url: http://localhost:8080/api/v1
token: (sẽ set tự động sau khi login)
```

3. **Request 1: Register**
```
Method: POST
URL: {{base_url}}/auth/register
Body (raw JSON):
{
  "email": "test@example.com",
  "password": "Test1234",
  "firstName": "Test",
  "lastName": "User",
  "role": "CLIENT_USER"
}

Tests (Script để save token):
pm.test("Registration successful", function() {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    pm.expect(pm.response.code).to.equal(201);
});
```

4. **Request 2: Login**
```
Method: POST
URL: {{base_url}}/auth/login
Body (raw JSON):
{
  "email": "test@example.com",
  "password": "Test1234"
}

Tests (Script để save token):
pm.test("Login successful", function() {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.token);
    pm.expect(pm.response.code).to.equal(200);
});
```

5. **Request 3: Get Profile**
```
Method: GET
URL: {{base_url}}/auth/me
Headers:
  Authorization: Bearer {{token}}
```

---

## ⚠️ Common Errors & Solutions

### **Error: "Login failed for user 'sa'"**

**Nguyên nhân:** SQL Server Authentication chưa được enable hoặc password sai

**Giải pháp:**
1. Mở SSMS với Windows Authentication
2. Server Properties → Security → SQL Server and Windows Authentication mode
3. Restart SQL Server service
4. Reset SA password:
```sql
ALTER LOGIN sa WITH PASSWORD = 'YourStrong@Password123';
```

### **Error: "A network-related or instance-specific error"**

**Nguyên nhân:** TCP/IP chưa được enable

**Giải pháp:**
1. SQL Server Configuration Manager
2. Protocols for MSSQLSERVER → Enable TCP/IP
3. Restart SQL Server service

### **Error: "Cannot connect to localhost"**

**Nguyên nhân:** SQL Server service chưa chạy

**Giải pháp:**
```
1. Open Services (services.msc)
2. Find "SQL Server (MSSQLSERVER)"
3. Right-click → Start
```

### **Error: "Validation Error: Password must contain..."**

**Nguyên nhân:** Password không đủ mạnh

**Giải pháp:** Sử dụng password có:
- Ít nhất 8 ký tự
- Chữ hoa, chữ thường, và số

---

## 🎯 Test Scenarios (Full Flow)

### **Scenario 1: Đăng ký User mới**

```bash
# 1. Đăng ký Client User
POST /api/v1/auth/register
{
  "email": "client@test.com",
  "password": "Client123",
  "firstName": "Client",
  "lastName": "User",
  "role": "CLIENT_USER"
}
# → Lưu token

# 2. Verify bằng cách get profile
GET /api/v1/auth/me
Header: Authorization: Bearer <token>
```

### **Scenario 2: Đăng ký Gym Staff**

```bash
# 1. Đăng ký Gym Staff
POST /api/v1/auth/register
{
  "email": "gym@test.com",
  "password": "Gym123",
  "firstName": "Gym",
  "lastName": "Owner",
  "role": "GYM_STAFF"
}
# → Lưu token

# 2. Login lại
POST /api/v1/auth/login
{
  "email": "gym@test.com",
  "password": "Gym123"
}
# → Nhận token mới
```

### **Scenario 3: Đổi Password**

```bash
# 1. Login
POST /api/v1/auth/login
{
  "email": "client@test.com",
  "password": "Client123"
}
# → Lưu token

# 2. Đổi password
POST /api/v1/auth/change-password
Header: Authorization: Bearer <token>
{
  "currentPassword": "Client123",
  "newPassword": "NewClient456"
}

# 3. Login với password mới
POST /api/v1/auth/login
{
  "email": "client@test.com",
  "password": "NewClient456"
}
```

---

## 🗄️ Verify Data trong SQL Server

```sql
-- Xem tất cả users
USE easybody_db;
GO

SELECT 
    id,
    email,
    first_name,
    last_name,
    role,
    active,
    created_at
FROM users;
GO

-- Password sẽ được hash bởi bcrypt (không thấy plaintext)
SELECT 
    email,
    LEFT(password, 20) + '...' as hashed_password
FROM users;
GO
```

---

## ✅ Checklist Test Authentication

- [ ] SQL Server đã cài đặt và chạy
- [ ] Database `easybody_db` đã được tạo
- [ ] Backend server chạy thành công (npm run dev)
- [ ] Tables đã được tạo tự động
- [ ] Test Register user mới → Nhận token
- [ ] Test Login với email/password → Nhận token
- [ ] Test GET /auth/me với token → Nhận user info
- [ ] Test Update profile
- [ ] Test Change password
- [ ] Verify data trong SQL Server với SSMS

---

## 🚀 Next Steps

Sau khi test authentication thành công:

1. ✅ **Test Gym APIs** - Tạo gym, list gyms
2. ✅ **Test PT User APIs** - Tạo PT profile
3. ✅ **Test Offers APIs** - Tạo offers
4. ✅ **Integrate Frontend** - Connect React/Vue app
5. ⏳ **Setup AWS Cognito** (khi cần production)
6. ⏳ **Setup AWS S3** (khi cần upload images)

---

## 📚 API Documentation

Xem file **FRONTEND_GUIDE.md** để biết chi tiết tất cả APIs có sẵn.

---

**Xong! Bây giờ bạn đã có backend Node.js với SQL Server và authentication hoàn chỉnh! 🎉**
# 🗄️ Hướng dẫn Setup SQL Server Local + Test Authentication

## 📋 Tổng quan

Backend hiện đã được cấu hình để:
- ✅ Sử dụng **SQL Server** thay vì PostgreSQL
- ✅ Authentication **LOCAL** với JWT (không cần AWS Cognito)
- ✅ Đăng ký/Đăng nhập với bcrypt password hashing
- ✅ Sẵn sàng để test ngay!

---

## 💻 Cài đặt SQL Server

### **Windows**

#### **Option 1: SQL Server Express (Khuyến nghị cho Development)**

**Bước 1: Download SQL Server 2022 Express**
1. Truy cập: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
2. Click "Download now" ở mục **Express**
3. Chạy installer

**Bước 2: Cài đặt**
```
- Installation Type: Basic
- Accept License Terms
- Install Location: Default (C:\Program Files\Microsoft SQL Server)
- Instance Configuration: Default Instance (MSSQLSERVER)
```

**Bước 3: Install SQL Server Management Studio (SSMS)**
1. Sau khi cài SQL Server, installer sẽ hiện link download SSMS
2. Hoặc truy cập: https://aka.ms/ssmsfullsetup
3. Download và cài đặt SSMS

**Bước 4: Enable TCP/IP và SQL Server Authentication**

Mở **SQL Server Configuration Manager**:
```
1. Search "SQL Server Configuration Manager" trong Windows
2. Expand "SQL Server Network Configuration"
3. Click "Protocols for MSSQLSERVER"
4. Right-click "TCP/IP" → Enable
5. Restart SQL Server service
```

Enable SQL Server Authentication:
```
1. Mở SSMS
2. Connect với Windows Authentication
3. Right-click server name → Properties
4. Security → SQL Server and Windows Authentication mode
5. Restart SQL Server service
```

**Bước 5: Create SA user password**

Mở SSMS và chạy:
```sql
-- Enable SA account và đặt password
USE master;
GO
ALTER LOGIN sa ENABLE;
GO
ALTER LOGIN sa WITH PASSWORD = 'YourStrong@Password123';
GO
```

#### **Option 2: SQL Server Developer Edition (Full version, Free)**

1. Download: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
2. Click "Download now" ở mục **Developer**
3. Cài đặt tương tự Express nhưng có nhiều features hơn

---

## 🔧 Setup Database cho Easy Body

### **Bước 1: Connect SQL Server với SSMS**

```
Server name: localhost
Authentication: SQL Server Authentication
Login: sa
Password: YourStrong@Password123
```

### **Bước 2: Tạo Database**

Chạy query sau trong SSMS:

```sql
-- Tạo database
CREATE DATABASE easybody_db;
GO

-- Verify
SELECT name FROM sys.databases WHERE name = 'easybody_db';
GO
```

### **Bước 3: Enable SQL Server Browser (nếu cần)**

Nếu gặp lỗi connection:
```
1. Open "SQL Server Configuration Manager"
2. Click "SQL Server Services"
3. Right-click "SQL Server Browser" → Start
4. Right-click "SQL Server Browser" → Properties → Set "Start Mode" to "Automatic"
```

---

## ⚙️ Cấu hình Backend

File `.env` đã được cấu hình sẵn:

```env
# Database Configuration (SQL SERVER LOCAL)
DB_HOST=localhost
DB_PORT=1433
DB_NAME=easybody_db
DB_USER=sa
DB_PASSWORD=YourStrong@Password123
DB_DIALECT=mssql

# JWT Configuration (LOCAL AUTH)
JWT_SECRET=easybody_super_secret_key_2024_change_in_production
JWT_EXPIRES_IN=24h
```

**⚠️ Lưu ý:** Nếu bạn dùng password khác cho SA user, hãy cập nhật `DB_PASSWORD` trong file `.env`

---

## 🚀 Start Backend Server

```bash
cd C:\Users\kenfi\Desktop\AWS_WS_BE
npm run dev
```

**Kết quả mong đợi:**
```
✅ Database connection established successfully.
✅ Database synchronized.
🚀 Server is running on port 8080
📡 API Base URL: http://localhost:8080/api/v1
🌍 Environment: development
```

Sequelize sẽ **tự động tạo tables** trong SQL Server:
- users (có cột password để lưu hashed password)
- locations
- gyms
- pt_users
- gym_pt_associations
- offers
- ratings
- reports

### **Verify Tables trong SSMS:**

```sql
USE easybody_db;
GO

-- List all tables
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE';
GO

-- View users table structure
EXEC sp_help 'users';
GO
```

---

## 🧪 Test Authentication APIs

### **1. Đăng ký User mới**

**Endpoint:** `POST /api/v1/auth/register`

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "Password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+84901234567",
  "role": "CLIENT_USER"
}
```

**Response Success (201):**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+84901234567",
    "role": "CLIENT_USER",
    "active": true,
    "createdAt": "2025-10-06T10:30:00.000Z"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Test với cURL:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"john.doe@example.com\",\"password\":\"Password123\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"role\":\"CLIENT_USER\"}"
```

**Test với PowerShell:**
```powershell
$body = @{
    email = "john.doe@example.com"

