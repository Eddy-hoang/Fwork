<p align="center">
  <img src="./assets/logo.png" alt="Fwork - Workspace & Kanban" width="460" />
</p>

<h1 align="center">Fwork — Enterprise Real-time Kanban Collaboration Platform</h1>

<p align="center">
  <strong>Nền tảng quản lý công việc và Kanban cộng tác thời gian thực cho doanh nghiệp</strong>
</p>

<p align="center">
  <a href="./PROJECT_REPORT.md"><img src="https://img.shields.io/badge/Build-Passing-brightgreen.svg" alt="Build Status" /></a>
  <a href="https://openjdk.org/projects/jdk/21/"><img src="https://img.shields.io/badge/Java-21-orange.svg" alt="Java Version" /></a>
  <a href="https://spring.io/projects/spring-boot"><img src="https://img.shields.io/badge/Spring%20Boot-4.1.0-green.svg" alt="Spring Boot" /></a>
  <a href="https://react.dev/"><img src="https://img.shields.io/badge/React-19.2-blue.svg" alt="React" /></a>
  <a href="https://vitejs.dev/"><img src="https://img.shields.io/badge/Vite-8.0-purple.svg" alt="Vite" /></a>
  <a href="https://www.postgresql.org/"><img src="https://img.shields.io/badge/PostgreSQL-16-blue.svg" alt="PostgreSQL" /></a>
  <a href="https://redis.io/"><img src="https://img.shields.io/badge/Redis-7-red.svg" alt="Redis" /></a>
</p>

---

## 📌 Giới thiệu dự án (Project Overview)

**Fwork** là hệ sinh thái quản lý công việc và Kanban cộng tác thời gian thực chuẩn doanh nghiệp, được xây dựng trên nền tảng **Spring Boot 4.1 + Java 21** và **React 19 + Vite 8**. Dự án giải quyết trọn vẹn luồng cộng tác hiện đại từ web đến trình duyệt:

- **Hệ thống Kanban đa tầng**: Quản lý theo cấu trúc `Workspace` ➔ `Board` ➔ `Column` ➔ `Task`, kéo thả mượt mà với `@dnd-kit`, tự động chuyển trạng thái khi hoàn tất.
- **Đồng bộ thời gian thực (Zero-latency Real-time)**: Kiến trúc STOMP WebSocket broker cập nhật mọi thay đổi bảng tức thì cho tất cả thành viên mà không cần tải lại trang.
- **Trợ lý AI thông minh**: Tích hợp Google Gemini AI hỗ trợ tự động phân rã đầu việc lớn (`AI Breakdown`) và sinh nhanh danh sách nhiệm vụ từ mô tả mục tiêu.
- **Fwork Companion (Chrome Extension)**: Tiện ích mở rộng (Manifest V3) cho phép người dùng lưu nhanh trang web thành task kèm Google Sign-In và chọn board/cột đích.
- **Kiến trúc chịu tải & Bảo mật**: Phân quyền ma trận chặt chẽ (RBAC), chống N+1 queries với JPA Dynamic `@EntityGraph`, 2 tầng cache Redis và cơ chế giới hạn tải phân tán Sliding Window.

---

##  Mục lục

