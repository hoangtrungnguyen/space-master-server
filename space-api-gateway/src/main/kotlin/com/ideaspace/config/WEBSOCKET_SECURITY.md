# WebSocket Security Implementation

This document explains how WebSocket connections are secured in the Space Master Server.

## Security Features Implemented

### 1. JWT Authentication
- All WebSocket connections require a valid JWT token
- Token must be passed as a query parameter: `?token=<your_jwt_token>`
- JWT tokens are verified against the configured secret, issuer, and audience
- User existence is validated against the database

### 2. Document Access Authorization
- Users must have permission to access the specific document
- Document existence is verified before allowing connection
- Authorization logic can be extended for team-based access control

### 3. Rate Limiting
- **Connection Rate Limiting**: Maximum 10 WebSocket connections per IP per minute
- **Message Rate Limiting**: Maximum 200 messages per IP per minute
- **General Rate Limiting**: Maximum 100 requests per IP per minute

### 4. Security Headers & Configuration
- Maximum frame size limited to 64KB to prevent DoS attacks
- Proper error handling with secure error messages
- Connection timeouts and ping/pong for connection health

### 5. Input Validation
- All incoming messages are validated and deserialized safely
- Malformed messages are handled gracefully without crashing

## How to Connect to Secure WebSocket

### 1. Obtain a JWT Token
First, authenticate with the API to get a JWT token:

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "your_password"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 123,
    "email": "user@example.com"
  }
}
```

### 2. Connect to WebSocket with Token
Use the token in the WebSocket connection URL:

```javascript
const token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
const documentId = "your-document-uuid";
const ws = new WebSocket(`ws://localhost:8080/ws/documents/${documentId}?token=${encodeURIComponent(token)}`);

ws.onopen = function(event) {
    console.log('Connected to document:', documentId);
};

ws.onmessage = function(event) {
    const data = JSON.parse(event.data);
    console.log('Received:', data);
};

ws.onerror = function(error) {
    console.error('WebSocket error:', error);
};

ws.onclose = function(event) {
    console.log('Connection closed:', event.code, event.reason);
};
```

### 3. Handle Connection Response
Upon successful connection, you'll receive a welcome message:

```json
{
  "type": "connection_established",
  "message": "Successfully connected to document your-document-uuid",
  "userId": 123
}
```

## Security Configuration

### JWT Configuration
Configure JWT settings in `application.yaml`:

```yaml
jwt:
  secret: ${JWT_SECRET:your-super-secret-jwt-key-change-this-in-production}
  issuer: ${JWT_ISSUER:space-master}
  audience: ${JWT_AUDIENCE:space-master-users}
  realm: "Space Master Server"
```

### Environment Variables
Set these environment variables in production:

```bash
JWT_SECRET=your-very-secure-secret-key-at-least-256-bits
JWT_ISSUER=space-master-production
JWT_AUDIENCE=space-master-users
```

## Error Handling

### Authentication Errors
- **Missing Token**: Connection closed with code `1008` and reason "Authentication failed"
- **Invalid Token**: Connection closed with code `1008` and reason "Authentication failed"
- **User Not Found**: Connection closed with code `1008` and reason "Authentication failed"

### Authorization Errors
- **No Document Access**: Connection closed with code `1008` and reason "Access denied to document"
- **Document Not Found**: Connection closed with code `1008` and reason "Access denied to document"

### Rate Limiting
- Connections exceeding rate limits will be rejected
- HTTP 429 status for REST endpoints
- WebSocket connections will be closed immediately if rate limit exceeded

## Best Practices

### Client-Side
1. **Store tokens securely** (not in localStorage in production)
2. **Handle token expiration** - implement token refresh logic
3. **Implement reconnection logic** with exponential backoff
4. **Validate server messages** before processing

### Server-Side Security Enhancements
1. **Use HTTPS/WSS in production**
2. **Implement proper CORS policies**
3. **Use strong JWT secrets** (minimum 256 bits)
4. **Monitor connection patterns** for abuse
5. **Implement document-level permissions**

## Authorization Extension Points

To implement more sophisticated authorization:

1. **Team-based access**: Check if user is member of document's team
2. **Role-based access**: Implement read/write permissions
3. **Document sharing**: Support public/private documents
4. **Audit logging**: Log all access attempts

Example extension in `WebSocketAuth.kt`:

```kotlin
suspend fun validateDocumentAccess(
    call: ApplicationCall,
    userId: Long,
    docUuid: String
): Boolean {
    val documentRepo = call.application.dependencies.resolve<CrudDocumentRepository>()
    val document = documentRepo.findByIdUuid(docUuid) ?: return false
    
    // Check if user owns the document
    if (document.ownerId == userId) return true
    
    // Check if user is team member
    val teamRepo = call.application.dependencies.resolve<TeamRepository>()
    val isTeamMember = teamRepo.isUserMemberOfDocumentTeam(userId, document.id)
    
    // Check if document is publicly shared
    val isPublic = document.isPublic
    
    return isTeamMember || isPublic
}
```

## Monitoring and Debugging

### Connection Logs
The server logs all WebSocket connection events:
- Authentication attempts
- Authorization checks
- Connection establishment/termination
- Message processing errors

### Health Checks
Monitor WebSocket health:
- Connection count per document
- Message rate per user
- Error rates
- Authentication failure rates

This security implementation provides a robust foundation for secure real-time document collaboration.
