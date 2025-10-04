# Easy Body - Backend REST API

A comprehensive RESTful backend service for the **Easy Body** platform, built with Spring Boot 3, Java 21, and PostgreSQL with PostGIS.

## 🏋️ Overview

Easy Body is a web platform where:
- **Gyms** can register and publish fitness offers
- **Personal Trainers (PTs)** can create profiles and publish training packages
- **Clients** can search, review, and contact gyms/PTs
- **Admins** moderate content and manage reports

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 21
- **Database**: PostgreSQL with PostGIS extension
- **Authentication**: AWS Cognito (JWT-based)
- **Cloud Services**: 
  - AWS S3 (Media storage with pre-signed URLs)
  - AWS SQS (Image moderation queue)
  - AWS CloudWatch (Logging)
  - AWS X-Ray (Tracing)
- **Build Tool**: Gradle
- **Security**: Spring Security with JWT
- **Validation**: Jakarta Validation (Bean Validation)

## 📁 Project Structure

```
src/main/java/com/easybody/
├── config/                 # Configuration classes
│   ├── AwsConfig.java
│   ├── SecurityConfig.java
│   └── JwtAuthenticationFilter.java
├── controller/            # REST Controllers
│   ├── AuthController.java
│   ├── GymController.java
│   ├── PTUserController.java
│   ├── OfferController.java
│   ├── SearchController.java
│   ├── RatingController.java
│   ├── ReportController.java
│   ├── AdminController.java
│   └── MediaController.java
├── service/               # Business Logic Layer
│   ├── UserService.java
│   ├── GymService.java
│   ├── PTUserService.java
│   ├── OfferService.java
│   ├── RatingService.java
│   ├── ReportService.java
│   ├── GymPTAssociationService.java
│   └── S3Service.java
├── repository/            # JPA Repositories
│   ├── UserRepository.java
│   ├── GymRepository.java
│   ├── PTUserRepository.java
│   ├── OfferRepository.java
│   ├── RatingRepository.java
│   ├── ReportRepository.java
│   └── ...
├── model/                 # Domain Models
│   ├── entity/           # JPA Entities
│   └── enums/            # Enumerations
├── dto/                   # Data Transfer Objects
│   ├── request/          # Request DTOs
│   └── response/         # Response DTOs
├── exception/            # Custom Exceptions & Handler
│   ├── ResourceNotFoundException.java
│   ├── UnauthorizedException.java
│   └── GlobalExceptionHandler.java
└── EasyBodyApplication.java
```

## 🗃️ Database Schema

### Core Entities
- **User** - Base user entity with role (Admin, Gym_Staff, PT_User, Client_User)
- **Gym** - Gym profiles with location
- **PTUser** - Personal trainer profiles with specializations
- **GymStaff** - Staff members associated with gyms
- **GymPTAssociation** - Many-to-many relationship with approval workflow
- **ClientUser** - Client profiles with fitness goals
- **Location** - PostGIS-enabled location data (lat/lon + geometry)
- **Offer** - Gym or PT offers with pricing and status
- **Rating** - Client ratings for offers (1-5 stars)
- **Report** - User-submitted reports for moderation

## 🚀 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - Register new user
- `GET /api/v1/auth/me` - Get current user profile

### Gym Management
- `POST /api/v1/gyms` - Register a gym
- `PUT /api/v1/gyms/{id}` - Update gym
- `GET /api/v1/gyms/{id}` - Get gym details
- `GET /api/v1/gyms/search` - Search gyms (text or geo-location)
- `POST /api/v1/gyms/{id}/assign-pt` - Assign PT to gym
- `PUT /api/v1/gyms/pt-associations/{id}/approve` - Approve PT assignment

### PT Management
- `POST /api/v1/pt-users` - Create PT profile
- `PUT /api/v1/pt-users/{id}` - Update PT profile
- `GET /api/v1/pt-users/{id}` - Get PT details
- `GET /api/v1/pt-users` - List PTs (with geo-location filter)

### Offer Management
- `POST /api/v1/offers` - Create offer
- `PUT /api/v1/offers/{id}` - Update offer
- `GET /api/v1/offers/{id}` - Get offer details

