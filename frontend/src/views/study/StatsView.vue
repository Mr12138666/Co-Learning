<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { NCard, NStatistic, NSpace, NGrid, NGridItem, NSpin, NEmpty, NButton, useMessage } from 'naive-ui'
import { usePageLoad } from '@/composables/usePageLoad'
import StateError from '@/components/common/StateError.vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import { studyApi } from '@/api/study'
import { useThemeStore } from '@/stores/themeStore'

use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
])

const themeStore = useThemeStore()
const _message = useMessage()
const { loading, error, load, retry } = usePageLoad()

interface DailyStat {
  date: string
  focusSeconds: number
  sessionCount: number
  checkedIn: boolean
}

interface WeeklyStat {
  weekOfYear: number
  weekLabel: string
  focusSeconds: number
  sessionCount: number
  checkinCount: number
}

interface MonthlyStat {
  month: string
  monthLabel: string
  focusSeconds: number
  sessionCount: number
  focusDays: number
}

interface SubjectStat {
  subjectId: number
  subjectName: string
  subjectColor: string
  focusSeconds: number
  sessionCount: number
}

interface StatsResponse {
  todayFocusSeconds: number
  weekFocusSeconds: number
  monthFocusSeconds: number
  yearFocusSeconds: number
  totalFocusSeconds: number
  streakDays: number
  focusDays: number
  totalCheckins: number
  weekCheckinCount: number
  weekCompletedCount: number
  lastCheckinDate: string | null
  dailyStats: DailyStat[]
  weeklyStats: WeeklyStat[]
  monthlyStats: MonthlyStat[]
  subjectStats: SubjectStat[]
}

const stats = ref<StatsResponse | null>(null)

function formatSeconds(seconds: number): string {
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  if (hours > 0) {
    return `${hours}h ${minutes}m`
  }
  return `${minutes}m`
}

// Daily Bar Chart
const dailyChartOption = computed(() => {
  if (!stats.value) return {}
  const dates = stats.value.dailyStats.map(d => {
    const date = new Date(d.date)
    return `${date.getMonth() + 1}/${date.getDate()}`
  })
  const data = stats.value.dailyStats.map(d => Math.floor(d.focusSeconds / 60))
  
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    yAxis: { type: 'value', name: '分钟', axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    series: [{
      type: 'bar',
      data,
      itemStyle: {
        color: '#4f8cff',
        borderRadius: [4, 4, 0, 0],
      },
    }],
    backgroundColor: 'transparent',
  }
})

// Weekly Line Chart
const weeklyChartOption = computed(() => {
  if (!stats.value) return {}
  const labels = stats.value.weeklyStats.map(w => w.weekLabel)
  const focusData = stats.value.weeklyStats.map(w => Math.floor(w.focusSeconds / 60))
  const checkinData = stats.value.weeklyStats.map(w => w.checkinCount)
  
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: ['专注时长', '打卡次数'], textStyle: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels, axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    yAxis: [{ type: 'value', name: '分钟', axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } }, 
            { type: 'value', name: '次数', axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } }],
    series: [{
      name: '专注时长',
      type: 'line',
      data: focusData,
      smooth: true,
      lineStyle: { width: 3, color: '#4f8cff' },
      itemStyle: { color: '#4f8cff' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(79, 140, 255, 0.3)' },
            { offset: 1, color: 'rgba(79, 140, 255, 0)' },
          ],
        },
      },
    }, {
      name: '打卡次数',
      type: 'line',
      yAxisIndex: 1,
      data: checkinData,
      smooth: true,
      lineStyle: { width: 2, color: '#52c41a' },
      itemStyle: { color: '#52c41a' },
    }],
    backgroundColor: 'transparent',
  }
})

// Monthly Bar Chart
const monthlyChartOption = computed(() => {
  if (!stats.value) return {}
  const labels = stats.value.monthlyStats.map(m => m.monthLabel)
  const data = stats.value.monthlyStats.map(m => Math.floor(m.focusSeconds / 3600))
  
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: labels, axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    yAxis: { type: 'value', name: '小时', axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    series: [{
      type: 'bar',
      data,
      itemStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#722ed1' },
            { offset: 1, color: '#b37feb' },
          ],
        },
        borderRadius: [4, 4, 0, 0],
      },
    }],
    backgroundColor: 'transparent',
  }
})

// Subject Pie Chart
const subjectChartOption = computed(() => {
  if (!stats.value) return {}
  const data = stats.value.subjectStats.map(s => ({
    name: s.subjectName,
    value: Math.floor(s.focusSeconds / 60),
    itemStyle: { color: s.subjectColor },
  }))
  
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c}分钟 ({d}%)' },
    legend: { orient: 'vertical', right: '5%', top: 'center', textStyle: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['40%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 4, borderColor: themeStore.theme === 'dark' ? '#1a1a1a' : '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14 } },
      data,
    }],
    backgroundColor: 'transparent',
  }
})

async function fetchStats() {
  const res = await studyApi.getStats()
  stats.value = res.data.data
}

onMounted(() => load(fetchStats))
</script>

