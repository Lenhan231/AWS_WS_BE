# Connect Railway PostGIS via pgAdmin 4

| Setting | Value |
| --- | --- |
| Host | ballast.proxy.rlwy.net |
| Port | 40681 |
| Database | railway |
| User | postgres |
| Password | *(copy from `DATABASE_URL` / `DATABASE_PASSWORD` in Railway service variables)* |
| SSL Mode | require |
| Connection Timeout | 10 |

```text
DATABASE_URL=postgres://postgre:e2AE1DeeEedCbce4DB5cbcgEGeF66g5b@ballast.proxy.rlwy.net:40681/railway
```

## Optional SQL Checks

```sql
SELECT version();
SELECT PostGIS_Full_Version();
```

> ℹ️ Internal services running inside Railway (ví dụ `easybody-api`) nên dùng `DATABASE_PRIVATE_URL` / `JDBC_DATABASE_URL` để kết nối qua private network thay vì proxy.

---
