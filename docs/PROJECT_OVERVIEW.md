# 🧭 Easy Body – Project Overview (Full Consolidation v1.2)

_Last refreshed: October 2025 • Stack: Next.js · Spring Boot 3 · Java 21 · PostgreSQL/PostGIS · AWS Cognito/S3/SQS · Gradle_

> Quick links: [`API docs`](./api/API_DOCUMENTATION.md) • [`Backend plan`](./backend/BACKEND_PLAN.md) • [`AWS guides`](./aws/AWS_DEPLOY_GUIDE.md) • [`Frontend guide`](./frontend/FRONTEND_GUIDE.md) • [`Legacy notes`](./legacy/)

---

## 1️⃣ Product Vision & Role Matrix

### 🎯 Marketplace Mission
- Kết nối **Gyms**, **Personal Trainers (PTs)**, và **Clients** trong một nền tảng thống nhất.
- Hỗ trợ quảng bá, đặt dịch vụ, đánh giá, và quản lý hoạt động thể hình với tìm kiếm theo vị trí và media phong phú.

### 👥 Role & Chức Năng
| Role | Quyền hạn chính |
|------|-----------------|
| **Admin** | Duyệt nội dung, quản lý report & subscription tiers, giám sát hệ thống. |
| **Gym Staff** | Đăng ký/Quản lý gym, liên kết PT, tạo Gym Offers. |
| **PT User** | Tạo hồ sơ huấn luyện, liên kết gym, tạo PT Offers, quản lý lịch. |
| **Client User** | Tìm kiếm, bookmark, review, báo cáo vi phạm, yêu cầu huấn luyện. |

### 🖥️ Frontend Layout Highlights
- **Home**: Card-based discovery (Airbnb style) cho gyms/offers nổi bật.
- **Gym Offers / PT Offers**: Bộ lọc theo giá, radius, rating, subscription rank.
- **Profile**: Dashboard tùy role (client/PT/gym) với công cụ quản lý.

_Nguồn: `Idea.docx`, `Layout.docx`, `Cấu Trúc.md`._

---

## 2️⃣ Architecture Overview

```mermaid
flowchart TB
    User[Browser / Next.js 14] --> CF[CloudFront CDN]
    CF --> S3FE[S3 Static Hosting]
    User -->|REST| ALB[Application Load Balancer]
    ALB --> EC2[EC2 Auto Scaling Group\n(Spring Boot 3)]
    EC2 --> RDS[(Amazon RDS PostgreSQL 15\n+ PostGIS)]
    EC2 --> S3DATA[S3 Media Bucket]
    EC2 --> COG[AWS Cognito\n(User Pool + App Client)]
    EC2 --> SQS[AWS SQS\nImage Moderation Queue]
    EC2 --> CW[CloudWatch + X-Ray]
    RDS --> Backup[AWS Backup]
    Route53[Route 53] --> CF
    Route53 --> ALB
```

| Layer | Responsibility | Key Assets |
|-------|----------------|------------|
| **Frontend** | Next.js 14 (CSR/export) | `FRONTEND_GUIDE.md` |
| **Backend** | Spring Boot REST API | Controllers (`AuthController`, `GymController`, …) |
| **Database** | Postgres + PostGIS | Entities (`User`, `Gym`, `Offer`, `Location`, …) |
| **Security** | Cognito JWT + Spring Security | `SecurityConfig`, `JwtAuthenticationFilter` |
| **Infrastructure** | AWS VPC, ALB, EC2, S3, SQS, RDS, CloudWatch | Terraform/TBD |

---

## 3️⃣ Domain Model & Database

- **Bảng cốt lõi**: `users`, `gyms`, `pt_users`, `gym_staff`, `offers`, `ratings`, `reports`, `locations`, `subscription_plans`, `gym_pt_associations`.
- **Workflow nổi bật**:
  - `gym_pt_associations`: many-to-many với approval logs.
  - `offers`: enum phân biệt Gym/PT offer; ràng buộc FK có điều kiện.
  - `ratings`: trigger cập nhật `average_rating` + `rating_count`.
  - `subscription_plans.rank_weight`: ảnh hưởng thứ hạng hiển thị.
