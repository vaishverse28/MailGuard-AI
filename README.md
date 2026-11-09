<<<<<<< HEAD
# MailGuard AI - Backend

Java 17 + Spring Boot 3 + MySQL REST API.
Flow: **Frontend -> Backend -> Python AI Service -> MySQL -> Frontend**

## 1. Setup

1. Install Java 17+, Maven, MySQL.
2. (Optional) Run `database/schema.sql` in MySQL. Nahi keli tari chalte - app `mailguard_db` ani tables auto-create karto.
3. `src/main/resources/application.properties` madhye MySQL password set kar
   (kinva env variable: `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`).
4. Run:
   ```bash
   mvn spring-boot:run
   ```
   Backend: `http://localhost:8080`

### AI service mode
| Property | Meaning |
|---|---|
| `ai.service.mock=true` | Dummy keyword-based AI response (Member 3 chi service ready nasel tevha) |
| `ai.service.mock=false` | Real call: `POST {ai.service.url}/analyze` (30s timeout) |

> Final integration veli `ai.service.mock=false` kara visru nako.

### CORS
Default allowed: `http://127.0.0.1:5500`, `http://localhost:5500` (VS Code Live Server).
Badalayche asel tar `cors.allowed-origins` change kar.

## 2. Authentication
Login nantar milalela `token` pudhchya saglya `/api/emails/**` ani `/api/dashboard/**` requests madhye pathva:
```
Authorization: Bearer <token>
```
Har user fakt aapla data baghto (history / stats user-wise).

## 3. API Reference

| Method | Endpoint | Auth | Success | Errors |
|---|---|---|---|---|
| POST | `/api/auth/register` | No | 201 | 400 |
| POST | `/api/auth/login` | No | 200 | 400, 401 |
| POST | `/api/emails/analyze` | Yes | 201 | 400, 401, 503 |
| GET | `/api/emails/history?search=&status=&sort=` | Yes | 200 | 400, 401 |
| GET | `/api/emails/{id}` | Yes | 200 | 401, 404 |
| DELETE | `/api/emails/{id}` | Yes | 200 | 401, 404 |
| GET | `/api/dashboard/stats` | Yes | 200 | 401 |

### Register
```json
POST /api/auth/register
{ "fullName": "Vaishnav Salunke", "email": "user@example.com", "password": "Test@1234" }
```
201:
```json
{ "success": true, "message": "Registration successful",
  "user": { "id": 1, "fullName": "Vaishnav Salunke", "email": "user@example.com" } }
```

### Login
```json
POST /api/auth/login
{ "email": "user@example.com", "password": "Test@1234" }
```
200:
```json
{ "success": true, "message": "Login successful",
  "user": { "id": 1, "fullName": "Vaishnav Salunke", "email": "user@example.com" },
  "token": "eyJhbGciOi..." }
```

### Analyze
```json
POST /api/emails/analyze
{ "senderEmail": "support@paypa1-security.com",
  "subject": "Urgent: Verify Your Account",
  "body": "Your account will be suspended. Click here to verify..." }
```
201:
```json
{ "id": 101, "senderEmail": "support@paypa1-security.com",
  "subject": "Urgent: Verify Your Account",
  "status": "PHISHING", "riskScore": 92,
  "suspiciousWords": ["Urgent", "Verify Your Account", "suspended"],
  "explanation": "Sender domain looks fake and email creates urgency...",
  "recommendations": ["Do not click any links.", "Never share your password.", "Verify the sender before replying."],
  "createdAt": "2026-11-09T10:30:00" }
```
`status` values: `SAFE`, `SPAM`, `PROMOTIONAL`, `PHISHING`.

### History
`GET /api/emails/history` -> array (latest first).
Optional params:
- `search` - subject / sender madhye shodhto
- `status` - `SAFE | SPAM | PROMOTIONAL | PHISHING` (kinva `ALL`)
- `sort` - `latest` (default), `oldest`, `risk_desc`, `risk_asc`

`GET /api/emails/{id}` -> same JSON + `body` field.
`DELETE /api/emails/{id}` -> `{ "success": true, "message": "Email deleted successfully" }`

### Dashboard
`GET /api/dashboard/stats`
```json
{ "totalScanned": 12, "safe": 5, "spam": 3, "promotional": 2, "phishing": 2,
  "threatDetectionRate": 41.7,
  "weekly": [ { "day": "Mon", "date": "2026-11-03", "safe": 1, "spam": 0, "promotional": 0, "phishing": 0 } ],
  "recent": [ { "id": 101, "subject": "...", "sender": "...", "status": "SPAM", "createdAt": "..." } ] }
```
- `threatDetectionRate` = (SPAM + PHISHING) / totalScanned x 100
- `weekly` = last 7 days (nehmi 7 items, scan nasel tar 0)
- `recent` = latest 5 scans

### Error format (saglya errors)
```json
{ "success": false, "status": 400, "message": "Email body cannot be empty" }
```
| Code | When |
|---|---|
| 400 | Validation error / duplicate email / invalid param |
| 401 | Wrong login / token missing, invalid or expired |
| 404 | Email id not found |
| 503 | AI service down / timeout / invalid AI response |
| 500 | Unexpected server error |

## 4. AI Service contract (Member 3)
Backend call karto: `POST {ai.service.url}/analyze`
```json
{ "senderEmail": "...", "subject": "...", "body": "..." }
```
Expected response:
```json
{ "status": "SPAM", "riskScore": 78, "suspiciousWords": ["..."], "explanation": "...", "recommendations": ["..."] }
```
`riskScore` 0-100 (out of range asel tar clamp hoto). `status` invalid asel tar 503 return hoto.

## 5. Postman
Import `postman/MailGuard_AI.postman_collection.json`. Requests order madhye run kar (1 -> 17);
token ani email id automatically save hotat. Success + validation + 401/404 cases included.

## 6. Frontend example
```js
const res = await fetch("http://localhost:8080/api/emails/analyze", {
  method: "POST",
  headers: { "Content-Type": "application/json", "Authorization": "Bearer " + token },
  body: JSON.stringify({ senderEmail, subject, body })
});
const data = await res.json();   // check res.ok, error madhye data.message
```

## 7. Project structure
```
src/main/java/com/mailguard/
  MailGuardApplication.java
  controller/  AuthController, EmailController, DashboardController
  service/     AuthService, EmailService, AiClientService, DashboardService
  repository/  UserRepository, EmailAnalysisRepository
  model/       User, EmailAnalysis, EmailStatus, StringListConverter
  dto/         request/response records
  config/      CorsConfig, WebConfig, AppConfig
  security/    JwtUtil, AuthInterceptor
  exception/   ApiException, GlobalExceptionHandler
```
=======
## MailGuard-AI system
A web application that analyzes user-submitted emails to detect whether they are SAFE, SPAM, PROMOTIONAL, or PHISHING. Along with the classification result, the system generates a Risk Score, an AI Explanation, a list of Suspicious Words, and Safety Recommendations
>>>>>>> ccacbfee4bc71f7918a0a2df758d291b685b9d53
