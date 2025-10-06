   Folder name: offers/
   ```
4. Click **Create folder** cho mỗi folder

### Bước 3.7: Test Upload

1. Click vào folder `profiles/`
2. Click **Upload**
3. Upload một test image
4. Sau khi upload, click vào file
5. Copy **Object URL**: `https://easybody-media.s3.amazonaws.com/profiles/test.jpg`
6. Paste vào browser → image phải hiển thị được (public access)

**⚠️ LƯU VÀO FILE .env:**
```bash
AWS_REGION=us-east-1
S3_BUCKET_NAME=easybody-media
```

---

## 🔐 PHASE 4: Setup AWS Cognito User Pool

### Bước 4.1: Create User Pool

1. Truy cập **Cognito Console**: https://console.aws.amazon.com/cognito/
2. Click **Create user pool**

### Bước 4.2: Configure Sign-in Experience

**Authentication providers:**
```
☑️ Email
☐ Phone number
☐ User name
```

**Cognito user pool sign-in options:**
```
☑️ Email
```

**User name requirements:**
```
☐ Allow users to sign in with preferred user name (uncheck)
```

Click **Next**

### Bước 4.3: Configure Security Requirements

**Password policy:**
```
○ Cognito defaults (đơn giản cho MVP)
hoặc
○ Custom (cho production):
  Minimum length: 8
  ☑️ Contains at least 1 number
  ☑️ Contains at least 1 special character
  ☑️ Contains at least 1 uppercase letter
  ☑️ Contains at least 1 lowercase letter
```

**Multi-factor authentication (MFA):**
```
○ No MFA (cho development)
hoặc
○ Optional MFA (cho production)
```

**User account recovery:**
```
☑️ Enable self-service account recovery
Recovery method: Email only
```

Click **Next**

### Bước 4.4: Configure Sign-up Experience

**Self-service sign-up:**
```
☑️ Enable self-registration (cho clients đăng ký)
```

**Attribute verification and user account confirmation:**
```
☑️ Allow Cognito to automatically send messages to verify and confirm
☑️ Email
```

**Required attributes:**
```
☑️ email
☑️ given_name (first name)
☑️ family_name (last name)
```

**Custom attributes:**
```
Click "Add custom attribute"
  Attribute name: role
  Type: String
  Mutable: Yes
  Min length: 1
  Max length: 20
```

Click **Next**

### Bước 4.5: Configure Message Delivery

**Email:**
```
○ Send email with Cognito (Free tier - 50 emails/day)

Cho production, nên dùng:
○ Send email with Amazon SES
  (cần verify domain trước)
```

**FROM email address:**
```
no-reply@verificationemail.com (default)
hoặc
noreply@easybody.com (nếu dùng SES)
```

Click **Next**

### Bước 4.6: Integrate Your App

**User pool name:**
```
easybody-users
```

**Hosted authentication pages:**
```
☐ Use the Cognito Hosted UI (uncheck - frontend tự xử lý)
```

**Initial app client:**
```
App type: Public client
App client name: easybody-frontend
```

**Authentication flows:**
```
☑️ ALLOW_USER_PASSWORD_AUTH
☑️ ALLOW_REFRESH_TOKEN_AUTH
☐ ALLOW_CUSTOM_AUTH
☐ ALLOW_USER_SRP_AUTH (uncheck nếu không dùng SRP)
```

Click **Next**

### Bước 4.7: Review and Create

1. Review tất cả settings
2. Click **Create user pool**
3. Đợi vài giây

### Bước 4.8: Lấy Cognito Configuration

**Sau khi tạo xong:**

1. Click vào User Pool: `easybody-users`
2. **⚠️ LƯU LẠI User Pool ID:**
   ```
   User pool ID: us-east-1_XXXXXXXXX
   ```

3. Tab **App integration** → Scroll xuống **App clients and analytics**
4. Click vào `easybody-frontend`
5. **⚠️ LƯU LẠI Client ID:**
   ```
   Client ID: xxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

6. **Tạo JWKS URL:**
   ```
   JWKS URL: https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json
   ```

**⚠️ LƯU VÀO FILE .env:**
```bash
# Backend .env
COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_JWKS_URL=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json

