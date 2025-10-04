- [ ] Setup Route 53 domain
- [ ] Configure SSL certificate (ACM)
- [ ] Test all endpoints
- [ ] Setup monitoring & alarms
- [ ] Configure backup policies
- [ ] Document credentials

---

## 🆘 HỖ TRỢ & TÀI LIỆU

### AWS Documentation:
- Cognito: https://docs.aws.amazon.com/cognito/
- RDS: https://docs.aws.amazon.com/rds/
- S3: https://docs.aws.amazon.com/s3/
- Lambda: https://docs.aws.amazon.com/lambda/

### Terraform/CloudFormation (Optional):
Có thể dùng Infrastructure as Code để tự động hóa setup

---

**Lưu ý:** Chi phí trên chỉ là ước tính và có thể thay đổi tùy theo:
- Traffic thực tế
- Data storage
- Region được chọn
- Discount plans (Reserved Instances, Savings Plans)
# Các Dịch Vụ AWS Cần Thiết cho Easy Body Backend

## 📋 Tổng Quan

Để hoàn thành và deploy hệ thống Easy Body Backend, bạn cần các dịch vụ AWS sau:

---

## 1️⃣ AWS Cognito (Authentication) - **BẮT BUỘC**

### Mục đích:
- Quản lý đăng ký, đăng nhập người dùng
- Tạo và xác thực JWT tokens
- Quản lý user pools và federated identities

### Cấu hình cần thiết:
```yaml
# User Pool Settings
- User Pool Name: easybody-users
- Sign-in options: Email
- Password policy: Strong (min 8 chars, uppercase, lowercase, numbers)
- MFA: Optional (khuyến nghị bật cho ADMIN)

# Custom Attributes
- custom:role (String) - ADMIN, GYM_STAFF, PT_USER, CLIENT_USER

# App Client
- App client name: easybody-backend
- Authentication flows: USER_PASSWORD_AUTH, REFRESH_TOKEN_AUTH
- Token expiration: 
  - Access token: 1 hour
  - Refresh token: 30 days
```

### Environment Variables:
```bash
COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_JWKS_URL=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json
```

### Chi phí ước tính:
- Free tier: 50,000 MAUs (Monthly Active Users)
- Sau đó: $0.0055 per MAU

---

## 2️⃣ Amazon RDS PostgreSQL (Database) - **BẮT BUỘC**

### Mục đích:
- Lưu trữ dữ liệu chính (users, gyms, offers, ratings, reports)
- PostGIS extension cho geo-location queries

### Cấu hình khuyến nghị:

#### **Development/Staging:**
```yaml
Instance Class: db.t3.micro (2 vCPU, 1 GB RAM)
Storage: 20 GB SSD (gp3)
Multi-AZ: No
Backup: 7 days retention
PostgreSQL Version: 15.x
```

#### **Production:**
```yaml
Instance Class: db.t3.medium (2 vCPU, 4 GB RAM)
Storage: 50-100 GB SSD (gp3)
Multi-AZ: Yes (High Availability)
Backup: 30 days retention
Read Replicas: 1-2 (tùy traffic)
PostgreSQL Version: 15.x
```

### Extensions cần cài:
```sql
CREATE EXTENSION postgis;
CREATE EXTENSION pg_trgm;  -- For text search (optional)
```

### Environment Variables:
```bash
DB_HOST=easybody-db.xxxxxxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=easybody
DB_USERNAME=easybody_admin
DB_PASSWORD=<strong-password>
```

### Chi phí ước tính (Production):
- db.t3.medium: ~$61/month
- Storage (50GB): ~$6/month
- Multi-AZ: ~$122/month (gấp đôi)
- **Tổng: ~$189/month**

---

## 3️⃣ Amazon S3 (Media Storage) - **BẮT BUỘC**

### Mục đích:
- Lưu trữ hình ảnh (profile pictures, gym logos, offer images)
- Pre-signed URLs cho upload trực tiếp từ client

### Cấu hình:
```yaml
Bucket Name: easybody-media
Region: us-east-1
Versioning: Enabled (khuyến nghị)
Lifecycle Rules:
  - Delete incomplete multipart uploads after 7 days
  - Transition to S3 IA after 90 days (nếu cần tiết kiệm)

Folders Structure:
  /profiles/     - User profile images
  /gyms/         - Gym logos
  /offers/       - Offer images
```

