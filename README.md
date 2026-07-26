# Co-Learning 伴学平台

> 面向备考学生的公开伴学社区 - 技术演示级别响应式 Web 应用

## 技术栈

- **后端**: Java 21 + Spring Boot 3.x + Maven
- **前端**: Vue 3 + TypeScript + Vite + Naive UI
- **数据库**: PostgreSQL 16
- **缓存/实时状态**: Redis 7
- **对象存储**: MinIO (S3 兼容)
- **邮件测试**: MailHog
- **部署**: Docker Compose

## 快速开始

### 1. 启动基础设施

```bash
docker compose up -d
```

服务端口:
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- MinIO Console: `localhost:9001`
- MinIO API: `localhost:9000`
- MailHog SMTP: `localhost:1025`
- MailHog Web UI: `localhost:8025`

### 2. 启动后端

```bash
cd backend
./mvnw spring-boot:run
```

后端运行在 `http://localhost:8080`，Swagger UI 在 `http://localhost:8080/swagger-ui.html`

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`

## 项目结构

```
co-learning/
├── backend/          # Spring Boot 后端
├── frontend/         # Vue 3 前端
├── e2e/              # Playwright 端到端测试
├── deploy/           # 部署配置 (Nginx, MinIO, PostgreSQL)
├── docs/             # 项目文档
└── docker-compose.yml
```

## 开发阶段

1. **工程基础** - 项目脚手架、认证、用户基础
2. **个人学习闭环** - 专注计时、学习统计、日志
3. **公开陪伴房** - WebSocket 实时房间、在线状态
4. **成长和竞争** - 排行榜、宠物、成就
5. **AI 和治理** - AI 日志分析、举报、管理后台

---

## 本地开发详细指南

### 前置条件

- **Java 21** (JDK)
- **Node.js 20+** (LTS)
- **Docker** + **Docker Compose** (用于启动基础设施)
- **Maven** (已内置于 `mvnw`，无需单独安装)

### 环境变量配置

1. 复制环境变量模板：
```bash
cp .env.example .env
```

2. 根据需要修改 `.env` 文件，关键变量说明：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `POSTGRES_DB` | 数据库名称 | `colearning` |
| `POSTGRES_USER` | 数据库用户名 | `colearning` |
| `POSTGRES_PASSWORD` | 数据库密码 | `colearning_dev` |
| `DB_HOST` | 数据库主机 | `localhost` |
| `DB_PORT` | 数据库端口 | `5432` |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `MINIO_ENDPOINT` | MinIO API 地址 | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | MinIO 访问密钥 | `minio` |
| `MINIO_SECRET_KEY` | MinIO 密钥 | `minio123` |
| `MINIO_BUCKET_AVATARS` | 头像存储桶 | `avatars` |
| `MINIO_BUCKET_PETS` | 宠物图片存储桶 | `pets` |
| `MAIL_HOST` | SMTP 邮件服务器 | `smtp.yeah.net` |
| `MAIL_PORT` | SMTP 端口 | `25` |
| `MAIL_USERNAME` | 邮件用户名 | - |
| `MAIL_PASSWORD` | 邮件密码/授权码 | - (必填) |
| `MAIL_FROM` | 发件人地址 | `noreply@colearning.local` |
| `JWT_SECRET` | JWT 签名密钥 (≥256 bit) | 见 `.env.example` |
| `JWT_ACCESS_TTL` | Access Token 有效期(秒) | `900` (15分钟) |
| `JWT_REFRESH_TTL` | Refresh Token 有效期(秒) | `604800` (7天) |
| `APP_BASE_URL` | 前端访问地址 | `http://localhost:5173` |
| `BACKEND_PORT` | 后端端口 | `8080` |
| `VITE_API_BASE_URL` | 前端 API 地址 | `http://localhost:8080` |
| `VITE_WS_BASE_URL` | 前端 WebSocket 地址 | `ws://localhost:8080` |
| `SPRING_PROFILES_ACTIVE` | Spring 激活的 profile | `dev` |

> **注意**：`MAIL_PASSWORD` 是邮件发送功能的必需配置。开发环境使用 MailHog 时可留空；使用真实 SMTP 时需填写对应邮箱的授权码。

### 步骤 1：启动基础设施

```bash
docker compose up -d
```

启动后验证所有服务是否正常：

```bash
docker compose ps
```

### 步骤 2：启动后端

```bash
cd backend
./mvnw spring-boot:run          # Linux/macOS
.\mvnw.cmd spring-boot:run      # Windows
```

后端启动后：
- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 文档: `http://localhost:8080/v3/api-docs`

### 步骤 3：启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，开发模式下通过 Vite 代理将 `/api` 请求转发到后端。

### 数据库迁移 (Flyway)

项目使用 **Flyway** 进行数据库版本管理。迁移脚本位于：

