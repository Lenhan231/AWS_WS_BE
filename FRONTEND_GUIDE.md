      })
    });
    
    const { presignedUrl, publicUrl, key } = await presignedResponse.json();
    
    // Step 2: Upload file trực tiếp lên S3
    await fetch(presignedUrl, {
      method: 'PUT',
      headers: {
        'Content-Type': file.type
      },
      body: file
    });
    
    // Step 3: Xác nhận upload với backend
    await fetch('http://localhost:8080/api/v1/media/upload-complete', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ key })
    });
    
    // Step 4: Trả về publicUrl để lưu vào database
    return publicUrl;
    
  } catch (error) {
    console.error('Error uploading image:', error);
    throw error;
  }
};

// Sử dụng:
const handleFileUpload = async (event) => {
  const file = event.target.files[0];
  const imageUrl = await uploadImageToS3(file);
  
  // Bây giờ bạn có thể dùng imageUrl để tạo gym/offer
  await createGym({
    name: 'My Gym',
    logoUrl: imageUrl, // <-- URL từ S3
    // ...other fields
  });
};
```

---

## 🎨 React Component Examples

### **1. Login Component**

```jsx
import { useState } from 'react';
import { Auth } from 'aws-amplify';

const LoginForm = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      // Login via Cognito
      await Auth.signIn(email, password);
      
      // Get JWT token
      const session = await Auth.currentSession();
      const token = session.getIdToken().getJwtToken();
      
      // Call backend to get user info
      const response = await fetch('http://localhost:8080/api/v1/auth/me', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const user = await response.json();
      console.log('User:', user);
      
      // Redirect to dashboard
      window.location.href = '/dashboard';
      
    } catch (error) {
      setError(error.message);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input 
        type="email" 
        value={email} 
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
      />
      <input 
        type="password" 
        value={password} 
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Password"
      />
      <button type="submit">Login</button>
      {error && <p className="error">{error}</p>}
    </form>
  );
};
```

### **2. Gym List Component**

```jsx
import { useState, useEffect } from 'react';

const GymList = () => {
  const [gyms, setGyms] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchGyms();
  }, []);

  const fetchGyms = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/v1/gyms?page=0&size=20');
      const data = await response.json();
      setGyms(data.content);
    } catch (error) {
      console.error('Error fetching gyms:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="gym-list">
      {gyms.map(gym => (
        <div key={gym.id} className="gym-card">
          <img src={gym.logoUrl} alt={gym.name} />
          <h3>{gym.name}</h3>
          <p>{gym.description}</p>
          <div className="rating">
            ⭐ {gym.averageRating} ({gym.ratingCount} reviews)
          </div>
          <p>📍 {gym.location?.city}</p>
        </div>
      ))}
    </div>
  );
};
```

### **3. Create Offer Component**

```jsx
import { useState } from 'react';
import { Auth } from 'aws-amplify';

const CreateOfferForm = ({ gymId }) => {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    price: '',
    durationDescription: ''
  });

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      const session = await Auth.currentSession();
      const token = session.getIdToken().getJwtToken();
      
      const response = await fetch('http://localhost:8080/api/v1/offers', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          ...formData,
          offerType: 'GYM_OFFER',
          gymId: gymId,
          price: parseFloat(formData.price)
        })
      });
      
      const offer = await response.json();
      alert('Offer created successfully! Waiting for admin approval.');
      
    } catch (error) {
      console.error('Error creating offer:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <input
        type="text"
        placeholder="Offer Title"
        value={formData.title}
        onChange={(e) => setFormData({...formData, title: e.target.value})}
        required
      />
      <textarea
        placeholder="Description"
        value={formData.description}
        onChange={(e) => setFormData({...formData, description: e.target.value})}
      />
      <input
        type="number"
        placeholder="Price (USD)"
        value={formData.price}
        onChange={(e) => setFormData({...formData, price: e.target.value})}
        required
      />
      <input
        type="text"
        placeholder="Duration (e.g., 3 months)"
        value={formData.durationDescription}
        onChange={(e) => setFormData({...formData, durationDescription: e.target.value})}
      />
      <button type="submit">Create Offer</button>
    </form>
  );
};
```

---

## ⚠️ Error Handling

Tất cả API errors đều trả về format sau:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Gym not found",
  "timestamp": "2025-10-06T10:30:00.000Z",
  "path": "/api/v1/gyms/999"
}
```

