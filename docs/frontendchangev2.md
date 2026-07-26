你上一轮对 Co-Learning 的改造没有达到预期。本轮不是继续微调颜色，而是进行第二次重构级升级。

目标项目：
D:\Projects\code\co-learning

只读参考项目：
D:\Projects\super-productivity

视觉参考截图：
C:\Users\SUNRIS~1\AppData\Local\Temp\codex-clipboard-6461af1a-976a-4dcd-9eb1-4dc7f883a91b.png
C:\Users\SUNRIS~1\AppData\Local\Temp\codex-clipboard-892c67f9-b8f3-4efc-b4c5-ad94c794433a.png
C:\Users\SUNRIS~1\AppData\Local\Temp\codex-clipboard-c22251d6-a6db-4cc6-86f6-6f5af6466d89.png
C:\Users\SUNRIS~1\AppData\Local\Temp\codex-clipboard-a1bba138-965f-4fae-a259-a33cc3243182.png

当前效果截图：
C:\Users\SUNRIS~1\AppData\Local\Temp\codex-clipboard-71417a5d-c056-4f04-891f-2b109ae3a450.png

一、问题判断

当前版本只复制了暗色背景、侧栏和部分颜色，没有复现参考项目的核心：

- 没有高密度、可交互的任务行
- 没有 Today、Inbox、Planner、Schedule、Boards 等工作视图
- 没有周计划列、逾期任务区和时间容量
- 没有日历时间轴
- 没有艾森豪威尔四象限
- 没有 Kanban
- 没有拖拽、调整时间、快速创建和行内操作
- 顶部全局工具栏过于简单
- 首页仍是传统卡片 Dashboard
- 专注计时器占据过大空间
- 页面宽度利用率和信息密度远低于参考项目
- 缺少细腻的 Hover、展开、抽屉、菜单、拖拽和切换动效
- 基本没有引入适合复杂生产力工具的前端组件库

禁止再次只修改 `main.css`、背景色、按钮颜色和圆角后宣布完成。

二、总体目标

将 Co-Learning 从“普通后台管理式伴学平台”升级为“高密度、可规划、可拖拽、可专注的学习生产力工作台”。

保留现有：

- 用户与认证
- 科目、目标和任务
- 专注计时
- 学习统计
- 打卡与复盘
- 日志
- 自习室
- 排行榜
- 宠物、成就和每日任务

新增并真实实现：

- Today 今日工作台
- Inbox 收件箱
- Planner 多日规划器
- Schedule 日历时间轴
- Boards：艾森豪威尔矩阵与 Kanban
- 全局快速创建
- 全局搜索
- 科目和标签侧栏
- 任务拖拽与时间规划
- 紧凑专注控制器

不允许只创建静态页面。新增功能必须有真实数据、API、数据库持久化和测试。

三、先保护当前成果

当前工作树有大量未提交前端修改。开始前必须：

1. 执行 `git status` 和 `git diff --stat`
2. 不得 reset、checkout 或覆盖现有修改
3. 运行现有前后端测试并记录基线
4. 保存当前页面截图
5. 建立文件所有权清单
6. Flyway 已有迁移禁止修改，新迁移从当前最大版本之后开始

四、使用成熟组件库

不要手写复杂日历和拖拽引擎。先评估许可证、Vue 3 兼容性和维护状态，再优先采用：

- Naive UI：布局、菜单、抽屉、弹窗、表单、日期选择、菜单、Popover、Tabs、Segmented
- `@fullcalendar/vue3`
- `@fullcalendar/daygrid`
- `@fullcalendar/timegrid`
- `@fullcalendar/interaction`
- `sortablejs` 或维护良好的 Vue 3 Sortable 封装
- `lucide-vue-next`：统一工具图标
- `@vueuse/motion`：必要的界面过渡
- `cmdk-vue` 或同等级方案：全局命令面板
- 现有 ECharts：统计图表

禁止：

- 手写完整日历布局和日期计算
- 手写不可靠的拖拽系统
- 使用 Emoji 作为正式图标
- 为展示效果加入无法操作的假组件
- 引入多个功能重复的 UI 库

五、重构应用外壳

复现参考截图中的结构：

- 约 248-272px 的可折叠左侧栏
- 约 56px 高的顶部全局工具栏
- 主工作区根据页面决定宽度
- Today/任务列表可以居中控制阅读宽度
- Planner、Schedule、Boards 必须使用完整剩余工作区
- 侧栏和主区独立滚动
- 顶部工具栏固定
- 移动端侧栏变为 Drawer
- 不能出现双滚动条和无意义的大边距

