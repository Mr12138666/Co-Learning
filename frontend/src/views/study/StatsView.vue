<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { NStatistic, NGrid, NGridItem, NSpin, NEmpty, NButton, useMessage } from 'naive-ui'
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
import { statsApi, type Stats } from '@/api/stats'
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

const stats = ref<Stats | null>(null)

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
  
  const dark = themeStore.theme === 'dark'
  return {
    tooltip: {
      trigger: 'axis',
      backgroundColor: dark ? '#1f1f1f' : '#ffffff',
      borderColor: dark ? '#333' : '#e5e5e5',
      textStyle: { color: dark ? '#eee' : '#333' },
      borderWidth: 1,
      extraCssText: 'box-shadow: 0 8px 24px rgba(0,0,0,0.12); border-radius: 12px;',
    },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: dates, axisLabel: { color: dark ? '#aaa' : '#666' }, axisLine: { lineStyle: { color: dark ? '#333' : '#ddd' } } },
    yAxis: { type: 'value', name: '分钟', axisLabel: { color: dark ? '#aaa' : '#666' }, splitLine: { lineStyle: { color: dark ? '#222' : '#f0f0f0' } } },
    series: [{
      type: 'bar',
      data,
      itemStyle: {
        borderRadius: [6, 6, 0, 0],
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: '#7aa7ff' },
            { offset: 1, color: '#4f8cff' },
          ],
        },
        shadowColor: 'rgba(79, 140, 255, 0.35)',
        shadowBlur: 10,
        shadowOffsetY: 4,
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
    legend: {
      data: ['专注时长', '打卡次数'],
      top: 0,
      left: 'center',
      itemGap: 20,
      textStyle: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' },
    },
    grid: { top: 44, bottom: 12, left: '3%', right: '4%', containLabel: true },
    xAxis: { type: 'category', data: labels, axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } },
    yAxis: [{ type: 'value', name: '分钟', axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } }, 
            { type: 'value', name: '次数', axisLabel: { color: themeStore.theme === 'dark' ? '#aaa' : '#666' } }],
    series: [{
      name: '专注时长',
      type: 'line',
      data: focusData,
      smooth: true,
      lineStyle: { width: 3, color: '#4f8cff', shadowColor: 'rgba(79, 140, 255, 0.45)', shadowBlur: 12 },
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
  const res = await statsApi.getStats()
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
  <div v-else-if="stats" class="stats-view gradient-mesh">
    <!-- Header -->
    <div class="page-header">
      <h3 class="page-title">学习统计</h3>
      <NButton text size="small" @click="retry(fetchStats)">刷新数据</NButton>
    </div>

    <div class="page-divider" />

    <!-- Summary stats grid -->
    <NGrid :cols="4" :x-gap="8" :y-gap="8" responsive="screen" item-responsive class="summary-grid">
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="今日专注"
            :value="formatSeconds(stats.todayFocusSeconds)"
            class="stat--brand"
          />
        </div>
      </NGridItem>
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="本周专注"
            :value="formatSeconds(stats.weekFocusSeconds)"
            class="stat--success"
          />
        </div>
      </NGridItem>
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="本月专注"
            :value="formatSeconds(stats.monthFocusSeconds)"
            class="stat--warning"
          />
        </div>
      </NGridItem>
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="本年专注"
            :value="formatSeconds(stats.yearFocusSeconds)"
            class="stat--info"
          />
        </div>
      </NGridItem>
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="累计专注"
            :value="formatSeconds(stats.totalFocusSeconds)"
            class="stat--brand"
          />
        </div>
      </NGridItem>
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="连续专注"
            :value="stats.streakDays"
            suffix="天"
            class="stat--danger"
          />
        </div>
      </NGridItem>
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="专注天数"
            :value="stats.focusDays"
            suffix="天"
            class="stat--info"
          />
        </div>
      </NGridItem>
      <NGridItem span="4 s:2 m:1" class="stagger-in">
        <div class="summary-card glass--subtle">
          <NStatistic
            label="累计打卡"
            :value="stats.totalCheckins"
            suffix="次"
            class="stat--danger"
          />
        </div>
      </NGridItem>
    </NGrid>

    <!-- Charts -->
    <NGrid :cols="2" :x-gap="12" :y-gap="12" responsive="screen" item-responsive class="charts-grid">
      <!-- Daily Bar Chart -->
      <NGridItem span="2" class="stagger-in">
        <div class="chart-panel glass">
          <div class="chart-panel__header">
            <span class="chart-panel__title">每日专注时长</span>
          </div>
          <div class="chart-panel__body">
            <v-chart :option="dailyChartOption" class="chart-canvas chart-canvas--tall" autoresize />
          </div>
        </div>
      </NGridItem>

      <!-- Weekly Line Chart -->
      <NGridItem span="2 m:1" class="stagger-in">
        <div class="chart-panel glass">
          <div class="chart-panel__header">
            <span class="chart-panel__title">每周趋势</span>
          </div>
          <div class="chart-panel__body">
            <v-chart :option="weeklyChartOption" class="chart-canvas" autoresize />
          </div>
        </div>
      </NGridItem>

      <!-- Monthly Bar Chart -->
      <NGridItem span="2 m:1" class="stagger-in">
        <div class="chart-panel glass">
          <div class="chart-panel__header">
            <span class="chart-panel__title">每月专注</span>
          </div>
          <div class="chart-panel__body">
            <v-chart :option="monthlyChartOption" style="height: 260px; width: 100%;" autoresize />
          </div>
        </div>
      </NGridItem>

      <!-- Subject Pie Chart -->
      <NGridItem span="2 m:1" class="stagger-in">
        <div class="chart-panel glass">
          <div class="chart-panel__header">
            <span class="chart-panel__title">科目分布</span>
          </div>
          <div class="chart-panel__body">
            <v-chart v-if="stats.subjectStats.length > 0" :option="subjectChartOption" class="chart-canvas" autoresize />
            <div v-else class="chart-empty">
              <n-empty description="暂无科目数据" />
            </div>
          </div>
        </div>
      </NGridItem>

      <!-- Weekly Stats -->
      <NGridItem span="2 m:1" class="stagger-in">
        <div class="chart-panel glass">
          <div class="chart-panel__header">
            <span class="chart-panel__title">本周统计</span>
          </div>
          <div class="chart-panel__body chart-panel__body--flush">
            <div class="stat-row">
              <span class="stat-row__label">本周专注</span>
              <span class="stat-row__value">{{ formatSeconds(stats.weekFocusSeconds) }}</span>
            </div>
            <div class="stat-row">
              <span class="stat-row__label">本周打卡</span>
              <span class="stat-row__value">{{ stats.weekCheckinCount }}次</span>
            </div>
            <div class="stat-row">
              <span class="stat-row__label">完成复盘</span>
              <span class="stat-row__value">{{ stats.weekCompletedCount }}次</span>
            </div>
            <div class="stat-row">
              <span class="stat-row__label">最后打卡</span>
              <span class="stat-row__value">{{ stats.lastCheckinDate || '无' }}</span>
            </div>
          </div>
        </div>
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

.stats-view {
  width: 100%;
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding-bottom: var(--sp-4);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-3) 0;
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.page-divider {
  border-bottom: 1px solid var(--separator);
  margin-bottom: var(--sp-3);
}

/* Summary cards - flat, no heavy shadow */
.summary-grid {
  margin-bottom: var(--sp-4);
}

.summary-card {
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-sm);
  min-height: 64px;
  display: flex;
  align-items: center;
}

/* Stat color variants (applied to NStatistic via deep selector) */
.stat--brand :deep(.n-statistic-value__content) { color: var(--brand); font-size: var(--text-xl); font-weight: 700; }
.stat--success :deep(.n-statistic-value__content) { color: var(--success); font-size: var(--text-xl); font-weight: 700; }
.stat--warning :deep(.n-statistic-value__content) { color: var(--warning); font-size: var(--text-xl); font-weight: 700; }
.stat--danger :deep(.n-statistic-value__content) { color: var(--danger); font-size: var(--text-xl); font-weight: 700; }
.stat--info :deep(.n-statistic-value__content) { color: var(--info); font-size: var(--text-xl); font-weight: 700; }

/* Chart panels - subtle border, no heavy card */
.charts-grid {
  /* no extra margin needed */
}

.chart-panel {
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.chart-panel__header {
  padding: var(--sp-3) var(--sp-4);
  border-bottom: 1px solid var(--separator);
}

.chart-panel__title {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.chart-panel__body {
  padding: var(--sp-3);
}

.chart-panel__body--flush {
  padding: 0;
}

.chart-empty {
  padding: var(--sp-8) 0;
  display: flex;
  justify-content: center;
}

/* Chart canvas dimensions */
.chart-canvas {
  height: 260px;
  width: 100%;
}

.chart-canvas--tall {
  height: 280px;
}

/* Stat rows inside weekly stats panel */
.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--sp-2) var(--sp-4);
  min-height: 36px;
  border-bottom: 1px solid var(--separator);
}

.stat-row:last-child {
  border-bottom: none;
}

.stat-row__label {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.stat-row__value {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}
</style>