### CORS Configuration:
```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST"],
    "AllowedOrigins": ["https://easybody.com", "http://localhost:3000"],
    "ExposeHeaders": ["ETag"]
  }
]
```

### Bucket Policy (Public Read):
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

### Environment Variables:
```bash
S3_BUCKET_NAME=easybody-media
S3_PRESIGNED_URL_EXPIRATION=3600
```

### Chi phí ước tính:
- Storage: $0.023 per GB/month
- PUT requests: $0.005 per 1,000 requests
- GET requests: $0.0004 per 1,000 requests
- Data transfer out: $0.09 per GB
- **Ví dụ 10GB + 100k requests: ~$1-2/month**

---

## 4️⃣ Amazon SQS (Message Queue) - **KHUYẾN NGHỊ**

### Mục đích:
- Queue cho image moderation (khi offer có hình ảnh mới)
- Xử lý bất đồng bộ để không làm chậm API response

### Cấu hình:
```yaml
Queue Name: easybody-image-moderation-queue
Queue Type: Standard (hoặc FIFO nếu cần thứ tự)
Visibility Timeout: 300 seconds (5 minutes)
Message Retention: 4 days
Dead Letter Queue: easybody-dlq (cho failed messages)
```

### Environment Variables:
```bash
SQS_IMAGE_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/easybody-image-moderation-queue
```

### Chi phí ước tính:
- Free tier: 1 million requests/month
- Sau đó: $0.40 per million requests
- **Thường < $1/month cho MVP**

---

## 5️⃣ AWS Lambda (Serverless Functions) - **KHUYẾN NGHỊ**

### Mục đích:
- Consumer cho SQS queue
- Image moderation với AWS Rekognition
- Image resize/optimization

### Functions cần tạo:

#### **Function 1: Image Moderation**
```yaml
Function Name: easybody-image-moderator
Runtime: Java 21 (hoặc Python 3.11 cho nhanh)
Memory: 512 MB
Timeout: 60 seconds
Trigger: SQS queue
Environment:
  - REKOGNITION_MIN_CONFIDENCE: 80
  - OFFER_API_ENDPOINT: https://api.easybody.com
```

#### **Function 2: Image Resize**
```yaml
Function Name: easybody-image-resizer
Runtime: Node.js 20.x (với sharp library)
Memory: 1024 MB
Timeout: 30 seconds
Trigger: S3 PUT events (offers/, gyms/, profiles/)
```

### Chi phí ước tính:
- Free tier: 1M requests + 400,000 GB-seconds/month
- Sau đó: $0.20 per 1M requests + $0.0000166667 per GB-second
- **Thường < $5/month cho traffic vừa**

---

## 6️⃣ AWS Rekognition (Image Moderation) - **KHUYẾN NGHỊ**

### Mục đích:
- Tự động phát hiện nội dung không phù hợp trong hình ảnh
- Detect explicit content, violence, etc.

### API sử dụng:
```java
DetectModerationLabels API
- Min confidence: 80%
- Categories: Explicit Nudity, Violence, Drugs, Hate Symbols
```

### Chi phí ước tính:
- Free tier: 5,000 images/month (12 tháng đầu)
- Sau đó: $1.00 per 1,000 images
- **Ví dụ 10,000 images/month: $10/month**

---

## 7️⃣ Amazon CloudWatch (Monitoring & Logging) - **BẮT BUỘC**

### Mục đích:
- Log aggregation từ Spring Boot application
- Metrics & alarms
- Troubleshooting & debugging

### Cấu hình:
```yaml
Log Groups:
  - /aws/springboot/easybody-api
  - /aws/lambda/easybody-image-moderator
  - /aws/lambda/easybody-image-resizer

Retention: 30 days (production), 7 days (dev)

Alarms:
  - High error rate (>5% in 5 minutes)
  - Database connection failures
  - High latency (>2 seconds p99)
  - High memory usage (>80%)
```

### Environment Variables:
```bash
CLOUDWATCH_NAMESPACE=EasyBody
```

