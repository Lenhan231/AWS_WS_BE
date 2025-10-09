# Easy Body API - Frontend Integration Guide

**Version:** 1.0  
**Last Updated:** October 5, 2025  
**Backend Tech:** Spring Boot 3 + Java 21 + PostgreSQL + PostGIS

---

## 📋 Mục Lục

1. [Thông Tin Chung](#thông-tin-chung)
2. [Authentication Flow](#authentication-flow)
3. [API Endpoints](#api-endpoints)
4. [Request/Response Format](#requestresponse-format)
5. [Error Handling](#error-handling)
6. [Image Upload Flow](#image-upload-flow)
7. [Geo-Location Search](#geo-location-search)
8. [Pagination](#pagination)
9. [Best Practices](#best-practices)
10. [Example Code](#example-code)

---

## 🌐 Thông Tin Chung

### Base URLs

```javascript
// Development
const API_BASE_URL = 'http://localhost:8080/api/v1';

// Staging
const API_BASE_URL = 'https://staging-api.easybody.com/api/v1';

// Production
const API_BASE_URL = 'https://api.easybody.com/api/v1';
```

### Request Headers

```javascript
// Authenticated requests
const headers = {
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${JWT_TOKEN}`,
  'Accept': 'application/json'
};

// Public requests (no auth)
const headers = {
  'Content-Type': 'application/json',
  'Accept': 'application/json'
};
```

---

## 🔐 Authentication Flow

### 1. User Registration (với AWS Cognito)

**Flow:**
```
User → Frontend → AWS Cognito → Get JWT Token → Backend Registration
```

**Step-by-step:**

#### Step 1: Đăng ký với Cognito (Frontend xử lý)
```javascript
// Sử dụng AWS Amplify hoặc Cognito SDK
import { CognitoUserPool, CognitoUserAttribute } from 'amazon-cognito-identity-js';

const poolData = {
  UserPoolId: 'us-east-1_XXXXXXXXX',
  ClientId: 'xxxxxxxxxxxxxxxxxxxxxxxxxx'
};

const userPool = new CognitoUserPool(poolData);

// Đăng ký user mới
function registerUser(email, password, firstName, lastName, role) {
  const attributeList = [
    new CognitoUserAttribute({ Name: 'email', Value: email }),
    new CognitoUserAttribute({ Name: 'given_name', Value: firstName }),
    new CognitoUserAttribute({ Name: 'family_name', Value: lastName }),
    new CognitoUserAttribute({ Name: 'custom:role', Value: role })
  ];

  userPool.signUp(email, password, attributeList, null, (err, result) => {
    if (err) {
      console.error(err);
      return;
    }
    const cognitoUser = result.user;
    console.log('User registered:', cognitoUser.getUsername());
    // Proceed to verification
  });
}
```

#### Step 2: Xác thực email (Cognito gửi mã OTP)
```javascript
function verifyEmail(username, code) {
  const userData = {
    Username: username,
    Pool: userPool
  };
  
  const cognitoUser = new CognitoUser(userData);
  
  cognitoUser.confirmRegistration(code, true, (err, result) => {
    if (err) {
      console.error(err);
      return;
    }
    console.log('Email verified:', result);
    // User can now login
  });
}
```

#### Step 3: Login và lấy JWT token
```javascript
import { AuthenticationDetails, CognitoUser } from 'amazon-cognito-identity-js';

function loginUser(email, password) {
  const authenticationData = {
    Username: email,
    Password: password,
  };
  
  const authenticationDetails = new AuthenticationDetails(authenticationData);
  
  const userData = {
    Username: email,
    Pool: userPool
  };
  
  const cognitoUser = new CognitoUser(userData);
  
  cognitoUser.authenticateUser(authenticationDetails, {
    onSuccess: (result) => {
      const accessToken = result.getAccessToken().getJwtToken();
      const idToken = result.getIdToken().getJwtToken();
      const refreshToken = result.getRefreshToken().getToken();
      
      // Store tokens
      localStorage.setItem('accessToken', accessToken);
      localStorage.setItem('idToken', idToken);
      localStorage.setItem('refreshToken', refreshToken);
      
      // Register user in backend
      registerUserInBackend(accessToken);
    },
    onFailure: (err) => {
      console.error('Login failed:', err);
    }
  });
}
```

#### Step 4: Đăng ký user trong Backend Database
```javascript
async function registerUserInBackend(jwtToken) {
  const response = await fetch(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({
      email: 'user@example.com',
      firstName: 'John',
      lastName: 'Doe',
      phoneNumber: '+1234567890',
      role: 'CLIENT_USER', // ADMIN, GYM_STAFF, PT_USER, CLIENT_USER
      profileImageUrl: 'https://...' // optional
    })
  });
  
  const user = await response.json();
  console.log('User registered in backend:', user);
  return user;
}
```

### 2. Get Current User Profile

```javascript
async function getCurrentUser(jwtToken) {
  const response = await fetch(`${API_BASE_URL}/auth/me`, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
  
  const user = await response.json();
  return user;
}
```

**Response:**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "role": "CLIENT_USER",
  "active": true,
  "profileImageUrl": "https://...",
  "createdAt": "2025-10-05T10:00:00",
  "updatedAt": "2025-10-05T10:00:00"
}
```

### 3. Refresh Token

```javascript
function refreshAccessToken(refreshToken) {
  const cognitoUser = userPool.getCurrentUser();
  
  if (cognitoUser) {
    cognitoUser.refreshSession(refreshToken, (err, session) => {
      if (err) {
        console.error('Refresh failed:', err);
        // Redirect to login
        return;
      }
      
      const newAccessToken = session.getAccessToken().getJwtToken();
      localStorage.setItem('accessToken', newAccessToken);
    });
  }
}
```

### 4. Logout

```javascript
function logout() {
  const cognitoUser = userPool.getCurrentUser();
  if (cognitoUser) {
    cognitoUser.signOut();
  }
  
  // Clear local storage
  localStorage.removeItem('accessToken');
  localStorage.removeItem('idToken');
  localStorage.removeItem('refreshToken');
  
  // Redirect to login page
  window.location.href = '/login';
}
```

---

## 📡 API Endpoints

### Authentication

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/auth/register` | ✅ (JWT from Cognito) | Register user in backend after Cognito signup |
| GET | `/auth/me` | ✅ | Get current logged-in user profile |

---

### Gym Management

#### 1. Register Gym
```javascript
// POST /gyms
// Role: GYM_STAFF, ADMIN
async function registerGym(jwtToken, gymData) {
  const response = await fetch(`${API_BASE_URL}/gyms`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({
      name: 'FitZone Gym',
      description: 'Premium fitness facility',
      logoUrl: 'https://s3.../logo.jpg',
      address: '123 Main St',
      city: 'New York',
      state: 'NY',
      country: 'USA',
      postalCode: '10001',
      phoneNumber: '+1234567890',
      email: 'info@fitzone.com',
      website: 'https://fitzone.com',
      latitude: 40.7128,
      longitude: -74.0060
    })
  });
  
  return await response.json();
}
```

#### 2. Update Gym
```javascript
// PUT /gyms/{gymId}
// Role: GYM_STAFF, ADMIN
async function updateGym(jwtToken, gymId, gymData) {
  const response = await fetch(`${API_BASE_URL}/gyms/${gymId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify(gymData)
  });
  
  return await response.json();
}
```

#### 3. Get Gym Details
```javascript
// GET /gyms/{gymId}
// Public endpoint
async function getGymDetails(gymId) {
  const response = await fetch(`${API_BASE_URL}/gyms/${gymId}`);
  return await response.json();
}
```

#### 4. Search Gyms
```javascript
// GET /gyms/search
// Public endpoint

// Search by text
async function searchGymsByText(query) {
  const response = await fetch(
    `${API_BASE_URL}/gyms/search?query=${encodeURIComponent(query)}`
  );
  return await response.json();
}

// Search by location
async function searchGymsByLocation(lat, lng, radiusKm = 10) {
  const response = await fetch(
    `${API_BASE_URL}/gyms/search?latitude=${lat}&longitude=${lng}&radiusKm=${radiusKm}`
  );
  return await response.json();
}
```

#### 5. Assign PT to Gym
```javascript
// POST /gyms/{gymId}/assign-pt?ptUserId={ptUserId}
// Role: GYM_STAFF, ADMIN
async function assignPTToGym(jwtToken, gymId, ptUserId) {
  const response = await fetch(
    `${API_BASE_URL}/gyms/${gymId}/assign-pt?ptUserId=${ptUserId}`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${jwtToken}`
      }
    }
  );
  return await response.json();
}
```

---

### Personal Trainer Management

#### 1. Create PT Profile
```javascript
// POST /pt-users
// Role: PT_USER
async function createPTProfile(jwtToken, ptData) {
  const response = await fetch(`${API_BASE_URL}/pt-users`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({
      bio: 'Certified personal trainer with 5+ years experience',
      specializations: 'Strength Training, Weight Loss, Nutrition',
      certifications: 'NASM-CPT, ACE',
      yearsOfExperience: 5,
      profileImageUrl: 'https://s3.../profile.jpg',
      latitude: 40.7128,
      longitude: -74.0060
    })
  });
  
  return await response.json();
}
```

#### 2. Get PT Details
```javascript
// GET /pt-users/{ptUserId}
// Public endpoint
async function getPTDetails(ptUserId) {
  const response = await fetch(`${API_BASE_URL}/pt-users/${ptUserId}`);
  return await response.json();
}
```

#### 3. Search PTs by Location
```javascript
// GET /pt-users?latitude={lat}&longitude={lng}&radiusKm={radius}
// Public endpoint
async function searchPTsByLocation(lat, lng, radiusKm = 10) {
  const response = await fetch(
    `${API_BASE_URL}/pt-users?latitude=${lat}&longitude=${lng}&radiusKm=${radiusKm}`
  );
  return await response.json();
}
```

---

### Offer Management

#### 1. Create Offer
```javascript
// POST /offers
// Role: GYM_STAFF, PT_USER, ADMIN
async function createOffer(jwtToken, offerData) {
  const response = await fetch(`${API_BASE_URL}/offers`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({
      title: 'Monthly Membership',
      description: 'Full gym access with all amenities',
      offerType: 'GYM_OFFER', // or 'PT_OFFER'
      gymId: 1, // Required if GYM_OFFER
      ptUserId: null, // Required if PT_OFFER
      price: 49.99,
      currency: 'USD',
      durationDescription: '1 Month',
      imageUrls: 'https://s3.../img1.jpg,https://s3.../img2.jpg'
    })
  });
  
  return await response.json();
}
```

#### 2. Update Offer
```javascript
// PUT /offers/{offerId}
// Role: GYM_STAFF, PT_USER, ADMIN
async function updateOffer(jwtToken, offerId, offerData) {
  const response = await fetch(`${API_BASE_URL}/offers/${offerId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify(offerData)
  });
  
  return await response.json();
}
```

#### 3. Get Offer Details
```javascript
// GET /offers/{offerId}
// Public endpoint
async function getOfferDetails(offerId) {
  const response = await fetch(`${API_BASE_URL}/offers/${offerId}`);
  return await response.json();
}
```

---

### Search Offers (Most Important!)

#### Advanced Search with Filters
```javascript
// POST /search/offers
// Public endpoint
async function searchOffers(filters) {
  const response = await fetch(`${API_BASE_URL}/search/offers`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      // Location filters
      latitude: 40.7128,
      longitude: -74.0060,
      radiusKm: 10,
      
      // Price filters
      minPrice: 0,
      maxPrice: 100,
      
      // Type filter
      offerType: 'GYM_OFFER', // or 'PT_OFFER' or null for both
      
      // Rating filter
      minRating: 4.0,
      
      // Text search
      searchQuery: 'yoga',
      
      // Specific gym/PT
      gymId: null,
      ptUserId: null,
      
      // Pagination
      page: 0,
      size: 20,
      sortBy: 'averageRating', // or 'price', 'createdAt'
      sortDirection: 'DESC' // or 'ASC'
    })
  });
  
  return await response.json();
}
```

**Response Format:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Monthly Membership",
      "description": "Full gym access...",
      "offerType": "GYM_OFFER",
      "gymName": "FitZone Gym",
      "ptUserName": null,
      "price": 49.99,
      "currency": "USD",
      "durationDescription": "1 Month",
      "imageUrls": "https://...",
      "averageRating": 4.5,
      "ratingCount": 120,
      "distanceKm": 2.5,
      "location": {
        "id": 1,
        "latitude": 40.7128,
        "longitude": -74.0060,
        "formattedAddress": "123 Main St, New York, NY"
      },
      "createdAt": "2025-10-01T10:00:00"
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true
}
```

#### Simple GET Search
```javascript
// GET /search/offers?params
// Public endpoint
async function searchOffersSimple(params) {
  const queryParams = new URLSearchParams({
    latitude: params.lat,
    longitude: params.lng,
    radiusKm: params.radius || 10,
    minPrice: params.minPrice || '',
    maxPrice: params.maxPrice || '',
    minRating: params.minRating || '',
    searchQuery: params.query || '',
    page: params.page || 0,
    size: params.size || 20
  });
  
  const response = await fetch(`${API_BASE_URL}/search/offers?${queryParams}`);
  return await response.json();
}
```

---

### Rating System

#### 1. Submit Rating
```javascript
// POST /ratings
// Role: CLIENT_USER
async function submitRating(jwtToken, ratingData) {
  const response = await fetch(`${API_BASE_URL}/ratings`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({
      offerId: 1,
      rating: 5, // 1-5
      comment: 'Excellent gym with great equipment!'
    })
  });
  
  return await response.json();
}
```

#### 2. Get Offer Ratings
```javascript
// GET /ratings/offer/{offerId}?page=0&size=20
// Public endpoint
async function getOfferRatings(offerId, page = 0, size = 20) {
  const response = await fetch(
    `${API_BASE_URL}/ratings/offer/${offerId}?page=${page}&size=${size}`
  );
  return await response.json();
}
```

---

### Report System

#### Submit Report
```javascript
// POST /reports
// Authenticated users only
async function submitReport(jwtToken, reportData) {
  const response = await fetch(`${API_BASE_URL}/reports`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${jwtToken}`
    },
    body: JSON.stringify({
      offerId: 1, // optional
      reportedUserId: 5, // optional
      reason: 'Inappropriate content',
      details: 'Contains misleading information'
    })
  });
  
  return await response.json();
}
```

---

### Media Upload (S3 Pre-signed URLs)

#### Flow:
```
1. Frontend requests pre-signed URL from backend
2. Backend generates S3 pre-signed URL
3. Frontend uploads file directly to S3
4. Frontend uses public URL in form submissions
```

#### Get Pre-signed Upload URL
```javascript
// GET /media/presigned-url?folder=offers&fileExtension=jpg
// Authenticated users only
async function getPresignedUploadUrl(jwtToken, folder, fileExtension) {
  // folder: 'profiles', 'offers', 'gyms'
  // fileExtension: 'jpg', 'jpeg', 'png', 'gif', 'webp'
  
  const response = await fetch(
    `${API_BASE_URL}/media/presigned-url?folder=${folder}&fileExtension=${fileExtension}`,
    {
      headers: {
        'Authorization': `Bearer ${jwtToken}`
      }
    }
  );
  
  return await response.json();
}
```

**Response:**
```json
{
  "uploadUrl": "https://easybody-media.s3.amazonaws.com/offers/uuid.jpg?X-Amz-Algorithm=...",
  "fileKey": "offers/uuid.jpg",
  "publicUrl": "https://easybody-media.s3.amazonaws.com/offers/uuid.jpg",
  "expiresIn": 3600
}
```

#### Upload Image to S3
```javascript
async function uploadImage(jwtToken, file, folder) {
  // Step 1: Get pre-signed URL
  const fileExtension = file.name.split('.').pop();
  const presignedData = await getPresignedUploadUrl(jwtToken, folder, fileExtension);
  
  // Step 2: Upload to S3
  const uploadResponse = await fetch(presignedData.uploadUrl, {
    method: 'PUT',
    body: file,
    headers: {
      'Content-Type': file.type
    }
  });
  
  if (!uploadResponse.ok) {
    throw new Error('Upload failed');
  }
  
  // Step 3: Return public URL to use in forms
  return presignedData.publicUrl;
}

// Usage in React
async function handleImageUpload(e) {
  const file = e.target.files[0];
  const jwtToken = localStorage.getItem('accessToken');
  
  try {
    const imageUrl = await uploadImage(jwtToken, file, 'offers');
    console.log('Image uploaded:', imageUrl);
    // Use imageUrl in your form
  } catch (error) {
    console.error('Upload error:', error);
  }
}
```

---

## ⚠️ Error Handling

### Error Response Format
```json
{
  "message": "Resource not found",
  "error": "Not Found",
  "status": 404,
  "path": "/api/v1/offers/999",
  "timestamp": 1699123456789
}
```

### Validation Errors
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

### HTTP Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (invalid/expired token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `500` - Internal Server Error

### Error Handling Example
```javascript
async function apiCall() {
  try {
    const response = await fetch(`${API_BASE_URL}/offers/1`);
    
    if (!response.ok) {
      const error = await response.json();
      
      switch (response.status) {
        case 401:
          // Token expired - refresh or redirect to login
          refreshAccessToken();
          break;
        case 403:
          // No permission
          alert('You do not have permission to perform this action');
          break;
        case 404:
          // Not found
          alert('Resource not found');
          break;
        case 400:
          // Validation errors
          if (error.errors) {
            Object.keys(error.errors).forEach(field => {
              console.error(`${field}: ${error.errors[field]}`);
            });
          }
          break;
        default:
          // Server error
          alert('Something went wrong. Please try again.');
      }
      
      throw new Error(error.message);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API Error:', error);
    throw error;
  }
}
```

---

## 🗺️ Geo-Location Features

### Getting User's Location
```javascript
function getUserLocation() {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error('Geolocation not supported'));
      return;
    }
    
    navigator.geolocation.getCurrentPosition(
      (position) => {
        resolve({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude
        });
      },
      (error) => {
        reject(error);
      }
    );
  });
}

// Usage
async function searchNearbyOffers() {
  try {
    const location = await getUserLocation();
    const offers = await searchOffers({
      latitude: location.latitude,
      longitude: location.longitude,
      radiusKm: 10
    });
    console.log('Nearby offers:', offers);
  } catch (error) {
    console.error('Location error:', error);
  }
}
```

### Distance Calculation (for display)
```javascript
function calculateDistance(lat1, lon1, lat2, lon2) {
  const R = 6371; // Earth's radius in km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon/2) * Math.sin(dLon/2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  const distance = R * c;
  return distance.toFixed(2); // km
}
```

---

## 📄 Pagination

### Pagination Parameters
```javascript
const paginationParams = {
  page: 0,      // Page number (0-based)
  size: 20,     // Items per page (max: 100)
  sortBy: 'createdAt',     // Sort field
  sortDirection: 'DESC'     // ASC or DESC
};
```

### Pagination Response
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8,
  "last": false,
  "first": true
}
```

### Pagination UI Helper
```javascript
function PaginationInfo({ page, totalPages, totalElements }) {
  const start = page * 20 + 1;
  const end = Math.min((page + 1) * 20, totalElements);
  
  return (
    <div>
      Showing {start}-{end} of {totalElements} results
      <button disabled={page === 0} onClick={() => goToPage(page - 1)}>
        Previous
      </button>
      <span>Page {page + 1} of {totalPages}</span>
      <button disabled={page === totalPages - 1} onClick={() => goToPage(page + 1)}>
        Next
      </button>
    </div>
  );
}
```

---

## 💡 Best Practices

### 1. Token Management
```javascript
// Store tokens securely
class AuthService {
  static setTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
  }
  
  static getAccessToken() {
    return localStorage.getItem('accessToken');
  }
  
  static clearTokens() {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }
  
  static isAuthenticated() {
    return !!this.getAccessToken();
  }
}
```

### 2. API Service Class
```javascript
class ApiService {
  static baseUrl = API_BASE_URL;
  
  static async request(endpoint, options = {}) {
    const token = AuthService.getAccessToken();
    
    const config = {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...options.headers
      }
    };
    
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    
    try {
      const response = await fetch(`${this.baseUrl}${endpoint}`, config);
      
      if (response.status === 401) {
        // Token expired
        AuthService.clearTokens();
        window.location.href = '/login';
        return;
      }
      
      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'API Error');
      }
      
      return await response.json();
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }
  
  static get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  }
  
  static post(endpoint, data) {
    return this.request(endpoint, {
      method: 'POST',
      body: JSON.stringify(data)
    });
  }
  
  static put(endpoint, data) {
    return this.request(endpoint, {
      method: 'PUT',
      body: JSON.stringify(data)
    });
  }
  
  static delete(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  }
}

// Usage
const offers = await ApiService.get('/search/offers?page=0&size=20');
const newOffer = await ApiService.post('/offers', offerData);
```

### 3. Debounce Search Input
```javascript
import { useState, useEffect } from 'react';

function useDebounce(value, delay) {
  const [debouncedValue, setDebouncedValue] = useState(value);
  
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);
    
    return () => {
      clearTimeout(handler);
    };
  }, [value, delay]);
  
  return debouncedValue;
}