侧栏信息架构调整为：

- Today
- Inbox，显示未规划任务数量
- Planner
- Schedule
- Boards
- 每日复盘
- 分割线
- 科目，可展开并快速新增
- 标签，可展开并快速新增
- 学习统计
- 自习室
- 日志
- 排行榜
- 宠物与成就
- 底部：搜索、主题、用户菜单

顶部工具栏包含：

- 当前页面标题
- 快速开始/恢复专注
- 全局快速创建
- 搜索或命令面板
- 主题切换
- 通知
- 用户菜单

六、重新设计 Today

不要保留当前“四个统计卡片 + 巨型计时器”的结构。

Today 应参考 super-productivity：

- 今日标题与日期
- 顶部紧凑学习摘要条
- 今日已规划任务
- 逾期任务折叠区
- Inbox 中可加入今天的任务
- 每条任务使用紧凑 Task Row
- “加入今天”“完成今日计划”等明确命令
- 页面顶部或工具栏提供紧凑专注按钮
- 当前计时状态显示为迷你播放器
- 点击迷你播放器进入完整专注模式
- 学习统计、宠物信息作为次要区域，不能抢占任务主流程

Task Row 至少包含：

- 完成复选框
- 任务标题
- 科目和标签
- 计划日期
- 预计时长
- 实际专注时长
- 优先级或四象限标记
- 评论/备注提示
- Hover 后出现编辑、规划、删除等操作
- 支持拖拽排序

七、新增 Inbox

Inbox 展示所有尚未规划日期的任务：

- 快速输入任务
- Enter 创建
- 可选择科目、标签、预计时长
- 可拖入 Today、Planner、Schedule、Boards
- 支持批量规划
- 显示任务总数
- 不能和现有任务清单形成两套独立数据

Inbox、Today、Planner、Schedule、Boards 必须共享同一个任务实体。

八、新增 Planner

参考提供的 Planner 截图，高保真实现：

- 左侧逾期任务列
- 后续多个日期列
- 每列显示日期、星期
- 显示 Planned 和 Available 时间
- 每列支持快速新增
- 任务可以跨列拖拽
- 拖到某天即更新计划日期
- 支持前后翻页和返回今天
- 支持按科目、标签过滤
- 桌面端横向工作区
- 移动端改为单日分页或横向 Snap，不压缩成无法阅读的小列

规划结果必须持久化，刷新后不能丢失。

九、新增 Schedule

使用 FullCalendar 实现真实日历：

- 日视图
- 周视图
- 月视图
- 时间轴
- 当前时间线
- 今日高亮
- 上一周期、下一周期、今天
- 点击空白时间创建任务
- 拖动任务修改时间
- 调整任务块高度修改时长
- 未规划任务抽屉
- 可将未规划任务拖入日历
- 科目颜色映射到日历任务
- 点击任务打开编辑抽屉
- 正确处理时区和跨日任务
- 移动端提供日程列表/日视图

需要为任务增加缺失的计划字段，例如：

- plannedDate
- scheduledStart
- scheduledEnd
- estimatedMinutes
- sortOrder

必须先检查现有表结构，不得重复创建已有字段。

十、新增 Boards

Boards 顶部使用 Tabs：

- 艾森豪威尔矩阵
- Kanban

艾森豪威尔矩阵：

- 紧急且重要
- 不紧急但重要
- 紧急但不重要
- 不紧急且不重要
- 2×2 稳定布局
- 任务可在四个象限间拖拽
- 拖拽后持久化 urgent、important
- 每个象限支持快速新增
- 任务使用与 Today 相同的 Task Row/Compact Task Card

Kanban：

- 待办
- 进行中
- 已完成
- 已归档，可作为隐藏列或筛选
- 支持列内排序
- 支持跨列拖拽
- 拖拽后更新任务状态和 sortOrder
- 支持按科目和标签过滤
- 不得复制出第二套任务数据库

十一、科目和标签

现有科目映射为参考项目中的 Projects：

- 侧栏展开显示
- 每个科目有颜色
- 点击进入该科目任务视图
- 支持侧栏快速新增
- 显示未完成任务数量

如果现有项目没有标签系统，则增加：

- Tag
- TaskTag 关联
- 标签颜色
- 标签筛选
- 侧栏标签分组
- 多标签选择

数据库修改必须使用新的 Flyway 迁移。

十二、动效和交互

必须实现克制但明显的交互细节：

