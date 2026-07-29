# Co-Learning 伴学平台

> 面向备考学生的公开伴学社区 — 技术演示级别响应式 Web 应用

## ✨ 功能特性

- 🔐 **账号系统** — 邮箱注册 + 6 位验证码验证 + JWT 双 Token 自动刷新
- 📚 **学习管理** — 科目 / 标签 / 任务三级体系，灵活的每日专注目标
- ⏱️ **专注计时** — Pomodoro 风格计时器，支持暂停/恢复，超限宽限期自动兜底
- ✍️ **每日复盘** — 打卡 + 图片上传 + 历史查看，支持编辑修改
- 📖 **学习日志** — Markdown 编辑器，公开日志广场，支持私密/公开切换
- 🏠 **陪伴房间** — WebSocket 实时房间，成员在线状态、专注时长、学习内容展示
- 🐾 **宠物养成** — 代币经济系统，每日任务/专注/成就三重代币来源，宠物属性衰减机制
- 🏆 **排行榜** — 专注时长排名（`xhxmin` 格式），成就系统
- 📊 **学习统计** — 周/月趋势图、专注时长分布、打卡完成率，ECharts 可视化
- 🔔 **实时通知** — 成就达成、任务完成等事件实时推送

## 🏗️ 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | LTS，虚拟线程就绪 |
| Spring Boot | 3.3.5 | Web / Security / JPA / WebSocket / Mail |
| PostgreSQL | 16 | 主数据库 |
| Redis | 7 | 缓存 + 实时状态 + Token 存储 |
| MinIO | latest | S3 兼容对象存储（头像/宠物图片/打卡图片） |
| Flyway | - | 数据库版本迁移（16 个迁移脚本） |
| Springdoc | 2.6.0 | OpenAPI 3 / Swagger UI |
| JJWT | 0.12.6 | JWT 生成与解析 |
| Argon2id | 2.2 | 密码哈希（argon2-jvm） |
| Lombok / MapStruct | - | 代码生成 |
| Docker Compose | - | 一键启动全套基础设施 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | Composition API |
| TypeScript | 5.7 | 类型安全 |
| Vite | 6.0 | 极速开发服务器 + 构建 |
| Naive UI | 2.40 | Vue 3 原生组件库 |
| Pinia | 3.0 | 状态管理 + 持久化插件 |
| Vue Router | 4.5 | 路由 + 导航守卫 |
| Axios | 1.7 | HTTP 客户端 |
| ECharts | 6.1 | 数据可视化 |
| FullCalendar | 6.1 | 日历视图（学习计划） |
| SockJS + STOMP | - | WebSocket 实时通信 |
| Marked + DOMPurify | - | Markdown 渲染 + XSS 防护 |
| Day.js | - | 日期处理 |
| Vitest | 2.1 | 单元测试 |

## 📁 项目结构

```
co-learning/
├── backend/                          # Spring Boot 后端应用
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/colearning/
│   │   │   │   ├── auth/             # 认证模块（注册/登录/邮箱验证）
│   │   │   │   ├── common/           # 公共配置/异常/安全/存储
│   │   │   │   ├── gamification/     # 游戏化（宠物/成就/每日任务）
│   │   │   │   ├── journal/          # 学习日志
│   │   │   │   ├── leaderboard/      # 排行榜
│   │   │   │   ├── room/            # 陪伴房间（WebSocket）
│   │   │   │   ├── study/           # 学习核心（科目/任务/专注/打卡/统计）
│   │   │   │   └── user/            # 用户资料
│   │   │   └── resources/
│   │   │       ├── db/migration/    # Flyway SQL 迁移脚本（V1-V16）
│   │   │       └── application*.yml # 配置文件（dev/prod）
│   │   └── test/                     # 集成测试（Testcontainers）
│   ├── Dockerfile
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── run-mvn.sh
├── frontend/                         # Vue 3 前端应用
│   ├── src/
│   │   ├── api/                      # API 请求封装
│   │   ├── components/               # 可复用组件（focus/journal/room/study/task）
│   │   ├── composables/              # Vue Composables（useFocusTimer/useWebSocket）
│   │   ├── layouts/                  # 布局组件
│   │   ├── router/                   # 路由配置
│   │   ├── stores/                   # Pinia 状态仓库
│   │   ├── types/                    # TypeScript 类型定义
│   │   ├── utils/                    # 工具函数
│   │   ├── views/                    # 页面视图
│   │   │   ├── auth/                 # 登录/注册/验证/重置密码
│   │   │   ├── gamification/         # 宠物/成就/每日任务/排行榜
│   │   │   ├── rooms/                # 房间列表/房间详情
│   │   │   ├── study/                # 考试目标/科目/任务/统计
│   │   │   ├── user/                 # 个人资料
│   │   │   ├── workstation/          # 工作台（今日/收件箱/计划/日程/看板）
│   │   │   └── *.vue                 # 仪表盘/打卡/日志等
│   │   ├── App.vue
│   │   └── main.ts
│   ├── Dockerfile
│   ├── nginx.conf
│   ├── vite.config.ts
│   └── package.json
├── deploy/
│   └── postgres/init.sql             # PostgreSQL 初始化脚本
├── docs/                             # 项目文档
├── .env.example                      # 环境变量模板
├── .gitignore
├── docker-compose.yml                # Docker Compose 编排
└── README.md
```

