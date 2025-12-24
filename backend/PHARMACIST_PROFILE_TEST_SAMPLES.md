# Pharmacist Profile API Testing Guide

## Table of Contents
1. [Registration Flow](#registration-flow)
2. [Profile Creation (Public Endpoint)](#profile-creation-public-endpoint)
3. [Profile Creation (Authenticated)](#profile-creation-authenticated)
4. [Profile Update](#profile-update)
5. [Profile Retrieval](#profile-retrieval)
6. [Admin Operations](#admin-operations)

---

## Registration Flow

### Step 1: Register Pharmacist User

**Endpoint:** `POST /api/auth/register`

**Request Body (Swagger/Postman):**
```json
{
  "email": "pharmacist.john@healthcare.com",
  "password": "SecurePass123!",
  "firstName": "John",
  "lastName": "Smith",
  "accountType": "PHARMACIST"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "User registered successfully. Please check your email for verification code.",
  "data": {
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "email": "pharmacist.john@healthcare.com"
  },
  "statusCode": 201
}
```

---

### Step 2: Verify Email

**Endpoint:** `PUT /api/auth/verify-email`

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "code": "123456"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Email verified successfully",
  "data": {
    "verified": true,
    "message": "Email verification successful"
  },
  "statusCode": 200
}
```

---

### Step 3: Generate Profile Creation Token

**Endpoint:** `POST /api/auth/generate-profile-token`

**Request Body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Profile creation token generated successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "message": "Token valid for 24 hours. Use this token to create your profile."
  },
  "statusCode": 200
}
```

---

## Profile Creation (Public Endpoint)

### Create Pharmacist Profile (Public - No JWT Required)

   **Endpoint:** `POST /api/v1/profile/public/pharmacist/{userId}`

**Path Variable:**
- `userId`: `550e8400-e29b-41d4-a716-446655440000`

**Headers:**
```
Profile-Creation-Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request Body (Minimal - Required Fields Only):**
```json
{
  "licenseNumber": "PCN-2024-001234",
  "registrationNumber": "REG-NG-567890",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "+234-803-456-7890",
  "address": "15 Allen Avenue, Ikeja, Lagos"
}
```

**Request Body (Complete - All Fields):**
```json
{
  "pharmacyId": null,
  "licenseNumber": "PCN-2024-001234",
  "registrationNumber": "REG-NG-567890",
  "licenseExpiryDate": "2026-12-31",
  "issuingAuthority": "Pharmacists Council of Nigeria (PCN)",
  "gender": "MALE",
  "dateOfBirth": "1990-05-15",
  "phoneNumber": "+234-803-456-7890",
  "alternativePhone": "+234-802-123-4567",
  "address": "15 Allen Avenue, Ikeja, Lagos State, Nigeria",
  "specialization": "Clinical Pharmacy",
  "yearsOfExperience": 5,
  "qualifications": "B.Pharm (University of Lagos), M.Sc. Clinical Pharmacy (University of Ibadan)",
  "pharmacySchool": "University of Lagos",
  "graduationYear": 2018,
  "employmentType": "FULL_TIME",
  "position": "Senior Pharmacist",
  "hireDate": "2020-01-15",
  "isSuperintendent": false,
  "bio": "Experienced clinical pharmacist with 5+ years in patient care and medication management. Specialized in chronic disease management and pharmaceutical care.",
  "certificateUrl": "https://example.com/certificates/john-smith-pcn.pdf"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Pharmacist profile created successfully",
  "data": {
    "id": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "pharmacist.john@healthcare.com",
    "fullName": "John Smith",
    "pharmacyId": null,
    "pharmacyName": null,
    "licenseNumber": "PCN-2024-001234",
    "registrationNumber": "REG-NG-567890",
    "licenseExpiryDate": "2026-12-31",
    "issuingAuthority": "Pharmacists Council of Nigeria (PCN)",
    "gender": "MALE",
    "dateOfBirth": "1990-05-15",
    "age": 34,
    "phoneNumber": "+234-803-456-7890",
    "alternativePhone": "+234-802-123-4567",
    "address": "15 Allen Avenue, Ikeja, Lagos State, Nigeria",
    "specialization": "Clinical Pharmacy",
    "yearsOfExperience": 5,
    "qualifications": "B.Pharm (University of Lagos), M.Sc. Clinical Pharmacy (University of Ibadan)",
    "pharmacySchool": "University of Lagos",
    "graduationYear": 2018,
    "employmentType": "FULL_TIME",
    "position": "Senior Pharmacist",
    "hireDate": "2020-01-15",
    "isSuperintendent": false,
    "profileStatus": "PENDING_VERIFICATION",
    "profileVerificationDate": null,
    "totalPrescriptionsDispensed": 0,
    "rating": 0.0,
    "totalReviews": 0,
    "isAvailable": true,
    "availabilityNotes": null,
    "bio": "Experienced clinical pharmacist with 5+ years in patient care and medication management.",
    "certificateUrl": "https://example.com/certificates/john-smith-pcn.pdf",
    "profileImageUrl": null,
    "isProfileComplete": true,
    "isVerified": false,
    "canDispensePrescriptions": false,
    "isLicenseValid": true,
    "isLicenseExpired": false,
    "createdAt": "2024-12-13T10:30:00",
    "updatedAt": "2024-12-13T10:30:00"
  },
  "statusCode": 201
}
```

---

## Profile Creation (Authenticated)

### Login to Get JWT Token

**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "pharmacist.john@healthcare.com",
  "password": "SecurePass123!"
}
```

**Expected Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE2MzY...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE2MzY..."
  }
}
```

---

### Create Pharmacist Profile (Authenticated)

**Endpoint:** `POST /api/v1/profile/pharmacist`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NTBlODQwMC1lMjliLTQxZDQtYTcxNi00NDY2NTU0NDAwMDAiLCJpYXQiOjE2MzY...
Content-Type: application/json
```

**Request Body:**
```json
{
  "licenseNumber": "PCN-2024-002345",
  "registrationNumber": "REG-NG-678901",
  "licenseExpiryDate": "2027-06-30",
  "issuingAuthority": "Pharmacists Council of Nigeria (PCN)",
  "gender": "FEMALE",
  "dateOfBirth": "1992-08-20",
  "phoneNumber": "+234-805-678-9012",
  "alternativePhone": "+234-806-789-0123",
  "address": "22 Awolowo Road, Ikoyi, Lagos State, Nigeria",
  "specialization": "Hospital Pharmacy",
  "yearsOfExperience": 3,
  "qualifications": "B.Pharm (Obafemi Awolowo University)",
  "pharmacySchool": "Obafemi Awolowo University",
  "graduationYear": 2020,
  "employmentType": "FULL_TIME",
  "position": "Staff Pharmacist",
  "hireDate": "2021-03-01",
  "isSuperintendent": false,
  "bio": "Hospital pharmacist with expertise in inpatient medication management and sterile compounding."
}
```

---

## Profile Update

### Update Pharmacist Profile

**Endpoint:** `PUT /api/v1/profile/pharmacist`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
Content-Type: application/json
```

**Request Body (Update Specific Fields):**
```json
{
  "pharmacyId": 1,
  "yearsOfExperience": 6,
  "position": "Chief Pharmacist",
  "isAvailable": true,
  "availabilityNotes": "Available Monday-Friday, 9AM-5PM",
  "bio": "Senior clinical pharmacist with 6+ years experience in patient-centered pharmaceutical care. Currently serving as Chief Pharmacist.",
  "certificateUrl": "https://example.com/certificates/john-smith-updated.pdf"
}
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Pharmacist profile updated successfully",
  "data": {
    "id": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "pharmacist.john@healthcare.com",
    "fullName": "John Smith",
    "pharmacyId": 1,
    "pharmacyName": "HealthPlus Pharmacy",
    "yearsOfExperience": 6,
    "position": "Chief Pharmacist",
    "isAvailable": true,
    "availabilityNotes": "Available Monday-Friday, 9AM-5PM",
    "bio": "Senior clinical pharmacist with 6+ years experience...",
    "updatedAt": "2024-12-13T15:45:00"
  }
}
```

---

## Profile Retrieval

### Get Current Pharmacist Profile

**Endpoint:** `GET /api/v1/profile/pharmacist`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**No Request Body Required**

**Expected Response:**
```json
{
  "success": true,
  "message": "Pharmacist profile retrieved successfully",
  "data": {
    "id": 1,
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "email": "pharmacist.john@healthcare.com",
    "fullName": "John Smith",
    "pharmacyId": 1,
    "pharmacyName": "HealthPlus Pharmacy",
    "licenseNumber": "PCN-2024-001234",
    "registrationNumber": "REG-NG-567890",
    "licenseExpiryDate": "2026-12-31",
    "issuingAuthority": "Pharmacists Council of Nigeria (PCN)",
    "gender": "MALE",
    "dateOfBirth": "1990-05-15",
    "age": 34,
    "phoneNumber": "+234-803-456-7890",
    "specialization": "Clinical Pharmacy",
    "yearsOfExperience": 6,
    "isProfileComplete": true,
    "isVerified": true,
    "canDispensePrescriptions": true,
    "isLicenseValid": true
  }
}
```

---

## Admin Operations

### Admin Create Pharmacist Profile for User

**Endpoint:** `POST /api/v1/profile/admin/pharmacist/{userId}`

**Path Variable:**
- `userId`: `550e8400-e29b-41d4-a716-446655440000`

**Headers:**
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
Content-Type: application/json
```

**Request Body:**
```json
{
  "pharmacyId": 2,
  "licenseNumber": "PCN-2024-003456",
  "registrationNumber": "REG-NG-789012",
  "licenseExpiryDate": "2028-12-31",
  "issuingAuthority": "Pharmacists Council of Nigeria (PCN)",
  "gender": "MALE",
  "dateOfBirth": "1988-03-10",
  "phoneNumber": "+234-807-890-1234",
  "address": "45 Victoria Island, Lagos",
  "specialization": "Community Pharmacy",
  "yearsOfExperience": 8,
  "employmentType": "FULL_TIME",
  "position": "Superintendent Pharmacist",
  "isSuperintendent": true
}
```

---

### Admin Get User Pharmacist Profile

**Endpoint:** `GET /api/v1/profile/user/{userId}/pharmacist`

**Path Variable:**
- `userId`: `550e8400-e29b-41d4-a716-446655440000`

**Headers:**
```
Authorization: Bearer {ADMIN_JWT_TOKEN}
```

---

### Delete Pharmacist Profile

**Endpoint:** `DELETE /api/v1/profile/pharmacist`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Expected Response:**
```json
{
  "success": true,
  "message": "Pharmacist profile deleted successfully",
  "data": "Profile deleted"
}
```

---

## Postman Collection Setup

### Environment Variables

Create a Postman environment with these variables:

```
BASE_URL: http://localhost:8081
USER_ID: (set after registration)
PROFILE_CREATION_TOKEN: (set after generating token)
ACCESS_TOKEN: (set after login)
```

### Pre-request Scripts

**For authenticated endpoints, add this pre-request script:**
```javascript
pm.environment.set("ACCESS_TOKEN", pm.environment.get("ACCESS_TOKEN"));
```

### Tests Scripts

**Add to registration endpoint:**
```javascript
if (pm.response.code === 201) {
    var jsonData = pm.response.json();
    pm.environment.set("USER_ID", jsonData.data.uuid);
    console.log("User ID saved: " + jsonData.data.uuid);
}
```

**Add to token generation endpoint:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("PROFILE_CREATION_TOKEN", jsonData.data.token);
    console.log("Profile Creation Token saved");
}
```

**Add to login endpoint:**
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("ACCESS_TOKEN", jsonData.data.accessToken);
    console.log("Access Token saved");
}
```

---

## Common Error Responses

### 400 - Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "licenseNumber": "License number is required",
    "dateOfBirth": "Date of birth is required",
    "phoneNumber": "Phone number is required"
  },
  "statusCode": 400
}
```

### 400 - Duplicate License
```json
{
  "success": false,
  "message": "License number already exists",
  "statusCode": 400
}
```

### 401 - Unauthorized
```json
{
  "success": false,
  "message": "Unauthorized - Invalid or expired token",
  "statusCode": 401
}
```

### 404 - Profile Not Found
```json
{
  "success": false,
  "message": "Pharmacist profile not found for user",
  "statusCode": 404
}
```

---

## Additional Test Cases

### Test Case 1: Create Profile with Pharmacy Assignment
```json
{
  "pharmacyId": 1,
  "licenseNumber": "PCN-2024-004567",
  "registrationNumber": "REG-NG-890123",
  "dateOfBirth": "1991-11-25",
  "phoneNumber": "+234-809-012-3456",
  "address": "78 Herbert Macaulay Way, Yaba, Lagos",
  "specialization": "Retail Pharmacy",
  "position": "Store Manager",
  "isSuperintendent": true
}
```

### Test Case 2: Intern Pharmacist
```json
{
  "licenseNumber": "PCN-INT-2024-001",
  "registrationNumber": "REG-NG-INT-001",
  "dateOfBirth": "1999-07-14",
  "phoneNumber": "+234-810-111-2222",
  "address": "10 University Road, Nsukka",
  "specialization": "General Practice",
  "yearsOfExperience": 0,
  "employmentType": "INTERN",
  "position": "Intern Pharmacist",
  "graduationYear": 2023,
  "pharmacySchool": "University of Nigeria"
}
```

### Test Case 3: Part-Time Pharmacist
```json
{
  "licenseNumber": "PCN-2024-005678",
  "registrationNumber": "REG-NG-901234",
  "dateOfBirth": "1985-04-30",
  "phoneNumber": "+234-811-222-3333",
  "address": "32 Broad Street, Lagos Island",
  "specialization": "Clinical Pharmacy",
  "yearsOfExperience": 12,
  "employmentType": "PART_TIME",
  "position": "Consulting Pharmacist",
  "isAvailable": false,
  "availabilityNotes": "Available weekends only"
}
```

---

## Swagger UI Testing

1. **Navigate to:** `http://localhost:8081/swagger-ui.html`

2. **Find the Profile Controller** section

3. **Expand** `POST /api/v1/profile/public/pharmacist/{userId}`

4. **Click** "Try it out"

5. **Fill in:**
   - Path parameter: `userId`
   - Header: `Profile-Creation-Token`
   - Request body: Use the JSON samples above

6. **Click** "Execute"

7. **View response** in the "Response body" section

---

## Notes

- All phone numbers should follow the format: `+234-XXX-XXX-XXXX`
- Date format: `YYYY-MM-DD`
- Employment types: `FULL_TIME`, `PART_TIME`, `CONTRACT`, `INTERN`, `LOCUM`
- Gender values: `MALE`, `FEMALE`, `OTHER`
- License numbers must be unique across all pharmacists
- Profile creation token is valid for 24 hours only

---

**Happy Testing! 🧪**
