# 🗄️ Hướng dẫn Setup PostgreSQL Local

## 📋 Tổng quan

Trong môi trường development, bạn sẽ chạy PostgreSQL trên máy local thay vì sử dụng AWS RDS. Điều này giúp:
- ✅ Tiết kiệm chi phí
- ✅ Development nhanh hơn
- ✅ Không cần kết nối internet
- ✅ Dễ dàng reset data khi cần

---

## 💻 Cài đặt PostgreSQL

### **Windows**

#### **Bước 1: Download PostgreSQL**
1. Truy cập: https://www.postgresql.org/download/windows/
2. Download PostgreSQL 15 hoặc 16 (khuyến nghị version mới nhất)
3. Chạy installer

#### **Bước 2: Cài đặt**
```
- Port: 5432 (mặc định)
- Superuser: postgres
- Password: Đặt password mạnh và LƯU LẠI (ví dụ: postgres123)
- Locale: Default locale
```

#### **Bước 3: Verify cài đặt**
Mở Command Prompt và kiểm tra:
```cmd
psql --version
```

Nếu không nhận diện, thêm PostgreSQL vào PATH:
```
C:\Program Files\PostgreSQL\15\bin
```

### **macOS**

#### **Option 1: Homebrew (Khuyến nghị)**
```bash
# Cài Homebrew (nếu chưa có)
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Cài PostgreSQL
brew install postgresql@15

# Start PostgreSQL
brew services start postgresql@15

# Verify
psql --version
```

#### **Option 2: Postgres.app**
1. Download: https://postgresapp.com/
2. Kéo vào Applications
3. Click "Initialize" để tạo default server
4. Add to PATH: `export PATH="/Applications/Postgres.app/Contents/Versions/latest/bin:$PATH"`

### **Linux (Ubuntu/Debian)**

```bash
# Update package list
sudo apt update

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Start PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Verify
psql --version
```

---

## 🔧 Cấu hình PostgreSQL cho Easy Body

### **Bước 1: Truy cập PostgreSQL**

#### **Windows:**
```cmd
psql -U postgres
# Nhập password bạn đã đặt khi cài đặt
```

#### **macOS/Linux:**
```bash
# Switch to postgres user
sudo -u postgres psql

# Hoặc
psql postgres
```

### **Bước 2: Tạo Database và User**

```sql
-- Tạo database
CREATE DATABASE easybody_db;

-- Tạo user riêng cho application (khuyến nghị)
CREATE USER easybody_user WITH PASSWORD 'easybody_password_2024';

-- Grant permissions
GRANT ALL PRIVILEGES ON DATABASE easybody_db TO easybody_user;

-- Kết nối vào database
\c easybody_db

-- Grant schema permissions
GRANT ALL ON SCHEMA public TO easybody_user;

-- Verify
\l  -- List databases
\du -- List users
```

### **Bước 3: Enable PostGIS Extension**

PostGIS cần thiết cho geo-location queries (tìm gyms/PTs gần user).

```sql
-- Kết nối vào easybody_db
\c easybody_db

-- Enable PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;

-- Verify
SELECT PostGIS_Version();

-- Kết quả mong đợi:
-- 3.3.3 hoặc version mới hơn
```

**Nếu gặp lỗi "extension postgis does not exist":**

#### **Windows:**
PostGIS thường đi kèm với PostgreSQL installer. Nếu không có:
1. Download PostGIS: https://postgis.net/windows_downloads/
2. Run installer và chọn PostgreSQL version tương ứng

#### **macOS (Homebrew):**
```bash
brew install postgis
```

#### **Linux (Ubuntu):**
```bash
sudo apt install postgresql-15-postgis-3
```

### **Bước 4: Enable pg_trgm (Optional - cho text search)**

```sql
-- Kết nối vào easybody_db
\c easybody_db

-- Enable pg_trgm cho fuzzy text search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Verify
SELECT * FROM pg_extension WHERE extname = 'pg_trgm';
```

---

## ⚙️ Cấu hình .env cho Backend

Cập nhật file `.env` với thông tin PostgreSQL local:

```env
# Server Configuration
NODE_ENV=development
PORT=8080
API_VERSION=v1

# Database Configuration (LOCAL PostgreSQL)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=easybody_db
DB_USER=easybody_user
DB_PASSWORD=easybody_password_2024
DB_DIALECT=postgres

# AWS Cognito Configuration (sẽ setup sau)
AWS_REGION=us-east-1
AWS_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
AWS_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
AWS_COGNITO_ISSUER=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX

# AWS S3 Configuration (sẽ setup sau)
AWS_S3_BUCKET_NAME=easybody-media
AWS_S3_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# JWT Configuration
JWT_SECRET=your_super_secret_jwt_key_change_this_in_production
JWT_EXPIRES_IN=24h

# CORS Configuration
CORS_ORIGIN=http://localhost:3000,http://localhost:5173

# Logging
LOG_LEVEL=debug

# Pagination
DEFAULT_PAGE_SIZE=20
MAX_PAGE_SIZE=100

# Geo Search
DEFAULT_RADIUS_KM=10
MAX_RADIUS_KM=100

# Image Upload
MAX_FILE_SIZE_MB=10
ALLOWED_IMAGE_TYPES=image/jpeg,image/png,image/gif,image/webp
```

