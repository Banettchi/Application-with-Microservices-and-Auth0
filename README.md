# 🐦 Twitly — Twitter-like Application with Auth0 & Spring Boot

A simplified Twitter-like application built as a **Spring Boot monolith** secured with **Auth0 (OAuth2/JWT)**. Users can log in, create posts of up to 140 characters, and view the public stream — all secured and documented with full Swagger/OpenAPI support.

---

## 📐 Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         FRONTEND (S3)                           │
│   HTML + Vanilla JS + Auth0 SPA JS SDK (CDN)                    │
│   - Login / Logout (redirect flow)                              │
│   - View public stream (GET /api/stream)                        │
│   - Create posts with JWT (POST /api/posts)                     │
└───────────────────────┬─────────────────────────────────────────┘
                        │ HTTPS + Bearer JWT
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│               BACKEND — Spring Boot Monolith :8080              │
│                                                                 │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────────┐     │
│  │ PostController│  │StreamController│  │ UserController   │     │
│  │ GET /api/posts│  │GET /api/stream │  │ GET /api/me      │     │
│  │POST /api/posts│  └───────────────┘  │ GET /api/me/posts│     │
│  └──────┬───────┘                      └───────┬──────────┘     │
│         │                                      │                 │
│  ┌──────▼───────────────────────────────────── ▼──────────┐     │
│  │              PostService / StreamService                │     │
│  └──────────────────────────┬──────────────────────────────┘     │
│                             │                               │     │
│  ┌──────────────────────────▼──────────────────────────┐   │     │
│  │         PostRepository (Spring Data JPA)            │   │     │
│  └──────────────────────────┬──────────────────────────┘   │     │
│                             │                               │     │
│  ┌──────────────────────────▼──────────────────────────┐   │     │
│  │               H2 In-Memory Database                 │   │     │
│  └─────────────────────────────────────────────────────┘   │     │
│                                                             │     │
│  🔐 Spring Security — OAuth2 Resource Server                │     │
│       - Validates JWT issuer + audience (Auth0)             │     │
│       - CORS configured for frontend origin                 │     │
│       - Swagger UI: /swagger-ui/index.html                  │     │
└─────────────────────────────────────────────────────────────────┘
                        │ JWKS validation
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                      AUTH0 TENANT                               │
│   - SPA Application (frontend)                                  │
│   - API Definition with Audience                                │
│   - Issues JWT access tokens                                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🗂 Project Structure

```
twitter-app/
├── backend/                          ← Spring Boot monolith
│   ├── src/main/java/com/twitterapp/
│   │   ├── TwitterBackendApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java   ← OAuth2 Resource Server + CORS
│   │   │   ├── AudienceValidator.java← Custom JWT audience check
│   │   │   └── OpenApiConfig.java    ← Swagger / OpenAPI setup
│   │   ├── controller/
│   │   │   ├── PostController.java   ← GET /api/posts, POST /api/posts
│   │   │   ├── StreamController.java ← GET /api/stream
│   │   │   └── UserController.java   ← GET /api/me, GET /api/me/posts
│   │   ├── model/
│   │   │   ├── Post.java             ← JPA entity (140-char constraint)
│   │   │   ├── User.java             ← Virtual model (from JWT claims)
│   │   │   └── Stream.java           ← Virtual model (global feed)
│   │   ├── repository/
│   │   │   └── PostRepository.java   ← JPA repository
│   │   ├── service/
│   │   │   ├── PostService.java
│   │   │   └── StreamService.java
│   │   ├── dto/
│   │   │   ├── CreatePostRequest.java
│   │   │   ├── PostResponse.java
│   │   │   ├── UserInfoResponse.java
│   │   │   └── StreamResponse.java
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java
│   ├── src/test/java/com/twitterapp/
│   │   └── TwitterBackendApplicationTests.java ← Integration tests
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── .env.example
│   └── pom.xml
│
└── frontend/                         ← Static web app (no build step)
    ├── index.html
    ├── style.css
    ├── app.js                        ← Auth0 SPA JS SDK + Fetch API
    └── .env.example
```

---

## 🔐 API Endpoints

| Method | Endpoint        | Auth Required | Description                          |
|--------|-----------------|:-------------:|--------------------------------------|
| GET    | `/api/posts`    | ❌ Public     | List all posts (newest first)        |
| POST   | `/api/posts`    | ✅ JWT        | Create a new post (max 140 chars)    |
| GET    | `/api/stream`   | ❌ Public     | Global public feed with metadata     |
| GET    | `/api/me`       | ✅ JWT        | Current user info from JWT claims    |
| GET    | `/api/me/posts` | ✅ JWT        | All posts by the current user        |