<template>
  <!-- Loading state -->
  <div v-if="loading" class="stats-loading">
    <n-spin size="large" />
  </div>

  <!-- Error state -->
  <StateError
    v-else-if="error"
    :title="error"
    @retry="retry(fetchStats)"
  />

  <!-- Stats content -->
  <div v-else-if="stats" class="stats-view">
    <!-- Header -->
    <NCard :bordered="false" class="stats-header">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <span style="font-size: 24px;">📊</span>
          <span style="font-size: 20px; font-weight: 600; margin-left: 8px;">学习统计</span>
        </div>
        <NButton text @click="retry(fetchStats)">刷新数据</NButton>
      </div>
    </NCard>

    <!-- Stats Cards -->
    <NGrid :cols="4" :x-gap="12" :y-gap="12" responsive="screen" item-responsive style="margin-bottom: 24px;">
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="今日专注"
            :value="formatSeconds(stats.todayFocusSeconds)"
            :value-style="{ color: '#4f8cff', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="本周专注"
            :value="formatSeconds(stats.weekFocusSeconds)"
            :value-style="{ color: '#52c41a', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="本月专注"
            :value="formatSeconds(stats.monthFocusSeconds)"
            :value-style="{ color: '#faad14', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="本年专注"
            :value="formatSeconds(stats.yearFocusSeconds)"
            :value-style="{ color: '#722ed1', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="累计专注"
            :value="formatSeconds(stats.totalFocusSeconds)"
            :value-style="{ color: '#1890ff', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="连续专注"
            :value="stats.streakDays"
            suffix="天"
            :value-style="{ color: '#f5222d', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="专注天数"
            :value="stats.focusDays"
            suffix="天"
            :value-style="{ color: '#13c2c2', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
      <NGridItem span="4 s:2 m:1">
        <NCard :bordered="false" class="stat-card">
          <NStatistic
            label="累计打卡"
            :value="stats.totalCheckins"
            suffix="次"
            :value-style="{ color: '#eb2f96', fontSize: '24px', fontWeight: '700' }"
          />
        </NCard>
      </NGridItem>
    </NGrid>

    <!-- Charts -->
    <NGrid :cols="2" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
      <!-- Daily Bar Chart -->
      <NGridItem span="2">
        <NCard :bordered="false" class="chart-card">
          <template #header>
            <span style="font-size: 16px; font-weight: 600;">📅 每日专注时长</span>
          </template>
          <div class="chart-container">
            <v-chart :option="dailyChartOption" style="height: 300px; width: 100%;" autoresize />
          </div>
        </NCard>
      </NGridItem>

      <!-- Weekly Line Chart -->
      <NGridItem span="2 m:1">
        <NCard :bordered="false" class="chart-card">
          <template #header>
            <span style="font-size: 16px; font-weight: 600;">📈 每周趋势</span>
          </template>
          <div class="chart-container">
            <v-chart :option="weeklyChartOption" style="height: 300px; width: 100%;" autoresize />
          </div>
        </NCard>
      </NGridItem>

      <!-- Monthly Bar Chart -->
      <NGridItem span="2 m:1">
        <NCard :bordered="false" class="chart-card">
          <template #header>
            <span style="font-size: 16px; font-weight: 600;">📉 每月专注</span>
          </template>
          <div class="chart-container">
            <v-chart :option="monthlyChartOption" style="height: 300px; width: 100%;" autoresize />
          </div>
        </NCard>
      </NGridItem>

      <!-- Subject Pie Chart -->
      <NGridItem span="2 m:1">
        <NCard :bordered="false" class="chart-card">
          <template #header>
            <span style="font-size: 16px; font-weight: 600;">🥧 科目分布</span>
          </template>
          <div class="chart-container">
            <v-chart v-if="stats.subjectStats.length > 0" :option="subjectChartOption" style="height: 300px; width: 100%;" autoresize />
            <n-empty v-else description="暂无科目数据" style="padding: 80px 0;" />
          </div>
        </NCard>
      </NGridItem>

      <!-- Weekly Stats -->
      <NGridItem span="2 m:1">
        <NCard :bordered="false" class="chart-card">
          <template #header>
            <span style="font-size: 16px; font-weight: 600;">📋 本周统计</span>
          </template>
          <NSpace vertical :size="8">
            <div class="stat-row">
              <span class="stat-label">本周专注</span>
              <span class="stat-value">{{ formatSeconds(stats.weekFocusSeconds) }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">本周打卡</span>
              <span class="stat-value">{{ stats.weekCheckinCount }}次</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">完成复盘</span>
              <span class="stat-value">{{ stats.weekCompletedCount }}次</span>
            </div>
            <div class="stat-row">
              <span class="stat-label">最后打卡</span>
              <span class="stat-value">{{ stats.lastCheckinDate || '无' }}</span>
            </div>
          </NSpace>
        </NCard>
      </NGridItem>
    </NGrid>
  </div>
</template>

<style scoped>
.stats-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.stats-error {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.stats-view {
  padding-bottom: var(--sp-6);
}

.stats-header {
  margin-bottom: var(--sp-6);
}

.stat-card {
  background-color: var(--bg-card);
}

.chart-card {
  background-color: var(--bg-card);
}

.chart-container {
  padding: var(--sp-2) 0;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--sp-2) 0;
  border-bottom: 1px solid var(--separator);
}

.stat-row:last-child {
  border-bottom: none;
}

.stat-label {
  color: var(--text-secondary);
  font-size: var(--text-base);
}

.stat-value {
  font-weight: var(--weight-semibold);
  font-size: var(--text-base);
}
</style>
