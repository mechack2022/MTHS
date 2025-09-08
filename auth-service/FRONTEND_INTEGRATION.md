# Frontend Integration Guide

This guide explains how to integrate your frontend application with the auth-service backend.

## Table of Contents
1. [Backend Configuration](#backend-configuration)
2. [Frontend Setup](#frontend-setup)
3. [Authentication Flow](#authentication-flow)
4. [API Endpoints](#api-endpoints)
5. [Error Handling](#error-handling)
6. [Environment Variables](#environment-variables)

## Backend Configuration

### 1. Environment Setup
1. Copy `.env.example` to `.env`
2. Update the environment variables, especially:
   ```bash
   # Update with your frontend URLs
   FRONTEND_ORIGINS=http://localhost:3000,http://localhost:5173,https://yourdomain.com
   FRONTEND_BASE_URL=http://localhost:3000
   
   # Set your JWT secret (256-bit)
   JWT_SECRET=your-very-long-secret-key-here
   
   # Database configuration
   DB_URL=jdbc:postgresql://localhost:5432/your_db_name
   DB_USER=your_db_user
   DB_PASSWORD=your_db_password
   ```

### 2. Start the Backend
```bash
# Using Maven wrapper
./mvnw spring-boot:run

# Or using jar
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

The backend will run on port 8081 by default.

## Frontend Setup

### 1. Base API Configuration
```javascript
// api.js
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8081';

const apiClient = {
  // Base fetch wrapper
  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const config = {
      headers: {
        'Content-Type': 'application/json',
        ...options.headers,
      },
      credentials: 'include', // Important for CORS with credentials
      ...options,
    };

    const response = await fetch(url, config);
    const data = await response.json();
    
    if (!response.ok) {
      throw new Error(data.message || 'API request failed');
    }
    
    return data;
  }
};

export default apiClient;
```

### 2. Authentication Service
```javascript
// authService.js
import apiClient from './api';

class AuthService {
  // User registration
  async register(userData) {
    return apiClient.request('/api/auth/register', {
      method: 'POST',
      body: JSON.stringify(userData)
    });
  }

  // Email verification
  async verifyEmail(userId, verificationCode) {
    return apiClient.request('/api/auth/verify-email', {
      method: 'PUT',
      body: JSON.stringify({ userId, verificationCode })
    });
  }

  // Login
  async login(email, password) {
    const response = await apiClient.request('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    });
    
    // Store tokens
    if (response.data) {
      localStorage.setItem('access_token', response.data.accessToken);
      localStorage.setItem('refresh_token', response.data.refreshToken);
    }
    
    return response;
  }

  // Generate profile creation token
  async generateProfileToken(email) {
    return apiClient.request('/api/auth/generate-profile-token', {
      method: 'POST',
      body: JSON.stringify({ email })
    });
  }

  // Create profile with token
  async createProfile(profileType, userId, profileData, token) {
    return apiClient.request(`/api/v1/profile/public/${profileType}/${userId}`, {
      method: 'POST',
      headers: {
        'Profile-Creation-Token': token
      },
      body: JSON.stringify(profileData)
    });
  }

  // Get authenticated requests
  async authenticatedRequest(endpoint, options = {}) {
    const token = localStorage.getItem('access_token');
    return apiClient.request(endpoint, {
      ...options,
      headers: {
        'Authorization': `Bearer ${token}`,
        ...options.headers
      }
    });
  }

  // Logout
  logout() {
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
  }

  // Check if user is authenticated
  isAuthenticated() {
    return !!localStorage.getItem('access_token');
  }
}

export default new AuthService();
```

### 3. Profile Service
```javascript
// profileService.js
import authService from './authService';

class ProfileService {
  // Get user profile
  async getUserProfile(userId) {
    return authService.authenticatedRequest(`/api/v1/profile/user/${userId}`);
  }

  // Update patient profile
  async updatePatientProfile(userId, profileData) {
    return authService.authenticatedRequest(`/api/v1/profile/patient/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(profileData)
    });
  }

  // Update doctor profile
  async updateDoctorProfile(userId, profileData) {
    return authService.authenticatedRequest(`/api/v1/profile/doctor/${userId}`, {
      method: 'PUT',
      body: JSON.stringify(profileData)
    });
  }
}

export default new ProfileService();
```

## Authentication Flow

### 1. Complete Registration Flow
```javascript
// components/RegistrationFlow.js
import React, { useState } from 'react';
import authService from '../services/authService';

const RegistrationFlow = () => {
  const [step, setStep] = useState(1);
  const [userData, setUserData] = useState({});
  const [profileToken, setProfileToken] = useState('');

  const handleRegistration = async (formData) => {
    try {
      // Step 1: Register user
      const registerResponse = await authService.register(formData);
      setUserData(registerResponse.data);
      setStep(2);
    } catch (error) {
      console.error('Registration failed:', error);
    }
  };

  const handleEmailVerification = async (verificationCode) => {
    try {
      // Step 2: Verify email
      await authService.verifyEmail(userData.uuid, verificationCode);
      
      // Step 3: Generate profile token
      const tokenResponse = await authService.generateProfileToken(userData.email);
      setProfileToken(tokenResponse.data.token);
      setStep(3);
    } catch (error) {
      console.error('Email verification failed:', error);
    }
  };

  const handleProfileCreation = async (profileData) => {
    try {
      // Step 4: Create profile
      const profileType = userData.accountType.toLowerCase();
      await authService.createProfile(profileType, userData.uuid, profileData, profileToken);
      setStep(4); // Success
    } catch (error) {
      console.error('Profile creation failed:', error);
    }
  };

  // Render different steps based on current step
  return (
    <div>
      {step === 1 && <RegistrationForm onSubmit={handleRegistration} />}
      {step === 2 && <EmailVerificationForm onSubmit={handleEmailVerification} />}
      {step === 3 && <ProfileCreationForm onSubmit={handleProfileCreation} />}
      {step === 4 && <RegistrationSuccess />}
    </div>
  );
};

export default RegistrationFlow;
```

### 2. Login Flow
```javascript
// components/LoginForm.js
import React, { useState } from 'react';
import authService from '../services/authService';
import { useNavigate } from 'react-router-dom';

const LoginForm = () => {
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      await authService.login(credentials.email, credentials.password);
      navigate('/dashboard');
    } catch (error) {
      console.error('Login failed:', error);
    }
  };

  return (
    <form onSubmit={handleLogin}>
      <input
        type="email"
        value={credentials.email}
        onChange={(e) => setCredentials({...credentials, email: e.target.value})}
        placeholder="Email"
        required
      />
      <input
        type="password"
        value={credentials.password}
        onChange={(e) => setCredentials({...credentials, password: e.target.value})}
        placeholder="Password"
        required
      />
      <button type="submit">Login</button>
    </form>
  );
};

export default LoginForm;
```

## API Endpoints

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `PUT /api/auth/verify-email` - Email verification
- `POST /api/auth/login` - User login
- `POST /api/auth/generate-profile-token` - Generate profile creation token

### Profile Endpoints (Public - Token-based)
- `POST /api/v1/profile/public/patient/{userId}` - Create patient profile
- `POST /api/v1/profile/public/doctor/{userId}` - Create doctor profile
- `POST /api/v1/profile/public/lab-technician/{userId}` - Create lab tech profile

### Profile Endpoints (Authenticated)
- `GET /api/v1/profile/user/{userId}` - Get user profile
- `PUT /api/v1/profile/patient/{userId}` - Update patient profile
- `PUT /api/v1/profile/doctor/{userId}` - Update doctor profile

### Appointment Endpoints
- `POST /api/v1/appointments` - Create appointment
- `GET /api/v1/appointments/{id}` - Get appointment
- `GET /api/v1/appointments/patient/{patientId}` - Get patient appointments

## Error Handling

```javascript
// utils/errorHandler.js
export const handleApiError = (error) => {
  if (error.response?.status === 401) {
    // Token expired or invalid
    authService.logout();
    window.location.href = '/login';
  } else if (error.response?.status === 403) {
    // Forbidden - insufficient permissions
    console.error('Access denied');
  } else if (error.response?.status >= 500) {
    // Server error
    console.error('Server error occurred');
  } else {
    // Other errors
    console.error('API Error:', error.message);
  }
};
```

## Environment Variables

Create a `.env` file in your frontend project root:

```bash
# React
REACT_APP_API_URL=http://localhost:8081

# Vue.js
VUE_APP_API_URL=http://localhost:8081

# Angular (environment.ts)
# export const environment = {
#   apiUrl: 'http://localhost:8081'
# };
```

## Testing with Frontend

### 1. Start Backend
```bash
cd auth-service
./mvnw spring-boot:run
```

### 2. Start Frontend
```bash
# React
npm start

# Vue.js
npm run serve

# Angular
ng serve
```

### 3. Test CORS
Open browser console and check for CORS errors. If configured correctly, you should see no CORS-related errors when making API calls.

## Common Issues and Solutions

### 1. CORS Errors
- Ensure `FRONTEND_ORIGINS` in `.env` includes your frontend URL
- Check that `credentials: 'include'` is set in fetch requests
- Verify the backend is running on the correct port

### 2. Authentication Issues
- Check JWT token format and expiration
- Verify token is included in Authorization header
- Ensure login endpoint returns proper token structure

### 3. Profile Creation Issues
- Verify `Profile-Creation-Token` header is included
- Check token is generated after email verification
- Ensure public endpoints are accessible without authentication

## Security Considerations

1. **Token Storage**: Consider using secure HTTP-only cookies instead of localStorage
2. **HTTPS**: Always use HTTPS in production
3. **CORS Origins**: Restrict CORS origins to specific domains in production
4. **Token Expiration**: Implement token refresh logic
5. **Input Validation**: Validate all inputs on both frontend and backend