### **Common Status Codes:**

- `200` - Success
- `201` - Created
- `204` - No Content (delete success)
- `400` - Bad Request (validation errors)
- `401` - Unauthorized (missing/invalid token)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `500` - Internal Server Error

### **Error Handling Example:**

```javascript
const apiCallWithErrorHandling = async (endpoint, options) => {
  try {
    const response = await fetch(`http://localhost:8080/api/v1${endpoint}`, options);
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'API Error');
    }
    
    return await response.json();
    
  } catch (error) {
    console.error('API Error:', error);
    // Show error to user
    alert(error.message);
    throw error;
  }
};
```

---

## 🌍 Pagination

Tất cả list APIs đều support pagination với format sau:

### **Request:**
```
GET /api/v1/gyms?page=0&size=20
```

### **Response:**
```json
{
  "content": [...],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

### **React Pagination Example:**

```jsx
const PaginatedList = () => {
  const [data, setData] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchData = async (pageNum) => {
    const response = await fetch(`http://localhost:8080/api/v1/gyms?page=${pageNum}&size=20`);
    const result = await response.json();
    setData(result.content);
    setTotalPages(result.totalPages);
  };

  useEffect(() => {
    fetchData(page);
  }, [page]);

  return (
    <div>
      {/* Render data */}
      {data.map(item => <div key={item.id}>{item.name}</div>)}
      
      {/* Pagination controls */}
      <div className="pagination">
        <button 
          onClick={() => setPage(p => p - 1)} 
          disabled={page === 0}
        >
          Previous
        </button>
        <span>Page {page + 1} of {totalPages}</span>
        <button 
          onClick={() => setPage(p => p + 1)} 
          disabled={page === totalPages - 1}
        >
          Next
        </button>
      </div>
    </div>
  );
};
```

---

## 🗺️ Geolocation Search

### **Lấy vị trí của user:**

```javascript
const getUserLocation = () => {
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
      (error) => reject(error)
    );
  });
};

// Sử dụng:
const searchNearbyGyms = async () => {
  try {
    const { latitude, longitude } = await getUserLocation();
    
    const response = await fetch(
      `http://localhost:8080/api/v1/gyms/search?latitude=${latitude}&longitude=${longitude}&radiusKm=10`
    );
    
    const gyms = await response.json();
    console.log('Nearby gyms:', gyms);
    
  } catch (error) {
    console.error('Error:', error);
  }
};
```

---

## 📱 Environment Variables cho Frontend

Tạo file `.env` trong React app:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api/v1
REACT_APP_AWS_REGION=us-east-1
REACT_APP_COGNITO_USER_POOL_ID=us-east-1_XXXXXXXXX
REACT_APP_COGNITO_CLIENT_ID=xxxxxxxxxxxxxxxxxxxxxxxxxx
```

Sử dụng trong code:

```javascript
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL;

const fetchGyms = async () => {
  const response = await fetch(`${API_BASE_URL}/gyms`);
  return await response.json();
};
```

---

## 🚀 Quick Start Checklist cho Frontend

- [ ] Cài đặt AWS Amplify: `npm install aws-amplify`
- [ ] Configure Amplify với Cognito User Pool
- [ ] Implement login/signup flow
- [ ] Sau khi login, gọi `POST /auth/register` để tạo user trong backend
- [ ] Lưu JWT token (localStorage hoặc context)
- [ ] Tạo API utility function để tự động thêm token vào headers
- [ ] Implement error handling cho tất cả API calls
- [ ] Test với Postman trước khi integrate vào UI

