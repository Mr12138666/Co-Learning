<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useMessage } from 'naive-ui'
import { useGamificationStore } from '@/stores/gamificationStore'
import {
  NCard, NSpace, NButton, NProgress, NTag, NGrid, NGridItem,
  NSpin, NStatistic,
} from 'naive-ui'

const message = useMessage()
const store = useGamificationStore()

const taskTypeIcons: Record<string, string> = {
  FOCUS_ONCE: '📚',
  FOCUS_30MIN: '⏱️',
  FOCUS_60MIN: '⏰',
  FOCUS_120MIN: '🔥',
  FEED_PET: '🍖',
  CHECKIN: '✅',
  WRITE_JOURNAL: '📝',
}

const taskTypeLabels: Record<string, string> = {
  FOCUS_ONCE: '专注一次',
  FOCUS_30MIN: '专注30分钟',
  FOCUS_60MIN: '专注1小时',
  FOCUS_120MIN: '专注2小时',
  FEED_PET: '喂食宠物',
  CHECKIN: '每日打卡',
  WRITE_JOURNAL: '写日志',
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
    return `${minutes}/${targetMinutes} 分钟`
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
      <n-space justify="space-around">
        <n-statistic label="今日进度" :value="`${completedCount}/${totalCount}`" />
        <n-statistic label="待领取代币" :value="totalReward">
          <template #suffix><span class="token-icon">🪙</span></template>
        </n-statistic>
        <n-statistic label="我的代币" :value="store.profile?.tokens || 0">
          <template #suffix><span class="token-icon">🪙</span></template>
        </n-statistic>
      </n-space>
    </n-card>

    <n-spin :show="store.loading">
      <!-- Tasks List -->
      <n-grid :cols="2" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
        <n-grid-item v-for="task in store.dailyTasks" :key="task.id" span="2 m:1">
          <n-card 
            class="task-card" 
            :bordered="false"
            :class="{ completed: task.status === 'COMPLETED', claimed: task.status === 'CLAIMED' }"
          >
            <div class="task-header">
              <span class="task-icon">{{ taskTypeIcons[task.taskType] || '📋' }}</span>
              <n-tag :type="statusColors[task.status]" size="small" round>
                {{ statusLabels[task.status] }}
              </n-tag>
            </div>
            
            <h3 class="task-title">{{ task.title }}</h3>
            <p class="task-desc">{{ task.description }}</p>
            
            <div class="task-progress">
              <n-progress
                type="line"
                :percentage="Math.min(100, Math.round((task.currentProgress / task.targetValue) * 100))"
                :show-indicator="false"
                :height="6"
                class="progress-bar"
              />
              <span class="progress-text">{{ formatProgress(task) }}</span>
            </div>
            
            <div class="task-footer">
              <span class="reward-text">奖励: {{ task.rewardTokens }} 🪙</span>
              <n-button 
                v-if="task.status === 'COMPLETED'"
                size="small" 
                type="primary" 
                @click="handleClaim(task.id)"
              >
                领取奖励
              </n-button>
              <n-tag v-else-if="task.status === 'CLAIMED'" size="small" type="success" round>
                已领取
              </n-tag>
            </div>
          </n-card>
        </n-grid-item>
      </n-grid>

      <n-empty v-if="store.dailyTasks.length === 0" description="加载中..." style="padding: 40px 0" />
    </n-spin>

    <!-- Tips -->
    <n-card class="tips-card" :bordered="false">
      <div class="tips-header">
        <span class="tips-icon">💡</span>
        <h3 class="tips-title">小贴士</h3>
      </div>
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
  padding: 0 4px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 22px;
}

.stats-card {
  margin-bottom: 20px;
}

.token-icon {
  font-size: 16px;
}

.task-card {
  transition: all 0.3s ease;
}

.task-card.completed {
  border: 1px solid var(--success-color) !important;
}

.task-card.claimed {
  opacity: 0.7;
}

.task-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.task-icon {
  font-size: 28px;
}

.task-title {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
}

.task-desc {
  margin: 0 0 12px 0;
  font-size: 13px;
  color: var(--text-color-3);
  line-height: 1.4;
}

.task-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.progress-bar {
  flex: 1;
}

.progress-text {
  font-size: 12px;
  color: var(--text-color-4);
  flex-shrink: 0;
}

.task-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reward-text {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}

.tips-card {
  margin-top: 24px;
  background-color: var(--bg-card);
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.tips-icon {
  font-size: 18px;
}

.tips-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: var(--text-color-3);
}

.tips-list li {
  margin-bottom: 4px;
}
</style>