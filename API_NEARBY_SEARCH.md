# API TÌMKIẾM THEO KHOẢNG CÁCH (NEARBY SEARCH)

## Endpoint
```
GET /api/v1/search/nearby
```

## Mô tả
Tìm kiếm các địa điểm (Gyms, PTs) nằm trong bán kính X km từ tọa độ người dùng.  
Kết quả được sắp xếp theo khoảng cách từ gần đến xa.

---

## Query Parameters

| Tham số | Kiểu | Bắt buộc | Mô tả | Ví dụ |
|---------|------|----------|-------|-------|
| `lat` | Number | **Có** | Latitude (vĩ độ) của vị trí người dùng | `21.0285` |
| `lon` | Number | **Có** | Longitude (kinh độ) của vị trí người dùng | `105.8542` |
| `radius` | Number | **Có** | Bán kính tìm kiếm (km), tối đa 100km | `5` |
| `type` | String | Không | Lọc theo loại: `gym`, `pt` (nếu bỏ qua sẽ tìm tất cả) | `gym` |
| `page` | Integer | Không | Số trang (bắt đầu từ 0) | `0` |
| `size` | Integer | Không | Số kết quả mỗi trang (tối đa 100, mặc định 20) | `20` |

---

## Ví dụ Request

### 1. Tìm tất cả Gyms trong bán kính 5km
```http
GET http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=5&type=gym
```

### 2. Tìm tất cả PTs trong bán kính 10km
```http
GET http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=10&type=pt
```

### 3. Tìm tất cả (Gyms + PTs) trong bán kính 3km
```http
GET http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=3
```

### 4. Tìm kiếm với phân trang
```http
GET http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=5&page=0&size=10
```

---

## Response Format

### Success Response (200 OK)

```json
{
  "results": [
    {
      "id": 1,
      "name": "EasyBody Fitness Center",
      "description": "Modern gym with premium equipment",
      "logoUrl": "https://example.com/logo.jpg",
      "phoneNumber": "+84123456789",
      "email": "contact@easybody.com",
      "website": "https://easybody.com",
      "averageRating": 4.5,
      "ratingCount": 120,
      "location": {
        "latitude": 21.0290,
        "longitude": 105.8530,
        "address": "123 Đường ABC",
        "city": "Hà Nội",
        "formattedAddress": "123 Đường ABC, Quận Ba Đình, Hà Nội"
      },
      "distance": 0.52,
      "type": "gym"
    },
    {
      "id": 2,
      "name": "John Doe",
      "bio": "Certified personal trainer with 10 years experience",
      "specializations": "Weight Loss, Muscle Building",
      "certifications": "ACE, NASM",
      "experience": 10,
      "hourlyRate": 500000,
      "availability": "Mon-Fri: 6AM-9PM",
      "averageRating": 4.8,
      "ratingCount": 45,
      "location": {
        "latitude": 21.0300,
        "longitude": 105.8550,
        "address": "456 Đường XYZ",
        "city": "Hà Nội",
        "formattedAddress": "456 Đường XYZ, Quận Hoàn Kiếm, Hà Nội"
      },
      "distance": 1.23,
      "type": "pt"
    }
  ],
  "pagination": {
    "page": 0,
    "size": 20,
    "total": 15,
    "totalPages": 1
  },
  "searchCriteria": {
    "latitude": 21.0285,
    "longitude": 105.8542,
    "radius": 5,
    "type": "all"
  }
}
```

### Error Responses

#### 400 Bad Request - Missing Parameters
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Missing required parameters: lat, lon, radius",
  "timestamp": "2025-10-07T10:30:00.000Z"
}
```

#### 400 Bad Request - Invalid Coordinates
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid coordinates. Latitude must be between -90 and 90, longitude between -180 and 180",
  "timestamp": "2025-10-07T10:30:00.000Z"
}
```

#### 400 Bad Request - Invalid Radius
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Radius must be between 0 and 100 km",
  "timestamp": "2025-10-07T10:30:00.000Z"
}
```

#### 500 Internal Server Error
```json
{
  "status": 500,
  "error": "Internal Server Error",
  "message": "Database connection failed",
  "timestamp": "2025-10-07T10:30:00.000Z"
}
```

---

## Cách lấy tọa độ người dùng (Frontend)

### 1. Sử dụng GPS của trình duyệt
```javascript
// Lấy vị trí hiện tại của người dùng
navigator.geolocation.getCurrentPosition(
  (position) => {
    const lat = position.coords.latitude;
    const lon = position.coords.longitude;
    
    // Gọi API nearby search
    fetch(`http://localhost:8080/api/v1/search/nearby?lat=${lat}&lon=${lon}&radius=5`)
      .then(res => res.json())
      .then(data => console.log(data));
  },
  (error) => {
    console.error("Unable to retrieve location", error);
  }
);
```

### 2. Chuyển đổi địa chỉ thành tọa độ (Geocoding)
Nếu người dùng nhập địa chỉ, bạn cần sử dụng Geocoding API:

#### Sử dụng Google Maps Geocoding API
```javascript
const address = "123 Đường ABC, Hà Nội";
const geocodeUrl = `https://maps.googleapis.com/maps/api/geocode/json?address=${encodeURIComponent(address)}&key=YOUR_API_KEY`;

