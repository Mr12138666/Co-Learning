# Tasks

- [x] Task 1: 修复弹窗宽度失效
  - [x] SubTask 1.1: TasksView.vue 的 NModal 加 `style="width: 480px; max-width: 90vw"`（移除失效的 scoped .task-modal 限宽）
  - [x] SubTask 1.2: SubjectsView.vue 的 NModal 同样处理（400px）
- [x] Task 2: 科目/标签管理与筛选断链修复
  - [x] SubTask 2.1: DefaultLayout.vue 侧栏科目/标签项悬浮显示删除按钮（NPopconfirm 确认），调用 store 的 deleteSubject/deleteTag
  - [x] SubTask 2.2: TasksView.vue/TaskList.vue 读取并响应 `route.query.subjectId` 实现科目筛选
- [x] Task 3: 每日复盘增强（后端）
  - [x] SubTask 3.1: V16 迁移为 daily_checkins 增加 images 字段（jsonb），实体/DTO 同步
  - [x] SubTask 3.2: CheckinController 新增 `GET /api/checkins/history?from&to`（复用已有 repository 区间查询），分页或近 30 天
  - [x] SubTask 3.3: UpdateCheckinRequest 支持 images；补充/更新后端测试
- [x] Task 4: 每日复盘增强（前端）
  - [x] SubTask 4.1: CheckinView.vue 移除 isCompleted 禁用，已打卡状态下仍可编辑保存；保留"已打卡"标签
  - [x] SubTask 4.2: 布局按"今日计划 → 学习复盘"顺序引导（步骤序号提示）
  - [x] SubTask 4.3: 图片上传（复用 storageApi.upload，最多 6 张，预览与删除）
  - [x] SubTask 4.4: 新增"历史复盘"区块/标签页，调用历史 API 展示
- [x] Task 5: 日志发布可见性确认
  - [x] SubTask 5.1: 发布时若 visibility=PRIVATE 弹确认框（私密发布/公开发布）
  - [x] SubTask 5.2: 日志广场空态文案改为"暂无公开日志，发布日志时选择'公开'即可展示在这里"
- [x] Task 6: 自习室成员学习信息（后端）
  - [x] SubTask 6.1: RoomMemberResponse 增加 focusElapsedSeconds、focusTaskTitle；RoomServiceImpl 批量查询进行中 focus session 与任务标题填充
- [x] Task 7: 自习室成员学习信息与资料卡（前端）
  - [x] SubTask 7.1: RoomMemberList.vue 显示"学习中 · Xh Ym · 任务标题"，本地每分钟递增
  - [x] SubTask 7.2: 点击成员弹出资料卡（NPopover/NModal），调用 userApi.getProfile 显示头像/昵称/简介；修正 getProfile 返回类型为 PublicUserProfileResponse
- [x] Task 8: 头像全局同步
  - [x] SubTask 8.1: authStore 增加 updateAvatar action；ProfileView 上传成功后调用
- [x] Task 9: 回归验证
  - [x] SubTask 9.1: 前端 vue-tsc / eslint / vitest / build
  - [x] SubTask 9.2: 后端 mvnw test（含 V16 迁移）

# Task Dependencies
- Task 4 depends on Task 3
- Task 7 depends on Task 6
- Task 9 depends on Task 1-8
