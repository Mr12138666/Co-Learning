# Checklist

- [x] main.css 存在完整设计令牌（表面层级/边框/圆角/间距/字号），工作台视图无大面积硬编码色值
- [x] 侧栏/顶部工具栏视觉与交互打磨到位（Active 指示器、折叠过渡、移动端 Drawer 可用）
- [x] TaskRow 在各页面风格一致，Hover 出现操作按钮，完成有动画
- [x] StateEmpty / StateError / StateLoading 风格统一，加载使用 Skeleton 而非整页转圈
- [x] Today / Inbox / Planner / Schedule / Boards 无卡片套卡片、无双滚动条、无横向溢出
- [x] 1440 / 1280 / 768 / 390 宽度下工作台页面布局正常
- [x] Stats、Journal、Rooms、宠物/成就/排行榜、Profile 页面风格与工作台一致
- [x] 任务列表接口（含科目/标签）无 N+1 查询
- [x] 新增 Flyway 迁移补充任务查询索引，且未修改任何已有迁移文件
- [x] StudyService 只读查询标记 readOnly，DTO 映射无明显重复
- [x] 越权/不存在资源返回统一 ApiResponse 错误结构（BusinessException + ErrorCode）
- [x] vue-tsc、eslint、vitest、vite build 全部通过
- [x] 后端 Maven 测试通过（或明确记录环境限制无法运行）
