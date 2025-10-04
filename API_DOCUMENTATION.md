# Easy Body API Documentation

## Authentication
All authenticated endpoints require JWT token in Authorization header:
```
Authorization: Bearer <JWT_TOKEN>
```

---

## 📋 API Endpoints Summary

### 🔐 Authentication Module
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/v1/auth/register` | ✅ | Any | Register user after Cognito signup |
| GET | `/api/v1/auth/me` | ✅ | Any | Get current user profile |

### 🏢 Gym Management
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/v1/gyms` | ✅ | GYM_STAFF, ADMIN | Register new gym |
| PUT | `/api/v1/gyms/{gymId}` | ✅ | GYM_STAFF, ADMIN | Update gym details |
| GET | `/api/v1/gyms/{gymId}` | ❌ | Public | Get gym by ID |
| GET | `/api/v1/gyms` | ❌ | Public | List all active gyms |
| GET | `/api/v1/gyms/search` | ❌ | Public | Search gyms by text or location |
| POST | `/api/v1/gyms/{gymId}/assign-pt` | ✅ | GYM_STAFF, ADMIN | Assign PT to gym |
| GET | `/api/v1/gyms/{gymId}/pt-associations` | ❌ | Public | Get gym's PT associations |
| PUT | `/api/v1/gyms/pt-associations/{id}/approve` | ✅ | GYM_STAFF, ADMIN | Approve PT-Gym association |
| PUT | `/api/v1/gyms/pt-associations/{id}/reject` | ✅ | GYM_STAFF, ADMIN | Reject PT-Gym association |

### 💪 Personal Trainer Management
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/v1/pt-users` | ✅ | PT_USER | Create PT profile |
| PUT | `/api/v1/pt-users/{ptUserId}` | ✅ | PT_USER, ADMIN | Update PT profile |
| GET | `/api/v1/pt-users/{ptUserId}` | ❌ | Public | Get PT details |
| GET | `/api/v1/pt-users` | ❌ | Public | List PTs (with location filter) |
| GET | `/api/v1/pt-users/{ptUserId}/gym-associations` | ❌ | Public | Get PT's gym associations |

### 🎯 Offer Management
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/v1/offers` | ✅ | GYM_STAFF, PT_USER, ADMIN | Create new offer |
| PUT | `/api/v1/offers/{offerId}` | ✅ | GYM_STAFF, PT_USER, ADMIN | Update offer |
| GET | `/api/v1/offers/{offerId}` | ❌ | Public | Get offer by ID |

### 🔍 Search Module
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/v1/search/offers` | ❌ | Public | Advanced offer search (JSON body) |
| GET | `/api/v1/search/offers` | ❌ | Public | Search offers (query params) |

### ⭐ Rating System
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/v1/ratings` | ✅ | CLIENT_USER | Submit rating for offer |
| GET | `/api/v1/ratings/offer/{offerId}` | ❌ | Public | Get ratings for offer (paginated) |

### 🚩 Report System
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| POST | `/api/v1/reports` | ✅ | Any | Submit report (offer or user) |

### 👨‍💼 Admin Moderation
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET | `/api/v1/admin/offers/pending` | ✅ | ADMIN | Get pending offers |
| PUT | `/api/v1/admin/offers/{offerId}/moderate` | ✅ | ADMIN | Approve/reject offer |
| GET | `/api/v1/admin/reports/pending` | ✅ | ADMIN | Get pending reports |
| GET | `/api/v1/admin/reports` | ✅ | ADMIN | Get reports by status |
| PUT | `/api/v1/admin/reports/{reportId}/resolve` | ✅ | ADMIN | Resolve report |
| PUT | `/api/v1/admin/reports/{reportId}/dismiss` | ✅ | ADMIN | Dismiss report |
| GET | `/api/v1/admin/pt-associations/pending` | ✅ | ADMIN | Get pending PT-Gym associations |

### 📸 Media Upload
| Method | Endpoint | Auth | Role | Description |
|--------|----------|------|------|-------------|
| GET | `/api/v1/media/presigned-url` | ✅ | Any | Get S3 pre-signed upload URL |

---

## 📝 Request/Response Examples

### 1. Register User
**POST** `/api/v1/auth/register`
```json
{
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "CLIENT_USER",
  "profileImageUrl": "https://..."
}
```

