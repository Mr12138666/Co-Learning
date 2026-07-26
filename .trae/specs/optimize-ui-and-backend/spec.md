# 前端 UI 美化与后端逻辑优化 Spec

## Why

V2 工作台重构（Today/Inbox/Planner/Schedule/Boards）已基本落地但未打磨：部分页面视觉密度、层级、动效与 v2 视觉标准仍有差距；后端在任务查询、统计聚合等路径存在可优化的查询与代码质量问题。本轮在不改变功能范围的前提下，提升 UI 观感一致性与后端代码质量/性能。

## What Changes

- 前端：统一设计令牌（颜色/间距/圆角/字号），打磨工作台五视图与公共组件（侧栏、顶部工具栏、Task Row、抽屉、空状态、Skeleton）的视觉细节与交互动效
- 前端：清理旧页面（Dashboard、Stats、Journal、Rooms、Gamification 等）与新工作台风格不一致的问题（大卡片、过大标题、双滚动条、间距浪费）
- 后端：消除任务/统计相关的 N+1 查询，补充必要索引（新 Flyway 迁移，不修改已有迁移）
- 后端：规范化异常处理与事务边界，收敛重复代码（DTO 映射、校验逻辑）
- 不新增业务功能、不新增第二套任务实体、不修改已有 Flyway 迁移

## Impact

- Affected specs: 前端视觉系统、工作台视图、study 模块后端
- Affected code:
  - `frontend/src/assets/styles/main.css`（设计令牌）
  - `frontend/src/layouts/DefaultLayout.vue`、`frontend/src/components/task/*`、`frontend/src/components/common/*`
  - `frontend/src/views/workstation/*` 及其余视图
  - `backend/src/main/java/com/colearning/study/**`（查询与服务层）
  - `backend/src/main/resources/db/migration/`（仅新增 V15+）

## ADDED Requirements

### Requirement: 统一设计令牌
前端 SHALL 在 `main.css` 中以 CSS 变量集中定义颜色、表面层级、边框、圆角（4-8px）、间距与字号刻度，所有视图 SHALL 引用变量而非硬编码值。

#### Scenario: 视图使用统一令牌
- **WHEN** 检查工作台五视图与公共组件的样式
- **THEN** 颜色/圆角/间距均来自 CSS 变量，无大面积硬编码色值

### Requirement: 工作台视觉打磨
Today/Inbox/Planner/Schedule/Boards SHALL 满足 v2 视觉标准：紧凑任务行、1px 低对比边框、无卡片套卡片、无双滚动条与横向溢出、Hover 工具渐显、Skeleton 加载。

#### Scenario: Today 页面视觉验收
- **WHEN** 打开 Today 页面
- **THEN** 呈现紧凑摘要条 + 任务行列表结构，无巨型计时器与四张统计大卡片，Hover 任务行出现操作按钮

#### Scenario: 响应式无溢出
- **WHEN** 在 1440/1280/768/390 宽度下浏览工作台页面
- **THEN** 无横向滚动条、无文字遮挡、侧栏在移动端变为 Drawer

### Requirement: 旧页面风格统一
统计、日志、自习室、宠物/成就/排行榜等页面 SHALL 采用与工作台一致的表面层级、标题字号与间距，去除装饰性大卡片堆叠。

#### Scenario: 旧页面一致性
- **WHEN** 从 Today 切换到统计或宠物页面
- **THEN** 背景层级、边框、圆角、标题字号与工作台一致

### Requirement: 后端查询优化
study 模块的任务列表、Planner 聚合、统计接口 SHALL 避免 N+1 查询（使用 fetch join / 批量查询），并为高频过滤字段（如 `planned_date`、`status`、`user_id` 组合）在新迁移中补充缺失索引。

#### Scenario: 任务列表单次查询
- **WHEN** 调用任务列表接口（含科目、标签信息）
- **THEN** 不产生每任务一次的附加 SQL（通过日志或测试验证）

### Requirement: 后端代码质量
study 模块 SHALL 规范化：DTO 映射逻辑收敛（消除重复映射代码）、事务注解位于服务层且只读查询标记 `readOnly`、业务异常统一走 `BusinessException` + `ErrorCode`。

#### Scenario: 异常规范
- **WHEN** 请求不存在或不属于当前用户的任务
- **THEN** 返回统一 ApiResponse 错误结构与对应 ErrorCode，而非 500

## MODIFIED Requirements

无（不改变既有功能行为，仅优化实现与视觉）。

## REMOVED Requirements

无。