## 🚀 快速开始

### 前置条件

- **Java 21** (JDK)
- **Node.js 20+** (LTS)
- **Docker** + **Docker Compose**（用于启动基础设施）
- **Maven**（已内置 `mvnw` wrapper，无需单独安装）

### 1. 配置环境变量

```bash
cp .env.example .env
```

关键变量说明：

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `POSTGRES_DB` | 数据库名称 | `colearning` |
| `POSTGRES_USER` | 数据库用户名 | `colearning` |
| `POSTGRES_PASSWORD` | 数据库密码 | `colearning_dev` |
| `DB_HOST` | 数据库主机 | `localhost` |
| `DB_PORT` | 数据库端口 | `5432` |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `MINIO_ENDPOINT` | MinIO API 地址 | `http://localhost:9000` |
| `MAIL_HOST` | SMTP 服务器 | `smtp.yeah.net` |
| `MAIL_USERNAME` | 邮箱账号 | — |
| `MAIL_PASSWORD` | **SMTP 授权码**（非登录密码） | — |
| `MAIL_FROM` | 发件人地址 | `noreply@colearning.local` |
| `JWT_SECRET` | JWT 密钥（≥256 bit） | 见 `.env.example` |
| `APP_BASE_URL` | 前端访问地址 | `http://localhost:5173` |
| `VITE_API_BASE_URL` | 前端 API 地址 | `http://localhost:8080` |
| `VITE_WS_BASE_URL` | 前端 WebSocket 地址 | `ws://localhost:8080` |

> ⚠️ `MAIL_PASSWORD` 使用真实 SMTP 时需填写**邮箱授权码**（如 yeah.net/QQ 邮箱的授权码，非登录密码）。开发环境使用 MailHog 时可留空。

### 2. 启动基础设施

```bash
docker compose up -d
```

服务端口一览：

| 服务 | 端口 | 说明 |
|------|------|------|
| PostgreSQL 16 | 5432 | 主数据库 |
| Redis 7 | 6379 | 缓存 + 实时状态 |
| MinIO API | 9000 | 对象存储 |
| MinIO Console | 9001 | MinIO 管理界面 |
| MailHog SMTP | 1025 | 邮件测试服务 |
| MailHog Web | 8025 | 邮件查看界面 |

MinIO 初始化容器会自动创建 `avatars`、`pets`、`images` 存储桶并设置公开下载权限。

### 3. 启动后端

```bash
cd backend
./mvnw spring-boot:run          # Linux / macOS
.\mvnw.cmd spring-boot:run      # Windows
```

后端启动后：
- API: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 文档: `http://localhost:8080/v3/api-docs`

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`，开发模式下 Vite 自动代理 `/api` 和 `/ws` 请求到后端。

### 5. （可选）生产构建

```bash
# 前端
cd frontend
npm run build     # 输出到 dist/

# 后端
cd backend
./mvnw clean package -DskipTests   # 输出到 target/
```

## 🗄️ 数据库迁移（Flyway）

项目使用 Flyway 管理数据库版本。迁移脚本位于 `backend/src/main/resources/db/migration/`：

```
V1__init_auth.sql                          # 用户认证表
V2__init_user_profile.sql                  # 用户资料 + 屏蔽
V3__init_study.sql                         # 学习模块（科目/任务/专注/打卡）
V4__init_journal.sql                       # 日志模块
V5__init_room.sql                          # 陪伴房间
V6__init_gamification.sql                 # 游戏化（代币/宠物/成就）
V7__daily_tasks.sql                        # 每日任务系统
V8__more_achievements.sql                  # 更多成就
V9__localize_and_rebalance_pet_items.sql   # 道具中文化 + 价格平衡
V10__focus_session_grace_deadline.sql      # 专注会话宽限期
V11__localize_and_expand_achievements.sql  # 成就中文化 + 扩展
V12__daily_focus_goal.sql                  # 每日专注目标
V13__task_scheduling_fields.sql           # 任务调度字段
V14__tags_system.sql                       # 标签系统
V15__task_query_indexes.sql               # 任务查询索引优化
V16__checkin_images.sql                   # 打卡图片字段
```

迁移在后端启动时 **自动执行**（`spring.flyway.enabled=true`）。Hibernate DDL 模式为 `validate`，仅校验不自动建表。

如需手动迁移：

```bash
cd backend
./mvnw flyway:migrate
```

## 🔐 API 响应格式

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

## 🧪 测试

```bash
# 前端类型检查 + 单元测试
cd frontend
npx vue-tsc --noEmit       # TypeScript 类型检查
npm run test                # Vitest 单元测试