```
backend/src/main/resources/db/migration/
├── V1__init_auth.sql                          # 用户认证表
├── V2__init_user_profile.sql                  # 用户资料、屏蔽
├── V3__init_study.sql                         # 学习模块(科目/任务/专注/打卡)
├── V4__init_journal.sql                       # 日志模块
├── V5__init_room.sql                          # 陪伴房间模块
├── V6__init_gamification.sql                  # 游戏化(经验值/宠物/成就/道具商店)
├── V7__daily_tasks.sql                        # 每日任务系统
├── V8__more_achievements.sql                  # 更多成就定义
├── V9__localize_and_rebalance_pet_items.sql   # 道具中文化及价格平衡
├── V10__focus_session_grace_deadline.sql      # 专注会话宽限期
├── V11__localize_and_expand_achievements.sql  # 成就中文化及扩展
└── V12__daily_focus_goal.sql                  # 每日专注目标
```

迁移在后端启动时 **自动执行**（`spring.flyway.enabled=true`）。如需手动迁移：

```bash
cd backend
./mvnw flyway:migrate
```

> **注意**：Hibernate DDL 模式为 `validate`，仅校验不自动建表，所有表结构变更必须通过 Flyway 迁移脚本管理。

### 测试

#### 前端类型检查 + 构建

```bash
cd frontend
npx vue-tsc --noEmit       # TypeScript 类型检查
npx vite build             # 生产构建
```

#### 后端编译

```bash
cd backend
.\mvnw.cmd compile -B      # 编译（Windows）
./mvnw compile -B          # 编译（Linux/macOS）
```

#### 后端集成测试（需要 Docker/Testcontainers）

```bash
cd backend
.\mvnw.cmd test            # 运行测试（Windows）
./mvnw test                # 运行测试（Linux/macOS）
```

> 后端测试使用 **Testcontainers** 自动启动 PostgreSQL 和 Redis 容器，运行测试前确保 Docker 已启动。

### 开发环境默认测试账号

后端在 `dev` profile 下启动时会自动创建以下测试账号（由 `DevDataSeeder` 生成）：

| 邮箱 | 密码 | 角色 | 邮箱已验证 |
|------|------|------|-----------|
| `admin@colearning.local` | `admin123` | ADMIN | ✅ |
| `student@test.com` | `student123` | USER | ✅ |
| `newbie@test.com` | `newbie123` | USER | ❌ |

> **注意**：这些账号仅在 `dev` profile 下自动创建，生产环境不会生成。

---

## Docker 部署

### 开发环境 Docker Compose

```bash
docker compose up -d
```

启动以下服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| PostgreSQL 16 | 5432 | 主数据库 |
| Redis 7 | 6379 | 缓存/实时状态 |
| MinIO API | 9000 | 对象存储 |
| MinIO Console | 9001 | MinIO 管理界面 |
| MailHog SMTP | 1025 | 邮件测试服务 |
| MailHog Web | 8025 | 邮件查看界面 |

MinIO 初始化容器（`minio-init`）会自动创建 `avatars`、`pets`、`images` 存储桶并设置公开下载权限。

### 清除数据

```bash
docker compose down -v    # 停止并删除所有数据卷
```

---

## API 响应格式

所有 API 返回统一的 `ApiResponse` 结构：

```json
{
  "code": "0",
  "message": "success",
  "data": { ... },
  "traceId": "...",
  "timestamp": "2026-01-01T00:00:00Z"
}
```

- `code` 为 `"0"` 表示成功，非零字符串表示错误（如 `"AUTH-001"`、`"STUDY-005"`）
- 错误码格式：`模块-编号`，详见 `ErrorCode` 枚举

---

## 常见问题

### Q: 后端启动报数据库连接失败？
确认 Docker 中的 PostgreSQL 容器正在运行：`docker compose ps`。确保 `.env` 中的数据库配置与 Docker Compose 一致。

### Q: 邮件发送失败？
开发环境默认使用 MailHog，`MAIL_PASSWORD` 可留空。如果使用真实 SMTP（如 yeah.net、QQ 邮箱），需要填写正确的 SMTP 授权码（非登录密码）。

### Q: 前端页面白屏或 API 请求 404？
确认后端已启动在 8080 端口。前端开发模式通过 Vite 代理转发 `/api` 和 `/ws` 到后端，无需额外配置 CORS。

### Q: WebSocket 连接失败？
确认后端已启动，且浏览器通过 `ws://localhost:5173/ws`（开发模式代理）或 `ws://localhost:8080/ws` 连接。WebSocket 需要携带 JWT Token 认证。

### Q: MinIO 文件上传失败？
确认 MinIO 容器正在运行，且 `minio-init` 容器已执行完毕（首次启动时自动创建存储桶）。可通过 MinIO Console (`http://localhost:9001`) 查看存储桶状态。

### Q: 后端编译报 Lombok/MapStruct 错误？
确保使用 Java 21。Lombok 和 MapStruct 的注解处理器在 Maven 编译插件中配置，IDE 中需安装对应插件并启用注解处理。

### Q: 数据库迁移失败？
Flyway 迁移脚本不可变，已执行的脚本不能修改。如需回退，使用 `./mvnw flyway:repair`。严重问题可删除 Docker 数据卷重建：`docker compose down -v && docker compose up -d`。

### Q: 如何切换到生产 profile？
设置环境变量 `SPRING_PROFILES_ACTIVE=prod`，并配置所有必需的环境变量（数据库、Redis、邮件等无默认值）。