---

## 🧪 Test Database Connection

### **Test 1: Direct psql connection**

```bash
psql -h localhost -p 5432 -U easybody_user -d easybody_db
# Nhập password: easybody_password_2024

# Trong psql:
SELECT version();
SELECT PostGIS_Version();
\dt  -- List tables (sẽ trống lúc đầu)
\q   -- Quit
```

### **Test 2: Test từ Node.js backend**

Chạy server để Sequelize tự động tạo tables:

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

Sequelize sẽ tự động tạo tất cả tables:
- users
- locations (với PostGIS geolocation)
- gyms
- pt_users
- gym_pt_associations
- offers
- ratings
- reports

### **Test 3: Verify tables created**

```bash
psql -U easybody_user -d easybody_db

\dt  -- List all tables

# Kết quả mong đợi:
# users
# locations
# gyms
# pt_users
# gym_pt_associations
# offers
# ratings
# reports
```

---

## 📊 GUI Tools (Optional - Khuyến nghị)

### **1. pgAdmin 4** (Official PostgreSQL GUI)
- Download: https://www.pgadmin.org/download/
- Free, powerful, cross-platform
- Best cho quản lý database

### **2. DBeaver** (Universal Database Tool)
- Download: https://dbeaver.io/download/
- Free, hỗ trợ nhiều DB
- Tốt cho developers

### **3. TablePlus** (Modern GUI)
- Download: https://tableplus.com/
- Đẹp, nhanh, UX tốt
- Free version đủ dùng

### **Connect với pgAdmin:**
```
Host: localhost
Port: 5432
Database: easybody_db
Username: easybody_user
Password: easybody_password_2024
```

---

## 🔄 Database Management Commands

### **Backup Database**

```bash
# Backup toàn bộ database
pg_dump -U easybody_user -d easybody_db > backup_$(date +%Y%m%d).sql

# Backup chỉ schema (không có data)
pg_dump -U easybody_user -d easybody_db --schema-only > schema_backup.sql

# Backup chỉ data
pg_dump -U easybody_user -d easybody_db --data-only > data_backup.sql
```

### **Restore Database**

```bash
# Restore từ backup file
psql -U easybody_user -d easybody_db < backup_20251006.sql
```

### **Reset Database**

```sql
-- Xóa tất cả tables và tạo lại
DROP DATABASE IF EXISTS easybody_db;
CREATE DATABASE easybody_db;
GRANT ALL PRIVILEGES ON DATABASE easybody_db TO easybody_user;

\c easybody_db
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;
GRANT ALL ON SCHEMA public TO easybody_user;
```

### **View Table Schema**

```sql
-- Xem cấu trúc của table
\d users
\d gyms
\d locations

-- Xem indexes
\di

-- Xem foreign keys
SELECT * FROM information_schema.table_constraints 
WHERE constraint_type = 'FOREIGN KEY';
```

---

## 🚀 Seed Sample Data (Optional)

Tạo file `seed-data.sql` để test:

```sql
-- Seed sample data for testing

-- Insert sample users
INSERT INTO users (cognito_sub, email, first_name, last_name, role, active, created_at, updated_at) VALUES
('test-admin-001', 'admin@easybody.com', 'Admin', 'User', 'ADMIN', true, NOW(), NOW()),
('test-gym-001', 'gym1@easybody.com', 'John', 'Gym Owner', 'GYM_STAFF', true, NOW(), NOW()),
('test-pt-001', 'pt1@easybody.com', 'Jane', 'Trainer', 'PT_USER', true, NOW(), NOW()),
('test-client-001', 'client1@easybody.com', 'Mike', 'Client', 'CLIENT_USER', true, NOW(), NOW());

-- Insert sample locations
INSERT INTO locations (latitude, longitude, address, city, state, country, postal_code, geolocation, created_at, updated_at) VALUES
(10.7769, 106.7009, '123 Nguyen Hue St', 'Ho Chi Minh City', 'Ho Chi Minh', 'Vietnam', '700000', ST_SetSRID(ST_MakePoint(106.7009, 10.7769), 4326), NOW(), NOW()),
(10.8231, 106.6297, '456 Le Van Viet St', 'Ho Chi Minh City', 'Ho Chi Minh', 'Vietnam', '700000', ST_SetSRID(ST_MakePoint(106.6297, 10.8231), 4326), NOW(), NOW());

-- Insert sample gyms
INSERT INTO gyms (name, description, phone_number, email, owner_id, location_id, active, verified, created_at, updated_at) VALUES
('Fitness Pro Gym', 'Premium gym with modern equipment', '+84901234567', 'info@fitnesspro.com', 2, 1, true, true, NOW(), NOW()),
('Power House Gym', 'Strength training focused gym', '+84909876543', 'contact@powerhouse.com', 2, 2, true, true, NOW(), NOW());

-- Insert sample PT profile
INSERT INTO pt_users (user_id, bio, specializations, years_of_experience, hourly_rate, location_id, active, verified, created_at, updated_at) VALUES
(3, 'Certified personal trainer with 5+ years experience', 'Weight Loss, Strength Training, Yoga', 5, 35.00, 1, true, true, NOW(), NOW());

-- Insert sample offers
INSERT INTO offers (title, description, offer_type, price, currency, gym_id, created_by, status, active, created_at, updated_at) VALUES
('1 Month Membership', 'Full access to all equipment and classes', 'GYM_OFFER', 50.00, 'USD', 1, 2, 'APPROVED', true, NOW(), NOW()),
('3 Month Membership', 'Save 20% with 3-month package', 'GYM_OFFER', 120.00, 'USD', 1, 2, 'APPROVED', true, NOW(), NOW());

INSERT INTO offers (title, description, offer_type, price, currency, pt_user_id, created_by, status, active, created_at, updated_at) VALUES
('10 PT Sessions', 'One-on-one personal training', 'PT_OFFER', 300.00, 'USD', 1, 3, 'APPROVED', true, NOW(), NOW());

-- Verify
SELECT 'Users:', COUNT(*) FROM users;
SELECT 'Gyms:', COUNT(*) FROM gyms;
SELECT 'Offers:', COUNT(*) FROM offers;
```