- 侧栏展开/折叠过渡
- 导航 Active 指示器
- Task Row Hover 后工具渐显
- Checkbox 完成动画
- 新建任务插入动画
- 任务完成后平滑移出
- Tabs 指示器滑动
- Drawer、Modal、Popover 进入退出动效
- 拖拽占位、拖拽镜像和有效落点反馈
- 日历任务拖动和调整尺寸反馈
- 专注播放器展开为专注模式的过渡
- Loading 使用 Skeleton，禁止整页转圈
- 支持 `prefers-reduced-motion`

不要加入装饰性大动画、光球、粒子和无意义渐变。

十三、视觉标准

不要继续沿用当前大量大卡片的 Dashboard 风格。

参考项目的关键视觉特征：

- 主背景接近黑色但有层级
- 侧栏、工具栏、任务行使用不同深度的中性表面
- 1px 低对比度边框
- 活跃导航使用中性灰表面，而非大面积绿色
- 主要强调色接近参考项目的蓝色
- 绿色只用于完成、成功和专注状态
- 红色只用于逾期和危险操作
- 圆角控制在 4-8px
- 任务行高度稳定
- 字体层级紧凑
- 页面标题不宜过大
- 工具按钮优先使用图标
- 所有陌生图标提供 Tooltip
- 禁止卡片套卡片
- 禁止把每个统计数字做成独立大卡片
- 禁止大面积空白浪费工作区

十四、多 Agent 分工

最多 4 个 Agent：

Agent 1：应用外壳、设计系统、全局工具栏、侧栏、公共 Task Row

Agent 2：Today、Inbox、Planner、任务 API 和数据库字段

Agent 3：Schedule、FullCalendar、时间规划和时区处理

Agent 4：Boards、拖拽、响应式、E2E 和视觉验收

规则：

- 同一文件只能由一个 Agent 修改
- Agent 1 完成公共组件后，其他 Agent 才能并行
- 所有视图必须复用同一个 Task Row 和任务实体
- 主 Agent 负责 API 契约、数据库迁移、集成测试和最终一致性
- 不得让多个 Agent 分别发明自己的颜色和组件风格

十五、测试要求

必须补充：

- Planner 日期分配和容量计算测试
- Schedule 创建、移动、调整时长测试
- 时区和跨日边界测试
- 四象限状态转换测试
- Kanban 状态和排序测试
- 拖拽失败回滚测试
- Today、Inbox 和 Planner 数据一致性测试
- 后端权限和任务所有权测试
- Flyway 全新数据库迁移测试
- 前端组件测试
- Playwright 核心 E2E
- 桌面和移动端截图测试

执行并记录：

- npm run build
- npm run lint
- npm run test -- --run
- 后端完整 Maven 测试
- Playwright E2E
- docker compose config
- docker compose up -d --build

十六、视觉验收

至少截图验证：

- 1440×900
- 1280×800
- 1024×768
- 768×1024
- 390×844
- 375×667

必须提供以下页面的改造后截图：

- Today
- Inbox
- Planner
- Schedule 周视图
- Schedule 移动端日视图
- 艾森豪威尔矩阵
- Kanban
- 完整专注模式
- 侧栏展开和折叠状态

将改造后页面与用户提供的参考截图并排比较，逐项检查：

- 工作区利用率
- 信息密度
- 侧栏层级
- 顶部工具栏
- 任务行
- 拖拽反馈
- 边框、间距和字号
- 空状态
- 动效
- 响应式

十七、拒收条件

出现以下任一情况都不能宣布完成：

- 只修改颜色、圆角或 CSS
- Planner、Schedule、Boards 是静态假页面
- 拖拽后刷新数据丢失
- 新增第二套任务实体
- 日历由手写日期网格实现
- 任务行在各页面风格不一致
- 没有实际使用 FullCalendar 或可靠拖拽库
- 没有后端持久化
- 没有 E2E
- 只有桌面端，没有移动端方案
- 页面出现横向溢出、双滚动条或文字遮挡
- 为了通过测试而跳过测试
- 没有提供截图对比
- 仍然保留“四张统计卡片 + 巨型计时器”作为 Today 主结构

现在开始执行：

1. 审计当前未提交修改并保护它们。
2. 分析用户提供的五张截图和参考源码。
3. 输出功能模型、数据库字段、组件库选择和页面线框。
4. 先完成 AppShell、Task Row 和设计系统。
5. 先用真实数据完成 Today 页面并截图验收。
6. Today 达到参考项目的信息密度后，再实现 Inbox、Planner、Schedule 和 Boards。
7. 完成持久化、测试、浏览器验证和 Docker 验收。
8. 不要只给方案，必须实际实现。
9. 无法验证的内容必须明确说明，禁止虚构测试通过。