# Easy Body Backend - Node.js/Express

## 🚀 Tổng quan

Backend RESTful API cho nền tảng **Easy Body** - nơi các Gym và Personal Trainers (PT) có thể đăng các gói dịch vụ, và khách hàng có thể tìm kiếm, đánh giá và liên hệ.

## 🛠️ Tech Stack

- **Runtime**: Node.js 18+
- **Framework**: Express.js 5
- **Database**: PostgreSQL with PostGIS extension
- **ORM**: Sequelize
- **Authentication**: AWS Cognito (JWT)
- **Storage**: AWS S3 (Pre-signed URLs)
- **Cloud**: AWS (RDS, Cognito, S3, CloudWatch)

## 📦 Dependencies

```json
{
  "express": "^5.1.0",
  "sequelize": "^6.37.7",
  "pg": "^8.16.3",
  "aws-sdk": "^2.1692.0",
  "jsonwebtoken": "^9.0.2",
  "jwks-rsa": "^3.2.0",
  "express-validator": "^7.2.1",
  "cors": "^2.8.5",
  "helmet": "^8.1.0",
  "dotenv": "^17.2.3"
}
```

## 🏗️ Cấu trúc dự án

```
AWS_WS_BE/
├── server.js                 # Entry point
├── .env                      # Environment variables
├── package.json
├── src/
│   ├── app.js               # Express app configuration
│   ├── config/
│   │   └── database.js      # Database connection
│   ├── models/              # Sequelize models
│   │   ├── index.js
│   │   ├── User.js
│   │   ├── Gym.js
│   │   ├── PTUser.js
│   │   ├── Offer.js
│   │   ├── Location.js
│   │   ├── Rating.js
│   │   ├── Report.js
│   │   └── GymPTAssociation.js
│   ├── controllers/         # Business logic
│   │   ├── auth.controller.js
│   │   ├── gym.controller.js
│   │   ├── ptUser.controller.js
│   │   ├── offer.controller.js
│   │   ├── rating.controller.js
│   │   ├── report.controller.js
│   │   ├── admin.controller.js
│   │   ├── media.controller.js
│   │   └── search.controller.js
│   ├── routes/              # API routes
│   │   ├── auth.routes.js
│   │   ├── gym.routes.js
│   │   ├── ptUser.routes.js
│   │   ├── offer.routes.js
│   │   ├── rating.routes.js
│   │   ├── report.routes.js
│   │   ├── admin.routes.js
│   │   ├── media.routes.js
│   │   └── search.routes.js
│   └── middleware/
│       └── auth.js          # JWT verification
```

## ⚙️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/Kenfiz123/AWS_WS_BE.git
cd AWS_WS_BE
```

### 2. Cài đặt dependencies

```bash
npm install
```

### 3. Cấu hình environment variables

Copy file `.env.example` thành `.env` và cập nhật các giá trị:

```env
# Server
NODE_ENV=development
PORT=8080

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=easybody_db
DB_USER=postgres
DB_PASSWORD=your_password

# AWS Cognito
AWS_REGION=us-east-1
AWS_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
AWS_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
AWS_COGNITO_ISSUER=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX

# AWS S3
AWS_S3_BUCKET_NAME=easybody-media
AWS_S3_REGION=us-east-1
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key

# CORS
CORS_ORIGIN=http://localhost:3000
```

### 4. Setup PostgreSQL với PostGIS

```sql
-- Tạo database
CREATE DATABASE easybody_db;

-- Kết nối vào database
\c easybody_db

-- Enable PostGIS extension
CREATE EXTENSION IF NOT EXISTS postgis;
```

### 5. Chạy server

**Development mode (with auto-reload):**
```bash
npm run dev
```

**Production mode:**
```bash
npm start
```

Server sẽ chạy tại: `http://localhost:8080`

## 📡 API Endpoints

### Base URL
```
http://localhost:8080/api/v1
```

### Authentication
- `POST /auth/register` - Đăng ký user (sau khi Cognito signup)
- `GET /auth/me` - Lấy thông tin user hiện tại
- `PUT /auth/me` - Cập nhật profile

### Gyms
- `GET /gyms` - Danh sách gym (có phân trang)
- `GET /gyms/search` - Tìm kiếm gym (theo location, text)
- `GET /gyms/:id` - Chi tiết gym
- `POST /gyms` - Tạo gym mới (GYM_STAFF, ADMIN)
- `PUT /gyms/:id` - Cập nhật gym
- `DELETE /gyms/:id` - Xóa gym (ADMIN)
- `POST /gyms/:id/assign-pt` - Gán PT cho gym
- `GET /gyms/:id/personal-trainers` - Danh sách PT của gym