fetch(geocodeUrl)
  .then(res => res.json())
  .then(data => {
    if (data.results.length > 0) {
      const location = data.results[0].geometry.location;
      const lat = location.lat;
      const lon = location.lng;
      
      // Gọi API nearby search
      fetch(`http://localhost:8080/api/v1/search/nearby?lat=${lat}&lon=${lon}&radius=5`)
        .then(res => res.json())
        .then(data => console.log(data));
    }
  });
```

#### Sử dụng OpenStreetMap Nominatim (Miễn phí)
```javascript
const address = "123 Đường ABC, Hà Nội";
const nominatimUrl = `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(address)}&format=json&limit=1`;

fetch(nominatimUrl)
  .then(res => res.json())
  .then(data => {
    if (data.length > 0) {
      const lat = parseFloat(data[0].lat);
      const lon = parseFloat(data[0].lon);
      
      // Gọi API nearby search
      fetch(`http://localhost:8080/api/v1/search/nearby?lat=${lat}&lon=${lon}&radius=5`)
        .then(res => res.json())
        .then(data => console.log(data));
    }
  });
```

---

## Lưu ý cho Frontend

1. **Validation trước khi gọi API:**
   - Kiểm tra lat phải nằm trong khoảng [-90, 90]
   - Kiểm tra lon phải nằm trong khoảng [-180, 180]
   - Kiểm tra radius > 0 và <= 100

2. **Hiển thị kết quả:**
   - Sắp xếp theo `distance` (đã được BE sắp xếp sẵn)
   - Hiển thị khoảng cách với đơn vị km
   - Có thể hiển thị trên bản đồ (Google Maps / Leaflet / Mapbox)

3. **Xử lý lỗi:**
   - Người dùng từ chối quyền truy cập vị trí → Yêu cầu nhập địa chỉ thủ công
   - Không tìm thấy kết quả → Hiển thị thông báo "Không có địa điểm nào trong bán kính X km"

4. **Tối ưu performance:**
   - Cache kết quả tìm kiếm trong một khoảng thời gian ngắn
   - Sử dụng debounce khi người dùng thay đổi bán kính
   - Lazy load danh sách kết quả (pagination)

---

## Ví dụ React Component

```javascript
import React, { useState, useEffect } from 'react';
import axios from 'axios';

function NearbySearch() {
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [radius, setRadius] = useState(5);
  const [userLocation, setUserLocation] = useState(null);

  // Lấy vị trí hiện tại khi component mount
  useEffect(() => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        setUserLocation({
          lat: position.coords.latitude,
          lon: position.coords.longitude
        });
      },
      (error) => {
        console.error("Location error:", error);
        alert("Vui lòng bật quyền truy cập vị trí");
      }
    );
  }, []);

  // Tìm kiếm khi có vị trí và radius
  useEffect(() => {
    if (userLocation) {
      searchNearby();
    }
  }, [userLocation, radius]);

  const searchNearby = async () => {
    setLoading(true);
    try {
      const response = await axios.get('http://localhost:8080/api/v1/search/nearby', {
        params: {
          lat: userLocation.lat,
          lon: userLocation.lon,
          radius: radius
        }
      });
      setResults(response.data.results);
    } catch (error) {
      console.error("Search error:", error);
      alert("Có lỗi xảy ra khi tìm kiếm");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Tìm kiếm gần bạn</h1>
      
      {/* Slider chọn bán kính */}
      <div>
        <label>Bán kính: {radius} km</label>
        <input 
          type="range" 
          min="1" 
          max="50" 
          value={radius}
          onChange={(e) => setRadius(e.target.value)}
        />
      </div>

      {/* Hiển thị kết quả */}
      {loading ? (
        <p>Đang tìm kiếm...</p>
      ) : (
        <div>
          <h2>Tìm thấy {results.length} kết quả</h2>
          {results.map(item => (
            <div key={`${item.type}-${item.id}`} style={{ border: '1px solid #ccc', margin: '10px', padding: '10px' }}>
              <h3>{item.name}</h3>
              <p>{item.description || item.bio}</p>
              <p><strong>Khoảng cách:</strong> {item.distance} km</p>
              <p><strong>Địa chỉ:</strong> {item.location.formattedAddress}</p>
              <p><strong>Đánh giá:</strong> ⭐ {item.averageRating} ({item.ratingCount} reviews)</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default NearbySearch;
```

---

## Test API

Tôi đã tạo file test PowerShell: `test-nearby-search.ps1`

Chạy lệnh sau để test:
```powershell
.\test-nearby-search.ps1
```

Hoặc test thủ công bằng curl:
```bash
curl "http://localhost:8080/api/v1/search/nearby?lat=21.0285&lon=105.8542&radius=5&type=gym"
```

