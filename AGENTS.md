# Repository Guidelines

This document outlines best practices for contributing to the Co-Learning (伴学平台) repository. It covers project structure, development workflow, coding standards, testing, and contribution process.

## Project Structure & Module Organization

```
co-learning/
├── backend/                  # Spring Boot 3.3+ Java backend
│   ├── src/main/java/com/colearning/  # Main package
│   │   ├── auth/             # Authentication, JWT, email verification
│   │   ├── gamification/     # Pets, achievements, daily tasks, leaderboard
│   │   ├── journal/          # Markdown learning logs
│   │   ├── room/             # WebSocket companion rooms
│   │   ├── study/            # Study modules: subjects, tasks, focus timer, check-ins
│   │   └── user/             # User profiles, security
│   ├── src/main/resources/   # Config, Flyway migrations (V1-V16)
│   ├── test/                 # Integration tests with Testcontainers
│   └── pom.xml
├── frontend/                 # Vue 3 + TypeScript frontend
│   ├── src/
│   │   ├── api/              # API client wrappers
│   │   ├── components/       # Reusable Vue components
│   │   ├── composables/      # Vue 3 composables (e.g., useFocusTimer)
│   │   ├── stores/           # Pinia state management
│   │   ├── router/           # Vue Router config
│   │   ├── types/            # TypeScript interfaces
│   │   ├── utils/            # Helper functions
│   │   ├── views/            # Pages (auth, gamification, rooms, study)
│   │   └── App.vue, main.ts
│   ├── vite.config.ts
│   └── package.json
├── deploy/                   # Docker Compose + Postgres init
├── docs/                     # Project documentation
├── docker-compose.yml        # Full-stack infrastructure (DB, Redis, MinIO, MailHog)
└── .env.example              # Environment variables
```

See [README.md](https://github.com/yourorg/co-learning) for detailed architecture and features. (Note: adjust links if public)

## Build, Test, and Development Commands

### Prerequisites
- Java 21
- Node.js 20+
- Docker + Docker Compose
- Maven wrapper (mvnw)

### Key Commands
- **Backend**
  - cd backend && ./mvnw spring-boot:run — Start dev server (API: http://localhost:8080)
  - cd backend && ./mvnw clean package -DskipTests — Build production JAR
  - cd backend && ./mvnw flyway:migrate — Run database migrations manually

- **Frontend**
  - cd frontend && npm install — Install dependencies
  - cd frontend && npm run dev — Start Vite dev server (http://localhost:5173)
  - cd frontend && npm run build — Build for production
  - cd frontend && npm run lint — Run ESLint
  - cd frontend && npm run test — Run Vitest unit tests

- **Full Stack**
  - Start Docker infra first: docker compose up -d
  - Then cd backend && ./mvnw spring-boot:run
  - cd frontend && npm run dev (Vite proxies API calls automatically)

- **Production build**: cd frontend && npm run build and cd backend && ./mvnw clean package

## Coding Style & Naming Conventions

- **Indentation**: 2 spaces (root); 4 spaces for .java files (from .editorconfig).
- **Line endings**: LF.
- **Java**: Follow Spring conventions, Lombok, MapStruct for DTOs. Use descriptive module packages.
- **TypeScript/Vue**: Strict TypeScript, kebab-case for Vue components, Composition API.
- **Linting**: ESLint + TypeScript ESLint for frontend. Use IDE formatting for Java.
- **Formatting**: Run 
pm run lint in frontend.

## Testing Guidelines

- **Frontend**: Vitest (unit tests), Vue Test Utils. Run 
pm run test.
- **Backend**: JUnit 5, Spring Boot Test, Testcontainers for integration. Run mvn test.
- **Requirements**: High test coverage (80%+). Test core features like focus timer, WebSocket, gamification.
- **CI**: Tests run on PRs.

## Commit & Pull Request Guidelines

- **Commit messages**: Follow conventional commits:
  - eat:  — new features
  - ix:  — bug fixes
  - 
efactor:  — code improvements
  - 	est:  — test changes
  (Examples: eat: update README, fix email verification flow)
- **PR process**:
  - Reference issue: Fixes #123
  - Provide clear description, changes summary, and screenshots for UI changes.
  - Ensure tests pass, lint clean, build succeeds.
  - Update README/docs if relevant.
  - For DB changes, update migrations.

## Additional Tips

- **Contributing**: Fork repo, create feature branch, implement changes, commit with conventional message, open PR.
- **Local Development**: Copy .env.example → .env, start Docker, run backend then frontend.
- **Architecture**: Domain-driven modules (auth, study, gamification, room). Use Spring WebSocket, JPA, Redis caching.
- **Security**: Argon2 hashing, JWT, XSS-safe Markdown rendering with DOMPurify.
- **Docs**: Check docs/ and README for details.

For full project overview, see [README.md](https://github.com/yourorg/co-learning).