---

## 📞 Support

Nếu có vấn đề hoặc câu hỏi về API:
- 📧 Email: backend@easybody.com
- 💬 Slack: #backend-support
- 📖 API Documentation: http://localhost:8080/api/v1/docs (coming soon)

---

## 🔄 Version History

- **v1.0.0** (Oct 2025) - Initial release với Node.js/Express
# 📘 Easy Body API - Hướng dẫn cho Frontend Developer

## 🌐 Base URL

```
Development: http://localhost:8080/api/v1
Production: https://api.easybody.com/api/v1
```

---

## 🔐 Authentication

### 1. **Flow đăng nhập/đăng ký**

```javascript
// Bước 1: User signup/login qua AWS Cognito (dùng AWS Amplify hoặc Cognito SDK)
import { Auth } from 'aws-amplify';

// Đăng ký user mới
const signUp = async (email, password, firstName, lastName) => {
  try {
    const { user } = await Auth.signUp({
      username: email,
      password,
      attributes: {
        email,
        given_name: firstName,
        family_name: lastName
      }
    });
    return user;
  } catch (error) {
    console.error('Error signing up:', error);
  }
};

// Đăng nhập
const signIn = async (email, password) => {
  try {
    const user = await Auth.signIn(email, password);
    return user;
  } catch (error) {
    console.error('Error signing in:', error);
  }
};

// Lấy JWT token
const getToken = async () => {
  try {
    const session = await Auth.currentSession();
    return session.getIdToken().getJwtToken();
  } catch (error) {
    console.error('Error getting token:', error);
  }
};
```

### 2. **Đăng ký user trong Backend**

Sau khi user đăng ký thành công trên Cognito, bạn **BẮT BUỘC** phải gọi API này để tạo user trong database backend:

```javascript
// POST /api/v1/auth/register
const registerInBackend = async () => {
  const token = await getToken();
  
  const response = await fetch('http://localhost:8080/api/v1/auth/register', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: 'user@example.com',
      firstName: 'John',
      lastName: 'Doe',
      phoneNumber: '+84901234567',
      role: 'CLIENT_USER' // Hoặc 'GYM_STAFF', 'PT_USER', 'ADMIN'
    })
  });
  
  const user = await response.json();
  return user;
};
```

**Response:**
```json
{
  "id": 1,
  "cognitoSub": "a1b2c3d4-5678-90ab-cdef-1234567890ab",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+84901234567",
  "role": "CLIENT_USER",
  "active": true,
  "createdAt": "2025-10-06T10:30:00.000Z",
  "updatedAt": "2025-10-06T10:30:00.000Z"
}
```

### 3. **Gửi token trong mọi request**

Tất cả các API requests (trừ public APIs) phải có JWT token trong header:

```javascript
const apiCall = async (endpoint, method = 'GET', body = null) => {
  const token = await getToken();
  
  const options = {
    method,
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    }
  };
  
  if (body) {
    options.body = JSON.stringify(body);
  }
  
  const response = await fetch(`http://localhost:8080/api/v1${endpoint}`, options);
  return await response.json();
};
```

---

## 🏋️ API Endpoints Chi Tiết

### **1. Authentication APIs**

#### 📌 GET /auth/me - Lấy thông tin user hiện tại

```javascript
const getCurrentUser = async () => {
  const response = await apiCall('/auth/me');
  return response;
};