# 后端集成测试（需要 Docker + Testcontainers）
cd backend
./mvnw test                 # Linux/macOS
.\mvnw.cmd test             # Windows
```

## 👤 开发环境默认测试账号

后端在 `dev` profile 下启动时，`DevDataSeeder` 会自动创建：

| 邮箱 | 密码 | 角色 | 邮箱已验证 |
|------|------|------|-----------|
| `admin@colearning.local` | `admin123` | ADMIN | ✅ |
| `student@test.com` | `student123` | USER | ✅ |
| `newbie@test.com` | `newbie123` | USER | ❌ |

> 仅在 `dev` profile 下自动创建，生产环境不会生成。

## 🌐 跨设备访问（局域网）

如需从另一台设备访问开发服务器：

```bash
# 前端 — 暴露到局域网
cd frontend
npm run dev -- --host

# 后端 — CORS 已预配置局域网 IP
# 编辑 backend/src/main/resources/application.yml 的 app.cors.allowed-origins
# 添加你的局域网 IP，例如: http://192.168.1.100:5173
```

## 🐳 Docker 一键部署

```bash
# 完整构建 + 启动所有服务
docker compose up -d --build

# 仅基础设施
docker compose up -d postgres redis minio minio-init mailhog

# 停止
docker compose down

# 清除所有数据
docker compose down -v
```

Docker 部署端口映射：

| 服务 | 容器内端口 | 宿主机端口 |
|------|-----------|-----------|
| 后端 Spring Boot | 8080 | 8080 |
| 前端 Nginx | 80 | 3000 |
| PostgreSQL | 5432 | 5432 |
| Redis | 6379 | 6379 |
| MinIO API | 9000 | 9000 |
| MinIO Console | 9001 | 9001 |
| MailHog SMTP | 1025 | 1025 |
| MailHog Web | 8025 | 8025 |

## 🔑 架构说明

### 认证流程

```
用户注册 → 发送 6 位验证码邮件 → 用户验证邮箱 → 账号激活
                                                    ↓
                                              未验证 → 禁止登录
```

- 邮箱验证使用 **6 位数字验证码**（24 小时有效）
- 验证码发送失败时**自动回滚**注册流程，不会创建无效账号
- 登录时检测未验证邮箱，引导用户完成验证
- JWT 采用 Access Token（15 分钟）+ Refresh Token（7 天）双 Token 机制

### 代币经济系统

```
代币获取来源：
├── 每日任务（5-20 代币/天）
├── 专注时长（1 代币/10 分钟）
└── 成就解锁（5-500 代币）

代币消耗：
└── 宠物商店购买道具
```

### 专注会话机制

- 最大连续有效时长：**8 小时**
- 最大暂停时长：**1 小时**
- 超限后进入 **30 分钟宽限期**，超时自动中止
- 宽限期内可继续恢复会话

## ⚠️ 常见问题

### Q: 后端启动报数据库连接失败？

确认 Docker 中 PostgreSQL 容器正在运行：`docker compose ps`。确保 `.env` 中数据库配置与 Docker Compose 一致。

### Q: 邮件发送失败？

开发环境默认使用 MailHog，`MAIL_PASSWORD` 可留空。使用真实 SMTP（yeah.net、QQ 邮箱等）时，需填写正确的**SMTP 授权码**（非登录密码）。

### Q: 前端页面白屏或 API 请求 404？

确认后端已启动在 8080 端口。前端开发模式通过 Vite 代理转发 `/api` 和 `/ws` 到后端，无需额外配置 CORS。

### Q: WebSocket 连接失败？

确认后端已启动，且浏览器通过 `ws://localhost:5173/ws`（开发模式代理）连接。WebSocket 需要携带 JWT Token 认证。

### Q: MinIO 文件上传失败？

确认 MinIO 容器正在运行，且 `minio-init` 容器已执行完毕。可通过 MinIO Console（`http://localhost:9001`）查看存储桶状态。

### Q: 数据库迁移失败？

Flyway 迁移脚本不可变，已执行的脚本不能修改。如需回退，使用 `./mvnw flyway:repair`。严重问题可删除 Docker 数据卷重建：`docker compose down -v && docker compose up -d`。

### Q: 如何切换到生产 profile？

设置环境变量 `SPRING_PROFILES_ACTIVE=prod`，并配置所有必需的环境变量（数据库、Redis、邮件等无默认值）。

### Q: 另一台设备无法访问开发服务器？

需要：① 前端添加 `--host` 参数暴露到局域网；② 后端 CORS 配置中添加设备的 IP 地址；③ 检查 Windows 防火墙是否放行对应端口。

## 📝 开发阶段规划

| 阶段 | 内容 | 状态 |
|------|------|------|
| 1. 工程基础 | 项目脚手架、认证、用户基础 | ✅ 已完成 |
| 2. 个人学习闭环 | 专注计时、学习统计、日志 | ✅ 已完成 |
| 3. 公开陪伴房 | WebSocket 实时房间、在线状态 | ✅ 已完成 |
| 4. 成长和竞争 | 排行榜、宠物、成就、每日任务 | ✅ 已完成 |
| 5. AI 和治理 | AI 日志分析、举报、管理后台 | 🔄 规划中 |

---

© 2026 Co-Learning Study Companion. Released under the MIT License.
