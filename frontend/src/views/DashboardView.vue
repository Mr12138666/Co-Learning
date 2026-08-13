<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, type StyleValue } from 'vue'
import { useRouter } from 'vue-router'
import {
  NEmpty,
  NButton,
  NModal,
  NForm,
  NFormItem,
  NInputNumber,
  NSpin,
  NIcon,
  useMessage,
} from 'naive-ui'
import {
  TrophyOutline,
  SchoolOutline,
  CheckboxOutline,
  StatsChartOutline,
  TodayOutline,
  BookOutline,
} from '@vicons/ionicons5'
import { Flame, Timer, CalendarRange, Target, Sparkles } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/authStore'
import { useStudyStore } from '@/stores/studyStore'
import { useDashboardStore } from '@/stores/dashboardStore'
import { userApi, type UserProfileResponse } from '@/api/user'
import { usePageLoad } from '@/composables/usePageLoad'
import FocusTimer from '@/components/focus/FocusTimer.vue'
import TaskList from '@/components/study/TaskList.vue'
import StateError from '@/components/common/StateError.vue'

const router = useRouter()
const authStore = useAuthStore()
const studyStore = useStudyStore()
const dashboardStore = useDashboardStore()
const profile = ref<UserProfileResponse | null>(null)
const message = useMessage()
const { loading, error, load, retry } = usePageLoad()

// Daily goal edit modal
const showEditModal = ref(false)
const editGoalForm = ref({
  hours: 2,
  minutes: 0,
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const displayName = computed(
  () => authStore.user?.displayName || authStore.user?.email?.split('@')[0] || '同学',
)

const today = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return d.getMonth() + 1 + '月' + d.getDate() + '日 周' + weekdays[d.getDay()]
})

// 今日激励文案
const motivation = computed(() => {
  const s = dashboardStore.stats
  const st = s?.streakDays ?? 0
  if (st >= 7) return '已连续专注 ' + st + ' 天，太强了！保持这个节奏 🌟'
  if (st >= 3) return '连续 ' + st + ' 天打卡，习惯正在形成，继续加油 💪'
  if (st > 0) return '今天也要延续连续 ' + st + ' 天的势头哦 ✨'
  return '新的一天，从一次专注开始 🚀'
})

// Stats helpers
function formatMinutes(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  if (mins < 60) return mins + 'min'
  const hours = Math.floor(mins / 60)
  return hours + 'h' + (mins % 60) + 'm'
}

function formatTimeFromMinutes(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) return hours + 'h' + mins + 'min'
  if (hours > 0) return hours + 'h'
  return mins + 'min'
}

const todayFocus = computed(() => dashboardStore.stats?.todayFocusSeconds ?? 0)
const weekFocus = computed(() => dashboardStore.stats?.weekFocusSeconds ?? 0)
const streak = computed(() => dashboardStore.stats?.streakDays ?? 0)
const checkinCompleted = computed(() => dashboardStore.todayCheckin?.completed ?? false)

// Daily goal from user settings, default to 120 minutes
const dailyGoalMin = computed(() => profile.value?.dailyFocusGoalMinutes ?? 120)
const dailyProgress = computed(() => {
  const mins = Math.floor(todayFocus.value / 60)
  return Math.min(100, Math.round((mins / dailyGoalMin.value) * 100))
})

// ===== Count-up animation (数字滚动) =====
function useCountUp(getTarget: () => number, duration = 500) {
  const value = ref(0)
  let raf = 0
  const reducedMotion =
    typeof window !== 'undefined' &&
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches

  const animate = (target: number) => {
    cancelAnimationFrame(raf)
    if (reducedMotion) {
      value.value = target
      return
    }
    const from = value.value
    const start = performance.now()
    const step = (now: number) => {
      const t = Math.min(1, (now - start) / duration)
      const eased = 1 - Math.pow(1 - t, 3)
      value.value = Math.round(from + (target - from) * eased)
      if (t < 1) raf = requestAnimationFrame(step)
    }
    raf = requestAnimationFrame(step)
  }

  watch(getTarget, animate, { immediate: true })
  onUnmounted(() => cancelAnimationFrame(raf))
  return value
}

