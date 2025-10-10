# 🧭 Easy Body – Full Stack Project Overview (v1.3)

Next.js · Spring Boot 3 · Java 21 · PostgreSQL/PostGIS · AWS Cognito/S3/SQS

> Một nền tảng kết nối **Gyms**, **Personal Trainers (PTs)** và **Clients**, giúp quảng bá gói tập, quản lý ưu đãi, đặt lịch và đánh giá trong cùng hệ thống.

---

## 📘 1. Architecture Snapshot

```
Frontend (Next.js)
   ↓ API calls
Backend (Spring Boot 3, Java 21)
   ↳ PostgreSQL + PostGIS
   ↳ AWS S3 (media)
   ↳ AWS Cognito (JWT Auth)
   ↳ AWS SQS (moderation queue)
```

| AWS Service | Purpose |
|-------------|---------|
| **EC2 / ECS** | Host Spring Boot JAR or container |
| **RDS (PostgreSQL)** | Main database (PostGIS enabled) |
| **S3** | Media uploads via presigned URLs + static frontend hosting |
| **Cognito** | JWT authentication & user management |
| **CloudWatch / X-Ray** | Logging, metrics, tracing |
| **SQS** | Image moderation queue (phase 2) |
| **Route 53 + CloudFront** | Custom domains & CDN |

---

## 🧱 2. Roles & Core Features

| Role | Capabilities |
|------|--------------|
| **Admin** | Approve offers, review reports, manage subscription tiers |
| **Gym Staff** | Register/manage gyms, assign PTs, publish gym offers |
| **PT User** | Create PT profile, link to gyms, publish PT offers |
| **Client User** | Discover gyms/PTs, bookmark, rate, report content |

Frontend layout nổi bật: Home (card gallery), Gym Offers, PT Offers, Profile dashboards theo từng role (tham khảo `docs/frontend/FRONTEND_GUIDE.md`).

---

## 🗄️ 3. Database (PostgreSQL + PostGIS)

- Bảng chính: `users`, `gyms`, `pt_users`, `gym_staff`, `offers`, `ratings`, `reports`, `locations`, `subscription_plans`, `gym_pt_associations`.
- Flyway migrations điều khiển schema (`src/main/resources/db/migration`).
- Extension cần thiết:
  ```sql
  CREATE EXTENSION IF NOT EXISTS postgis;
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  ```
- Trigger cập nhật rating giữ đồng bộ `average_rating` và `rating_count` (xem `V2__offer_rating_trigger.sql`).
- Tài liệu setup local: [`docs/backend/DATABASE_LOCAL_SETUP.md`](docs/backend/DATABASE_LOCAL_SETUP.md).

**Geo-search sample**
```sql
SELECT id,
       ST_Distance(location, ST_MakePoint(:lon,:lat)::geography)/1000 AS distance_km
FROM gyms
WHERE ST_DWithin(location, ST_MakePoint(:lon,:lat)::geography, :radiusKm * 1000)
ORDER BY distance_km;
```

---

## ⚙️ 4. Backend Setup

```bash
./gradlew build
docker compose up -d db
./gradlew bootRun
```

**Dockerfile (multi-stage)**
```dockerfile
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
```

**docker-compose.yml excerpt**
```yaml
services:
  app:
    build: .
    ports: ["8080:8080"]
    depends_on: [db]
    environment:
      SPRING_PROFILES_ACTIVE: local
      DB_HOST: db
      DB_USERNAME: postgres
      DB_PASSWORD: postgres

  db:
    image: postgis/postgis:15-3.4-alpine
    environment:
      POSTGRES_DB: easybody
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports: ["5432:5432"]
```

`.env.example` chứa tất cả biến cần thiết (DB, AWS toggles, port publish).

---

## 🔐 5. Authentication (AWS Cognito)

```
Frontend → Cognito Hosted UI / Amplify → JWT → Spring Boot (`/api/v1/auth/register`, `/api/v1/auth/me`)
```