Full interactive documentation: **`http://localhost:8080/swagger-ui/index.html`**

---

## 🚀 Quick Start — Local Setup

### Prerequisites

- Java 21+
- Maven 3.9+
- An [Auth0](https://auth0.com) account (free tier works)

---

### 1. Auth0 Configuration

#### A. Create a SPA Application
1. Go to **Auth0 Dashboard → Applications → Create Application**
2. Choose **Single Page Application**
3. In settings, add to **Allowed Callback URLs**: `http://localhost:8080` (or wherever you serve the frontend)
4. Add to **Allowed Logout URLs** and **Allowed Web Origins** the same URL
5. Note your **Domain** and **Client ID**

#### B. Create an API
1. Go to **Auth0 Dashboard → APIs → Create API**
2. Set **Name**: `Twitter App API`
3. Set **Identifier (Audience)**: `https://twitter-app-api`
4. (Optional) Add scopes: `read:posts`, `write:posts`, `read:profile`
5. Note the **Audience** value

---

### 2. Backend Setup

```bash
cd twitter-app/backend
```

Edit `src/main/resources/application.properties` and replace:
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=https://YOUR_DOMAIN.us.auth0.com/
auth0.audience=https://twitter-app-api
```

Or set environment variables before running:
```powershell
$env:AUTH0_DOMAIN="YOUR_DOMAIN.us.auth0.com"
$env:AUTH0_AUDIENCE="https://twitter-app-api"
```

Start the backend:
```bash
mvn spring-boot:run
```

The backend starts at **http://localhost:8080**

✅ Swagger UI: http://localhost:8080/swagger-ui/index.html  
✅ H2 Console: http://localhost:8080/h2-console

---

### 3. Frontend Setup

Edit `frontend/app.js` — update the `CONFIG` object at the top:
```javascript
const CONFIG = {
  auth0Domain:   'YOUR_DOMAIN.us.auth0.com',  // from step 1A
  auth0ClientId: 'YOUR_CLIENT_ID',            // from step 1A
  auth0Audience: 'https://twitter-app-api',   // from step 1B
  apiBaseUrl:    'http://localhost:8080',
};
```

Serve the frontend locally (any static file server):
```bash
# Python (if available)
cd twitter-app/frontend
python -m http.server 3000

# Or just open index.html directly in your browser
```

---

### 4. Run Tests

```bash
cd twitter-app/backend
mvn test
```

Tests run with mock JWT (Spring Security Test), no real Auth0 connection needed.

---

## 🧪 Test Report

The integration test suite (`TwitterBackendApplicationTests.java`) covers:

| Test | Scenario | Expected Result |
|------|----------|-----------------|
| `givenNoAuth_whenGetPosts_thenReturn200` | GET /api/posts without token | 200 OK + list of posts |
| `givenNoAuth_whenGetStream_thenReturn200` | GET /api/stream without token | 200 OK + stream object |
| `givenNoAuth_whenPostPost_thenReturn401` | POST /api/posts without token | 401 Unauthorized |
| `givenNoAuth_whenGetMe_thenReturn401` | GET /api/me without token | 401 Unauthorized |
| `givenValidJwt_whenPostPost_thenReturn201` | POST /api/posts with valid JWT | 201 Created + post data |
| `givenValidJwt_whenGetMe_thenReturn200` | GET /api/me with valid JWT | 200 OK + user info |
| `givenValidJwt_whenPostWithContentOver140Chars_thenReturn400` | 141-char content | 400 Bad Request |
| `givenValidJwt_whenPostWithBlankContent_thenReturn400` | Empty content | 400 Bad Request |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.2, Java 21 |
| Security | Spring Security, OAuth2 Resource Server, JWT (Auth0) |
| Database | H2 In-Memory (dev) |
| API Docs | Springdoc OpenAPI 2.5 (Swagger UI) |
| Frontend | HTML5 + Vanilla JS (no build step) |
| Auth (Frontend) | Auth0 SPA JS SDK v2 (CDN) |
| HTTP Client | Fetch API (native browser) |

---

## 🔮 Next Steps (Microservices Migration)

The monolith is structured to facilitate a clean migration:

- **User Service** → extracts `UserController` + Auth0 integration → AWS Lambda
- **Posts Service** → extracts `PostController` + `PostService` + DB → AWS Lambda  
- **Stream Service** → extracts `StreamController` + `StreamService` → AWS Lambda (aggregator)

Each service would expose its endpoint via **AWS API Gateway** with Auth0 JWT authorizer.
