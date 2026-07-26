<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { useGamificationStore } from '@/stores/gamificationStore'
import {
  NCard, NButton, NProgress, NTag, NSpin,
} from 'naive-ui'

const message = useMessage()
const store = useGamificationStore()

const taskTypeShort: Record<string, string> = {
  FOCUS_ONCE: 'FO',
  FOCUS_30MIN: '30',
  FOCUS_60MIN: '60',
  FOCUS_120MIN: '2h',
  FEED_PET: 'FD',
  CHECKIN: 'CK',
  WRITE_JOURNAL: 'JR',
}

const statusLabels: Record<string, string> = {
  IN_PROGRESS: '进行中',
  COMPLETED: '待领取',
  CLAIMED: '已领取',
}

const statusColors: Record<string, 'info' | 'success' | 'default'> = {
  IN_PROGRESS: 'info',
  COMPLETED: 'success',
  CLAIMED: 'default',
}

function formatProgress(task: typeof store.dailyTasks[0]): string {
  if (task.taskType.startsWith('FOCUS')) {
    const minutes = Math.floor(task.currentProgress / 60)
    const targetMinutes = Math.floor(task.targetValue / 60)
    return `${minutes}/${targetMinutes} min`
  }
  return `${task.currentProgress}/${task.targetValue}`
}

const completedCount = computed(() =>
  store.dailyTasks.filter((t: typeof store.dailyTasks[0]) => t.status === 'COMPLETED' || t.status === 'CLAIMED').length
)

const totalCount = computed(() => store.dailyTasks.length)

const totalReward = computed(() =>
  store.dailyTasks.filter((t: typeof store.dailyTasks[0]) => t.status === 'COMPLETED').reduce((sum: number, t: typeof store.dailyTasks[0]) => sum + t.rewardTokens, 0)
)

async function handleClaim(taskId: number) {
  try {
    await store.claimTaskReward(taskId)
    message.success('领取成功！')
  } catch (e: any) {
    message.error(e?.response?.data?.message || '领取失败')
  }
}

onMounted(() => {
  store.loadDailyTasks()
  store.loadProfile()
})
</script>

<template>
  <div class="daily-tasks-view">
    <h2 class="page-title">每日任务</h2>

    <!-- Stats Card -->
    <n-card class="stats-card" :bordered="false">
      <div class="stats-row">
        <div class="stat-block">
          <span class="stat-label">今日进度</span>
          <span class="stat-value">{{ completedCount }}/{{ totalCount }}</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-block">
          <span class="stat-label">待领取</span>
          <span class="stat-value stat-value--brand">{{ totalReward }} T</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-block">
          <span class="stat-label">我的代币</span>
          <span class="stat-value">{{ store.profile?.tokens || 0 }} T</span>
        </div>
      </div>
    </n-card>

    <n-spin :show="store.loading">
      <!-- Tasks List -->
      <div class="task-list">
        <div
          v-for="task in store.dailyTasks"
          :key="task.id"
          class="task-row"
          :class="{ 'task-row--completed': task.status === 'COMPLETED', 'task-row--claimed': task.status === 'CLAIMED' }"
        >
          <span class="task-badge">{{ taskTypeShort[task.taskType] || '??' }}</span>
          <div class="task-info">
            <div class="task-top">
              <h3 class="task-title">{{ task.title }}</h3>
              <n-tag :type="statusColors[task.status]" size="tiny" round>
                {{ statusLabels[task.status] }}
              </n-tag>
            </div>
            <div class="task-progress-row">
              <n-progress
                type="line"
                :percentage="Math.min(100, Math.round((task.currentProgress / task.targetValue) * 100))"
                :show-indicator="false"
                :height="4"
                class="task-bar"
              />
              <span class="task-progress-text">{{ formatProgress(task) }}</span>
            </div>
          </div>
          <div class="task-action">
            <span class="task-reward">{{ task.rewardTokens }} T</span>
            <n-button
              v-if="task.status === 'COMPLETED'"
              size="tiny"
              type="primary"
              @click="handleClaim(task.id)"
            >
              领取
            </n-button>
            <span v-else-if="task.status === 'CLAIMED'" class="task-claimed-label">--</span>
          </div>
        </div>
      </div>

      <n-empty v-if="store.dailyTasks.length === 0" description="加载中..." class="section-empty" />
    </n-spin>

    <!-- Tips -->
    <n-card class="tips-card" :bordered="false">
      <h3 class="tips-title">小贴士</h3>
      <ul class="tips-list">
        <li>每日任务在凌晨0点刷新</li>
        <li>完成任务后请及时领取奖励</li>
        <li>专注时间会累积到多个任务中</li>
        <li>所有任务奖励合计可达45代币/天</li>
      </ul>
    </n-card>
  </div>
</template>

<style scoped>
.daily-tasks-view {
  padding: var(--sp-4);
}

.page-title {
  margin: 0 0 var(--sp-4) 0;
  font-size: var(--text-xl);
}

/* --- Stats Card --- */
.stats-card {
  margin-bottom: var(--sp-4);
  background: var(--surface-2);
  border: 1px solid var(--divider);
  border-radius: var(--radius-md);
}

.stats-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.stat-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-label {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

.stat-value {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--text-color);
}

.stat-value--brand {
  color: var(--brand);
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: var(--separator);
}

/* --- Task List --- */
.task-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
}

.task-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3);
  background: var(--surface-2);
  border: 1px solid var(--divider);
  border-left: 3px solid transparent;
  border-radius: var(--radius-md);
  transition: border-color var(--transition-fast), opacity var(--transition-fast);
}

.task-row--completed {
  border-left-color: var(--success);
}

.task-row--claimed {
  opacity: 0.6;
}

.task-badge {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  background: var(--bg-page);
  border: 1px solid var(--separator);
  font-size: 11px;
  font-weight: var(--weight-bold);
  color: var(--text-color-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.task-info {
  flex: 1;
  min-width: 0;
}

.task-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--sp-2);
  margin-bottom: var(--sp-2);
}

.task-title {
  margin: 0;
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  color: var(--text-color);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-progress-row {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.task-bar {
  flex: 1;
  min-width: 0;
}

.task-progress-text {
  font-size: 11px;
  color: var(--text-color-muted);
  flex-shrink: 0;
  min-width: 50px;
  text-align: right;
}

.task-action {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: var(--sp-1);
  flex-shrink: 0;
}

.task-reward {
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  color: var(--brand);
}

.task-claimed-label {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

/* --- Tips --- */
.tips-card {
  margin-top: var(--sp-4);
  background: var(--surface-2);
  border: 1px solid var(--divider);
  border-radius: var(--radius-md);
}

.tips-title {
  margin: 0 0 var(--sp-2) 0;
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  color: var(--text-color);
}

.tips-list {
  margin: 0;
  padding-left: var(--sp-5);
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.tips-list li {
  margin-bottom: var(--sp-1);
}

.section-empty {
  padding: var(--sp-8) 0;
}

@media (max-width: 768px) {
  .stats-row {
    gap: var(--sp-3);
    flex-wrap: wrap;
    justify-content: center;
  }

  .stat-divider {
    display: none;
  }
}
</style>