### Chi phí ước tính:
- Log ingestion: $0.50 per GB
- Log storage: $0.03 per GB/month
- Metrics: $0.30 per custom metric/month
- **Ví dụ 10GB logs/month: ~$5-10/month**

---

## 8️⃣ AWS X-Ray (Distributed Tracing) - **TÙY CHỌN**

### Mục đích:
- Trace requests qua các services
- Performance analysis
- Bottleneck identification

### Cấu hình:
```yaml
Sampling Rate: 10% (để tiết kiệm chi phí)
Service Name: easybody-api
```

### Chi phí ước tính:
- Free tier: 100,000 traces/month
- Sau đó: $5.00 per million traces
- **Thường < $2/month**

---

## 9️⃣ Amazon EC2 / ECS / Elastic Beanstalk (Compute) - **BẮT BUỘC**

### Lựa chọn triển khai:

#### **Option 1: EC2 (Đơn giản nhất)**
```yaml
Instance Type: t3.small (2 vCPU, 2 GB RAM)
OS: Amazon Linux 2023
Java 21: Cài sẵn
Auto Scaling: Optional
Load Balancer: Application LB (nếu cần HA)
```

**Chi phí:** ~$15/month (1 instance)

#### **Option 2: ECS Fargate (Container, Khuyến nghị)**
```yaml
Task CPU: 0.5 vCPU
Task Memory: 1 GB
Service: easybody-api
Desired Count: 2 (HA)
Load Balancer: Application LB
```

**Chi phí:** ~$30-40/month (2 tasks)

#### **Option 3: Elastic Beanstalk (Dễ nhất cho MVP)**
```yaml
Platform: Java 21 with Corretto
Environment: Load balanced
Instance: t3.small
Min instances: 1
Max instances: 4 (auto scaling)
```

**Chi phí:** ~$15-60/month (tùy scaling)

---

## 🔟 Amazon Route 53 (DNS) - **KHUYẾN NGHỊ**

### Mục đích:
- Domain management
- Health checks
- Routing policies

### Cấu hình:
```yaml
Domain: api.easybody.com
Records:
  - A record → ALB (hoặc EC2 IP)
  - CNAME → S3 bucket (nếu dùng CloudFront)
Health Check: HTTP/HTTPS endpoint check every 30s
```

### Chi phí ước tính:
- Hosted zone: $0.50/month
- Queries: $0.40 per million queries
- Health checks: $0.50/month each
- **Tổng: ~$2-3/month**

---

## 1️⃣1️⃣ Amazon CloudFront (CDN) - **TÙY CHỌN** (Khuyến nghị cho production)

### Mục đích:
- Cache static content (images from S3)
- Reduce latency globally
- HTTPS termination

### Cấu hình:
```yaml
Origin: easybody-media.s3.amazonaws.com
Price Class: Use Only North America and Europe
Cache Policy: CachingOptimized
TTL: 86400 (24 hours)
Custom Domain: cdn.easybody.com
```

### Chi phí ước tính:
- Data transfer out: $0.085 per GB (first 10 TB)
- Requests: $0.0075 per 10,000 HTTPS requests
- **Ví dụ 100GB + 1M requests: ~$8-10/month**

---

## 1️⃣2️⃣ AWS Secrets Manager - **KHUYẾN NGHỊ**

### Mục đích:
- Lưu trữ an toàn database credentials
- Auto rotation passwords
- API keys management

### Secrets cần lưu:
```yaml
Secrets:
  - easybody/db/password
  - easybody/cognito/client-secret
  - easybody/s3/access-keys (nếu không dùng IAM roles)
```

### Chi phí ước tính:
- $0.40 per secret per month
- $0.05 per 10,000 API calls
- **Ví dụ 3 secrets: ~$1.2/month**

---

## 📊 TỔNG CHI PHÍ ƯỚC TÍNH

### **Development/MVP (Traffic thấp):**
| Service | Monthly Cost |
|---------|-------------|
| Cognito | Free (< 50k users) |
| RDS (db.t3.micro) | $15 |
| S3 | $2 |
| SQS | Free |
| Lambda | Free |
| Rekognition | Free (first year) |
| CloudWatch | $5 |
| EC2 (t3.small) | $15 |
| Route 53 | $2 |
| **TỔNG** | **~$39/month** |

