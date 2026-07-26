# Tasks

- [x] Task 1: 前端设计令牌与公共组件打磨：在 `frontend/src/assets/styles/main.css` 中审计并补全 CSS 变量（表面层级、边框、圆角 4-8px、间距、字号刻度），替换视图中的硬编码样式；打磨 `DefaultLayout.vue`（侧栏/顶部工具栏）、`components/task/TaskRow.vue`、`components/common/*`（空状态、Skeleton、CommandPalette）的视觉与动效（Hover 渐显、过渡、`prefers-reduced-motion`）。
  - [x] SubTask 1.1: 审计 main.css 现有变量，补全缺失令牌并清理冗余
  - [x] SubTask 1.2: 打磨侧栏与顶部工具栏（Active 指示器、折叠过渡、移动端 Drawer）
  - [x] SubTask 1.3: 打磨 TaskRow / QuickAddTask / TaskEditorDrawer 的密度、Hover 与完成动画
  - [x] SubTask 1.4: 统一 StateEmpty / StateError / StateLoading（Skeleton）风格

- [x] Task 2: 工作台五视图 UI 打磨：对 `views/workstation/` 下 TodayView、InboxView、PlannerView、ScheduleView、BoardsView 逐页检查并修复：卡片套卡片、双滚动条、横向溢出、间距浪费、标题过大、图标缺 Tooltip 等问题；确保 1440/1280/768/390 宽度下布局正常。
  - [x] SubTask 2.1: Today / Inbox 打磨
  - [x] SubTask 2.2: Planner / Boards 打磨（列宽、拖拽反馈、空状态）
  - [x] SubTask 2.3: Schedule（FullCalendar 主题变量对齐、事件块样式、移动端日视图）

- [x] Task 3: 旧页面风格统一：将 Stats、Journal 系列、Rooms、Pet/Achievements/Leaderboard/DailyTasks、Profile、Checkin 页面的表面层级、标题字号、间距与工作台对齐，去除装饰性大卡片堆叠。
  - [x] SubTask 3.1: Stats / Checkin / Journal 系列
  - [x] SubTask 3.2: Rooms / Gamification 系列 / Profile

- [x] Task 4: 后端查询优化：审计 `study` 模块任务列表、Planner 日期范围查询、统计聚合的 SQL（开启 SQL 日志或阅读 Repository），用 fetch join / `@EntityGraph` / 批量查询消除 N+1；新增 Flyway 迁移（V15+）补充 `study_tasks(user_id, planned_date)`、`(user_id, status)` 等缺失索引。
  - [x] SubTask 4.1: 审计并修复任务/标签/科目关联的 N+1
  - [x] SubTask 4.2: 新增索引迁移（不修改已有迁移）

- [x] Task 5: 后端代码质量：收敛 `StudyServiceImpl` 等重复 DTO 映射；服务层事务注解检查（只读查询加 `readOnly = true`）；确认越权/不存在资源统一抛 `BusinessException` + `ErrorCode`。

- [x] Task 6: 验证与回归：运行 `npx vue-tsc --noEmit`、`npm run lint`、`npm run test -- --run`、`npm run build`（frontend）与 `.\mvnw.cmd test`（backend，需 Docker），修复本轮改动引入的失败；确认前端页面可正常启动浏览。

- [x] Task 7: 针对“任务列表接口无 N+1 查询”验收：为任务列表（含 inbox/today/planner/quadrant 等）编写或补充最小后端集成测试/断言，证明同一请求不会对 `tags` 或 `focus_sessions` 产生逐任务查询；若发现残留逐任务查询则修复并回归。

# Task Dependencies

- Task 2、Task 3 depends on Task 1（设计令牌与公共组件先行）
- Task 6 depends on Task 1-5
- Task 4 与 Task 5 可与前端任务并行；Task 2 与 Task 3 可并行
- Task 7 depends on Task 4 / Task 5（验证后端列表路径 N+1）