# Frontend config
REACT_APP_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
REACT_APP_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
REACT_APP_COGNITO_REGION=us-east-1
```

### Bước 4.9: Test Cognito với AWS Console

1. Tab **Users** → **Create user**
2. Tạo test user:
   ```
   Email: test@easybody.com
   Email verified: ☑️ Mark email as verified
   Temporary password: TempPass123!
   ```
3. Click **Create user**
4. User được tạo → có thể dùng để test login

---

## 📨 PHASE 5: Tạo SQS Queue (Optional)

### Bước 5.1: Create Queue

1. Truy cập **SQS Console**: https://console.aws.amazon.com/sqs/
2. Click **Create queue**

### Bước 5.2: Queue Configuration

**Details:**
```
Type: ○ Standard (cho MVP)
Name: easybody-image-moderation-queue
```

**Configuration:**
```
Visibility timeout: 5 minutes
Message retention period: 4 days
Delivery delay: 0 seconds
Maximum message size: 256 KB
Receive message wait time: 0 seconds
```

**Access policy:**
```
○ Basic (chọn này cho đơn giản)
```

Click **Create queue**

### Bước 5.3: Lấy Queue URL

1. Click vào queue vừa tạo
2. **⚠️ LƯU LẠI URL:**
   ```
   URL: https://sqs.us-east-1.amazonaws.com/123456789012/easybody-image-moderation-queue
   ```

**⚠️ LƯU VÀO FILE .env:**
```bash
SQS_IMAGE_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/easybody-image-moderation-queue
```

---

## 🚀 PHASE 6: Deploy Backend Application

### Option A: Deploy lên EC2 (Đơn giản nhất)

### Bước 6A.1: Launch EC2 Instance

1. Truy cập **EC2 Console**: https://console.aws.amazon.com/ec2/
2. Click **Launch instance**

### Bước 6A.2: Instance Configuration

**Name:**
```
easybody-backend-server
```

**Application and OS Images:**
```
Amazon Linux 2023 AMI (free tier eligible)
Architecture: 64-bit (x86)
```

**Instance type:**
```
Development: t3.micro (1 vCPU, 1 GB) - Free tier
Production: t3.small (2 vCPU, 2 GB)
```

**Key pair:**
```
Click "Create new key pair"
  Key pair name: easybody-backend-key
  Key pair type: RSA
  Private key file format: .pem (Linux/Mac) hoặc .ppk (Windows/PuTTY)
  
⚠️ DOWNLOAD VÀ LƯU FILE .pem - CHỈ HIỆN 1 LẦN!
```

**Network settings:**
```
☑️ Allow SSH traffic from: My IP
☑️ Allow HTTPS traffic from the internet
☑️ Allow HTTP traffic from the internet
```

**Configure storage:**
```
20 GB gp3
```

**Advanced details:**
```
IAM instance profile: [Tạo role với S3, RDS, SQS access]
```

Click **Launch instance**

### Bước 6A.3: Connect to EC2

**Windows (PuTTY):**
```
1. Convert .pem to .ppk bằng PuTTYgen
2. Open PuTTY
3. Host Name: ec2-user@[EC2-PUBLIC-IP]
4. Connection → SSH → Auth → Private key file: [chọn .ppk file]
5. Click Open
```

**Mac/Linux:**
```bash
chmod 400 easybody-backend-key.pem
ssh -i easybody-backend-key.pem ec2-user@[EC2-PUBLIC-IP]
```

### Bước 6A.4: Install Java 21 & Dependencies

```bash
# Update system
sudo yum update -y

# Install Java 21
sudo yum install java-21-amazon-corretto -y

# Verify
java -version

# Install Git
sudo yum install git -y

# Install PostgreSQL client
sudo yum install postgresql15 -y
```

### Bước 6A.5: Deploy Application

```bash
# Clone repository (hoặc upload JAR file)
cd /home/ec2-user
git clone [YOUR_REPO_URL]
cd AWS_WS_BE

# Create .env file
nano .env
```

**Paste vào .env:**
```bash
# Database
DB_HOST=easybody-db.xxxxxxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=easybody
DB_USERNAME=easybody_admin
DB_PASSWORD=[your-password]

# AWS
AWS_REGION=us-east-1
S3_BUCKET_NAME=easybody-media
COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_JWKS_URL=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json
SQS_IMAGE_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/easybody-image-moderation-queue
```

**Build & Run:**
```bash
# Build application
./gradlew build

# Run application
nohup java -jar build/libs/easybody-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

# Check logs
tail -f app.log

