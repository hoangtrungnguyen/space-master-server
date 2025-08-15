# Authentication API Usage

This document explains how to use the `/api/auth/login` endpoint to obtain JWT tokens for authentication.

## Login Endpoint

### `POST /api/auth/login`

Authenticates a user by loginName and returns a JWT token valid for 7 days.

**Note**: Password authentication is not implemented yet. The endpoint currently only checks if a user with the given `loginName` exists in the database.

#### Request

```http
POST /api/auth/login
Content-Type: application/json

{
  "loginName": "john_doe"
}
```

#### Successful Response (200 OK)

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJzcGFjZS1tYXN0ZXItdXNlcnMiLCJpc3MiOiJzcGFjZS1tYXN0ZXIiLCJ1c2VySWQiOjEyMywidXNlck5hbWUiOiJqb2huX2RvZSIsImV4cCI6MTczNzYyNzIwMCwiaWF0IjoxNzM2NDE3NjAwfQ.example_signature_here",
  "user": {
    "id": 123,
    "loginName": "john_doe",
    "fullName": "John Doe"
  },
  "expiresAt": "2024-01-23T12:00:00Z"
}
```

#### Error Responses

**400 Bad Request - Empty login name:**
```json
{
  "error": "INVALID_INPUT",
  "message": "Login name cannot be empty",
  "details": null,
  "timestamp": 1736417600000
}
```

**401 Unauthorized - User not found:**
```json
{
  "error": "USER_NOT_FOUND",
  "message": "User with login name 'nonexistent_user' not found",
  "details": null,
  "timestamp": 1736417600000
}
```

**500 Internal Server Error:**
```json
{
  "error": "INTERNAL_ERROR",
  "message": "An error occurred during login",
  "details": null,
  "timestamp": 1736417600000
}
```

## Using the Token for WebSocket Authentication

Once you have the JWT token, use it to connect to secure WebSocket endpoints:

```javascript
// Extract token from login response
const loginResponse = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ loginName: 'john_doe' })
});

const { token } = await loginResponse.json();

// Use token for WebSocket connection
const documentId = "your-document-uuid";
const ws = new WebSocket(
    `ws://localhost:8080/ws/documents/${documentId}?token=${encodeURIComponent(token)}`
);
```

## Using the Token for API Authentication

For protected API endpoints that require JWT authentication:

```javascript
// Use token in Authorization header
const response = await fetch('/protected/profile', {
    headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
    }
});
```

## JWT Token Details

The JWT token contains the following claims:
- `userId`: User's database ID
- `loginName`: User's login name
- `iss` (issuer): Configured issuer (default: "space-master")
- `aud` (audience): Configured audience (default: "space-master-users")
- `exp` (expires): Token expiration time (7 days from issue)
- `iat` (issued at): Token issue time

## Configuration

The JWT configuration is managed through environment variables:

```bash
# Required in production
JWT_SECRET=your-super-secure-secret-key-minimum-256-bits

# Optional (have defaults)
JWT_ISSUER=space-master
JWT_AUDIENCE=space-master-users
```

## Testing the Endpoint

### Using curl:

```bash
# Test successful login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginName": "existing_user"}'

# Test with non-existent user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginName": "nonexistent_user"}'

# Test with empty login name
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"loginName": ""}'
```

### Using JavaScript (Frontend):

```javascript
async function login(loginName) {
    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ loginName })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(`Login failed: ${error.message}`);
        }

        const data = await response.json();
        console.log('Login successful:', data);
        
        // Store token for future use
        localStorage.setItem('authToken', data.token);
        localStorage.setItem('user', JSON.stringify(data.user));
        
        return data;
    } catch (error) {
        console.error('Login error:', error);
        throw error;
    }
}

// Usage
login('john_doe')
    .then(data => console.log('Logged in successfully'))
    .catch(error => console.error('Login failed:', error));
```

## Health Check

The auth service also provides a health check endpoint:

```http
GET /api/auth/health
```

Response:
```json
{
  "status": "ok",
  "service": "auth"
}
```

## Future Enhancements

1. **Password Authentication**: Add password field to User model and implement password verification
2. **Token Refresh**: Implement refresh token mechanism for long-term sessions
3. **Role-Based Access**: Add user roles and permissions
4. **Session Management**: Track active sessions and provide logout functionality
5. **Rate Limiting**: Add login attempt rate limiting for security
