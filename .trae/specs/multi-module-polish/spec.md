# 多模块体验修复与增强 Spec

## Why
用户在浏览器巡检中发现 5 个模块存在体验缺陷或功能断链：任务弹窗全屏拉伸、科目/标签无删除入口且侧栏筛选断链、每日复盘缺历史/顺序/修改/图片能力、日志广场看不到公开日志（默认私密导致）、自习室成员信息过少且头像不同步。

## What Changes
- 修复任务/科目弹窗宽度失效（scoped 样式匹配不到 teleport 的 NModal 卡片）
- 侧栏科目/标签支持右键或悬浮操作（删除；科目另有编辑），并修复科目筛选断链（TasksView 读取 `route.query.subjectId`）
- 后端补充标签更新接口（PUT /api/study/tags/{tagId}）为可选项——本期仅接入删除，不做标签编辑 UI
- 每日复盘：历史复盘列表（新增后端历史区间查询 API 暴露）、打卡后允许修改（前端解除禁用 + 保留"已打卡"标识）、布局强化"先计划后复盘"顺序引导、支持插入图片（复用 storage 上传，新增 images 字段）
- 日志广场：发布流程增加可见性确认（发布时若为 PRIVATE 提示可选公开），广场空态文案说明；确保公开日志正常展示
- 自习室成员列表：显示当前专注时长与正在学习的任务标题（后端 RoomMemberResponse 扩展 + 定时刷新），点击成员弹出公开资料卡（复用 GET /api/users/{userId}/profile）
- 头像同步：上传头像成功后同步更新 authStore.user.avatarUrl，使侧栏/顶栏/聊天室即时生效

## Impact
- Affected specs: study(tasks/checkin/tags/subjects), journal, room, user, storage
- Affected code:
  - 前端: TasksView.vue, SubjectsView.vue, DefaultLayout.vue, TaskList.vue, CheckinView.vue, checkin.ts, JournalSquareView.vue, JournalEditor.vue, RoomMemberList.vue, roomStore.ts, ProfileView.vue, authStore.ts, api/room.ts, api/user.ts
  - 后端: CheckinController/CheckinServiceImpl/DailyCheckin(+迁移 V16), RoomServiceImpl/RoomMemberResponse, StudyController(tags), PublicUserController
  - 数据库: V16 迁移（daily_checkins 增加 images jsonb 字段）

## ADDED Requirements

### Requirement: 弹窗宽度约束
任务与科目的新增/编辑弹窗 SHALL 居中显示且宽度不超过 480px（科目 400px），小屏时为 90vw。

#### Scenario: 打开编辑任务弹窗
- **WHEN** 用户在任务页点击编辑任务
- **THEN** 弹窗居中显示，宽度 ≤480px，不再横向拉满屏幕

### Requirement: 科目/标签管理入口
侧栏科目与标签项 SHALL 提供删除入口（悬浮显示操作按钮 + 二次确认）；科目项点击后 SHALL 正确按科目筛选任务列表。

#### Scenario: 删除标签
- **WHEN** 用户悬浮侧栏标签项并点击删除且确认
- **THEN** 调用 DELETE /api/study/tags/{tagId}，侧栏与任务标签即时移除

#### Scenario: 科目筛选
- **WHEN** 用户点击侧栏某科目
- **THEN** 跳转 /tasks?subjectId=x 且任务列表仅显示该科目任务

### Requirement: 每日复盘增强
系统 SHALL 支持查看历史复盘、打卡后修改、按"计划→复盘"顺序引导、插入图片。

#### Scenario: 查看历史复盘
- **WHEN** 用户在每日复盘页切换到"历史"
- **THEN** 展示按日期倒序的历史打卡记录（计划/复盘/心情/专注分钟/图片）

#### Scenario: 打卡后修改
- **WHEN** 用户已完成今日打卡后再次编辑并保存
- **THEN** 保存成功，"已打卡"状态保持不变

#### Scenario: 插入图片
- **WHEN** 用户在复盘中上传图片（≤10MB，JPG/PNG/GIF/WebP）
- **THEN** 图片上传至 storage 并随打卡记录保存、回显

### Requirement: 自习室成员学习信息
成员列表 SHALL 显示专注中成员的已学习时长与正在进行的任务标题，并支持点击查看公开资料卡。

#### Scenario: 查看成员详情
- **WHEN** 用户点击成员项
- **THEN** 弹出资料卡显示头像、昵称、简介（GET /api/users/{userId}/profile）

### Requirement: 头像全局同步
上传头像成功后 SHALL 立即同步 authStore，侧栏/顶栏/聊天室头像即时更新，无需重新登录。

## MODIFIED Requirements

### Requirement: 日志发布可见性
发布日志时，若可见性为私密，系统 SHALL 提示用户选择是否公开；日志广场空态 SHALL 说明"仅展示公开日志"。（根因：默认 PRIVATE，发布≠公开，广场只查 PUBLIC+PUBLISHED）

#### Scenario: 发布私密日志
- **WHEN** 用户点击发布且可见性为私密
- **THEN** 弹出确认框提供"仅自己可见发布 / 公开发布"选项

## REMOVED Requirements
无