### Personal Trainers
- `GET /pt-users` - Danh sách PT
- `GET /pt-users/search` - Tìm kiếm PT
- `GET /pt-users/:id` - Chi tiết PT
- `POST /pt-users` - Tạo PT profile
- `PUT /pt-users/:id` - Cập nhật PT profile
- `DELETE /pt-users/:id` - Xóa PT (ADMIN)

### Offers
- `GET /offers` - Danh sách offers đã approved
- `GET /offers/search` - Tìm kiếm offers
- `GET /offers/:id` - Chi tiết offer
- `POST /offers` - Tạo offer mới
- `PUT /offers/:id` - Cập nhật offer
- `DELETE /offers/:id` - Xóa offer

### Search
- `GET /search` - Tìm kiếm tổng hợp (gyms, PTs, offers)

### Ratings
- `GET /ratings/offer/:offerId` - Danh sách ratings của offer
- `POST /ratings` - Tạo rating mới
- `PUT /ratings/:id` - Cập nhật rating
- `DELETE /ratings/:id` - Xóa rating

### Reports
- `POST /reports` - Tạo report
- `GET /reports/my-reports` - Danh sách reports của tôi

### Admin (Chỉ ADMIN)
- `GET /admin/pending-offers` - Offers chờ duyệt
- `GET /admin/pending-gym-pt-associations` - PT-Gym associations chờ duyệt
- `POST /admin/moderate-offer/:id` - Duyệt/từ chối offer
- `POST /admin/moderate-gym-pt-association/:id` - Duyệt/từ chối PT-Gym association
- `GET /admin/reports` - Tất cả reports
- `POST /admin/reports/:id/review` - Review report
- `GET /admin/users` - Danh sách users
- `PUT /admin/users/:id/toggle-active` - Kích hoạt/vô hiệu hóa user

### Media
- `POST /media/presigned-url` - Lấy presigned URL để upload lên S3
- `POST /media/upload-complete` - Xác nhận upload hoàn tất

## 🔐 Authentication

Backend sử dụng **AWS Cognito JWT tokens**. Frontend phải gửi token trong header:

```
Authorization: Bearer <JWT_TOKEN>
```

### User Roles
- `ADMIN` - Quản trị viên (full access)
- `GYM_STAFF` - Nhân viên gym (quản lý gym, offers)
- `PT_USER` - Personal Trainer (quản lý PT profile, offers)
- `CLIENT_USER` - Khách hàng (xem, đánh giá, report)

## 🌍 Geo Search với PostGIS

Backend hỗ trợ tìm kiếm theo vị trí địa lý sử dụng PostGIS:

**Example: Tìm gyms trong bán kính 10km**
```
GET /api/v1/gyms/search?latitude=10.7769&longitude=106.7009&radiusKm=10
```

## 📸 Upload Images lên S3

### Flow:
1. Frontend gọi `POST /media/presigned-url` với `fileName` và `fileType`
2. Backend trả về presigned URL
3. Frontend upload file trực tiếp lên S3 qua presigned URL
4. Frontend gọi `POST /media/upload-complete` để xác nhận
5. Sử dụng `publicUrl` để lưu vào database

## 🧪 Testing

Health check endpoint:
```bash
curl http://localhost:8080/health
```

Response:
```json
{
  "status": "OK",
  "timestamp": "2025-10-06T10:30:00.000Z",
  "environment": "development"
}
```

## 🚢 Deployment

### AWS Services cần thiết:

1. **RDS PostgreSQL** với PostGIS extension
2. **AWS Cognito User Pool** cho authentication
3. **S3 Bucket** cho media storage
4. **EC2 hoặc Elastic Beanstalk** để host Node.js app
5. **CloudWatch** cho logging

### Environment Variables (Production)

Đảm bảo set các biến môi trường trên production server.

## 📝 Database Schema

Database sẽ tự động tạo tables khi start server ở development mode (`sync({ alter: true })`).

**Tables:**
- users
- locations (với PostGIS geolocation)
- gyms
- pt_users
- gym_pt_associations
- offers
- ratings
- reports

## 📚 Tài liệu cho Frontend

Xem file **[FRONTEND_GUIDE.md](./FRONTEND_GUIDE.md)** để biết chi tiết:
- API endpoints và cách sử dụng
- Authentication flow
- Upload images lên S3
- Error handling
- Request/Response examples

## 🤝 Contributing

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

ISC

## 👥 Support

For support, email: support@easybody.com