const todayFocusShown = useCountUp(() => todayFocus.value)
const weekFocusShown = useCountUp(() => weekFocus.value)
const streakShown = useCountUp(() => streak.value)
const goalPctShown = useCountUp(() => dailyProgress.value, 700)

// 页面区块依次浮现的延迟
function enterDelay(index: number): StyleValue {
  return { animationDelay: (index * 0.06) + 's' }
}

// 考试倒计时色调
function examTone(days: number): 'danger' | 'warning' | 'brand' {
  if (days <= 30) return 'danger'
  if (days <= 60) return 'warning'
  return 'brand'
}

async function loadProfile() {
  try {
    const res = await userApi.getMyProfile()
    profile.value = res.data.data
  } catch {
    // Ignore profile load errors
  }
}

function openEditModal() {
  const totalMin = profile.value?.dailyFocusGoalMinutes ?? 120
  editGoalForm.value.hours = Math.floor(totalMin / 60)
  editGoalForm.value.minutes = totalMin % 60
  showEditModal.value = true
}

async function saveDailyGoal() {
  const totalMinutes = editGoalForm.value.hours * 60 + editGoalForm.value.minutes
  if (totalMinutes < 1) {
    message.error('日目标至少为1分钟')
    return
  }
  try {
    await userApi.updateSettings({ dailyFocusGoalMinutes: totalMinutes })
    await loadProfile()
    message.success('日目标已更新')
    showEditModal.value = false
  } catch (error: any) {
    message.error(error.response?.data?.message || '更新失败')
  }
}

async function loadDashboard() {
  await Promise.all([
    studyStore.fetchAll(),
    dashboardStore.refreshAll(),
    loadProfile(),
  ])
}

onMounted(() => load(loadDashboard))
</script>