### Search
- `POST /api/v1/search/offers` - Advanced offer search
- `GET /api/v1/search/offers` - Search with query parameters
  - Filters: location radius, price range, rating, offer type, text search

### Ratings
- `POST /api/v1/ratings` - Create rating (clients only)
- `GET /api/v1/ratings/offer/{offerId}` - Get offer ratings

### Reports
- `POST /api/v1/reports` - Submit report

### Admin Moderation
- `GET /api/v1/admin/offers/pending` - Get pending offers
- `PUT /api/v1/admin/offers/{id}/moderate` - Approve/reject offer
- `GET /api/v1/admin/reports/pending` - Get pending reports
- `PUT /api/v1/admin/reports/{id}/resolve` - Resolve report
- `PUT /api/v1/admin/reports/{id}/dismiss` - Dismiss report

### Media Upload
- `GET /api/v1/media/presigned-url` - Generate S3 pre-signed upload URL

## ⚙️ Configuration

Create `src/main/resources/application.yml` or set environment variables:

```yaml
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=easybody
DB_USERNAME=postgres
DB_PASSWORD=yourpassword

# AWS
AWS_REGION=us-east-1
COGNITO_USER_POOL_ID=your-pool-id
COGNITO_CLIENT_ID=your-client-id
COGNITO_JWKS_URL=https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
S3_BUCKET_NAME=easybody-media
SQS_IMAGE_QUEUE_URL=https://sqs.{region}.amazonaws.com/{account}/image-moderation

# Optional
SHOW_SQL=true
SQL_LOG_LEVEL=DEBUG
```

## 🏃 Running the Application

### Prerequisites
- Java 21
- PostgreSQL 14+ with PostGIS extension
- AWS Account (for Cognito, S3, SQS)

### Setup Database

```sql
CREATE DATABASE easybody;
\c easybody
CREATE EXTENSION postgis;
```

### Build and Run

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Or run JAR
java -jar build/libs/easybody-0.0.1-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

## 🔐 Authentication

All requests (except public endpoints) require a JWT token from AWS Cognito:

```
Authorization: Bearer <JWT_TOKEN>
```

The JWT should contain:
- `sub` - Cognito user ID
- `custom:role` - User role (ADMIN, GYM_STAFF, PT_USER, CLIENT_USER)

## 🌍 Geo-Location Features

The application uses PostGIS for geo-spatial queries:

- **Radius Search**: Find gyms/PTs/offers within X km of a location
- **Distance Calculation**: Calculate distances between points
- **Spatial Indexing**: Optimized geo-queries with spatial indexes

Example search:
```
GET /api/v1/search/offers?latitude=40.7128&longitude=-74.0060&radiusKm=5
```

## 📝 Business Logic

### Offer Workflow
1. Gym/PT creates offer → Status: `PENDING`
2. Images sent to SQS for moderation (TODO)
3. Admin reviews → Status: `APPROVED` or `REJECTED`
4. Approved offers appear in public search

### PT-Gym Association
1. Gym assigns PT → Status: `PENDING`
2. Gym staff/admin approves → Status: `APPROVED`
3. PT can create offers for that gym

### Rating System
- Clients rate offers (1-5 stars)
- Average rating auto-calculated on each new rating
- Ratings influence search ranking

### Report System
- Users report inappropriate content/users
- Admin reviews and resolves/dismisses reports
- Tracks report history and moderation decisions

## 🔜 TODO / Future Enhancements

- [ ] Implement SQS queue consumer for image moderation
- [ ] Integrate AWS SageMaker for content moderation
- [ ] Add OpenSearch for advanced text search
- [ ] Implement email notifications (SES)
- [ ] Add pagination improvements
- [ ] Create comprehensive unit tests
- [ ] Add API documentation (Swagger/OpenAPI)
- [ ] Implement rate limiting
- [ ] Add caching layer (Redis)
- [ ] Create CI/CD pipeline

## 📄 License

Proprietary - All rights reserved

## 👥 Contact

For questions or support, contact the development team.

