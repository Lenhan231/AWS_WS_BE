# CORS Configuration Guide

> **Cross-Origin Resource Sharing (CORS)** - Cho phép frontend từ domain khác call API backend

---

## 🎯 Vấn đề CORS là gì?

Khi frontend và backend chạy trên **domain khác nhau**, browser sẽ **chặn** request vì lý do bảo mật:

```
Frontend:  https://aws-ws-fe.vercel.app
Backend:   http://localhost:8080

❌ Browser blocks: "CORS policy: No 'Access-Control-Allow-Origin' header"
```

## ✅ Giải pháp: CORS Configuration

### 1. Backend Configuration (Spring Boot)

**File: `src/main/java/com/easybody/config/CorsConfig.java`**

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // ✅ Allowed Origins - Frontend URLs
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",           // Next.js dev
            "http://localhost:3001",           // Alternative port
            "https://aws-ws-fe.vercel.app"    // Production
        ));
        
        // ✅ Allowed Methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // ✅ Allowed Headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept"
        ));
        
        // ✅ Allow Credentials (for JWT tokens)
        configuration.setAllowCredentials(true);
        
        return source;
    }
}
```

**File: `src/main/java/com/easybody/config/SecurityConfig.java`**

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> {})  // ✅ Enable CORS
        // ...
        .build();
}
```

### 2. Environment Configuration

**File: `application.yml`**

```yaml
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000,http://localhost:3001,https://aws-ws-fe.vercel.app}
```

**File: `.env` (for local development)**

```bash
# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001,https://aws-ws-fe.vercel.app
```

**File: `.env.production` (for AWS deployment)**

```bash
# CORS Configuration - Production only
CORS_ALLOWED_ORIGINS=https://aws-ws-fe.vercel.app,https://www.easybody.com
```

---

## 🧪 Testing CORS

### Test 1: Browser Console

```javascript
// Mở browser console (F12) trên frontend
fetch('http://localhost:8080/api/v1/gyms')
  .then(res => res.json())
  .then(data => console.log('✅ CORS works!', data))
  .catch(err => console.error('❌ CORS blocked:', err));
```

### Test 2: curl (không bị CORS vì không phải browser)

```bash
curl -X GET http://localhost:8080/api/v1/gyms \
  -H "Origin: https://aws-ws-fe.vercel.app" \
  -v
```

**Expected response headers:**
```
Access-Control-Allow-Origin: https://aws-ws-fe.vercel.app
Access-Control-Allow-Credentials: true
```

### Test 3: Preflight Request (OPTIONS)

```bash
curl -X OPTIONS http://localhost:8080/api/v1/gyms \
  -H "Origin: https://aws-ws-fe.vercel.app" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Authorization,Content-Type" \
  -v
```

---

## 🔒 Security Best Practices

### ❌ KHÔNG NÊN (Development only)

```java
// ❌ Cho phép tất cả origins - KHÔNG AN TOÀN!
configuration.setAllowedOrigins(Arrays.asList("*"));
```

### ✅ NÊN (Production)

```java
// ✅ Chỉ định rõ frontend URLs
configuration.setAllowedOrigins(Arrays.asList(
    "https://aws-ws-fe.vercel.app",
    "https://www.easybody.com"
));
```

### Environment-based Configuration

```yaml
# Development
cors:
  allowed-origins: http://localhost:3000,http://localhost:3001

# Staging
cors:
  allowed-origins: https://staging.easybody.com

# Production
cors:
  allowed-origins: https://www.easybody.com,https://app.easybody.com
```

---

## 🚀 Frontend Integration

### Next.js Configuration

**File: `next.config.js`**

```javascript
module.exports = {
  env: {
    NEXT_PUBLIC_API_BASE_URL: process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080',
  },
};
```

**File: `.env.local` (frontend)**

```bash
# Development
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# Production
NEXT_PUBLIC_API_BASE_URL=https://api.easybody.com
```

### API Client (Frontend)

**File: `lib/api.ts`**

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export async function apiCall(endpoint: string, options = {}) {
  const token = localStorage.getItem('token');
  
  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers,
    },
    credentials: 'include', // ✅ Important for CORS with credentials
  });

  if (!response.ok) {
    throw new Error(`API Error: ${response.status}`);
  }

  return response.json();
}
```

---

## 🐛 Troubleshooting

### Error: "CORS policy: No 'Access-Control-Allow-Origin' header"

**Cause**: Backend không trả về CORS headers

**Solution**:
1. Check `CorsConfig.java` exists
2. Check `SecurityConfig.java` has `.cors(cors -> {})`
3. Restart backend server

### Error: "CORS policy: The value of the 'Access-Control-Allow-Origin' header must not be the wildcard '*'"

**Cause**: Dùng `*` với `allowCredentials = true`

**Solution**: Chỉ định rõ frontend URLs thay vì `*`

### Error: "CORS policy: Response to preflight request doesn't pass"

**Cause**: OPTIONS request bị chặn bởi Spring Security

**Solution**: Add OPTIONS to permitAll:

```java
.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
```

---

## 📚 Related Documentation

- [Spring CORS Documentation](https://docs.spring.io/spring-framework/reference/web/webmvc-cors.html)
- [MDN CORS Guide](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS)
- [Frontend API Integration](../api/FRONTEND_API_INTEGRATION.md)

---

**Last Updated**: January 2025  
**Status**: ✅ Configured and tested