**Chạy seed data:**
```bash
psql -U easybody_user -d easybody_db < seed-data.sql
```

---

## 🐛 Troubleshooting

### **Lỗi: "password authentication failed"**
```sql
-- Reset password
ALTER USER easybody_user WITH PASSWORD 'new_password';
```

### **Lỗi: "database does not exist"**
```bash
# Tạo lại database
createdb -U postgres easybody_db
```

### **Lỗi: "could not connect to server"**
```bash
# Windows: Check PostgreSQL service
services.msc
# Tìm "postgresql-x64-15" và Start

# macOS/Linux: Start PostgreSQL
brew services start postgresql@15
# hoặc
sudo systemctl start postgresql
```

### **Lỗi: "extension postgis does not exist"**
```bash
# Cài PostGIS extension
# Windows: Run PostGIS installer
# macOS: brew install postgis
# Linux: sudo apt install postgresql-15-postgis-3
```

### **Port 5432 đã được sử dụng**
```bash
# Kiểm tra process đang dùng port 5432
# Windows:
netstat -ano | findstr :5432

# macOS/Linux:
lsof -i :5432

# Stop process hoặc đổi port trong postgresql.conf
```

---

## 📝 Best Practices cho Development

### **1. Tách Environment**
```
Development: localhost:5432 (local PostgreSQL)
Staging: AWS RDS (khi deploy staging)
Production: AWS RDS Multi-AZ (khi production)
```

### **2. Version Control**
- ❌ KHÔNG commit `.env` file (đã có trong `.gitignore`)
- ✅ Commit `.env.example` như template
- ✅ Document database setup steps

### **3. Regular Backups**
```bash
# Tự động backup hàng ngày
pg_dump -U easybody_user easybody_db > backup_$(date +%Y%m%d).sql
```

### **4. Connection Pooling**
Sequelize đã tự động config connection pool:
```javascript
pool: {
  max: 5,      // Max 5 connections
  min: 0,      // Min 0 connections
  acquire: 30000,
  idle: 10000
}
```

---

## ✅ Checklist Setup PostgreSQL Local

- [ ] Cài đặt PostgreSQL 15/16
- [ ] Tạo database `easybody_db`
- [ ] Tạo user `easybody_user`
- [ ] Enable PostGIS extension
- [ ] Enable pg_trgm extension (optional)
- [ ] Cập nhật `.env` với database credentials
- [ ] Test connection với `psql`
- [ ] Start Node.js server để tạo tables
- [ ] Verify tables created
- [ ] (Optional) Seed sample data
- [ ] (Optional) Install GUI tool (pgAdmin/DBeaver)

---

## 🔄 Migrate sang AWS RDS (sau này)

Khi sẵn sàng deploy production:

1. **Export data từ local:**
```bash
pg_dump -U easybody_user easybody_db > production_migration.sql
```

2. **Setup AWS RDS** (xem AWS_SERVICES_REQUIRED.md)

3. **Import data vào RDS:**
```bash
psql -h easybody-db.xxx.rds.amazonaws.com -U easybody_admin -d easybody_db < production_migration.sql
```

4. **Update .env:**
```env
DB_HOST=easybody-db.xxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_USER=easybody_admin
DB_PASSWORD=<strong-password>
```

---

**Xong! Bây giờ bạn đã có PostgreSQL local sẵn sàng cho development! 🎉**

