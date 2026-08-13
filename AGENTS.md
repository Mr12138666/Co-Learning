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
│   │   ├── user/             # User profiles, security
│   │   └── common/           # Common utilities and base classes
│   │       ├── service/      # Generic CRUD service interfaces and base classes
│   │       ├── repository/   # Generic repository interfaces and query builders
│   │       ├── dto/          # Base DTO classes
│   │       ├── exception/    # Exception handling utilities
│   │       ├── cache/        # Cache helper utilities
│   │       └── entity/       # Base entity classes
│   ├── src/main/resources/   # Config, Flyway migrations (V1-V16)
│   ├── test/                 # Integration tests with Testcontainers
│   └── pom.xml
├── frontend/                 # Vue 3 + TypeScript frontend
│   ├── src/
│   │   ├── api/              # API client wrappers and helpers
│   │   │   ├── http.ts       # Axios instance with interceptors
│   │   │   └── ...           # Domain-specific API modules
│   │   ├── components/       # Reusable Vue components
│   │   │   ├── common/       # Base components (BaseCard, BaseButton, BaseForm, BaseTable, BaseModal, ThemeSwitcher)
│   │   │   ├── focus/        # Focus timer components
│   │   │   ├── journal/      # Journal editor components
│   │   │   ├── room/         # Room chat components
│   │   │   ├── study/        # Study task components
│   │   │   └── task/         # Task management components
│   │   ├── composables/      # Vue 3 composables
│   │   │   └── ...           # Composables (useFocusTimer, etc.)
│   │   ├── config/           # Configuration files
│   │   │   └── theme.ts      # Theme configuration (5 themes: light, dark, blue, green, purple)
│   │   ├── stores/           # Pinia state management
│   │   │   ├── authStore.ts  # Authentication state
│   │   │   ├── themeStore.ts # Theme state with 5 theme support
│   │   │   └── ...           # Domain-specific stores
│   │   ├── router/           # Vue Router config
│   │   ├── types/            # TypeScript interfaces
│   │   ├── utils/            # Helper functions and utilities
│   │   │   ├── http-error.ts # HTTP error handling
│   │   │   ├── markdown.ts   # Markdown rendering and sanitization
│   │   │   ├── format.ts     # Date and duration formatting
│   │   │   └── ...           # Other utility functions
│   │   ├── views/            # Pages
│   │   │   ├── auth/         # Authentication pages (login, register, verify-email, forgot-password, reset-password)
│   │   │   ├── gamification/ # Gamification pages (achievements, daily-tasks, leaderboard, pet)
│   │   │   ├── rooms/        # Room pages (room-list, room-detail)
│   │   │   ├── study/        # Study pages (goals, subjects, tasks, stats)
│   │   │   ├── user/         # User pages (profile, settings, notifications)
│   │   │   ├── workstation/  # Workstation pages (today, inbox, planner, schedule, boards)
│   │   │   └── ...           # Other pages (dashboard, journal, checkin, 404)
│   │   ├── layouts/          # Layout components
│   │   │   ├── DefaultLayout.vue # Main layout with sidebar and theme switcher
│   │   │   └── BlankLayout.vue   # Blank layout for auth pages
│   │   └── App.vue, main.ts
│   ├── vite.config.ts
│   └── package.json
├── deploy/                   # Docker Compose + Postgres init
├── docs/                     # Project documentation
├── docker-compose.yml        # Full-stack infrastructure (DB, Redis, MinIO, MailHog)
└── .env.example              # Environment variables
```

## Backend Architecture

### Generic CRUD Base Classes

The backend provides generic base classes to reduce code duplication:

- **`CrudService<E, ID, C, U, R>`**: Generic CRUD service interface
- **`BaseCrudService<E, ID, C, U, R>`**: Base implementation with transaction management

**Services Using BaseCrudService:**
- `JournalServiceImpl` - Journal CRUD operations (create, getById, update, delete, publish)

**Services with Custom Implementation:**
- `StudyServiceImpl` - Multiple entity types (ExamGoal, Subject, StudyTask)
- `GamificationServiceImpl` - Custom business logic (exp, tokens, achievements)
- `RoomServiceImpl` - Different method signatures (createRoom, getRoom, updateRoom, deleteRoom)
- `LeaderboardServiceImpl` - Custom scoring logic (addScore, getLeaderboard)
- `AuthServiceImpl` - Authentication-specific operations (login, register, verify)
- `UserServiceImpl` - User profile management
- `CheckinServiceImpl` - Daily check-in logic
- `FocusSessionServiceImpl` - Focus timer logic
- `StatsServiceImpl` - Statistics calculation
- `DailyTaskServiceImpl` - Daily task management
- `RoomMessageServiceImpl` - Room message handling
- `PresenceServiceImpl` - Online presence tracking

### Module Structure

Each domain module follows this structure:
```
module/
├── ModuleController.java      # REST endpoints
├── ModuleService.java         # Service interface
├── dto/
│   ├── request/               # Request DTOs
│   └── response/              # Response DTOs
└── internal/
    ├── ModuleServiceImpl.java # Service implementation
    ├── entity/                # JPA entities
    └── repository/            # Spring Data repositories
