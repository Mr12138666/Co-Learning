# Co-Learning 伴学平台 - 项目长期记忆

## 项目概述
备考学生学习社区平台，单体架构 (Spring Boot 3.3.5 + Java 21 + Vue 3 + TypeScript)

## 技术栈
- 后端: Spring Boot 3.3.5, Java 21, Maven, PostgreSQL 16, Redis 7, MinIO, Flyway
- 前端: Vue 3.5, TypeScript, Vite, Naive UI, Pinia 3, Axios, Dayjs
- 测试: Testcontainers (@ServiceConnection), JUnit 5
- 部署: Docker Compose (单机)

## 关键架构决策
- 模块化单体: 9 个领域模块 (auth, user, study, journal, room, leaderboard, gamification, ai, moderation)
- 模块封装: Spring Modulith 风格 `internal` 包模式
- 认证: JWT Access Token (15min, 内存) + Refresh Token (7d, Redis, HttpOnly cookie)
- 密码: Argon2id
- 专注计时: 服务端权威 (基于 started_at 计算 elapsed, 不依赖本地累计)
- 事件驱动: ApplicationEventPublisher + @TransactionalEventListener(AFTER_COMMIT)
- 软删除: deleted_at + @SQLRestriction
- Markdown: commonmark-java + OWASP HTML Sanitizer (后端), 简易渲染 (前端)

## 开发环境
- Maven wrapper: backend/run-mvn.sh (清除 CLASSPATH 环境变量)
- 后端启动: `cd backend && bash run-mvn.sh spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--server.port=8080"`
  - 注意: 必须传 --server.port=8080 参数，否则 spring-boot:run fork 进程会使用随机端口
- 前端启动: `cd frontend && npx vite --port 5173`
- Docker: `docker compose up -d`

## 测试账号 (dev profile)
- admin@colearning.local / admin123 (ADMIN)
- student@test.com / student123 (USER, verified)
- newbie@test.com / newbie123 (USER, unverified)

## 开发阶段进度
- Phase 1: ✅ 完成 (auth, user, Docker, 前端认证)
- Phase 2: ✅ 完成 (study, focus, checkin, stats, journal - 后端+前端)
- Phase 3: ✅ 完成 (room: WebSocket/STOMP, 实时陪伴房, Presence)
- Phase 4: ✅ 完成 (leaderboard, gamification, pets, achievements)
- Phase 5: 待开始 (AI fallback, moderation)

## Git 提交历史
1. ae969ab - chore: initialize monorepo structure
2. d17ff54 - feat: backend scaffold and common infrastructure
3. 5978893 - feat: auth module (JWT, email verification, password reset)
4. b6efad6 - feat: user module (profile, avatar, settings, blocks)
5. 199fe3a - feat: frontend Vue 3 scaffold + auth views + test infrastructure
6. (Phase 2 backend) - feat: Phase 2 backend (study, focus, checkin, stats, journal)
7. (Phase 2 frontend) - feat: Phase 2 frontend (focus timer, study views, journal editor)
