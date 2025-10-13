# Free Hosting Guide for EasyBody Backend

> Deploy Spring Boot + PostgreSQL + PostGIS lên internet **MIỄN PHÍ** hoặc giá rẻ

---

## 🎯 Platform Comparison

| Platform    | Free Tier       | Database               | Sleep?        | Setup | Recommend          |
| ----------- | --------------- | ---------------------- | ------------- | ----- | ------------------ |
| **Railway** | $5/month credit | ✅ PostgreSQL           | ❌ No          | ⭐⭐⭐⭐⭐ | 🏆 Best             |
| **Render**  | 750h/month      | ✅ PostgreSQL (90 days) | ✅ Yes (15min) | ⭐⭐⭐⭐  | 🥈 Good             |
| **Fly.io**  | 3 VMs (256MB)   | ✅ PostgreSQL           | ❌ No          | ⭐⭐⭐   | 🥉 OK               |
| **Koyeb**   | 1 service       | ❌ No                   | ✅ Yes (1h)    | ⭐⭐    | ⚠️ Need external DB |

---

## 🏆 Option 1: Railway.app (Recommended)

### Why Railway?
- ✅ $5 free credit/month (đủ chạy 24/7)
- ✅ PostgreSQL included với PostGIS
- ✅ Không sleep (always on)
- ✅ Deploy từ GitHub tự động
- ✅ UI đẹp, dễ dùng như Vercel

### Step-by-Step

#### 1. Tạo tài khoản
```bash
# Truy cập: https://railway.app
# Sign up with GitHub
```

#### 2. Deploy từ GitHub

**Option A: Qua Web UI (Dễ nhất)**
1. Click "New Project"
2. Chọn "Deploy from GitHub repo"
3. Chọn repo `AWS_WS_BE`
4. Railway tự động detect Dockerfile và deploy

**Option B: Qua CLI**
```bash
# Install Railway CLI
npm i -g @railway/cli

# Login
railway login

# Link to project
railway link

# Deploy
railway up
```

#### 3. Thêm PostgreSQL Database
1. Click "New" → "Database" → "Add PostgreSQL"
2. Railway tự động tạo database với PostGIS support
3. Environment variables tự động inject:
   - `DATABASE_URL`
   - `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

#### 4. Configure Environment Variables
```bash
# Trong Railway dashboard, thêm variables:
SPRING_PROFILES_ACTIVE=aws
DB_HOST=${{Postgres.PGHOST}}
DB_PORT=${{Postgres.PGPORT}}
DB_NAME=${{Postgres.PGDATABASE}}
DB_USERNAME=${{Postgres.PGUSER}}
DB_PASSWORD=${{Postgres.PGPASSWORD}}
CORS_ALLOWED_ORIGINS=https://aws-ws-fe.vercel.app
AWS_ENABLED=false
```

#### 5. Enable PostGIS Extension
```bash
# Connect to Railway PostgreSQL
railway connect postgres

# Run in psql:
CREATE EXTENSION IF NOT EXISTS postgis;
```

#### 6. Deploy!
```bash
# Railway tự động deploy khi push to GitHub
git push origin main

# Hoặc manual deploy:
railway up
```

#### 7. Get Public URL
```bash
# Railway sẽ generate URL:
https://easybody-api-production.up.railway.app

# ✅ CORS chỉ cần frontend URLs (KHÔNG CẦN backend URL)
cors:
  allowed-origins: https://aws-ws-fe.vercel.app,http://localhost:3000
```

---

## 🥈 Option 2: Render.com (100% Free)

### Why Render?
- ✅ Hoàn toàn miễn phí
- ✅ PostgreSQL free (90 ngày đầu)
- ✅ SSL/HTTPS tự động
- ⚠️ Service sleep sau 15 phút không dùng
- ⚠️ Cold start ~30 giây

### Step-by-Step

#### 1. Tạo tài khoản
```bash
# Truy cập: https://render.com
# Sign up with GitHub
```

#### 2. Deploy từ GitHub
1. Click "New" → "Web Service"
2. Connect GitHub repo `AWS_WS_BE`
3. Render tự động detect `render.yaml`
4. Click "Create Web Service"

#### 3. Configure (tự động từ render.yaml)
```yaml
# render.yaml đã có sẵn trong repo
services:
  - type: web
    name: easybody-api
    env: docker
    plan: free

databases:
  - name: easybody-db
    plan: free
```

#### 4. Enable PostGIS
```bash
# Trong Render dashboard:
# Database → easybody-db → Connect
# Copy connection string và connect bằng psql:

psql "postgresql://user:pass@host/db"

# Run:
CREATE EXTENSION IF NOT EXISTS postgis;
```

#### 5. Get Public URL
```
https://easybody-api.onrender.com
```

#### 6. Update Frontend
```bash
# Frontend .env.production
NEXT_PUBLIC_API_BASE_URL=https://easybody-api.onrender.com
```

---

## 🥉 Option 3: Fly.io (Free + Powerful)

### Why Fly.io?
- ✅ Free tier tốt (3 VMs, 256MB RAM)
- ✅ PostgreSQL free (3GB storage)
- ✅ Không sleep
- ✅ Global CDN
- ⚠️ Cần credit card verify (không charge)
- ⚠️ Config phức tạp hơn

### Step-by-Step

#### 1. Install Fly CLI
```bash
# macOS/Linux
curl -L https://fly.io/install.sh | sh

# Windows
powershell -Command "iwr https://fly.io/install.ps1 -useb | iex"
```

#### 2. Login
```bash
fly auth login
```

#### 3. Create PostgreSQL Database
```bash
# Create database with PostGIS
fly postgres create \
  --name easybody-db \
  --region sin \
  --initial-cluster-size 1 \
  --vm-size shared-cpu-1x \
  --volume-size 1

# Enable PostGIS
fly postgres connect -a easybody-db
CREATE EXTENSION IF NOT EXISTS postgis;
```

#### 4. Launch App
```bash
# Trong thư mục AWS_WS_BE
fly launch

# Fly sẽ hỏi:
# - App name: easybody-api
# - Region: Singapore (sin)
# - PostgreSQL: Yes (attach easybody-db)
```

#### 5. Deploy
```bash
fly deploy
```

#### 6. Get Public URL
```bash
fly status

# URL: https://easybody-api.fly.dev
```

---

## 📊 Cost Comparison

### Railway
```
Free: $5 credit/month
- API service: ~$3/month
- PostgreSQL: ~$2/month
Total: FREE (trong $5 credit)

Paid: $5/month (nếu vượt credit)
```

### Render
```
Free: $0/month
- API service: FREE (với sleep)
- PostgreSQL: FREE (90 ngày đầu)
Total: FREE

After 90 days:
- PostgreSQL: $7/month
```

### Fly.io
```
Free: $0/month
- 3 VMs (256MB): FREE
- PostgreSQL (3GB): FREE
Total: FREE

Paid: ~$2/month (nếu cần scale)
```

---

## 🎯 Recommendation

### For Demo/Testing (Short-term)
→ **Render.com** (100% free, dễ setup)

### For Production (Long-term)
→ **Railway.app** ($5/month, stable, no sleep)

### For Learning/Experiment
→ **Fly.io** (free, powerful, good for learning DevOps)

---

## 🔧 Post-Deployment Checklist

### 1. Update CORS
```yaml
# application.yml
cors:
  allowed-origins: https://aws-ws-fe.vercel.app,https://your-api-url.com
```

### 2. Update Frontend
```bash
# Frontend .env.production
NEXT_PUBLIC_API_BASE_URL=https://your-api-url.com
```

### 3. Test API
```bash
curl https://your-api-url.com/actuator/health
curl https://your-api-url.com/api/v1/gyms
```

### 4. Enable PostGIS
```sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

### 5. Run Flyway Migrations
```bash
# Migrations tự động chạy khi app start
# Check logs để verify
```

### 6. Monitor
- Railway: Dashboard → Metrics
- Render: Dashboard → Logs
- Fly.io: `fly logs`

---

## 🐛 Troubleshooting

### Issue: "Out of memory"
**Solution**: Tăng memory hoặc optimize JVM:
```dockerfile
# Dockerfile
ENV JAVA_OPTS="-Xmx256m -Xms128m"
```

### Issue: "Database connection failed"
**Solution**: Check environment variables:
```bash
railway variables
# hoặc
fly secrets list
```

### Issue: "Cold start too slow" (Render)
**Solution**: 
- Upgrade to paid plan ($7/month, no sleep)
- Hoặc dùng Railway/Fly.io

### Issue: "PostGIS extension not found"
**Solution**: Connect to database và run:
```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

---

## 📚 Related Documentation

- [Railway Documentation](https://docs.railway.app/)
- [Render Documentation](https://render.com/docs)
- [Fly.io Documentation](https://fly.io/docs/)
- [AWS Deployment Guide](../aws/AWS_DEPLOY_GUIDE.md)

---

**Last Updated**: January 2025  
**Recommended**: Railway.app for best balance of free + quality
