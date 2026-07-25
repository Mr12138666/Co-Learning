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
