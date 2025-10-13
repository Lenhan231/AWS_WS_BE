# Railway Deployment Guide

> Deploy EasyBody Backend lên Railway.app với Docker + PostgreSQL

---

## 🚀 Quick Start (5 phút)

### 1. Tạo tài khoản Railway

```bash
# Truy cập: https://railway.app
# Click "Login" → Sign in with GitHub
```

### 2. Deploy từ GitHub (Recommended)

#### Option A: Qua Web UI (Dễ nhất)

1. **New Project**
   - Click "New Project" trên Railway dashboard
   - Chọn "Deploy from GitHub repo"
   - Authorize Railway access GitHub
   - Chọn repo `AWS_WS_BE`

2. **Railway tự động:**
   - Detect `Dockerfile`
   - Build Docker image
   - Deploy service
   - Generate public URL

3. **Add PostgreSQL Database**
   - Click "New" → "Database" → "Add PostgreSQL"
   - Railway tự động:
     - Tạo PostgreSQL instance
     - Inject environment variables
     - Link với backend service

4. **Enable PostGIS Extension**
   ```bash
   # Click vào PostgreSQL service
   # Tab "Connect" → Copy connection command
   # Paste vào terminal local:
   
   psql postgresql://postgres:password@host:port/railway
   
   # Run trong psql:
   CREATE EXTENSION IF NOT EXISTS postgis;
   CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
   \q
   ```

5. **Configure Environment Variables**
   - Click vào backend service
   - Tab "Variables"
   - Add variables:
   
   ```bash
   SPRING_PROFILES_ACTIVE=staging
   JDBC_DATABASE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}?sslmode=require
   JDBC_DATABASE_USERNAME=${{Postgres.PGUSER}}
   JDBC_DATABASE_PASSWORD=${{Postgres.PGPASSWORD}}
   CORS_ALLOWED_ORIGINS=https://aws-ws-fe.vercel.app,http://localhost:3000
   AWS_ENABLED=false
   SHOW_SQL=false
   ```

6. **Redeploy**
   - Click "Deploy" → "Redeploy"
   - Hoặc push code mới lên GitHub (auto deploy)

7. **Get Public URL**
   - Tab "Settings" → "Networking"
   - Click "Generate Domain"
   - Copy URL: `https://easybody-api-production.up.railway.app`

#### Option B: Qua CLI (Nhanh hơn)

```bash
# 1. Install Railway CLI
npm i -g @railway/cli

# 2. Login
railway login

# 3. Initialize project (trong thư mục AWS_WS_BE)
railway init

# 4. Link to GitHub repo (optional)
railway link

# 5. Add PostgreSQL
railway add --database postgres

# 6. Enable PostGIS
railway connect postgres
# Trong psql:
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
\q

# 7. Set environment variables
railway variables set SPRING_PROFILES_ACTIVE=staging
railway variables set CORS_ALLOWED_ORIGINS=https://aws-ws-fe.vercel.app
railway variables set AWS_ENABLED=false
railway variables set JDBC_DATABASE_URL="$(railway variables get DATABASE_URL)?sslmode=require"
railway variables set JDBC_DATABASE_USERNAME="$(railway variables get PGUSER)"
railway variables set JDBC_DATABASE_PASSWORD="$(railway variables get PGPASSWORD)"

# 8. Deploy
railway up

# 9. Open in browser
railway open
```

---

## 🔧 Configuration Details

### Environment Variables

Railway tự động inject PostgreSQL variables:

```bash
# Auto-injected by Railway
DATABASE_URL=postgresql://user:pass@host:port/db
PGHOST=containers-us-west-xxx.railway.app
PGPORT=5432
PGDATABASE=railway
PGUSER=postgres
PGPASSWORD=xxx

# Manual config
SPRING_PROFILES_ACTIVE=staging
JDBC_DATABASE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}?sslmode=require
JDBC_DATABASE_USERNAME=${{Postgres.PGUSER}}
JDBC_DATABASE_PASSWORD=${{Postgres.PGPASSWORD}}
CORS_ALLOWED_ORIGINS=https://aws-ws-fe.vercel.app
AWS_ENABLED=false
```

### Dockerfile Configuration

Railway sử dụng `railway.json`:

```json
{
  "build": {
    "builder": "DOCKERFILE",
    "dockerfilePath": "Dockerfile"
  },
  "deploy": {
    "startCommand": "java -jar /app/app.jar",
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 10
  }
}
```

---

## 📊 Monitoring & Logs