# Check if running
ps aux | grep java
```

### Bước 6A.6: Configure Security Group

1. Quay lại EC2 Console
2. Click vào instance `easybody-backend-server`
3. Tab **Security** → Click vào Security Group
4. **Edit inbound rules** → **Add rule:**
   ```
   Type: Custom TCP
   Port range: 8080
   Source: 0.0.0.0/0
   Description: Spring Boot API
   ```
5. **Save rules**

### Bước 6A.7: Test API

```bash
# Từ máy local
curl http://[EC2-PUBLIC-IP]:8080/api/v1/gyms/search?query=test

# Nếu thấy response JSON → SUCCESS!
```

**⚠️ LƯU PUBLIC IP:**
```
API_BASE_URL=http://[EC2-PUBLIC-IP]:8080/api/v1
```

---

### Option B: Deploy lên Elastic Beanstalk (Khuyến nghị - Dễ hơn)

### Bước 6B.1: Prepare Application

```bash
# Build JAR file
./gradlew clean build

# JAR file sẽ ở: build/libs/easybody-0.0.1-SNAPSHOT.jar
```

### Bước 6B.2: Create Elastic Beanstalk Application

1. Truy cập **Elastic Beanstalk Console**: https://console.aws.amazon.com/elasticbeanstalk/
2. Click **Create application**

**Application information:**
```
Application name: easybody-backend
Platform: Java
Platform branch: Corretto 21
```

**Application code:**
```
○ Upload your code
  Click "Choose file" → Select easybody-0.0.1-SNAPSHOT.jar
```

**Presets:**
```
○ Single instance (free tier)
hoặc
○ High availability (production)
```

### Bước 6B.3: Configure Environment

Click **Configure more options**

**Software:**
```
Environment properties:
  DB_HOST = [RDS endpoint]
  DB_PORT = 5432
  DB_NAME = easybody
  DB_USERNAME = easybody_admin
  DB_PASSWORD = [password]
  AWS_REGION = us-east-1
  S3_BUCKET_NAME = easybody-media
  COGNITO_USER_POOL_ID = [pool-id]
  COGNITO_CLIENT_ID = [client-id]
  COGNITO_JWKS_URL = [jwks-url]
```

**Instances:**
```
Instance type: t3.small
```

Click **Create app**

Đợi 5-10 phút để deploy.

### Bước 6B.4: Get Application URL

1. Sau khi deploy xong, copy **Environment URL**:
   ```
   http://easybody-backend.us-east-1.elasticbeanstalk.com
   ```

2. Test API:
   ```bash
   curl http://easybody-backend.us-east-1.elasticbeanstalk.com/api/v1/gyms/search
   ```

**⚠️ LƯU URL:**
```
API_BASE_URL=http://easybody-backend.us-east-1.elasticbeanstalk.com/api/v1
```

---

## ✅ PHASE 7: Test & Verify

### Bước 7.1: Test Database Connection

```bash
# Test từ backend logs
# Nên thấy: "HikariPool-1 - Start completed"
```

### Bước 7.2: Test S3 Pre-signed URL

```bash
# Test API endpoint
curl -H "Authorization: Bearer [JWT_TOKEN]" \
  "http://[API_URL]/api/v1/media/presigned-url?folder=profiles&fileExtension=jpg"

# Should return JSON with uploadUrl
```

### Bước 7.3: Test Cognito

Dùng AWS CLI hoặc SDK để test:
```javascript
// Frontend test
import { CognitoUserPool } from 'amazon-cognito-identity-js';

const poolData = {
  UserPoolId: 'us-east-1_XXXXXXXXX',
  ClientId: 'xxxxxxxxxxxxxxxxxxxxxxxxxx'
};

const userPool = new CognitoUserPool(poolData);
console.log('Cognito configured:', userPool);
```

### Bước 7.4: Test Full Flow

1. Register user qua Cognito
2. Login và get JWT token
3. Call backend `/auth/register` với JWT token
4. Call `/auth/me` để verify user
5. Upload image lên S3
6. Create offer với image URL
7. Search offers
8. SUCCESS! 🎉

---

## 📋 SUMMARY: Environment Variables

**Backend (.env hoặc application.yml):**
```bash
# Database
DB_HOST=easybody-db.xxxxxxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=easybody
DB_USERNAME=easybody_admin
DB_PASSWORD=[your-strong-password]

# AWS Region
AWS_REGION=us-east-1

# Cognito
COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_JWKS_URL=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json

# S3
S3_BUCKET_NAME=easybody-media
S3_PRESIGNED_URL_EXPIRATION=3600

