# Dashboard 视觉升级设计文档

日期：2026-08-13
范围：仅 `frontend/src/views/DashboardView.vue`（必要时在 `main.css` 增加 1-2 个通用工具类）
方向：精致质感型（克制的高级感）

## 背景

Dashboard 功能完整（问候区 / 统计块 / 激励横幅 / 专注计时 / 考试倒计时 / 今日任务 / 快捷入口），
但视觉偏朴素：平铺白色卡片 + 默认蓝色进度条，缺少层次与细节。本次升级在不改变功能与设计系统
token 的前提下提升视觉品质。

## 设计决策

### ① 问候区
- 日期改为小号眉题（M月D日 · 周X，muted、字母间距），下方大号问候（28px、semibold、tight）。
- 右侧新增"连续打卡"徽章（Flame 图标 + 连续 N 天，琥珀色系胶囊），每日复盘按钮保留。
- 连续天数从统计区移入问候区徽章，统计区聚焦"时间投入"。

### ② 统计区（3 张卡片）
- 卡片：顶部 2px 蓝→紫渐变细线（::before），大数字（20px、tabular-nums），label 前加 14px 小图标。
- hover：translateY(-2px) + shadow-2 + 边框微亮，transition-standard。
- 数字加载时 count-up 滚动（500ms，easeOutCubic，rAF 实现）。
- 日目标卡片：进度条改蓝→紫渐变、高 6px、圆角，右侧显示百分比；百分比同样动画过渡。

### ③ 激励横幅
- 图标放入蓝→紫渐变圆角小方块（白色 lucide 图标），背景极淡渐变 + 左侧 3px 品牌色条。
- 连续 ≥7 天时切换琥珀色系变体。

### ④ 考试倒计时
- 弃用 NTag，改自定义 pill 卡片：色点 + 考试名 + N 天；≤30 天红色、≤60 天琥珀、其余品牌色；hover 微抬升。

### ⑤ 专注计时 / 今日任务 / 快捷入口
- 卡片容器统一：内边距、圆角、边框一致化；组件本身（FocusTimer / TaskList）不动。
- 快捷入口 hover 时图标变品牌色。

### ⑥ 动效
- 页面加载后各区块依次 fadeInUp（stagger 0.06s，delay 通过行内 style 绑定）。
- 全部使用既有 token（--duration-md、--ease-enter、--shadow-*）；暗色模式自动适配；
  全局 `prefers-reduced-motion` 规则已禁用动画，无需额外处理。

## 非目标
- 不改其他页面、不改后端、不改设计系统 token 定义、不引入新依赖。
- 不改变任何数据获取逻辑与交互行为。

## 验收标准
- `npm run lint` 通过；`vue-tsc -b` 类型检查通过；`npm run build` 构建成功。
- 明/暗主题下布局正常，移动端统计区纵向堆叠。