// Response:
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CLIENT_USER",
  "active": true
}
```

#### 📌 PUT /auth/me - Cập nhật profile

```javascript
const updateProfile = async (data) => {
  const response = await apiCall('/auth/me', 'PUT', {
    firstName: 'Jane',
    lastName: 'Smith',
    phoneNumber: '+84909876543',
    profileImageUrl: 'https://s3.amazonaws.com/...'
  });
  return response;
};
```

---

### **2. Gyms APIs**

#### 📌 GET /gyms - Danh sách gyms (Public)

```javascript
const getGyms = async (page = 0, size = 20) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/gyms?page=${page}&size=${size}`
  );
  return await response.json();
};

// Response:
{
  "content": [
    {
      "id": 1,
      "name": "Fitness Pro Gym",
      "description": "Premium gym with modern equipment",
      "phoneNumber": "+84901234567",
      "email": "info@fitnesspro.com",
      "averageRating": 4.5,
      "ratingCount": 120,
      "location": {
        "id": 1,
        "latitude": 10.7769,
        "longitude": 106.7009,
        "city": "Ho Chi Minh City",
        "address": "123 Nguyen Hue St"
      }
    }
  ],
  "pageNumber": 0,
  "pageSize": 20,
  "totalElements": 50,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

#### 📌 GET /gyms/search - Tìm kiếm gyms theo location

```javascript
// Tìm gyms gần vị trí user (bán kính 10km)
const searchNearbyGyms = async (lat, lng, radiusKm = 10) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/gyms/search?latitude=${lat}&longitude=${lng}&radiusKm=${radiusKm}`
  );
  return await response.json();
};

// Tìm kiếm theo text
const searchGymsByText = async (query) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/gyms/search?query=${encodeURIComponent(query)}`
  );
  return await response.json();
};
```

#### 📌 POST /gyms - Tạo gym mới (GYM_STAFF, ADMIN only)

```javascript
const createGym = async () => {
  const response = await apiCall('/gyms', 'POST', {
    name: 'New Fitness Center',
    description: 'Modern gym in downtown',
    phoneNumber: '+84901234567',
    email: 'contact@newfitness.com',
    website: 'https://newfitness.com',
    logoUrl: 'https://s3.amazonaws.com/easybody-media/gym-logo.jpg',
    // Location info
    latitude: 10.7769,
    longitude: 106.7009,
    address: '123 Main Street',
    city: 'Ho Chi Minh City',
    state: 'Ho Chi Minh',
    country: 'Vietnam',
    postalCode: '700000'
  });
  return response;
};
```

#### 📌 GET /gyms/:id/personal-trainers - Danh sách PTs của gym

```javascript
const getGymTrainers = async (gymId) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/gyms/${gymId}/personal-trainers`
  );
  return await response.json();
};
```

---

### **3. Personal Trainers APIs**

#### 📌 GET /pt-users/search - Tìm kiếm PTs

```javascript
// Tìm PT theo specialization và mức giá
const searchPTs = async () => {
  const params = new URLSearchParams({
    specialization: 'Yoga',
    minRate: 20,
    maxRate: 50,
    page: 0,
    size: 20
  });
  
  const response = await fetch(
    `http://localhost:8080/api/v1/pt-users/search?${params}`
  );
  return await response.json();
};

// Tìm PT gần vị trí user
const searchNearbyPTs = async (lat, lng, radiusKm = 10) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/pt-users/search?latitude=${lat}&longitude=${lng}&radiusKm=${radiusKm}`
  );
  return await response.json();
};
```

#### 📌 POST /pt-users - Tạo PT profile (PT_USER only)

```javascript
const createPTProfile = async () => {
  const response = await apiCall('/pt-users', 'POST', {
    bio: 'Certified personal trainer with 5 years experience',
    specializations: 'Weight Loss, Strength Training, Yoga',
    certifications: 'NASM-CPT, ACE Certified',
    yearsOfExperience: 5,
    hourlyRate: 35.00,
    profileImageUrl: 'https://s3.amazonaws.com/...',
    // Location
    latitude: 10.7769,
    longitude: 106.7009,
    address: '456 Fitness Street',
    city: 'Ho Chi Minh City',
    state: 'Ho Chi Minh',
    country: 'Vietnam'
  });
  return response;
};
```

---

### **4. Offers APIs**

