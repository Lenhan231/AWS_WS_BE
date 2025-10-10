# Archived – không còn dùng cho Spring Boot/Postgres
# Quick Start Auth

> ⚠️ Legacy Reference – quick test workflow for the old Node.js + SQL Server backend. The new Spring Boot service uses AWS Cognito and PostgreSQL (see [`../api/API_DOCUMENTATION.md`](../api/API_DOCUMENTATION.md)).

# 🚀 QUICK START - Test Đăng Ký/Đăng Nhập

## ⚡ Setup Nhanh (5 phút)

### **Bước 1: Cài SQL Server Express**
1. Download: https://go.microsoft.com/fwlink/p/?linkid=2216019
2. Chạy file → Chọn "Basic" → Install
3. Nhớ lại **Connection String** hiển thị sau khi cài xong

### **Bước 2: Enable SQL Authentication**
```sql
-- Mở SSMS (SQL Server Management Studio)
-- Connect với Windows Authentication
-- Chạy query này:

USE master;
GO
ALTER LOGIN sa ENABLE;
GO
ALTER LOGIN sa WITH PASSWORD = 'YourStrong@Password123';
GO
```

### **Bước 3: Tạo Database**
```sql
CREATE DATABASE easybody_db;
GO
```

### **Bước 4: Cập nhật file .env**
File `.env` đã được config sẵn:
```env
DB_HOST=localhost
DB_PORT=1433
DB_NAME=easybody_db
DB_USER=sa
DB_PASSWORD=YourStrong@Password123
```

**⚠️ Nếu dùng password khác, hãy sửa `DB_PASSWORD`**

### **Bước 5: Start Server**
```bash
npm run dev
```

Xong! Server sẽ tự động tạo tables.

---

## 🧪 Test APIs Ngay (Copy & Paste)

### **1. Đăng ký User mới**

**Windows Command Prompt:**
```cmd
curl -X POST http://localhost:8080/api/v1/auth/register -H "Content-Type: application/json" -d "{\"email\":\"test@example.com\",\"password\":\"Test1234\",\"firstName\":\"Test\",\"lastName\":\"User\",\"role\":\"CLIENT_USER\"}"
```

**PowerShell:**
```powershell
$body = @{
    email = "test@example.com"
    password = "Test1234"
    firstName = "Test"
    lastName = "User"
    role = "CLIENT_USER"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" -Method POST -Body $body -ContentType "application/json"
```

**Response:**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "email": "test@example.com",
    "firstName": "Test",
    "lastName": "User",
    "role": "CLIENT_USER"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### **2. Đăng nhập**

**Command Prompt:**
```cmd
curl -X POST http://localhost:8080/api/v1/auth/login -H "Content-Type: application/json" -d "{\"email\":\"test@example.com\",\"password\":\"Test1234\"}"
```

**PowerShell:**
```powershell
$body = @{
    email = "test@example.com"
    password = "Test1234"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method POST -Body $body -ContentType "application/json"
```

**Response:**
```json
{
  "message": "Login successful",
  "user": { ... },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**💡 Copy token từ response để dùng cho các API tiếp theo!**

### **3. Get User Profile (dùng token)**

**Command Prompt:**
```cmd
curl http://localhost:8080/api/v1/auth/me -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**PowerShell:**
```powershell
$token = "YOUR_TOKEN_HERE"
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/me" -Headers @{Authorization = "Bearer $token"}
```

---

## 🎯 Test với Postman (Khuyến nghị)

### **Import Collection vào Postman:**

1. **Create New Request: Register**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/auth/register`
   - Body → raw → JSON:
   ```json
   {
     "email": "john@test.com",
     "password": "John1234",
     "firstName": "John",
     "lastName": "Doe",
     "role": "CLIENT_USER"
   }
   ```
   - Tab "Tests" → Add script để save token tự động:
   ```javascript
   if (pm.response.code === 201) {
       pm.environment.set("token", pm.response.json().token);
   }
   ```

2. **Create New Request: Login**
   - Method: `POST`
   - URL: `http://localhost:8080/api/v1/auth/login`
   - Body → raw → JSON:
   ```json
   {
     "email": "john@test.com",
     "password": "John1234"
   }
   ```
   - Tab "Tests":
   ```javascript
   if (pm.response.code === 200) {
       pm.environment.set("token", pm.response.json().token);
   }
   ```

3. **Create New Request: Get Profile**
   - Method: `GET`
   - URL: `http://localhost:8080/api/v1/auth/me`
   - Headers:
     - Key: `Authorization`
     - Value: `Bearer {{token}}`

---

## 📋 Các User Roles Có Thể Dùng

```javascript
"CLIENT_USER"    // Khách hàng - mặc định
"GYM_STAFF"      // Nhân viên Gym - quản lý gym, offers
"PT_USER"        // Personal Trainer - tạo PT profile, offers
"ADMIN"          // Admin - full access
```

---

## ⚠️ Troubleshooting Nhanh

### **Lỗi: Cannot connect to SQL Server**
```bash
# 1. Check SQL Server đang chạy:
services.msc → Tìm "SQL Server (MSSQLSERVER)" → Start

# 2. Enable TCP/IP:
SQL Server Configuration Manager → 
Protocols for MSSQLSERVER → TCP/IP → Enable → Restart service
```

### **Lỗi: Login failed for user 'sa'**
```sql
-- Reset password trong SSMS:
ALTER LOGIN sa WITH PASSWORD = 'YourStrong@Password123';
ALTER LOGIN sa ENABLE;
```

### **Lỗi: Validation Error Password**
Password phải có:
- ✅ Tối thiểu 8 ký tự
- ✅ 1 chữ HOA (A-Z)
- ✅ 1 chữ thường (a-z)
- ✅ 1 số (0-9)

**Ví dụ hợp lệ:** `Password123`, `Test1234`, `MyPass99`

---

## ✅ Checklist

- [ ] SQL Server đã cài và chạy
- [ ] Database `easybody_db` đã tạo
- [ ] File `.env` đã có password đúng
- [ ] Run `npm run dev` thành công
- [ ] Test Register → nhận token
- [ ] Test Login → nhận token
- [ ] Test GET /auth/me với token

---

## 📞 Hỗ Trợ

Xem chi tiết đầy đủ tại:
- **SQL_SERVER_AUTH_SETUP.md** - Setup SQL Server chi tiết
- **FRONTEND_GUIDE.md** - Tất cả APIs có sẵn

**Xong! Giờ bạn có thể test đăng ký/đăng nhập ngay! 🎉**