# SQS (optional)
SQS_IMAGE_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/easybody-image-moderation-queue

# CloudWatch
CLOUDWATCH_NAMESPACE=EasyBody
```

**Frontend (.env.local):**
```bash
REACT_APP_API_BASE_URL=http://[EC2-IP]:8080/api/v1
# hoặc
REACT_APP_API_BASE_URL=http://easybody-backend.us-east-1.elasticbeanstalk.com/api/v1

REACT_APP_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
REACT_APP_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
REACT_APP_COGNITO_REGION=us-east-1
```

---

## 💰 Chi Phí Ước Tính (Tháng Đầu)

```
✅ RDS db.t3.micro (Free Tier): $0
✅ S3 (5GB): $0.12
✅ Cognito (< 50k MAUs): $0
✅ EC2 t3.micro (Free Tier): $0
✅ SQS (< 1M requests): $0
✅ Data Transfer (1GB): $0.09

TỔNG: ~$0.21/tháng (trong Free Tier)
```

**Sau 12 tháng Free Tier:**
```
RDS db.t3.micro: ~$15/month
EC2 t3.small: ~$15/month
S3: ~$2/month
Cognito: ~$5/month (100k MAUs)
Data Transfer: ~$5/month

TỔNG: ~$42/month
```

---

## 🆘 Troubleshooting

### Lỗi: Không connect được RDS
```bash
# Check security group
# Ensure port 5432 mở cho IP của bạn

# Test connection
telnet [RDS_ENDPOINT] 5432
# hoặc
nc -zv [RDS_ENDPOINT] 5432
```

### Lỗi: S3 upload failed
```bash
# Check CORS configuration
# Check bucket policy (public read)
# Check IAM permissions
```

### Lỗi: Cognito JWT invalid
```bash
# Check User Pool ID
# Check Client ID
# Check JWKS URL
# Ensure token chưa expired
```

### Lỗi: Backend không start
```bash
# Check logs
tail -f app.log