<template>
  <div class="dashboard">
    <!-- Loading State -->
    <div v-if="loading" class="dashboard__loader">
      <NSpin size="large" />
    </div>

    <!-- Error State -->
    <StateError
      v-else-if="error"
      :title="error"
      @retry="retry(loadDashboard)"
    />

    <template v-else>
    <!-- 1. Greeting Header -->
    <header class="dashboard__header dash-enter">
      <div class="dashboard__header-left">
        <span class="dashboard__eyebrow">{{ today }}</span>
        <h2 class="dashboard__greeting">
          {{ greeting }}，{{ displayName }}
        </h2>
      </div>
      <div class="dashboard__header-actions">
        <div
          class="streak-badge"
          :class="{ 'streak-badge--hot': streak >= 7 }"
          :title="'已连续专注 ' + streak + ' 天'"
        >
          <Flame :size="15" class="streak-badge__icon" />
          <span class="streak-badge__text">连续 <strong>{{ streakShown }}</strong> 天</span>
        </div>
        <NButton size="small" type="primary" @click="router.push('/checkin')">
          {{ checkinCompleted ? '查看复盘' : '每日复盘' }}
        </NButton>
      </div>
    </header>

    <!-- 2. Stats Row -->
    <section class="dashboard__stats dash-enter" :style="enterDelay(1)">
      <div class="stat-block">
        <span class="stat-block__label">
          <Timer :size="13" class="stat-block__icon" />
          今日专注
        </span>
        <span class="stat-block__value">{{ formatMinutes(todayFocusShown) }}</span>
      </div>
      <div class="stat-block">
        <span class="stat-block__label">
          <CalendarRange :size="13" class="stat-block__icon" />
          本周专注
        </span>
        <span class="stat-block__value">{{ formatMinutes(weekFocusShown) }}</span>
      </div>
      <div class="stat-block stat-block--goal">
        <div class="stat-block__goal-header">
          <span class="stat-block__label">
            <Target :size="13" class="stat-block__icon" />
            日目标
          </span>
          <span class="stat-block__goal-pct">{{ goalPctShown }}%</span>
        </div>
        <div class="goal-bar">
          <div class="goal-bar__fill" :style="{ width: goalPctShown + '%' }" />
        </div>
        <div class="stat-block__goal-footer">
          <span class="stat-block__goal-text">目标 {{ formatTimeFromMinutes(dailyGoalMin) }}</span>
          <NButton text size="tiny" @click="openEditModal">编辑</NButton>
        </div>
      </div>
    </section>

    <!-- 2.5 今日激励 banner -->
    <section
      class="dashboard__banner dash-enter"
      :class="{ 'dashboard__banner--streak': streak >= 7 }"
      :style="enterDelay(2)"
    >
      <span class="dashboard__banner-icon-box">
        <Sparkles :size="15" />
      </span>
      <span class="dashboard__banner-text">{{ motivation }}</span>
    </section>

    <!-- 3. Focus Timer -->
    <section class="dashboard__section dash-enter" :style="enterDelay(3)">
      <h4 class="dashboard__section-title">专注计时</h4>
      <div class="dashboard__timer-card">
        <FocusTimer @finished="dashboardStore.refreshAll()" />
      </div>
    </section>

    <!-- 4. Exam Countdown -->
    <section v-if="studyStore.activeGoals.length > 0" class="dashboard__section dash-enter" :style="enterDelay(4)">
      <h4 class="dashboard__section-title">考试倒计时</h4>
      <div class="exam-pills">
        <div
          v-for="goal in studyStore.activeGoals.slice(0, 3)"
          :key="goal.id"
          class="exam-pill"
          :class="'exam-pill--' + examTone(goal.daysRemaining)"
        >
          <span class="exam-pill__dot" />
          <span class="exam-pill__name">{{ goal.examName }}</span>
          <span class="exam-pill__days">{{ goal.daysRemaining }}天</span>
        </div>
      </div>
    </section>

    <!-- 5. Today Tasks -->
    <section class="dashboard__section dash-enter" :style="enterDelay(5)">
      <div class="dashboard__section-header">
        <h4 class="dashboard__section-title">今日任务</h4>
        <NButton text type="primary" size="small" @click="router.push('/tasks')">
          全部任务
        </NButton>
      </div>
      <div class="dashboard__tasks-card">
        <TaskList
          v-if="studyStore.todoTasks.length > 0 || studyStore.inProgressTasks.length > 0"
          filter-status="ALL"
        />
        <NEmpty v-else description="暂无任务，去创建一个吧" size="small">
          <template #extra>
            <NButton type="primary" size="small" @click="router.push('/tasks')">
              添加任务
            </NButton>
          </template>
        </NEmpty>
      </div>
    </section>

    <!-- 6. Quick Links -->
    <section class="dashboard__section dash-enter" :style="enterDelay(6)">
      <h4 class="dashboard__section-title">快捷入口</h4>
      <div class="dashboard__quick-links">
        <button class="quick-link" @click="router.push('/goals')">
          <NIcon size="18"><TrophyOutline /></NIcon>
          <span>考试目标</span>
        </button>
        <button class="quick-link" @click="router.push('/subjects')">
          <NIcon size="18"><SchoolOutline /></NIcon>
          <span>科目管理</span>
        </button>
        <button class="quick-link" @click="router.push('/tasks')">
          <NIcon size="18"><CheckboxOutline /></NIcon>
          <span>任务清单</span>
        </button>
        <button class="quick-link" @click="router.push('/stats')">
          <NIcon size="18"><StatsChartOutline /></NIcon>
          <span>学习统计</span>
        </button>
        <button class="quick-link" @click="router.push('/checkin')">
          <NIcon size="18"><TodayOutline /></NIcon>
          <span>每日复盘</span>
        </button>
        <button class="quick-link" @click="router.push('/journals')">
          <NIcon size="18"><BookOutline /></NIcon>
          <span>学习日志</span>
        </button>
      </div>
    </section>
    </template>
  </div>

  <!-- Daily Goal Edit Modal -->
  <NModal v-model:show="showEditModal" preset="card" title="设置日目标专注时长">
    <NForm :model="editGoalForm">
      <NFormItem label="日目标专注时长">
        <div class="modal-form-row">
          <NInputNumber v-model:value="editGoalForm.hours" :min="0" :max="24" :step="1" class="modal-input" />
          <span>小时</span>
          <NInputNumber v-model:value="editGoalForm.minutes" :min="0" :max="59" :step="5" class="modal-input" />
          <span>分钟</span>
        </div>
      </NFormItem>
      <div class="modal-actions">
        <NButton @click="showEditModal = false">取消</NButton>
        <NButton type="primary" @click="saveDailyGoal">保存</NButton>
      </div>
    </NForm>
  </NModal>
