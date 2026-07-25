<script setup lang="ts">
import { onMounted, computed } from 'vue'
import {
  NCard,
  NGrid,
  NGridItem,
  NStatistic,
  NEmpty,
  NText,
  NSpace,
  NTag,
} from 'naive-ui'
import { useDashboardStore } from '@/stores/dashboardStore'
import dayjs from 'dayjs'

const dashboardStore = useDashboardStore()

function formatDuration(seconds: number): string {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  if (h > 0) return `${h}h ${m}m`
  return `${m}m`
}

// Last 7 days data for bar chart
const weekData = computed(() => {
  if (!dashboardStore.stats?.dailyStats) return []
  return dashboardStore.stats.dailyStats.slice(-7).map((d) => ({
    date: d.date,
    dayLabel: dayjs(d.date).format('ddd'),
    minutes: Math.floor(d.focusSeconds / 60),
    checkedIn: d.checkedIn,
  }))
})

const maxMinutes = computed(() => {
  return Math.max(60, ...weekData.value.map((d) => d.minutes))
})

// Subject distribution
const subjectStats = computed(() => {
  return dashboardStore.stats?.subjectStats ?? []
})

const totalSubjectMinutes = computed(() => {
  return subjectStats.value.reduce((sum, s) => sum + s.focusSeconds, 0)
})

onMounted(() => {
  dashboardStore.fetchStats()
})
</script>

<template>
  <div>
    <!-- Overview stats -->
    <NGrid :cols="4" :x-gap="16" :y-gap="16" responsive="screen" item-responsive>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" size="small">
          <NStatistic label="今日专注" :value="formatDuration(dashboardStore.stats?.todayFocusSeconds ?? 0)" />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" size="small">
          <NStatistic label="本周专注" :value="formatDuration(dashboardStore.stats?.weekFocusSeconds ?? 0)" />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" size="small">
          <NStatistic label="累计专注" :value="formatDuration(dashboardStore.stats?.totalFocusSeconds ?? 0)" />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" size="small">
          <NStatistic label="连续天数" :value="dashboardStore.stats?.streakDays ?? 0" suffix="天" />
        </NCard>
      </NGridItem>
    </NGrid>

    <!-- Weekly chart -->
    <NCard title="本周专注趋势" :bordered="false" style="margin-top: 16px;">
      <div v-if="weekData.length > 0" class="week-chart">
        <div v-for="d in weekData" :key="d.date" class="bar-col">
          <div class="bar-container">
            <div
              class="bar"
              :style="{
                height: `${(d.minutes / maxMinutes) * 100}%`,
                background: d.minutes > 0 ? '#2080F0' : '#e0e0e6',
              }"
            />
            <NText v-if="d.minutes > 0" depth="3" style="font-size: 11px;">
              {{ d.minutes }}m
            </NText>
          </div>
          <div class="bar-label">
            <NText depth="3" style="font-size: 12px;">{{ d.dayLabel }}</NText>
            <span v-if="d.checkedIn" class="checkin-dot" />
          </div>
        </div>
      </div>
      <NEmpty v-else description="暂无数据" />
    </NCard>

    <!-- Subject distribution -->
    <NCard title="科目分布" :bordered="false" style="margin-top: 16px;">
      <NEmpty v-if="subjectStats.length === 0" description="完成专注会话后这里会显示科目分布" />
      <div v-else class="subject-stats">
        <div v-for="s in subjectStats" :key="s.subjectId" class="subject-row">
          <div class="subject-info">
            <span class="color-dot" :style="{ background: s.subjectColor }" />
            <NText strong>{{ s.subjectName }}</NText>
            <NText depth="3" style="font-size: 13px;">
              {{ formatDuration(s.focusSeconds) }} / {{ s.sessionCount }} 次
            </NText>
          </div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{
                width: `${totalSubjectMinutes > 0 ? (s.focusSeconds / totalSubjectMinutes) * 100 : 0}%`,
                background: s.subjectColor,
              }"
            />
          </div>
        </div>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.week-chart {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  height: 200px;
  padding: 16px 0;
}

.bar-col {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
}

.bar-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
  gap: 4px;
}

.bar {
  width: 60%;
  max-width: 40px;
  min-height: 2px;
  border-radius: 4px 4px 0 0;
  transition: height 0.3s ease;
}

.bar-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
  margin-top: 4px;
}

.checkin-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #18a058;
}

.subject-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.subject-row {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.subject-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.progress-bar {
  height: 8px;
  background: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s ease;
}
</style>