### **Production (Traffic trung bình):**
| Service | Monthly Cost |
|---------|-------------|
| Cognito | $5 (100k MAUs) |
| RDS (db.t3.medium, Multi-AZ) | $189 |
| S3 | $10 |
| SQS | $1 |
| Lambda | $5 |
| Rekognition | $10 |
| CloudWatch | $10 |
| ECS Fargate (2 tasks) | $40 |
| Route 53 | $3 |
| CloudFront | $10 |
| Secrets Manager | $2 |
| **TỔNG** | **~$285/month** |

---

## 🚀 PLAN TRIỂN KHAI

### **Phase 1: MVP (Week 1-2)**
1. ✅ Setup Cognito User Pool
2. ✅ Create RDS PostgreSQL instance
3. ✅ Create S3 bucket
4. ✅ Deploy backend to EC2/Elastic Beanstalk
5. ✅ Configure CloudWatch logs
6. ✅ Test API endpoints

**Chi phí:** ~$39/month

### **Phase 2: Image Moderation (Week 3)**
1. ✅ Setup SQS queue
2. ✅ Create Lambda function for Rekognition
3. ✅ Integrate with backend API
4. ✅ Test moderation workflow

**Chi phí thêm:** ~$5/month

### **Phase 3: Production Ready (Week 4)**
1. ✅ Enable Multi-AZ for RDS
2. ✅ Setup Auto Scaling for compute
3. ✅ Add CloudFront for S3
4. ✅ Configure Secrets Manager
5. ✅ Setup monitoring & alarms
6. ✅ Load testing

**Chi phí thêm:** ~$241/month (total ~$285)

---

## 🔐 IAM ROLES & POLICIES CẦN TẠO

### **1. Backend Application Role**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:PutObject",
        "s3:GetObject",
        "s3:DeleteObject"
      ],
      "Resource": "arn:aws:s3:::easybody-media/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "sqs:SendMessage",
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage"
      ],
      "Resource": "arn:aws:sqs:*:*:easybody-*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "arn:aws:logs:*:*:*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "secretsmanager:GetSecretValue"
      ],
      "Resource": "arn:aws:secretsmanager:*:*:secret:easybody/*"
    }
  ]
}
```

### **2. Lambda Execution Role**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "rekognition:DetectModerationLabels"
      ],
      "Resource": "*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject"
      ],
      "Resource": "arn:aws:s3:::easybody-media/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "sqs:ReceiveMessage",
        "sqs:DeleteMessage",
        "sqs:GetQueueAttributes"
      ],
      "Resource": "arn:aws:sqs:*:*:easybody-image-moderation-queue"
    }
  ]
}
```

---

## 📝 ENVIRONMENT VARIABLES HOÀN CHỈNH

```bash
# Database
DB_HOST=easybody-db.xxxxxxxxx.us-east-1.rds.amazonaws.com
DB_PORT=5432
DB_NAME=easybody
DB_USERNAME=easybody_admin
DB_PASSWORD=<stored-in-secrets-manager>

# AWS Configuration
AWS_REGION=us-east-1
AWS_ACCOUNT_ID=123456789012

# Cognito
COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
COGNITO_JWKS_URL=https://cognito-idp.us-east-1.amazonaws.com/us-east-1_XXXXXXXXX/.well-known/jwks.json

# S3
S3_BUCKET_NAME=easybody-media
S3_PRESIGNED_URL_EXPIRATION=3600

# SQS
SQS_IMAGE_QUEUE_URL=https://sqs.us-east-1.amazonaws.com/123456789012/easybody-image-moderation-queue

# CloudWatch
CLOUDWATCH_NAMESPACE=EasyBody

# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080
```

---

## ✅ CHECKLIST SETUP AWS

- [ ] Tạo AWS Account
- [ ] Setup IAM users & roles
- [ ] Create Cognito User Pool
- [ ] Provision RDS PostgreSQL
- [ ] Create S3 bucket + configure CORS
- [ ] Setup SQS queue
- [ ] Create Lambda functions
- [ ] Configure CloudWatch logs
- [ ] Deploy backend application