# Common issues:
# - Database connection failed → check credentials
# - Port 8080 already in use → kill process
# - Out of memory → increase instance size
```

---

## 📞 Support

**AWS Support:**
- Basic support: Included free
- Developer support: $29/month
- Console: https://console.aws.amazon.com/support/

**Documentation:**
- RDS: https://docs.aws.amazon.com/rds/
- S3: https://docs.aws.amazon.com/s3/
- Cognito: https://docs.aws.amazon.com/cognito/
- Elastic Beanstalk: https://docs.aws.amazon.com/elasticbeanstalk/

---

## ✅ Final Checklist

- [ ] AWS Account created
- [ ] IAM User created with proper permissions
- [ ] RDS PostgreSQL database running
- [ ] PostGIS extension installed
- [ ] S3 bucket created and configured
- [ ] CORS and bucket policy set
- [ ] Cognito User Pool created
- [ ] App client configured
- [ ] Test user created in Cognito
- [ ] SQS queue created (optional)
- [ ] Backend deployed and running
- [ ] API accessible from internet
- [ ] All environment variables configured
- [ ] Full flow tested successfully

**🎉 CHÚC MỪNG! Hệ thống AWS đã sẵn sàng!**

---

**Last Updated:** October 5, 2025  
**Version:** 1.0  
**Author:** Backend Team
# Hướng Dẫn Setup AWS Services cho Easy Body - Step by Step

**Mục đích:** Tạo và cấu hình các AWS services cần thiết để chạy backend Easy Body

**Thời gian ước tính:** 2-3 giờ

---

## 📋 CHECKLIST TỔNG QUAN

- [ ] **Phase 1:** Tạo AWS Account & Setup IAM
- [ ] **Phase 2:** Tạo RDS PostgreSQL Database
- [ ] **Phase 3:** Tạo S3 Bucket cho media storage
- [ ] **Phase 4:** Setup AWS Cognito User Pool
- [ ] **Phase 5:** Tạo SQS Queue (optional)
- [ ] **Phase 6:** Deploy Backend Application
- [ ] **Phase 7:** Test & Verify

---

## 🚀 PHASE 1: AWS Account & IAM Setup

### Bước 1.1: Tạo AWS Account (nếu chưa có)

1. Truy cập: https://aws.amazon.com/
2. Click **"Create an AWS Account"**
3. Điền thông tin:
   - Email address
   - Password
   - AWS account name: `EasyBodyProduction`
4. Chọn Account Type: **Personal** (hoặc Business nếu có)
5. Điền thông tin thanh toán (cần thẻ tín dụng)
6. Xác thực số điện thoại
7. Chọn Support Plan: **Free - Basic Support** (đủ cho MVP)

### Bước 1.2: Tạo IAM User cho Development

**⚠️ Quan trọng:** Không nên dùng Root account để làm việc hàng ngày!

1. Đăng nhập AWS Console: https://console.aws.amazon.com/
2. Tìm service **IAM** (Identity and Access Management)
3. Click **Users** → **Create user**
4. Điền thông tin:
   ```
   User name: easybody-developer
   ☑️ Provide user access to the AWS Management Console
   Console password: Custom password
   ☑️ User must create a new password at next sign-in (uncheck nếu muốn)
   ```
5. Click **Next**

### Bước 1.3: Gán Permissions cho IAM User

1. Chọn **Attach policies directly**
2. Tìm và chọn các policies sau:
   ```
   ☑️ AmazonRDSFullAccess
   ☑️ AmazonS3FullAccess
   ☑️ AmazonCognitoPowerUser
   ☑️ AmazonSQSFullAccess
   ☑️ CloudWatchFullAccess
   ☑️ IAMReadOnlyAccess
   ```
3. Click **Next** → **Create user**
4. **⚠️ LƯU LẠI:**
   ```
   Console sign-in URL: https://[account-id].signin.aws.amazon.com/console
   User name: easybody-developer
   Password: [your-password]
   ```

### Bước 1.4: Tạo Access Keys cho Programmatic Access

1. Vào **IAM** → **Users** → Click user `easybody-developer`
2. Tab **Security credentials**
3. Scroll xuống **Access keys** → Click **Create access key**
4. Chọn use case: **Application running outside AWS**
5. Click **Next** → **Create access key**
6. **⚠️ LƯU LẠI NGAY (chỉ hiện 1 lần):**
   ```
   Access Key ID: AKIAXXXXXXXXXXXXXXXX
   Secret Access Key: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```
7. Download CSV file để backup

---

## 🗄️ PHASE 2: Tạo RDS PostgreSQL Database

### Bước 2.1: Khởi tạo RDS Database

1. Truy cập **RDS Console**: https://console.aws.amazon.com/rds/
2. Click **Create database**

### Bước 2.2: Database Settings

**Choose a database creation method:**
```
○ Standard create
```

**Engine options:**
```
Engine type: PostgreSQL
Engine version: PostgreSQL 15.x (latest stable)
```

**Templates:**
```
○ Free tier (cho development)
hoặc
○ Production (cho production - có Multi-AZ)
```

**Settings:**
```
DB instance identifier: easybody-db
Master username: easybody_admin
Master password: [TẠO PASSWORD MẠNH]
Confirm password: [NHẬP LẠI]
```

**⚠️ LƯU PASSWORD VÀO FILE:**
```
DB_USERNAME=easybody_admin
DB_PASSWORD=[your-strong-password]
```

### Bước 2.3: Instance Configuration

**DB instance class:**
```
Development:
  ○ db.t3.micro (1 vCPU, 1 GB RAM) - Free tier eligible
  
Production:
  ○ db.t3.small (2 vCPU, 2 GB RAM)
  hoặc
  ○ db.t3.medium (2 vCPU, 4 GB RAM)
```

**Storage:**
```
Storage type: General Purpose SSD (gp3)
Allocated storage: 20 GB (minimum)
☑️ Enable storage autoscaling
Maximum storage threshold: 100 GB
```

### Bước 2.4: Connectivity

**Compute resource:**
```
○ Don't connect to an EC2 compute resource (chọn này)
```

**Network type:**
```
○ IPv4
```

**Virtual private cloud (VPC):**
```
Default VPC (hoặc tạo VPC mới nếu cần)
```

**Public access:**
```
○ Yes (để connect từ máy local lúc development)

