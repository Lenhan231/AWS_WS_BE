## Environment Playbook

> Ghi chú nhanh cách bật BE trong ba môi trường chính sau khi restructure profile (`local`, `staging`, `prod`) và bổ sung PostGIS.

---

### 1. Local Development (Docker Compose)

**Option A – chạy tất cả bằng Docker:**

```bash
docker compose up --build
```

- Services: `easybody-db` (PostGIS), `easybody-minio`, `easybody-app`.
- App container dùng profile `local`, kết nối DB nội bộ bằng hostname `db`.
- Dừng stack: `docker compose down`.

**Option B – container DB + Gradle bootRun:**

```bash
docker compose up -d db s3     # chỉ Postgres + MinIO

# Khởi động Spring Boot từ host
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/easybody \
SPRING_DATASOURCE_USERNAME=postgres \
SPRING_DATASOURCE_PASSWORD=postgres \
./gradlew bootRun
```

> Nếu volume `pg-data` bị seed bằng credential khác, tra lại giá trị thực qua `docker compose exec db env` và sửa ba biến trên cho khớp.

---

### 2. Railway Staging

**Postgres/PostGIS:**
- Tạo service từ template `postgis/postgis:16-master` (hoặc image tương đương).
- Đặt các biến bắt buộc trên service PostGIS:
  ```
  POSTGRES_USER=postgre
  POSTGRES_PASSWORD=<StrongPassword>
  POSTGRES_DB=railway
  PGUSER=<same-as-POSTGRES_USER>
  PGPASSWORD=<same-as-POSTGRES_PASSWORD>
  PGDATABASE=<same-as-POSTGRES_DB>
  ```
- Sau khi service online, dùng Railway CLI hoặc tab “Connect” để bật extension:
  ```sql
  CREATE EXTENSION IF NOT EXISTS postgis;
  CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
  ```

**Backend service (Dockerfile build):**

Env bắt buộc:
```
SPRING_PROFILES_ACTIVE=staging
JDBC_DATABASE_URL=jdbc:postgresql://postgis-urev.railway.internal:5432/railway?sslmode=require
JDBC_DATABASE_USERNAME=postgres
JDBC_DATABASE_PASSWORD=<POSTGRES_PASSWORD>
AWS_ENABLED=false
CORS_ALLOWED_ORIGINS=https://aws-ws-fe.vercel.app,http://localhost:3000
```

> ✅ **Double-check before deploy:** Railway hiện đặt tên private host là `postgis-urev.railway.internal`. Luôn copy chính xác host này từ tab **Connect** của service PostGIS và giữ username là `postgres` (mặc dù UI đôi khi hiển thị `postgre`). Nếu service DB bị recreate, cập nhật lại hai giá trị này rồi hãy redeploy.

Redeploy để Flyway migrate (log mong đợi: `Schema "public" is up to date`).

---

### 3. AWS Production (App Runner + RDS)

**Hạ tầng tối thiểu:**
1. RDS PostgreSQL có PostGIS (bật extension sau khi tạo DB):
   ```sql
   CREATE EXTENSION IF NOT EXISTS postgis;
   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   ```
2. S3 bucket cho media (tùy chọn CloudFront).
3. App Runner service triển khai từ image đẩy lên ECR (CI đã hỗ trợ).

**Env trên App Runner:**
```
SPRING_PROFILES_ACTIVE=prod
JDBC_DATABASE_URL=jdbc:postgresql://<rds-endpoint>:5432/<db>?sslmode=require
JDBC_DATABASE_USERNAME=<db-user>
JDBC_DATABASE_PASSWORD=<db-password>
AWS_REGION=<region>
S3_BUCKET_NAME=<bucket>
AWS_ENABLED=true
```

> App Runner tự gán `PORT`, Dockerfile đã đọc `${PORT:-8080}` nên không cần cấu hình thêm.

---

### Quick Troubleshooting Checklist

| Issue | Nguyên nhân phổ biến | Cách xử lý |
| --- | --- | --- |
| `UnknownHostException: db` khi bootRun từ máy | Host `db` chỉ tồn tại trong Docker network | Dùng `localhost` + credentials thật hoặc chạy app trong compose |
| Flyway báo `extension "postgis" is not available` | DB không cài PostGIS | Đảm bảo dùng image PostGIS hoặc cài extension trên RDS |
| Railway healthcheck 503 | Thiếu `SPRING_PROFILES_ACTIVE=staging` hoặc `JDBC_DATABASE_*` | Kiểm tra biến env và copy đúng giá trị từ service PostGIS |
| Gradle build fail `GradleWrapperMain` | `gradle-wrapper.jar` bị ignore | Đã commit wrapper jar để Docker build thành công |