#### 📌 GET /offers - Danh sách offers (Public)

```javascript
const getOffers = async (page = 0, size = 20) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/offers?page=${page}&size=${size}`
  );
  return await response.json();
};

// Response:
{
  "content": [
    {
      "id": 1,
      "title": "1 Month Gym Membership",
      "description": "Full access to all equipment",
      "offerType": "GYM_OFFER",
      "price": 50.00,
      "currency": "USD",
      "status": "APPROVED",
      "averageRating": 4.8,
      "ratingCount": 45,
      "gym": {
        "id": 1,
        "name": "Fitness Pro Gym"
      }
    }
  ],
  "pageNumber": 0,
  "totalElements": 100
}
```

#### 📌 GET /offers/search - Tìm kiếm offers

```javascript
const searchOffers = async () => {
  const params = new URLSearchParams({
    query: 'yoga',
    offerType: 'PT_OFFER', // hoặc 'GYM_OFFER'
    minPrice: 20,
    maxPrice: 100,
    page: 0,
    size: 20
  });
  
  const response = await fetch(
    `http://localhost:8080/api/v1/offers/search?${params}`
  );
  return await response.json();
};
```

#### 📌 POST /offers - Tạo offer mới

```javascript
// Tạo Gym Offer
const createGymOffer = async () => {
  const response = await apiCall('/offers', 'POST', {
    title: '3 Month Premium Membership',
    description: 'Unlimited access to gym + classes',
    offerType: 'GYM_OFFER',
    price: 120.00,
    currency: 'USD',
    durationDescription: '3 months',
    imageUrls: [
      'https://s3.amazonaws.com/easybody-media/offer1.jpg',
      'https://s3.amazonaws.com/easybody-media/offer2.jpg'
    ],
    gymId: 1 // ID của gym
  });
  return response;
};

// Tạo PT Offer
const createPTOffer = async () => {
  const response = await apiCall('/offers', 'POST', {
    title: '10 Personal Training Sessions',
    description: 'One-on-one training with certified PT',
    offerType: 'PT_OFFER',
    price: 300.00,
    currency: 'USD',
    durationDescription: '10 sessions',
    ptUserId: 5 // ID của PT
  });
  return response;
};
```

---

### **5. Ratings APIs**

#### 📌 GET /ratings/offer/:offerId - Danh sách ratings của offer

```javascript
const getOfferRatings = async (offerId) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/ratings/offer/${offerId}`
  );
  return await response.json();
};

// Response:
{
  "content": [
    {
      "id": 1,
      "rating": 5,
      "comment": "Great gym with excellent equipment!",
      "user": {
        "firstName": "John",
        "lastName": "Doe"
      },
      "createdAt": "2025-10-01T10:00:00.000Z"
    }
  ]
}
```

#### 📌 POST /ratings - Tạo rating mới

```javascript
const createRating = async (offerId) => {
  const response = await apiCall('/ratings', 'POST', {
    offerId: offerId,
    rating: 5, // 1-5 stars
    comment: 'Excellent service and friendly staff!'
  });
  return response;
};
```

---

### **6. Search API - Tìm kiếm tổng hợp**

#### 📌 GET /search - Tìm tất cả (gyms, PTs, offers)

```javascript
const searchAll = async (query) => {
  const response = await fetch(
    `http://localhost:8080/api/v1/search?query=${encodeURIComponent(query)}`
  );
  return await response.json();
};

// Response:
{
  "gyms": [...],
  "pts": [...],
  "offers": [...],
  "total": 25
}
```

---

## 📸 Upload Images lên S3

### **Complete Flow với Code Example**

```javascript
const uploadImageToS3 = async (file) => {
  try {
    // Step 1: Lấy presigned URL từ backend
    const token = await getToken();
    const presignedResponse = await fetch('http://localhost:8080/api/v1/media/presigned-url', {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        fileName: file.name,
        fileType: file.type

