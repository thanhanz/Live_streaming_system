# Livestreaming System

Spring Boot backend for a livestreaming platform. The project handles user/channel management, live streaming sessions, VOD upload and HLS transcoding, real-time chat, search, memberships, and payment integration.

## Frontend

The ReactJS frontend is available in the restored repository: [Live-streaming-ReactJs](https://github.com/thanhanz/Live-streaming-ReactJs).

## Main Purpose

This project is the backend for a streaming platform with two core content flows:

- Live streaming: a channel prepares a stream, publishes to RTMP, viewers watch via HLS, and viewer counts/chat update in real time.
- Video on demand: creators upload video metadata and raw video files, the system transcodes them to HLS, stores assets in Cloudflare R2, and exposes them through REST APIs.

It also includes:

- JWT authentication and Google social login
- Channel follow/unfollow
- Comment system for VODs
- Full-text search across livestreams, VODs, and channels
- Membership packages with VNPay payment callback handling
- Admin APIs for moderation and statistics

## Technologies

- Java 21
- Spring Boot 3.5.3
- Spring Web
- Spring Data JPA
- Spring Security OAuth2 Resource Server / Client
- Spring WebSocket + STOMP + SockJS
- Spring Data Redis
- Spring AMQP (RabbitMQ)
- Spring Data Elasticsearch
- Spring Mail
- Thymeleaf
- PostgreSQL
- Redis
- RabbitMQ
- Elasticsearch + Kibana
- NGINX RTMP
- FFmpeg
- Cloudflare R2 via AWS S3 SDK
- Docker / Docker Compose
- Maven

## High-Level Architecture

### Runtime services

- `live-stream-be`: main Spring Boot application, exposed on `http://localhost:8081/livestream`
- `postgres`: primary relational database
- `redis`: cache, viewer/session tracking, token blacklist support
- `rabbitmq`: async messaging for transcoding, search sync, and email events
- `elasticsearch`: search index
- `kibana`: Elasticsearch UI
- `rtmp-server`: RTMP ingest + HLS static serving

### Main data flow

1. Creator prepares a stream with `/api/stream/prepare`.
2. OBS publishes RTMP to `rtmp://localhost:1935/live/{streamKey}`.
3. NGINX RTMP calls backend hooks:
   - `/api/stream/on_publish`
   - `/api/stream/finish`
4. FFmpeg generates HLS output under the shared `hls/` volume.
5. Viewers watch HLS playlists from the RTMP container HTTP endpoint.
6. Viewer counts and live status are pushed through WebSocket/STOMP.
7. VOD raw uploads go to Cloudflare R2, then RabbitMQ triggers FFmpeg transcoding to HLS.
8. Search documents are synchronized to Elasticsearch through messaging events.

## Main Features

### Authentication and authorization

- Username/password login with JWT access token
- Refresh token flow backed by database
- Token blacklist on logout using Redis
- Google OAuth login callback
- Role-based authorization with `USER` and `ADMIN`

### User and channel management

- User registration
- Get current user / current user channel
- Create, update, delete channels
- Follow and unfollow channels
- Admin user moderation and banning

### Live streaming

- Prepare stream session and generate unique stream key
- Validate RTMP publish requests
- Track stream lifecycle: `PREPARING`, `STREAMING`, `FINISHED`
- Live stream discovery for homepage and by channel
- Stream history
- Admin statistics and stream banning
- Download recorded livestream file from R2 when available

### Real-time features

- STOMP WebSocket endpoint at `/websocket`
- Live chat per stream
- Ban users from a stream chat
- Live viewer concurrency tracking stored in Redis
- Broadcast live status changes to subscribers

### Video on demand

- Create VOD metadata
- Upload thumbnails to R2
- Direct upload to R2 using presigned URLs and multipart upload
- Async FFmpeg transcoding from raw video to HLS
- Browse VODs by channel, category, tag, or homepage feed
- Count views using Redis-assisted delayed acceptance logic
- Admin VOD listing, search, totals, and top viewed statistics

### Search

- Elasticsearch-backed multi-match search
- Searches title, description, and channel name
- Search index sync via RabbitMQ events

### Membership and payments

- Membership package CRUD
- User membership checks by channel
- Membership statistics per channel
- VNPay payment URL generation
- VNPay callback verification and membership activation

## Project Structure

```text
.
|-- docker-compose.yaml
|-- Dockerfile
|-- nginx-rtmp/
|   |-- Dockerfile
|   `-- nginx.conf
|-- src/main/java/com/thanhan/livestreaming_system/
|   |-- auth/            # login, JWT, refresh token, OAuth callback
|   |-- user/            # users, channels, roles, follows, ban events
|   |-- livestream/      # stream lifecycle, RTMP hooks, websocket viewer state
|   |-- video/           # VOD metadata, R2 uploads, transcoding, statistics
|   |-- chat/            # stream chat + moderation
|   |-- search_service/  # Elasticsearch indexing and querying
|   |-- membership/      # packages, subscriptions, payments
|   |-- comment/         # VOD comments and replies
|   |-- category/        # VOD categories
|   |-- tag/             # VOD tags
|   |-- configuration/   # security, Redis, RabbitMQ, R2, Elasticsearch, VNPay
|   `-- common/          # shared API response, exceptions, pagination
|-- src/main/resources/
|   |-- application.yaml
|   `-- templates/
|-- src/test/
`-- hls/                 # generated locally at runtime through mounted volume
```

## Important Configuration

The application reads most settings from `src/main/resources/application.yaml`.

### Default server settings

- Backend port: `8081`
- Context path: `/livestream`
- RTMP port: `1935`
- HLS HTTP port: `8082`

### Required environment variables

#### Database and cache

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`
- `RABBITMQ_HOST`

#### Cloudflare R2

- `R2_ENDPOINT`
- `R2_ACCESSKEY`
- `R2_SECRETKEY`
- `R2_PUBLIC_URL`

#### Google OAuth

- `OAUTH2-GOOGLE-CLIENT-ID`
- `OAUTH2-GOOGLE-CLIENT-SECRET`
- `GOOGLE_REDIRECT_URL` (optional, defaults to localhost frontend callback)

#### Email

- `EMAIL_ADDRESS`
- `EMAIL_PASSWORD`

#### VNPay

- `VNP_TMNCODE`
- `VNP_SECRET_KEY`

## Running the Project

### Option 1: Docker Compose

This is the intended local setup because it starts PostgreSQL, Redis, RabbitMQ, Elasticsearch, Kibana, RTMP, and the backend together.

1. Build the Spring Boot jar:

```bash
./mvnw clean package
```

2. Provide the required environment variables.

3. Start services:

```bash
docker compose up --build
```

### Option 2: Run backend locally

Prerequisites:

- Java 21
- Maven
- FFmpeg installed and available on `PATH`
- PostgreSQL
- Redis
- RabbitMQ
- Elasticsearch
- NGINX RTMP or equivalent RTMP/HLS server

Run:

```bash
./mvnw spring-boot:run
```

## Streaming Notes

- RTMP ingest URL is currently hardcoded in the service layer as `rtmp://localhost:1935/live/`.
- NGINX RTMP is configured to call the backend using the Docker service name `live-stream-be`.
- HLS files are served from `http://localhost:8082/hls/{streamKey}/...`
- The backend also mounts `./hls` as a shared volume for generated playlists and segments.

## WebSocket Notes

- STOMP endpoint: `/websocket`
- Application prefix: `/livestream/app`
- Broker topic prefix: `/livestream/topic`

Typical usage:

- Send viewer join: `/livestream/app/viewer/join`
- Send viewer heartbeat: `/livestream/app/viewer/heartbeat`
- Send chat message: `/livestream/app/chat/{streamId}/send`
- Subscribe viewer count: `/livestream/topic/viewers/{streamId}`
- Subscribe chat: `/livestream/topic/stream/{streamId}`

## Main REST API Areas

Base URL:

```text
http://localhost:8081/livestream
```

Main route groups:

- `/auth`
- `/api/users`
- `/api/channels`
- `/api/stream`
- `/api/vods`
- `/api/vods/upload`
- `/api/comments`
- `/api/categories`
- `/api/tags`
- `/api/search`
- `/api/membership-packages`
- `/api/membership`
- `/api/payment`

## Default Seed Data

On startup, the application creates default roles and an admin account if it does not already exist:

- Username: `admin`
- Password: `admin`

This comes from `ApplicationInitConfig`. Change it immediately outside local development.

## Observations From the Current Codebase

- `spring.jpa.hibernate.ddl-auto=update` is enabled, so schema changes are applied automatically.
- Some URLs and origins are still local-development oriented, especially `http://localhost:3000`.
- Elasticsearch security is disabled in `docker-compose.yaml`, while `application.yaml` still contains username/password fields.
- Livestream recording upload to R2 exists in code, but part of that flow is currently commented out.
- Automated test coverage is minimal at the moment.

## Development Priorities You May Want Next

- Replace hardcoded local URLs with environment-driven configuration
- Add Flyway or Liquibase for database migrations
- Expand integration and service tests
- Add API documentation with OpenAPI/Swagger
- Split transcoding workers from the main API process if load increases