### View Logs

**Web UI:**
- Click vào service
- Tab "Deployments" → Click deployment
- Tab "Logs" → Real-time logs

**CLI:**
```bash
railway logs
```

### Metrics

**Web UI:**
- Tab "Metrics"
- View: CPU, Memory, Network usage

### Health Check

```bash
# Check actuator health
curl https://your-app.up.railway.app/actuator/health

# Check API
curl https://your-app.up.railway.app/api/v1/gyms
```

---

## 🔄 CI/CD Auto Deploy

Railway tự động deploy khi:
1. Push code lên GitHub (branch `main` hoặc `master`)
2. Merge Pull Request
3. Manual trigger từ dashboard

**Disable auto deploy:**
- Settings → "Deployments" → Uncheck "Auto Deploy"

---

## 💰 Pricing & Usage

### Free Tier ($5 credit/month)

```
Backend Service: ~$3/month
PostgreSQL: ~$2/month
Total: ~$5/month (FREE trong credit)
```

### Monitor Usage

- Dashboard → "Usage"
- View: Credit used, remaining

### Upgrade (nếu cần)

- Settings → "Plan" → "Upgrade to Pro"
- $5/month cho unlimited usage

---

## 🐛 Troubleshooting

### Issue: Build failed

**Check logs:**
```bash
railway logs --deployment
```

**Common causes:**
- Dockerfile syntax error → Check `Dockerfile`
- Missing dependencies → Check `build.gradle`
- Out of memory → Increase memory limit

### Issue: Database connection failed

**Check variables:**
```bash
railway variables
```

**Verify PostgreSQL:**
```bash
railway connect postgres
\l  # List databases
\q
```

### Issue: PostGIS not found

**Solution:**
```bash
railway connect postgres
CREATE EXTENSION IF NOT EXISTS postgis;
\dx  # List extensions
```

### Issue: App crashes on startup

**Check logs:**
```bash
railway logs
```

**Common causes:**
- Missing environment variables
- Database not ready (wait 30s)
- Flyway migration failed

### Issue: CORS errors

**Update CORS config:**
```yaml
# application.yml
cors:
  allowed-origins: https://aws-ws-fe.vercel.app,https://your-app.up.railway.app
```

**Redeploy:**
```bash
git push
# or
railway up
```

---

## 🔐 Security Best Practices

### 1. Environment Variables

❌ **KHÔNG** commit sensitive data:
```bash
# .gitignore
.env
.env.local
.env.production
```

✅ **SỬ DỤNG** Railway variables:
```bash
railway variables set DB_PASSWORD=xxx
```

### 2. CORS Configuration

❌ **KHÔNG** allow all origins:
```yaml
cors:
  allowed-origins: "*"  # DANGEROUS!
```

✅ **CHỈ ĐỊNH** frontend URLs:
```yaml
cors:
  allowed-origins: https://aws-ws-fe.vercel.app
```

### 3. Database Access

❌ **KHÔNG** expose database publicly

✅ **SỬ DỤNG** Railway private network:
- Database chỉ accessible từ Railway services
- Không có public IP

---

## 📚 Next Steps

### 1. Update Frontend

```bash
# Frontend .env.production
NEXT_PUBLIC_API_BASE_URL=https://your-app.up.railway.app
```

### 2. Test API

```bash
# Health check
curl https://your-app.up.railway.app/actuator/health

# Get gyms
curl https://your-app.up.railway.app/api/v1/gyms

# With auth
curl https://your-app.up.railway.app/api/v1/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Setup Custom Domain (Optional)

- Settings → "Networking" → "Custom Domain"
- Add: `api.easybody.com`
- Update DNS: CNAME → Railway domain

### 4. Enable Monitoring

- Settings → "Observability"
- Connect: Sentry, Datadog, etc.

---

## 🎉 Success Checklist

- [ ] Railway account created
- [ ] Project deployed from GitHub
- [ ] PostgreSQL database added
- [ ] PostGIS extension enabled
- [ ] Environment variables configured
- [ ] Public URL generated
- [ ] Health check passes
- [ ] API endpoints working
- [ ] Frontend updated with new URL
- [ ] CORS configured correctly

---

## 📞 Support

- **Railway Docs**: https://docs.railway.app
- **Railway Discord**: https://discord.gg/railway
- **Railway Status**: https://status.railway.app

---

**Last Updated**: January 2025  
**Deployment Time**: ~5 minutes  
**Cost**: FREE ($5 credit/month)
