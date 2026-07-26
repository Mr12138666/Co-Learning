<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NEmpty,
  NButton,
  NTag,
  NProgress,
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

const displayName = computed(() => authStore.user?.email?.split('@')[0] || '同学')

const today = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getMonth() + 1}月${d.getDate()}日 周${weekdays[d.getDay()]}`
})

// Stats helpers
function formatMinutes(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  if (mins < 60) return `${mins}min`
  const hours = Math.floor(mins / 60)
  return `${hours}h${mins % 60}m`
}

function formatTimeFromMinutes(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0 && mins > 0) return `${hours}h${mins}min`
  if (hours > 0) return `${hours}h`
  return `${mins}min`
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
    <!-- 1. Page Header: greeting + date + checkin -->
    <header class="dashboard__header">
      <div class="dashboard__header-left">
        <h2 class="dashboard__greeting">
          {{ greeting }}，{{ displayName }}
        </h2>
        <span class="dashboard__date">{{ today }}</span>
      </div>
      <NButton size="small" type="primary" @click="router.push('/checkin')">
        {{ checkinCompleted ? '查看复盘' : '每日复盘' }}
      </NButton>
    </header>

    <!-- 2. Stats Row -->
    <section class="dashboard__stats">
      <div class="stat-block">
        <span class="stat-block__label">今日专注</span>
        <span class="stat-block__value">{{ formatMinutes(todayFocus) }}</span>
      </div>
      <div class="stat-block">
        <span class="stat-block__label">本周专注</span>
        <span class="stat-block__value">{{ formatMinutes(weekFocus) }}</span>
      </div>
      <div class="stat-block">
        <span class="stat-block__label">连续天数</span>
        <span class="stat-block__value">{{ streak }}<span class="stat-block__unit">天</span></span>
      </div>
      <div class="stat-block stat-block--goal">
        <div class="stat-block__goal-header">
          <span class="stat-block__label">日目标</span>
          <span class="stat-block__goal-meta">
            <span class="stat-block__goal-text">{{ formatTimeFromMinutes(dailyGoalMin) }}</span>
            <NButton text size="tiny" @click="openEditModal">编辑</NButton>
          </span>
        </div>
        <NProgress
          type="line"
          :percentage="dailyProgress"
          :show-indicator="false"
          :height="4"
          class="goal-progress"
        />
      </div>
    </section>

    <!-- 3. Focus Timer -->
    <section class="dashboard__section">
      <h4 class="dashboard__section-title">专注计时</h4>
      <div class="dashboard__timer-card">
        <FocusTimer @finished="dashboardStore.refreshAll()" />
      </div>
    </section>

    <!-- 4. Exam Countdown -->
    <section v-if="studyStore.activeGoals.length > 0" class="dashboard__section">
      <h4 class="dashboard__section-title">考试倒计时</h4>
      <div class="dashboard__exam-tags">
        <NTag
          v-for="goal in studyStore.activeGoals.slice(0, 3)"
          :key="goal.id"
          size="medium"
          round
          :type="goal.daysRemaining <= 30 ? 'error' : goal.daysRemaining <= 60 ? 'warning' : 'info'"
        >
          {{ goal.examName }} · {{ goal.daysRemaining }}天
        </NTag>
      </div>
    </section>

    <!-- 5. Today Tasks -->
    <section class="dashboard__section">
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
    <section class="dashboard__section">
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
  animation: fadeInUp var(--duration-md) var(--ease-enter);
}

.dashboard__loader {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: var(--sp-14) 0;
}

/* ===== Header ===== */
.dashboard__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--sp-4);
  margin-bottom: var(--sp-4);
}

.dashboard__header-left {
  display: flex;
  flex-direction: column;
  gap: var(--sp-1);
}

.dashboard__greeting {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  line-height: var(--leading-tight);
}

.dashboard__date {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

/* ===== Stats Row ===== */
.dashboard__stats {
  display: flex;
  gap: var(--sp-3);
  margin-bottom: var(--sp-4);
  flex-wrap: wrap;
}

.stat-block {
  flex: 1;
  min-width: 100px;
  padding: var(--sp-3) var(--sp-4);
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
}

.stat-block__label {
  display: block;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  margin-bottom: var(--sp-1);
  letter-spacing: var(--tracking-wide);
}

.stat-block__value {
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  font-variant-numeric: tabular-nums;
  line-height: var(--leading-tight);
}

.stat-block__unit {
  font-size: var(--text-sm);
  font-weight: var(--weight-normal);
  color: var(--text-color-muted);
  margin-left: 2px;
}

.stat-block--goal {
  min-width: 160px;
}

.stat-block__goal-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.stat-block__goal-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
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

/* ===== Timer ===== */
.dashboard__timer-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--divider);
  padding: var(--sp-4);
}

/* ===== Exam Tags ===== */
.dashboard__exam-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
}

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
    box-shadow var(--transition-fast);
}

.quick-link:hover {
  background: var(--state-hover);
  border-color: var(--separator);
  box-shadow: var(--shadow-1);
}

.quick-link:active {
  background: var(--state-pressed);
}

.quick-link .n-icon {
  color: var(--text-color-muted);
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

.goal-progress {
  margin-top: 4px;
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

  .dashboard__stats {
    flex-direction: column;
  }

  .stat-block {
    min-width: 0;
  }

  .dashboard__quick-links {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