1. [Tổng quan kiến trúc & Điểm nhấn kỹ thuật](#-tổng-quan-kiến-trúc--điểm-nhấn-kỹ-thuật)
2. [Tính năng hệ thống](#-tính-năng-hệ-thống)
3. [Tech Stack & Lựa chọn công nghệ](#-tech-stack--lựa-chọn-công-nghệ)
4. [Kiến trúc hệ thống & Sơ đồ UML](#-kiến-trúc-hệ-thống--sơ-đồ-uml)
5. [Cấu trúc thư mục](#-cấu-trúc-thư-mục)
6. [Thiết lập môi trường (.env)](#-thiết-lập-môi-trường-env)
7. [Hướng dẫn khởi chạy (Local & Docker)](#-hướng-dẫn-khởi-chạy)
8. [API Documentation & Swagger](#-api-documentation--swagger)
9. [Kiểm thử & Đảm bảo chất lượng (Test Suites)](#-kiểm-thử--đảm-bảo-chất-lượng)

---

##  Tổng quan kiến trúc & Điểm nhấn kỹ thuật

Thay vì triển khai theo lối CRUD thông thường, Fwork được thiết kế theo tiêu chuẩn production của các nền tảng SaaS hiện đại:

- **State Sync & Real-time via STOMP Broker**: Thay vì polling liên tục, mọi thay đổi trên Kanban board (move task, update priority, assign, add label, comment) được broadcast qua channel `/topic/boards/{boardId}` với payload định danh rõ ràng. Kết hợp WebSocket Channel Interceptor để validate JWT token ngay tại frame `CONNECT` và check quyền Workspace khi `SUBSCRIBE`.
- **Database Query Tuning & Zero N+1**: Sử dụng dynamic `@EntityGraph` và batch GROUP BY queries cho các liên kết phân cấp (`Workspace` ➔ `Board` ➔ `Column` ➔ `Task`), loại bỏ hoàn toàn bẫy N+1 query thường gặp trong JPA. Cột `metadata` dạng JSONB kèm index tối ưu cho activity log.
- **Distributed Rate Limiting (Sliding Window)**: Triển khai thuật toán Sliding Window Counter bằng Redis `ZSET`, tự động ngắt tải khi client vượt ngưỡng (HTTP 429) và trang bị cơ chế fallback sang In-Memory Concurrent HashMap khi cụm Redis gặp sự cố.
- **Fail-safe Audit Logging**: Ghi nhận toàn bộ hành động nhạy cảm (phân quyền, chuyển giao quyền sở hữu Workspace, mời thành viên) với `Propagation.REQUIRES_NEW`, đảm bảo log kiểm toán luôn được lưu độc lập dù nghiệp vụ chính thành công hay rollback.
- **Two-tier Caching Layer**: Bộ đệm Redis kết hợp Spring Cache cho Workspace, Board và Metrics, giúp giảm tới 80% áp lực đọc trực tiếp từ PostgreSQL.

---

##  Tính năng hệ thống

### 1. Kanban Engine & Tương tác kéo thả
- Phân cấp quản lý đa tầng: `Workspace` ➔ `Board` ➔ `Column` ➔ `Task` ➔ `Checklist / Comment / Label`.
- Kéo thả thẻ và sắp xếp vị trí cột mượt mà với `@dnd-kit/core` và `@dnd-kit/sortable`.
- Logic thông minh: Tự động chuyển thẻ vào cột Hoàn thành khi check hoàn tất và khôi phục vị trí cũ khi bỏ tick.

### 2. Đồng bộ thời gian thực (Real-time Broadcast)
- Broadcast tức thì mọi cập nhật bảng công việc qua WebSocket.
- Người dùng cùng board nhận được update ngay lập tức mà không cần F5/tải lại trang.

### 3. Trợ lý lập kế hoạch bằng AI (Gemini AI)
- Tích hợp Spring AI với Google Gemini:
  - Tự động sinh danh sách công việc (`Generate tasks`) từ mô tả mục tiêu dự án.
  - Phân rã nhiệm vụ phức tạp (`AI Breakdown`) thành các checklist nhỏ gọn với độ ưu tiên logic.

### 4. Kiểm soát phân quyền tập trung (RBAC & Security)
- Ma trận phân quyền 3 cấp độ: `OWNER`, `ADMIN`, `MEMBER` quản lý qua `PermissionService`.
- Token mời vào Workspace được băm **SHA-256**, tự động vô hiệu hóa sau 7 ngày hoặc khi đã chấp thuận.
- Tự động bảo vệ trước các hành vi leo quyền (phòng chống IDOR, kiểm tra quyền sở hữu trước mỗi transaction).

### 5. Activity Tracking & Metrics
- Lưu lại nhật ký thay đổi chi tiết: ai đã đổi trạng thái thẻ nào, gán nhãn gì, bình luận lúc nào.
- Hỗ trợ xem phân trang activity log và xuất số liệu giám sát qua Micrometer Prometheus `/actuator`.

---

## Tech Stack & Lựa chọn công nghệ

| Thành phần | Công nghệ | Phiên bản | Ghi chú kiến trúc |
| :--- | :--- | :--- | :--- |
| **Backend Core** | Java, Spring Boot | 21 / 4.1.0 | Spring Security 6, Spring Data JPA |
| **Database** | PostgreSQL | 16 | Indexing `board_id`, `created_at`, lưu trữ JSONB |
| **Caching & Limiter** | Redis | 7 | Caching `@Cacheable`, Sliding Window Rate Limiting |
| **Real-time Protocol**| WebSocket + STOMP | — | SockJS Fallback, Channel Interceptor Security |
| **AI Integration** | Spring AI | 2.0.0 | Google Gemini SDK |
| **Frontend Core** | React, Vite | 19.2 / 8.0 | React Router v7, ES Modules |
| **Styling** | TailwindCSS | 4.3 | Custom Material Design 3 Palette & Glassmorphism |
| **Drag and Drop** | dnd-kit | 6.3 | Sensor abstraction, keyboard & touch accessibility |
| **API Client** | Axios | 1.17 | Interceptors xử lý tự động JWT Bearer & Refresh |

---

## Kiến trúc hệ thống & Sơ đồ UML

### 1. Kiến trúc phân tầng (High-Level Architecture)

```mermaid
graph TD
    Client[React 19 Frontend - TailwindCSS] -->|HTTP REST / Axios| Gateway[Spring Security 6 - Rate Limiter]
    Client -->|WebSocket STOMP| WSBroker[WebSocket Channel Interceptor]
    
    Gateway -->|JWT Validation| AuthFilter[JwtAuthenticationFilter]
    AuthFilter -->|Authorize Request| Controllers[Spring Boot Controllers]
    
    Controllers -->|Permission Check| PermService[PermissionService RBAC]
    Controllers -->|Business Logic| Services[Service Layer]
    
    Services -->|Cache Reads/Evicts| Redis[(Redis 7 Cache / ZSET)]
    Services -->|PostgreSQL Queries| Database[(PostgreSQL 16 DB)]
    
    Services -->|Async Event Publisher| EventBus[Transactional Event Listener]
    EventBus -->|Async Audit Log| AuditDB[(Audit Log - REQUIRES_NEW)]
    EventBus -->|Push Broadcast| WSBroker
```

### 2. Sơ đồ dữ liệu thực thể (Class Diagram)

```mermaid
classDiagram
    direction TB
    
    class User {
        +UUID id
        +String name
        +String email
        +String passwordHash
        +String avatar
        +Role role
        +LocalDateTime createdAt
    }

    class Workspace {
        +UUID id
        +String name
        +String slug
        +String description
        +boolean isArchived
        +User createdBy
        +List~WorkspaceMember~ members
        +List~Board~ boards
    }

    class WorkspaceMember {
        +UUID id
        +Workspace workspace
        +User user
        +WorkspaceRole role
        +LocalDateTime joinedAt
    }

    class Board {
        +UUID id
        +String title
        +String description
        +String color
        +Integer position
        +boolean isArchived
        +Workspace workspace
        +List~BoardColumn~ columns
    }

    class BoardColumn {
        +UUID id
        +String name
        +Integer position
        +Board board
        +List~Task~ tasks
    }

    class Task {
        +UUID id
        +String title
        +String description
        +Priority priority
        +LocalDateTime dueDate
        +Integer position
        +BoardColumn column
        +User assignee
        +Set~Label~ labels
        +boolean isArchived
    }

    class Label {
        +UUID id
        +String name
        +String color
        +Board board
    }

    class Comment {
        +UUID id
        +String content
        +Task task
        +User createdBy
    }

    class TaskActivity {
        +UUID id
        +UUID boardId
        +User actor
        +String actionType
        +String targetType
        +UUID targetId
        +String description
        +Map metadata
        +LocalDateTime createdAt
    }

    Workspace "1" *-- "many" WorkspaceMember : contains
    Workspace "1" *-- "many" Board : contains
    WorkspaceMember "many" --> "1" User : maps to
    Board "1" *-- "many" BoardColumn : contains
    Board "1" *-- "many" Label : owns
    BoardColumn "1" *-- "many" Task : contains
    Task "many" o-- "many" Label : categorized by
    Task "1" *-- "many" Comment : has
    TaskActivity "many" --> "1" User : actor
```

### 3. Quy trình kéo thả & Broadcast thời gian thực (Activity Diagram)

```mermaid
flowchart TD
    Start([Bắt đầu thao tác]) --> Drag[Người dùng kéo thẻ Task]
    Drag --> Drop[Thả thẻ sang cột hoặc vị trí mới]
    Drop --> PermCheck{Kiểm tra quyền truy cập}
    
    PermCheck -->|Từ chối| Reject[Rollback vị trí thẻ trên UI]
    Reject --> End1([Kết thúc])
    
    PermCheck -->|Hợp lệ| SaveDB[Cập nhật ColumnID & Position vào DB]
    SaveDB --> TxSuccess{Lưu thành công?}
    
    TxSuccess -->|Thất bại| ErrNotify[Thông báo lỗi & Revert UI]
    ErrNotify --> End1
    
    TxSuccess -->|Thành công| PublishEvent[Bắn TaskMovedEvent]
    PublishEvent --> WSPush[WebSocket Broadcast tới /topic/boards/id]
    WSPush --> SyncClients[Đồng bộ tức thì trên toàn bộ Client thành viên]
    SyncClients --> End2([Hoàn tất])
```

---

## Cấu trúc thư mục

```text
Fwork/
├── backend/                         # Spring Boot Application
│   ├── src/main/java/com/intern/fwork/
│   │   ├── config/                  # Security, Redis, WebSocket, OpenAPI config
│   │   ├── controllers/             # REST API Controllers
│   │   ├── dtos/                    # Request & Response Contracts
│   │   ├── entities/                # JPA Domain Entities
│   │   ├── enums/                   # RBAC Roles, Priority definitions
│   │   ├── exceptions/              # Global REST Exception Handler
│   │   ├── ratelimit/               # Sliding Window Redis & In-Memory Fallback
│   │   ├── repositories/            # Spring Data Repositories & EntityGraphs
│   │   ├── security/                # JwtFilter, SecurityUtils, CustomUserDetails
│   │   └── services/                # Business & RBAC Services
│   └── src/test/java/               # 54 Integration & Security Matrix Tests
├── frontend/                        # React 19 Client SPA
│   ├── src/
│   │   ├── components/              # Board, Task, Layout, UI Primitives
│   │   ├── context/                 # Auth & Language Contexts
│   │   ├── hooks/                   # useBoard, useWorkspace Custom Hooks
│   │   ├── lib/                     # Axios Instance & STOMP Client
│   │   ├── pages/                   # Dashboard, Kanban Board, Settings, Login
│   │   └── index.css                # Material Design 3 Design Tokens
│   ├── index.html                   # HTML Entry
├── extension/                       # Fwork Companion Chrome Extension (Manifest V3)
│   ├── public/                      # Manifest, icons & background scripts
│   ├── src/                         # Popup UI (Vite + React 19 + TypeScript)
│   └── package.json
├── docker-compose.yml               # Multi-container orchestration (DB, Redis, App)
└── README.md
```

---

## Thiết lập môi trường (.env)

Tạo file `.env` tại thư mục gốc dự án:

```env
# DATABASE POSTGRESQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=Fwork
DB_USERNAME=postgres
DB_PASSWORD=postgres

# REDIS CACHE & RATE LIMITER
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT AUTHENTICATION
JWT_SECRET=B9KWlBRILyT59ftELaWsuZsXQb9/piaorNS/SuX4IMzgTFNphg7wVfzhfasqpWehb83ApfZee3gX1/UV1EMxoA==
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# GOOGLE GEMINI AI
GEMINI_API_KEY=your_gemini_api_key_here
```

---

## Hướng dẫn khởi chạy

### Phương án 1: Chạy bằng Docker Compose (Khuyên dùng)

Yêu cầu môi trường: **Docker Engine** và **Docker Compose**.

```bash
# 1. Clone source code
git clone https://github.com/Eddy-hoang/Fwork.git
cd Fwork

# 2. Khởi tạo môi trường
cp .env.example .env

# 3. Build và khởi chạy toàn bộ dịch vụ
docker compose up --build -d
```

Các endpoint sau khi hệ thống sẵn sàng:
- **Frontend App:** `http://localhost:3000`
- **Backend API:** `http://localhost:8080/api`
- **Swagger Documentation:** `http://localhost:8080/swagger-ui/index.html`
- **Actuator Health & Metrics:** `http://localhost:8080/actuator/health`

---

### Phương án 2: Khởi chạy thủ công từng phần (Development)

#### 1. Chạy PostgreSQL & Redis (Docker):
```bash
docker run --name fwork-postgres -e POSTGRES_DB=Fwork -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16
docker run --name fwork-redis -p 6379:6379 -d redis:7
```

#### 2. Khởi chạy Backend (Spring Boot):
```bash
cd backend
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

#### 3. Khởi chạy Frontend (Vite):
```bash
cd frontend
npm install
npm run dev
```

#### 4. Cài đặt tiện ích Fwork Companion (Chrome Extension):

Cài đặt tiện ích trực tiếp vào các trình duyệt nhân Chromium (Google Chrome, Microsoft Edge, Brave, Cốc Cốc):

- **Cách 1: Nạp từ thư mục dist có sẵn (Nhanh nhất)**:
  1. Mở trình duyệt, truy cập `chrome://extensions/`
  2. Bật **Developer mode** (Chế độ cho nhà phát triển) ở góc trên bên phải.
  3. Bấm **Load unpacked** (Tải tiện ích đã giải nén) ➔ Chọn thư mục `extension/dist`.

- **Cách 2: Đóng gói và cài đặt từ file Zip**:
  1. Build & đóng gói file nén:
     ```bash
     cd extension
     npm install
     npm run pack
     ```
  2. File zip hoàn chỉnh sẽ nằm tại `extension/fwork-companion.zip`.
  3. Giải nén file zip và nạp vào trình duyệt bằng nút **Load unpacked** tại `chrome://extensions/`.

> 💡 **Cấu hình Server:** Mặc định extension kết nối tới Cloud (Render Backend & Vercel Web). Để trỏ về Localhost khi phát triển, mở popup extension ➔ bấm icon **Bánh răng (⚙️)** và nhập `http://localhost:8080/api`.

---

## API Documentation & Swagger

Tài liệu API được sinh tự động chuẩn OpenAPI 3.0 tại:
`http://localhost:8080/swagger-ui/index.html`

### Danh sách Endpoint tiêu biểu:

| Phương thức | Đường dẫn | Chức năng | Phân quyền yêu cầu |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Đăng ký tài khoản | Public |
| `POST` | `/api/auth/login` | Đăng nhập & lấy Bearer JWT | Public |
| `GET` | `/api/workspaces` | Lấy danh sách Workspace của User | Authenticated |
| `POST` | `/api/workspaces/{id}/members` | Thêm thành viên vào Workspace | Admin / Owner |
| `GET` | `/api/boards/{id}` | Lấy chi tiết Board, Cột & Thẻ công việc | Workspace Member |
| `PATCH`| `/api/tasks/{id}/move` | Kéo thả di chuyển trạng thái Task | Workspace Member |
| `POST` | `/api/boards/{id}/ai/generate-tasks` | Sinh task tự động qua Gemini AI | Workspace Member |
| `GET` | `/api/boards/{id}/activities` | Xem lịch sử hoạt động (phân trang) | Workspace Member |

---

## Kiểm thử & Đảm bảo chất lượng (Test Suites)

Hệ thống được bảo vệ bởi **54 Automated Integration Tests** bao phủ các luồng nghiệp vụ cốt lõi:

```text
[INFO] Results:
[INFO] Tests run: 54, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Các kịch bản kiểm thử trọng điểm:
- **`SecurityMatrixIntegrationTest`**: Kiểm thử ma trận phân quyền 3 cấp (Owner, Admin, Member), chặn triệt để hành vi leo quyền IDOR.
- **`TaskPaginationIntegrationTest`**: Đo đếm số lượng câu truy vấn Hibernate, kiểm tra dứt điểm lỗi N+1 query.
- **`CacheIntegrationTest`**: Xác thực tính đúng đắn của chu kỳ cache `@Cacheable` và dọn dẹp cache `@CacheEvict`.
- **`RateLimitIntegrationTest`**: Kiểm thử giới hạn tần suất gửi request, chặn spam và tấn công từ chối dịch vụ.
- **`KanbanEndToEndIntegrationTest`**: Kiểm thử toàn bộ vòng đời của một Task từ khởi tạo ➔ kéo thả chuyển cột ➔ hoàn thành ➔ lưu vết audit log.

---
*Tài liệu kỹ thuật nội bộ Fwork — Maintainer: Eddy Hoang*