</template>

<style scoped>
/* ===== Layout ===== */
.dashboard {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.dashboard__loader {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--sp-14) 0;
}

/* 区块依次浮现 */
.dash-enter {
  animation: fadeInUp var(--duration-md) var(--ease-enter) both;
}

/* ===== Header ===== */
.dashboard__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-4);
  margin-bottom: var(--sp-5);
}

.dashboard__header-left {
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
}

.dashboard__eyebrow {
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-color-muted);
  letter-spacing: var(--tracking-wide);
}

.dashboard__greeting {
  margin: 0;
  font-size: var(--text-3xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  line-height: var(--leading-tight);
  letter-spacing: var(--tracking-tighter);
}

.dashboard__header-actions {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  flex-wrap: wrap;
  justify-content: flex-end;
}

/* 连续打卡徽章 */
.streak-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 28px;
  padding: 0 var(--sp-3);
  border-radius: var(--radius-pill);
  border: 1px solid var(--divider);
  background: var(--bg-card);
  color: var(--text-color-muted);
  font-size: var(--text-sm);
}

.streak-badge__icon {
  color: var(--warning);
}

.streak-badge__text strong {
  color: var(--text-color-strong);
  font-weight: var(--weight-semibold);
  font-variant-numeric: tabular-nums;
}

.streak-badge--hot {
  border-color: var(--warning-muted);
  background: linear-gradient(90deg, var(--warning-muted), transparent 80%);
}

.streak-badge--hot .streak-badge__icon {
  animation: flame-flicker 1.6s ease-in-out infinite;
}

@keyframes flame-flicker {
  0%, 100% { transform: scale(1) rotate(-2deg); }
  50% { transform: scale(1.15) rotate(2deg); }
}

/* ===== Stats Row ===== */
.dashboard__stats {
  display: flex;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
  flex-wrap: wrap;
}

.stat-block {
  position: relative;
  flex: 1;
  min-width: 120px;
  padding: var(--sp-3) var(--sp-4) var(--sp-4);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  overflow: hidden;
  transition:
    transform var(--transition-standard),
    box-shadow var(--transition-standard),
    border-color var(--transition-standard);
}

/* 顶部渐变细线 */
.stat-block::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, var(--brand), var(--urgent));
  opacity: 0.75;
}

.stat-block:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-2);
  border-color: var(--separator);
}

.stat-block:active {
  transform: translateY(0);
}

.stat-block__label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  margin-bottom: var(--sp-1);
  letter-spacing: var(--tracking-wide);
}

.stat-block__icon {
  color: var(--text-color-muted);
  opacity: 0.8;
}

.stat-block__value {
  display: block;
  font-size: var(--text-2xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  font-variant-numeric: tabular-nums;
  line-height: var(--leading-tight);
}

/* 日目标卡片 */
.stat-block--goal {
  min-width: 220px;
  flex: 1.4;
}

.stat-block__goal-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.stat-block__goal-pct {
  font-size: var(--text-2xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  font-variant-numeric: tabular-nums;
  line-height: var(--leading-tight);
}

.goal-bar {
  margin-top: var(--sp-2);
  height: 6px;
  border-radius: var(--radius-pill);
  background: var(--surface-1);
  border: 1px solid var(--divider);
  overflow: hidden;
}

.goal-bar__fill {
  height: 100%;
  border-radius: var(--radius-pill);
  background: linear-gradient(90deg, var(--brand), var(--urgent));
  min-width: 2px;
}

.stat-block__goal-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: var(--sp-1);
}

.stat-block__goal-text {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

/* ===== Sections ===== */
.dashboard__section {
  margin-bottom: var(--sp-4);
}

.dashboard__section-title {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  margin: 0 0 var(--sp-3);
}

.dashboard__section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-3);
}

.dashboard__section-header .dashboard__section-title {
  margin-bottom: 0;
}

/* ===== Motivation Banner ===== */
.dashboard__banner {
  position: relative;
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  margin-bottom: var(--sp-4);
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  background: linear-gradient(90deg, var(--brand-subtle), transparent 70%);
  overflow: hidden;
}