```

## Frontend Architecture

### Theme System

The frontend supports 6 themes with dynamic switching:

| Theme | Description | Type |
|-------|-------------|------|
| `light` | Default light theme | Light |
| `dark` | Default dark theme | Dark |
| `cyberpunk` | Neon colors, glowing effects | Dark |
| `newspaper` | Retro newspaper style, black & white | Light |
| `pixel` | 8-bit pixel art style, no rounded corners | Light |
| `ocean` | Deep blue ocean theme | Dark |

**Theme Files:**
- `frontend/src/config/theme.ts` - Theme configuration with CSS variable mappings
- `frontend/src/stores/themeStore.ts` - Theme state management with `applyTheme()`
- `frontend/src/components/common/ThemeSwitcher.vue` - Theme switcher dropdown

**How It Works:**
1. **Light/Dark Mode**: Toggles `.dark` class on `<html>` element
2. **Color Themes**: Updates `--accent-*` CSS variables (accent50 through accent700)
3. **Auto-propagation**: `--brand` variable automatically follows `--accent-500`

**CSS Variables Affected:**
```css
--accent-50 through --accent-700  /* Theme colors */
--brand: var(--accent-500)        /* Primary brand color */
--brand-hover: var(--accent-600)  /* Brand hover state */
--brand-subtle: var(--accent-50)  /* Subtle brand background */
```

**Usage:**
```vue
<script setup>
import ThemeSwitcher from '@/components/common/ThemeSwitcher.vue'
</script>

<template>
  <ThemeSwitcher />
</template>
```

**Programmatic Usage:**
```typescript
import { useThemeStore } from '@/stores/themeStore'

const themeStore = useThemeStore()

// Switch to blue theme
themeStore.setTheme('blue')

// Toggle dark mode
themeStore.toggleDarkMode()

// Check current theme
console.log(themeStore.themeName) // 'blue'
console.log(themeStore.isDark)    // false
```

### Base Components

Reusable base components are available in `frontend/src/components/common/`:

- **`ThemeSwitcher.vue`**: Theme switcher dropdown (actively used)
- **`StateLoading.vue`**: Loading state component
- **`StateEmpty.vue`**: Empty state component
- **`StateError.vue`**: Error state component
- **`CommandPalette.vue`**: Command palette component
- **`AppAvatar.vue`**: Avatar component

Note: Existing code uses Naive UI components directly.

### Utility Libraries

Existing utility files in `frontend/src/utils/`:

- `http-error.ts` - HTTP error handling
- `markdown.ts` - Markdown rendering and sanitization
- `format.ts` - Date and duration formatting

### New Pages

Added pages with full routing:

- **`/settings`** - User settings (profile, appearance, notifications, privacy, account)
- **`/notifications`** - Notification center with read/unread filtering

## Build, Test, and Development Commands

### Prerequisites
- Java 21
- Node.js 20+
- Docker + Docker Compose
- Maven wrapper (mvnw)

### Key Commands
- **Backend**
  - `cd backend && ./mvnw spring-boot:run` — Start dev server (API: http://localhost:8080)
  - `cd backend && ./mvnw clean package -DskipTests` — Build production JAR
  - `cd backend && ./mvnw flyway:migrate` — Run database migrations manually

- **Frontend**
  - `cd frontend && npm install` — Install dependencies
  - `cd frontend && npm run dev` — Start Vite dev server (http://localhost:5173)
  - `cd frontend && npm run build` — Build for production
  - `cd frontend && npm run lint` — Run ESLint
  - `cd frontend && npm run test` — Run Vitest unit tests

- **Full Stack**
  - Start Docker infra first: `docker compose up -d`
  - Then `cd backend && ./mvnw spring-boot:run`
  - `cd frontend && npm run dev` (Vite proxies API calls automatically)

- **Production build**: `cd frontend && npm run build` and `cd backend && ./mvnw clean package`

## Coding Style & Naming Conventions

- **Indentation**: 2 spaces (root); 4 spaces for .java files (from .editorconfig).
- **Line endings**: LF.
- **Java**: Follow Spring conventions, Lombok, MapStruct for DTOs. Use descriptive module packages.
- **TypeScript/Vue**: Strict TypeScript, kebab-case for Vue components, Composition API.
- **Linting**: ESLint + TypeScript ESLint for frontend. Use IDE formatting for Java.
- **Formatting**: Run `npm run lint` in frontend.

## Testing Guidelines

- **Frontend**: Vitest (unit tests), Vue Test Utils. Run `npm run test`.
- **Backend**: JUnit 5, Spring Boot Test, Testcontainers for integration. Run `mvn test`.
- **Requirements**: High test coverage (80%+). Test core features like focus timer, WebSocket, gamification.
- **CI**: Tests run on PRs.

## Commit & Pull Request Guidelines

- **Commit messages**: Follow conventional commits:
  - `feat:` — new features
  - `fix:` — bug fixes
  - `refactor:` — code improvements
  - `test:` — test changes
  - `docs:` — documentation changes
  (Examples: `feat: add theme switcher`, `fix: email verification flow`)
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