- **PostGIS**:
  ```sql
  CREATE EXTENSION IF NOT EXISTS postgis;
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  ```
  Sử dụng GIST index trên `locations.coordinates` và các FK tới locations.
- **Migration**: quản lý bởi Flyway (`V1__init_extensions_and_indexes.sql`, `V2__offer_rating_trigger.sql`).
- **Tài liệu bổ trợ**:
  - Local bootstrap & seed: [`DATABASE_LOCAL_SETUP.md`](./backend/DATABASE_LOCAL_SETUP.md)
  - Legacy SQL Server setup: [`legacy/SQL_SERVER_AUTH_SETUP.md`](./legacy/SQL_SERVER_AUTH_SETUP.md)

---

## 4️⃣ Build & Local Run

```bash
# build + unit tests
./gradlew build

# start Postgres/PostGIS (healthcheck + seed)
docker compose up -d db

# run Spring Boot (local profile)
./gradlew bootRun
```

- Profile mặc định: `local` (JDBC tới `localhost:5432`).
- Swagger UI: `http://localhost:8080/swagger-ui.html`.
- Docker multi-stage build (`Dockerfile`) tạo `easybody-backend` image; compose orchestrates `app` + `db`.
- `.env.example` chứa biến môi trường mẫu (DB, AWS toggles, ports).

---

## 5️⃣ Deployment Snapshot (AWS)

1. **Provision**: VPC (public/private subnets), ALB, EC2 ASG, RDS Postgres + PostGIS, S3 buckets, Cognito User Pool, SQS queue, CloudWatch dashboards, Route 53 records.
2. **Build artifact**: `./gradlew bootJar` → `build/libs/easybody-*.jar`.
3. **Launch** app on EC2 (or ECS/Beanstalk):
   ```bash
   SPRING_PROFILES_ACTIVE=aws \
   DB_HOST=<rds-endpoint> \
   DB_USERNAME=postgres \
   DB_PASSWORD=*** \
   AWS_REGION=us-east-1 \
   java -jar easybody.jar
   ```
4. **Frontend**: `next build && npx next export` → upload `/out` to S3 → invalidate CloudFront.
5. **Migrations**: Flyway runs on startup or via `./gradlew flywayMigrate`.

Chi tiết từng service: [`AWS_SERVICES_REQUIRED.md`](./aws/AWS_SERVICES_REQUIRED.md) & [`AWS_DEPLOY_GUIDE.md`](./aws/AWS_DEPLOY_GUIDE.md).

---

## 6️⃣ Authentication & Authorization

- **Primary**: AWS Cognito JWT (RS256); local dev dùng token HS256 mẫu (`API_DOCUMENTATION.md` hướng dẫn).
- **Roles**: `ADMIN`, `GYM_STAFF`, `PT_USER`, `CLIENT_USER`.
- **Spring Security**:
  - Public: `/api/v1/search/**`, `/api/v1/gyms/**` (public endpoints), `/swagger-ui/**`, `/v3/api-docs/**`.
  - Role-based: admin moderation (`/api/v1/admin/**`), PT management, offer creation.
- **Swagger Config**: `@SecurityScheme(bearerAuth)` hiển thị bearer input.
- **TODO**: Validate JWT signature via Cognito JWKS + implement refresh token flow.

Legacy auth scripts (Node.js + SQL Server) lưu tại [`legacy/QUICK_START_AUTH.md`](./legacy/QUICK_START_AUTH.md).

---

## 7️⃣ API Surface

- Tổng quan 43 endpoint: [`API_DOCUMENTATION.md`](./api/API_DOCUMENTATION.md) (bao gồm sample request/response, lỗi).
- Nearby search chi tiết (PostGIS + FE snippet): [`API_NEARBY_SEARCH.md`](./api/API_NEARBY_SEARCH.md).
- FE call patterns (JWT attach, S3 upload, pagination): [`FRONTEND_API_INTEGRATION.md`](./api/FRONTEND_API_INTEGRATION.md).
- Swagger JSON: `http://localhost:8080/v3/api-docs` (importable to Postman / Stoplight).