- Roles hỗ trợ: `ADMIN`, `GYM_STAFF`, `PT_USER`, `CLIENT_USER`.
- Spring Security cấu hình public endpoints (`/api/v1/search/**`, `/swagger-ui/**`, `/v3/api-docs/**`) và role-based guard cho admin/PT/gym flows.
- Swagger UI đã bật `bearerAuth`:
  ```java
  @SecurityScheme(
      name = "bearerAuth",
      type = SecuritySchemeType.HTTP,
      scheme = "bearer",
      bearerFormat = "JWT"
  )
  ```
- Legacy auth (Node.js + SQL Server) lưu tại [`docs/legacy/QUICK_START_AUTH.md`](docs/legacy/QUICK_START_AUTH.md).

### 👩‍💻 Local testing (Swagger/Postman)
1. `./gradlew bootRun` → Flyway migrate + seed.
2. Mở `http://localhost:8080/swagger-ui.html` (hoặc Postman).
3. **Authorize** và paste JWT dev (không kèm chữ `Bearer`):
   ```
   eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJzZWVkLWFkbWluMy1zdWIiLCJjdXN0b206cm9sZSI6IkFETUlOIiwiZW1haWwiOiJkZW1vLmFkbWluQGVhc3lib2R5LmNvbSIsImV4cCI6MTg5MzQ1NjAwMH0.2KG9-ByhYx7dsXRC1fPIxQnjZFy_I4PUHsHbU_KmvSo
   ```
   (HS256, secret dev `local-dev-secret`, `sub = seed-admin3-sub`, role `ADMIN`.)
4. Gọi `POST /api/v1/auth/register` với payload role tương ứng → user mới sẽ xuất hiện trong bảng `users`.

Tự generate token khác (ví dụ cho `GYM_STAFF`) bằng Python:
```python
import jwt, datetime
secret = "local-dev-secret"
payload = {
  "sub": "seed-gymstaff2-sub",
  "custom:role": "GYM_STAFF",
  "email": "gymstaff2@easybody.com",
  "exp": int((datetime.datetime.utcnow() + datetime.timedelta(days=3650)).timestamp())
}
print(jwt.encode(payload, secret, algorithm="HS256"))
```

---

## 🌐 6. API Overview

| Module | Endpoint highlights |
|--------|---------------------|
| **Auth** | `/api/v1/auth/register`, `/api/v1/auth/me` |
| **Gyms** | `/api/v1/gyms`, `/api/v1/gyms/search`, `/api/v1/gyms/{id}` |
| **PT Users** | `/api/v1/pt-users`, `/api/v1/pt-users/{id}` |
| **Offers** | `/api/v1/offers`, `/api/v1/search/offers`, `/api/v1/offers/{id}` |
| **Ratings** | `/api/v1/ratings`, `/api/v1/ratings/offer/{offerId}` |
| **Reports** | `/api/v1/reports` |
| **Admin** | `/api/v1/admin/offers/*`, `/api/v1/admin/reports/*` |
| **Media** | `/api/v1/media/presigned-url` |

**Chi tiết:**
- Full catalogue & samples: [`docs/api/API_DOCUMENTATION.md`](docs/api/API_DOCUMENTATION.md)
- Geo-search deep dive: [`docs/api/API_NEARBY_SEARCH.md`](docs/api/API_NEARBY_SEARCH.md)
- Frontend call patterns: [`docs/api/FRONTEND_API_INTEGRATION.md`](docs/api/FRONTEND_API_INTEGRATION.md)
- Swagger JSON: `http://localhost:8080/v3/api-docs`

---

## 💻 7. Frontend Integration (Next.js 14)

- Base URL: `process.env.NEXT_PUBLIC_API_BASE_URL`
- Auth: Cognito SDK / Amplify (store access & refresh tokens in `localStorage`).
- Image upload: request presigned URL → `PUT` lên S3 → gửi metadata lại backend.
- Geo search: `POST /api/v1/search/offers` hoặc `GET /api/v1/search/nearby` với lat/lon/radius.
- Tham khảo chi tiết trong [`docs/frontend/FRONTEND_GUIDE.md`](docs/frontend/FRONTEND_GUIDE.md).