⚠️ Production: Nên chọn "No" và dùng VPN/Bastion host
```

**VPC security group:**
```
○ Create new
Security group name: easybody-db-sg
```

### Bước 2.5: Database Authentication

```
☑️ Password authentication
```

### Bước 2.6: Additional Configuration

**Database options:**
```
Initial database name: easybody
☑️ Enable automated backups
Backup retention period: 7 days (hoặc 30 days cho production)
Backup window: [chọn giờ ít traffic nhất]
```

**Encryption:**
```
☑️ Enable encryption (khuyến nghị)
```

**Maintenance:**
```
☑️ Enable auto minor version upgrade
Maintenance window: [chọn giờ phù hợp]
```

**Deletion protection:**
```
☑️ Enable deletion protection (cho production)
```

### Bước 2.7: Create Database

1. Review lại tất cả settings
2. Click **Create database**
3. Đợi 5-10 phút để database được tạo
4. Status sẽ chuyển từ "Creating" → "Available"

### Bước 2.8: Lấy Database Connection Info

1. Click vào database `easybody-db`
2. Tab **Connectivity & security**
3. **⚠️ LƯU LẠI THÔNG TIN:**
   ```
   Endpoint: easybody-db.xxxxxxxxx.us-east-1.rds.amazonaws.com
   Port: 5432
   ```

### Bước 2.9: Configure Security Group

1. Scroll xuống **Security** → Click vào Security group
2. Tab **Inbound rules** → **Edit inbound rules**
3. **Add rule:**
   ```
   Type: PostgreSQL
   Protocol: TCP
   Port range: 5432
   Source: My IP (hoặc 0.0.0.0/0 cho development)
   Description: Allow PostgreSQL access
   ```
4. **Save rules**

### Bước 2.10: Test Connection & Install PostGIS

**Connect từ máy local:**
```bash
# Install PostgreSQL client (nếu chưa có)
# Windows: Download từ https://www.postgresql.org/download/windows/
# Mac: brew install postgresql

# Test connection
psql -h easybody-db.xxxxxxxxx.us-east-1.rds.amazonaws.com \
     -U easybody_admin \
     -d easybody \
     -p 5432

# Enter password khi được hỏi
```

**Install PostGIS extension:**
```sql
-- Sau khi connect thành công
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Verify
SELECT PostGIS_version();
```

**⚠️ LƯU VÀO FILE .env:**
```bash
DB_HOST=easybody-db.xxxxxxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=easybody
DB_USERNAME=easybody_admin
DB_PASSWORD=[your-password]
```

---

## 📦 PHASE 3: Tạo S3 Bucket

### Bước 3.1: Create S3 Bucket

1. Truy cập **S3 Console**: https://console.aws.amazon.com/s3/
2. Click **Create bucket**

### Bước 3.2: Bucket Configuration

**General configuration:**
```
Bucket name: easybody-media
  (phải unique globally, có thể thêm suffix nếu bị trùng: easybody-media-2025)
AWS Region: us-east-1 (hoặc region gần user nhất)
```

**Object Ownership:**
```
○ ACLs disabled (recommended)
```

**Block Public Access settings:**
```
⚠️ UNCHECK tất cả (để public read images)
☐ Block all public access

⚠️ Xác nhận: "I acknowledge that the current settings might result in this bucket and the objects within becoming public"
```

**Bucket Versioning:**
```
☑️ Enable (khuyến nghị để recover files)
```

**Default encryption:**
```
○ Server-side encryption with Amazon S3 managed keys (SSE-S3)
☑️ Bucket Key (tiết kiệm chi phí)
```

### Bước 3.3: Create Bucket

1. Click **Create bucket**
2. Bucket được tạo ngay lập tức

### Bước 3.4: Configure CORS

1. Click vào bucket `easybody-media`
2. Tab **Permissions**
3. Scroll xuống **Cross-origin resource sharing (CORS)**
4. Click **Edit**
5. Paste JSON sau:

```json
[
  {
    "AllowedHeaders": [
      "*"
    ],
    "AllowedMethods": [
      "GET",
      "PUT",
      "POST",
      "DELETE"
    ],
    "AllowedOrigins": [
      "http://localhost:3000",
      "http://localhost:8080",
      "https://easybody.com",
      "https://www.easybody.com",
      "https://staging.easybody.com"
    ],
    "ExposeHeaders": [
      "ETag",
      "x-amz-meta-custom-header"
    ],
    "MaxAgeSeconds": 3600
  }
]
```

6. Click **Save changes**

### Bước 3.5: Configure Bucket Policy (Public Read)

1. Vẫn ở tab **Permissions**
2. Scroll xuống **Bucket policy**
3. Click **Edit**
4. Paste JSON sau (thay `easybody-media` bằng bucket name của bạn):

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::easybody-media/*"
    }
  ]
}
```

5. Click **Save changes**

### Bước 3.6: Create Folder Structure

1. Tab **Objects**
2. Click **Create folder**
3. Tạo 3 folders:
   ```
   Folder name: profiles/
   Folder name: gyms/

