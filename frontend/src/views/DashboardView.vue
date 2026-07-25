<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NGrid,
  NGridItem,
  NStatistic,
  NSpace,
  NEmpty,
  NButton,
  NTag,
  NText,
  NProgress,
} from 'naive-ui'
import { useAuthStore } from '@/stores/authStore'
import { useStudyStore } from '@/stores/studyStore'
import { useDashboardStore } from '@/stores/dashboardStore'
import FocusTimer from '@/components/focus/FocusTimer.vue'
import TaskList from '@/components/study/TaskList.vue'

const router = useRouter()
const authStore = useAuthStore()
const studyStore = useStudyStore()
const dashboardStore = useDashboardStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const displayName = computed(() => authStore.user?.email?.split('@')[0] || '同学')

// Stats helpers
function formatMinutes(seconds: number): string {
  const mins = Math.floor(seconds / 60)
  if (mins < 60) return `${mins}`
  const hours = Math.floor(mins / 60)
  return `${hours}h${mins % 60}m`
}

const todayFocus = computed(() => dashboardStore.stats?.todayFocusSeconds ?? 0)
const weekFocus = computed(() => dashboardStore.stats?.weekFocusSeconds ?? 0)
const streak = computed(() => dashboardStore.stats?.streakDays ?? 0)
const checkinCompleted = computed(() => dashboardStore.todayCheckin?.completed ?? false)

// Daily goal: 120 minutes
const dailyGoalMin = 120
const dailyProgress = computed(() => {
  const mins = Math.floor(todayFocus.value / 60)
  return Math.min(100, Math.round((mins / dailyGoalMin) * 100))
})

onMounted(async () => {
  await Promise.all([
    studyStore.fetchAll(),
    dashboardStore.refreshAll(),
  ])
})
</script>

<template>
  <div>
    <!-- Greeting -->
    <NCard :bordered="false" size="small" style="margin-bottom: 16px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h2 style="margin: 0; font-size: 24px;">
            {{ greeting }}，{{ displayName }}
          </h2>
          <p style="margin: 8px 0 0; color: #666;">
            每一分钟专注，都是通向目标的步伐。
          </p>
        </div>
        <NButton strong type="primary" @click="router.push('/checkin')">
          {{ checkinCompleted ? '查看复盘' : '每日复盘' }}
        </NButton>
      </div>
    </NCard>

    <!-- Main grid: Timer + Stats -->
    <NGrid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <!-- Focus Timer -->
      <NGridItem span="3 m:1">
        <NCard title="专注计时" :bordered="false">
          <FocusTimer @finished="dashboardStore.refreshAll()" />
        </NCard>
      </NGridItem>

      <!-- Stats & Goals -->
      <NGridItem span="3 m:2">
        <NGrid :cols="2" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
          <NGridItem span="2 m:1">
            <NCard size="small" :bordered="false">
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <NStatistic label="今日专注" :value="formatMinutes(todayFocus)" />
                <div style="text-align: right; flex: 1; margin-left: 16px;">
                  <NText depth="3" style="font-size: 12px;">日目标 {{ dailyGoalMin }} 分钟</NText>
                  <NProgress
                    type="line"
                    :percentage="dailyProgress"
                    :height="8"
                    style="margin-top: 4px;"
                  />
                </div>
              </div>
            </NCard>
          </NGridItem>

          <NGridItem span="2 m:1">
            <NCard size="small" :bordered="false">
              <NGrid :cols="2" :x-gap="8">
                <div>
                  <NStatistic label="本周专注" :value="formatMinutes(weekFocus)" />
                </div>
                <div>
                  <NStatistic label="连续天数" :value="streak" suffix="天" />
                </div>
              </NGrid>
            </NCard>
          </NGridItem>

          <!-- Exam goal countdown -->
          <NGridItem v-if="studyStore.activeGoals.length > 0" span="2">
            <NCard size="small" :bordered="false">
              <template #header>
                <NText strong style="font-size: 14px;">考试倒计时</NText>
              </template>
              <NSpace>
                <NTag
                  v-for="goal in studyStore.activeGoals.slice(0, 3)"
                  :key="goal.id"
                  size="large"
                  round
                  :type="goal.daysRemaining <= 30 ? 'error' : goal.daysRemaining <= 60 ? 'warning' : 'info'"
                >
                  {{ goal.examName }} - 还剩 {{ goal.daysRemaining }} 天
                </NTag>
              </NSpace>
            </NCard>
          </NGridItem>
        </NGrid>
      </NGridItem>
    </NGrid>

    <!-- Tasks + Quick links -->
    <NGrid :cols="3" :x-gap="16" :y-gap="16" responsive="screen" item-responsive style="margin-top: 16px;">
      <NGridItem span="3 m:2">
        <NCard :bordered="false">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <NText strong>今日任务</NText>
              <NButton text type="primary" @click="router.push('/tasks')">
                全部任务
              </NButton>
            </div>
          </template>
          <TaskList v-if="studyStore.todoTasks.length > 0 || studyStore.inProgressTasks.length > 0" filter-status="ALL" />
          <NEmpty v-else description="暂无任务，去创建一个吧">
            <template #extra>
              <NButton type="primary" @click="router.push('/tasks')">
                添加任务
              </NButton>
            </template>
          </NEmpty>
        </NCard>
      </NGridItem>

      <NGridItem span="3 m:1">
        <NCard title="快捷入口" :bordered="false">
          <NSpace vertical :size="8">
            <NButton block @click="router.push('/goals')">
              考试目标
            </NButton>
            <NButton block @click="router.push('/subjects')">
              科目管理
            </NButton>
            <NButton block @click="router.push('/tasks')">
              任务清单
            </NButton>
            <NButton block @click="router.push('/stats')">
              学习统计
            </NButton>
            <NButton block @click="router.push('/checkin')">
              每日复盘
            </NButton>
            <NButton block @click="router.push('/journals')">
              学习日志
            </NButton>
          </NSpace>
        </NCard>
      </NGridItem>
    </NGrid>
  </div>
</template>