---

## 🧰 8. Tooling & Developer Workflow

| Tool | Purpose |
|------|---------|
| **Swagger UI / Springdoc** | API explorer (`/swagger-ui.html`) |
| **Postman / curl** | Manual testing (samples trong docs) |
| **Flyway** | Schema migrations & seeding |
| **Docker Compose** | Local stack orchestration |
| **pgAdmin / DBeaver** | DB inspection |
| **CloudWatch / X-Ray** | Logs & tracing trên AWS |
| **Gradle Wrapper** | Build portability (`./gradlew`) |

---

## ☁️ 9. Deployment Playbook (AWS)

1. **Provision**: VPC (public/private subnets), RDS Postgres (PostGIS), S3 buckets (frontend + media), Cognito User Pool, SQS queue, CloudWatch dashboards, Route 53 records.
2. **Build artifact**: `./gradlew bootJar` → `build/libs/easybody-*.jar`.
3. **Deploy backend** (EC2/ECS/Beanstalk):
   ```bash
   SPRING_PROFILES_ACTIVE=aws \
   DB_HOST=<rds-endpoint> \
   DB_USERNAME=postgres \
   DB_PASSWORD=... \
   AWS_REGION=us-east-1 \
   java -jar easybody.jar
   ```
4. **Frontend**: `next build && npx next export` → upload `/out` lên S3 → invalidate CloudFront.
5. **DNS**: Route 53 map `api.easybody.dev` (API) và `www.easybody.dev` (frontend) tới ALB/CloudFront.
6. **Flyway**: chạy migrations khi deploy (`./gradlew flywayMigrate`).

Chi tiết từng bước: [`docs/aws/AWS_SERVICES_REQUIRED.md`](docs/aws/AWS_SERVICES_REQUIRED.md) & [`docs/aws/AWS_DEPLOY_GUIDE.md`](docs/aws/AWS_DEPLOY_GUIDE.md).

---

## 🧱 10. Project Structure

```
src/main/java/com/easybody/
├── config/
├── controller/
├── service/
├── repository/
├── model/
├── dto/
└── exception/

docs/
├── PROJECT_OVERVIEW.md        ← Hub tổng hợp
├── api/
├── backend/
├── aws/
├── frontend/
└── legacy/
```

---

## 🧭 11. Roadmap Highlights

| Track | Status |
|-------|--------|
| RESTful API coverage | ✅ Complete |
| PostGIS nearby search | ✅ Complete |
| JWT security (Cognito) | ✅ Integrated |
| Flyway migrations | ✅ In place |
| AWS infra readiness | ⚙️ Ready (consumer pending) |
| SQS moderation consumer | ⏳ Planned |
| CI/CD automation | ⏳ Planned |
| Security hardening (rate limit, WAF, Redis) | ⏳ Planned |

Detailed backlog: [`docs/backend/BACKEND_PLAN.md`](docs/backend/BACKEND_PLAN.md).

---

## 🪦 Legacy References (Archive)

- [`docs/legacy/BaseIdea/BASE_IDEA_SUMMARY.md`](docs/legacy/BaseIdea/BASE_IDEA_SUMMARY.md) – Ý tưởng & sơ đồ ban đầu.
- [`docs/legacy/PROJECT_SUMMARY.md`](docs/legacy/PROJECT_SUMMARY.md) – Ghi chú migration thời gian đầu.
- [`docs/legacy/SQL_SERVER_AUTH_SETUP.md`](docs/legacy/SQL_SERVER_AUTH_SETUP.md) – Node.js + SQL Server auth stack.
- [`docs/legacy/QUICK_START_AUTH.md`](docs/legacy/QUICK_START_AUTH.md) – Quick test đăng ký/đăng nhập cũ.

Các tài liệu này chỉ dùng đối chiếu lịch sử; production hiện tại dựa trên Spring Boot + PostgreSQL.

---

## 🧾 License

Proprietary © EasyBody Team 2025  
*For FCJ Bootcamp & internal demonstration use.*