// Usage in search component
function SearchComponent() {
  const [searchQuery, setSearchQuery] = useState('');
  const debouncedSearchQuery = useDebounce(searchQuery, 500);
  
  useEffect(() => {
    if (debouncedSearchQuery) {
      searchOffers({ searchQuery: debouncedSearchQuery });
    }
  }, [debouncedSearchQuery]);
  
  return (
    <input
      type="text"
      value={searchQuery}
      onChange={(e) => setSearchQuery(e.target.value)}
      placeholder="Search offers..."
    />
  );
}
```

### 4. Loading States
```javascript
function OfferList() {
  const [offers, setOffers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  useEffect(() => {
    async function fetchOffers() {
      try {
        setLoading(true);
        const data = await ApiService.get('/search/offers');
        setOffers(data.content);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }
    
    fetchOffers();
  }, []);
  
  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;
  
  return (
    <div>
      {offers.map(offer => (
        <OfferCard key={offer.id} offer={offer} />
      ))}
    </div>
  );
}
```

---

## 📚 Complete Example: Offer Search Component

```javascript
import React, { useState, useEffect } from 'react';
import ApiService from './services/ApiService';
import { getUserLocation } from './utils/location';

function OfferSearchPage() {
  const [filters, setFilters] = useState({
    latitude: null,
    longitude: null,
    radiusKm: 10,
    minPrice: '',
    maxPrice: '',
    offerType: '',
    minRating: '',
    searchQuery: '',
    page: 0,
    size: 20,
    sortBy: 'createdAt',
    sortDirection: 'DESC'
  });
  
  const [offers, setOffers] = useState([]);
  const [pagination, setPagination] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  
  // Get user location on mount
  useEffect(() => {
    async function getLocation() {
      try {
        const location = await getUserLocation();
        setFilters(prev => ({
          ...prev,
          latitude: location.latitude,
          longitude: location.longitude
        }));
      } catch (err) {
        console.error('Could not get location:', err);
      }
    }
    
    getLocation();
  }, []);
  
  // Search offers when filters change
  useEffect(() => {
    searchOffers();
  }, [filters]);
  
  async function searchOffers() {
    try {
      setLoading(true);
      setError(null);
      
      const response = await ApiService.post('/search/offers', filters);
      
      setOffers(response.content);
      setPagination({
        pageNumber: response.pageNumber,
        pageSize: response.pageSize,
        totalElements: response.totalElements,
        totalPages: response.totalPages,
        last: response.last,
        first: response.first
      });
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }
  
  function handleFilterChange(field, value) {
    setFilters(prev => ({
      ...prev,
      [field]: value,
      page: 0 // Reset to first page
    }));
  }
  
  function handlePageChange(newPage) {
    setFilters(prev => ({
      ...prev,
      page: newPage
    }));
  }
  
  return (
    <div className="offer-search-page">
      <h1>Search Offers</h1>
      
      {/* Filters */}
      <div className="filters">
        <input
          type="text"
          placeholder="Search..."
          value={filters.searchQuery}
          onChange={(e) => handleFilterChange('searchQuery', e.target.value)}
        />
        
        <select
          value={filters.offerType}
          onChange={(e) => handleFilterChange('offerType', e.target.value)}
        >
          <option value="">All Types</option>
          <option value="GYM_OFFER">Gym Offers</option>
          <option value="PT_OFFER">PT Offers</option>
        </select>
        
        <input
          type="number"
          placeholder="Min Price"
          value={filters.minPrice}
          onChange={(e) => handleFilterChange('minPrice', e.target.value)}
        />
        
        <input
          type="number"
          placeholder="Max Price"
          value={filters.maxPrice}
          onChange={(e) => handleFilterChange('maxPrice', e.target.value)}
        />
        
        <select
          value={filters.minRating}
          onChange={(e) => handleFilterChange('minRating', e.target.value)}
        >
          <option value="">Any Rating</option>
          <option value="4">4+ Stars</option>
          <option value="4.5">4.5+ Stars</option>
        </select>
        
        <select
          value={filters.radiusKm}
          onChange={(e) => handleFilterChange('radiusKm', e.target.value)}
        >
          <option value="5">Within 5 km</option>
          <option value="10">Within 10 km</option>
          <option value="25">Within 25 km</option>
          <option value="50">Within 50 km</option>
        </select>
      </div>
      
      {/* Loading State */}
      {loading && <div className="loading">Loading offers...</div>}
      
      {/* Error State */}
      {error && <div className="error">Error: {error}</div>}
      
      {/* Offers List */}
      {!loading && !error && (
        <>
          <div className="offers-list">
            {offers.length === 0 ? (
              <p>No offers found</p>
            ) : (
              offers.map(offer => (
                <OfferCard key={offer.id} offer={offer} />
              ))
            )}
          </div>
          
          {/* Pagination */}
          {pagination && (
            <div className="pagination">
              <button
                disabled={pagination.first}
                onClick={() => handlePageChange(filters.page - 1)}
              >
                Previous
              </button>
              
              <span>
                Page {pagination.pageNumber + 1} of {pagination.totalPages}
                {' '}({pagination.totalElements} total)
              </span>
              
              <button
                disabled={pagination.last}
                onClick={() => handlePageChange(filters.page + 1)}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

function OfferCard({ offer }) {
  return (
    <div className="offer-card">
      <img src={offer.imageUrls?.split(',')[0]} alt={offer.title} />
      <h3>{offer.title}</h3>
      <p>{offer.description}</p>
      <p className="price">${offer.price} {offer.currency}</p>
      <p className="rating">⭐ {offer.averageRating} ({offer.ratingCount} reviews)</p>
      {offer.distanceKm && <p className="distance">📍 {offer.distanceKm} km away</p>}
      <button onClick={() => viewOfferDetails(offer.id)}>View Details</button>
    </div>
  );
}

export default OfferSearchPage;
```

---

## 🔗 Important Links

- **API Base URL (Dev):** `http://localhost:8080/api/v1`
- **API Base URL (Prod):** `https://api.easybody.com/api/v1`
- **AWS Cognito Console:** https://console.aws.amazon.com/cognito/
- **S3 Bucket:** `easybody-media`

---

## 📞 Support

For technical questions or issues:
- Backend Team: backend@easybody.com
- API Documentation: See `API_DOCUMENTATION.md`
- Postman Collection: (Will be provided)

---

**Last Updated:** October 5, 2025  
**Version:** 1.0  
**Author:** Backend Team