/* 左侧品牌色条 */
.dashboard__banner::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, var(--brand), var(--urgent));
}

.dashboard__banner--streak {
  border-color: var(--warning-muted);
  background: linear-gradient(90deg, var(--warning-muted), transparent 70%);
}

.dashboard__banner--streak::before {
  background: linear-gradient(180deg, var(--warning), var(--danger));
}

.dashboard__banner-icon-box {
  width: 30px;
  height: 30px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--brand), var(--urgent));
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.dashboard__banner--streak .dashboard__banner-icon-box {
  background: linear-gradient(135deg, var(--warning), var(--danger));
}

.dashboard__banner-text {
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-color);
}

/* ===== Timer ===== */
.dashboard__timer-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  padding: var(--sp-4);
}

/* ===== Exam Pills ===== */
.exam-pills {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

.exam-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-2);
  height: 32px;
  padding: 0 var(--sp-3);
  border-radius: var(--radius-pill);
  border: 1px solid var(--divider);
  background: var(--bg-card);
  font-size: var(--text-sm);
  transition:
    transform var(--transition-fast),
    box-shadow var(--transition-fast),
    border-color var(--transition-fast);
}

.exam-pill:hover {
  transform: translateY(-1px);
  box-shadow: var(--shadow-1);
  border-color: var(--separator);
}

.exam-pill__dot {
  width: 8px;
  height: 8px;
  border-radius: var(--radius-pill);
  flex-shrink: 0;
}

.exam-pill--danger .exam-pill__dot { background: var(--danger); }
.exam-pill--warning .exam-pill__dot { background: var(--warning); }
.exam-pill--brand .exam-pill__dot { background: var(--brand); }

.exam-pill--danger { border-color: var(--danger-muted); background: linear-gradient(90deg, var(--danger-muted), transparent 70%); }
.exam-pill--warning { border-color: var(--warning-muted); background: linear-gradient(90deg, var(--warning-muted), transparent 70%); }
.exam-pill--brand { border-color: var(--brand-subtle); background: linear-gradient(90deg, var(--brand-subtle), transparent 70%); }

.exam-pill__name {
  color: var(--text-color);
  font-weight: var(--weight-medium);
}

.exam-pill__days {
  color: var(--text-color-muted);
  font-size: var(--text-xs);
  font-variant-numeric: tabular-nums;
}

.exam-pill--danger .exam-pill__days { color: var(--danger); font-weight: var(--weight-semibold); }
.exam-pill--warning .exam-pill__days { color: var(--warning); font-weight: var(--weight-semibold); }

/* ===== Tasks ===== */
.dashboard__tasks-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  padding: var(--sp-3);
}

/* ===== Quick Links ===== */
.dashboard__quick-links {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: var(--sp-2);
}

.quick-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--sp-1);
  padding: var(--sp-3) var(--sp-2);
  border: 1px solid var(--divider);
  border-radius: var(--radius-md);
  background: var(--bg-card);
  color: var(--text-color);
  font-size: var(--text-sm);
  font-family: inherit;
  cursor: pointer;
  transition:
    background-color var(--transition-fast),
    border-color var(--transition-fast),
    box-shadow var(--transition-fast),
    transform var(--transition-fast);
}

.quick-link:hover {
  background: var(--state-hover);
  border-color: var(--separator);
  box-shadow: var(--shadow-1);
  transform: translateY(-2px);
}

.quick-link:active {
  background: var(--state-pressed);
}

.quick-link .n-icon {
  color: var(--text-color-muted);
  transition: color var(--transition-fast);
}

.quick-link:hover .n-icon {
  color: var(--brand);
}

/* ===== Modal ===== */
.modal-form-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.modal-input {
  width: 100px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-2);
  margin-top: var(--sp-4);
}

/* ===== Responsive ===== */
@media (max-width: 640px) {
  .dashboard {
    padding: var(--sp-4) var(--sp-3);
  }

  .dashboard__header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--sp-3);
  }

  .dashboard__header-actions {
    justify-content: flex-start;
  }

  .dashboard__stats {
    flex-direction: column;
  }

  .stat-block {
    min-width: 0;
  }

  .stat-block--goal {
    min-width: 0;
  }

  .dashboard__quick-links {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