---

## 8️⃣ Geo Search & Recommendation

- Endpoint: `GET /api/v1/search/nearby?lat=<>&lon=<>&radiusKm=<>`.
- Query:
  ```sql
  SELECT id, ST_Distance(location, ST_MakePoint(:lon, :lat)::geography)/1000 AS distance_km
  FROM gyms
  WHERE ST_DWithin(location, ST_MakePoint(:lon, :lat)::geography, :radiusKm * 1000)
  ORDER BY distance_km;
  ```
- Recommendation weight factoring: subscription tiers (`rank_weight`), rating, distance.
- FE fallback (manual address geocoding) mô tả trong `API_NEARBY_SEARCH.md`.

---

## 9️⃣ Tooling & Dev Workflow

| Tool | Vai trò |
|------|---------|
| **Swagger UI / Springdoc** | Live API docs + bearer auth input |
| **Flyway** | Schema migrations & versioning |
| **Docker Compose** | Dev stack (Spring + PostGIS + seed) |
| **Postman / curl** | Manual testing (samples trong API docs) |
| **pgAdmin / DBeaver** | DB inspection |
| **Gradle Wrapper** | Build consistency (`./gradlew`) |
| **GitHub Projects** | Tracking roadmap (gợi ý) |

Frontend reference: [`../frontend/FRONTEND_GUIDE.md`](./frontend/FRONTEND_GUIDE.md).

---

## 🔟 Legacy Footprints

- Node.js + SQL Server era (archived):
  - [`legacy/SQL_SERVER_AUTH_SETUP.md`](./legacy/SQL_SERVER_AUTH_SETUP.md)
  - [`legacy/QUICK_START_AUTH.md`](./legacy/QUICK_START_AUTH.md)
- Product ideation & early planning artefacts:
  - [`legacy/BaseIdea/`](./legacy/BaseIdea/) (Idea, Layout, structural drafts)
  - [`legacy/PROJECT_SUMMARY.md`](./legacy/PROJECT_SUMMARY.md)
- Giữ để đối chiếu; không ảnh hưởng build Spring Boot hiện tại.

---

## 1️⃣1️⃣ Status Dashboard

| Capability | State |
|------------|-------|
| RESTful API coverage | ✅ Complete |
| PostGIS nearby search | ✅ Complete |
| JWT security (Cognito) | ✅ Integrated |
| AWS infrastructure readiness | ⚙️ Ready (consumer pending) |
| Flyway migrations | ✅ In place |
| Swagger/OpenAPI | ✅ Available |
| Docker workflow | ✅ Available |
| Legacy cleanup | ✅ Archived |
| CI/CD automation | ⏳ Planned |
| Moderation SQS consumer | ⏳ Planned |

---

## 1️⃣2️⃣ Roadmap & Next Steps

1. **Moderation Pipeline**: Implement SQS consumer + SageMaker/Bedrock moderation scoring, auto-update `risk_score` & status.
2. **Search Enhancements**: Integrate OpenSearch/Elasticsearch for fuzzy + hybrid search (text + geo + rank weight).
3. **Notifications**: Wire Amazon SES / SNS for booking, moderation, and approval events.
4. **CI/CD**: GitHub Actions deploying to S3/CloudFront & EC2/ECS, Flyway migrate step, smoke tests.
5. **Security Hardening**: Rate limiting, WAF policies, Cognito signature verification, optional Redis caching.

---

For sprint planning and historical context, see [`BACKEND_PLAN.md`](./backend/BACKEND_PLAN.md). For frontend onboarding, follow [`FRONTEND_GUIDE.md`](./frontend/FRONTEND_GUIDE.md). All supplemental documentation stays organised under `/docs` — this file serves as the canonical hub for proposals, onboarding, and FCJ deliverables. Commit updates here whenever new capabilities land.