### 2. Register Gym
**POST** `/api/v1/gyms`
```json
{
  "name": "FitZone Gym",
  "description": "Premium fitness facility",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "country": "USA",
  "postalCode": "10001",
  "phoneNumber": "+1234567890",
  "email": "info@fitzone.com",
  "website": "https://fitzone.com",
  "latitude": 40.7128,
  "longitude": -74.0060
}
```

### 3. Create Offer
**POST** `/api/v1/offers`
```json
{
  "title": "Monthly Membership",
  "description": "Full gym access with all amenities",
  "offerType": "GYM_OFFER",
  "gymId": 1,
  "price": 49.99,
  "currency": "USD",
  "durationDescription": "1 Month",
  "imageUrls": "https://s3.../image1.jpg,https://s3.../image2.jpg"
}
```

### 4. Search Offers
**POST** `/api/v1/search/offers`
```json
{
  "latitude": 40.7128,
  "longitude": -74.0060,
  "radiusKm": 10,
  "minPrice": 0,
  "maxPrice": 100,
  "offerType": "GYM_OFFER",
  "minRating": 4.0,
  "searchQuery": "yoga",
  "page": 0,
  "size": 20,
  "sortBy": "averageRating",
  "sortDirection": "DESC"
}
```

**OR GET** `/api/v1/search/offers?latitude=40.7128&longitude=-74.0060&radiusKm=10&minPrice=0&maxPrice=100`

### 5. Submit Rating
**POST** `/api/v1/ratings`
```json
{
  "offerId": 1,
  "rating": 5,
  "comment": "Excellent gym with great equipment!"
}
```

### 6. Submit Report
**POST** `/api/v1/reports`
```json
{
  "offerId": 1,
  "reason": "Inappropriate content",
  "details": "Contains misleading information"
}
```

### 7. Moderate Offer (Admin)
**PUT** `/api/v1/admin/offers/{offerId}/moderate`
```json
{
  "decision": "approve"
}
```
or
```json
{
  "decision": "reject",
  "reason": "Violates content policy"
}
```

### 8. Get Pre-signed Upload URL
**GET** `/api/v1/media/presigned-url?folder=offers&fileExtension=jpg`

Response:
```json
{
  "uploadUrl": "https://s3.amazonaws.com/...",
  "fileKey": "offers/uuid.jpg",
  "publicUrl": "https://easybody-media.s3.amazonaws.com/offers/uuid.jpg",
  "expiresIn": 3600
}
```

---

## 🔍 Search Filters

### Offer Search Parameters
- `latitude` / `longitude` - Location coordinates
- `radiusKm` - Search radius in kilometers (default: 10, max: 100)
- `minPrice` / `maxPrice` - Price range filter
- `offerType` - GYM_OFFER or PT_OFFER
- `minRating` - Minimum average rating (0-5)
- `searchQuery` - Text search in title/description
- `gymId` - Filter by specific gym
- `ptUserId` - Filter by specific PT
- `page` - Page number (default: 0)
- `size` - Results per page (default: 20, max: 100)
- `sortBy` - Sort field (createdAt, price, averageRating)
- `sortDirection` - ASC or DESC

---

## 📊 Pagination Response Format

All paginated endpoints return:
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "first": true
}
```

---

## ⚠️ Error Response Format

```json
{
  "message": "Resource not found",
  "error": "Not Found",
  "status": 404,
  "path": "/api/v1/offers/999",
  "timestamp": 1699123456789
}
```

### Validation Error Response
```json
{
  "message": "Validation failed",
  "errors": {
    "email": "Email should be valid",
    "price": "Price must be greater than 0"
  },
  "status": 400,
  "path": "/api/v1/offers",
  "timestamp": 1699123456789
}
```

---

## 🎭 User Roles

- **ADMIN** - Full system access, moderation capabilities
- **GYM_STAFF** - Manage gyms, assign PTs, create gym offers
- **PT_USER** - Create PT profile, create PT offers
- **CLIENT_USER** - Search, rate offers, submit reports

---

## 🌍 PostGIS Geo-Location Queries

The system uses PostgreSQL PostGIS extension for spatial queries:

- Radius search using `ST_DWithin`
- Distance calculation using `ST_Distance`
- Coordinate system: EPSG:4326 (WGS 84)
- Distance measurements in meters converted to kilometers

Example: Find all gyms within 5km of location
```
GET /api/v1/gyms/search?latitude=40.7128&longitude=-74.0060&radiusKm=5
```

