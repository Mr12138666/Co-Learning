<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  NButton,
  NInput,
  NRadioGroup,
  NRadio,
  NTag,
  NSpin,
  useMessage,
} from 'naive-ui'
import { useDashboardStore } from '@/stores/dashboardStore'
import { usePageLoad } from '@/composables/usePageLoad'
import StateError from '@/components/common/StateError.vue'
import dayjs from 'dayjs'

const dashboardStore = useDashboardStore()
const message = useMessage()
const { loading, error, load, retry } = usePageLoad()

const planText = ref('')
const reflectionText = ref('')
const mood = ref<number | null>(null)
const saving = ref(false)

const today = computed(() => dayjs().format('YYYY-MM-DD'))
const todayFocusMin = computed(() => Math.floor((dashboardStore.todayCheckin?.focusTotalSec ?? 0) / 60))
const isCompleted = computed(() => dashboardStore.todayCheckin?.completed ?? false)

const moodOptions = [
  { label: '很差', value: 1, emoji: '😞' },
  { label: '一般', value: 2, emoji: '😕' },
  { label: '还行', value: 3, emoji: '😐' },
  { label: '不错', value: 4, emoji: '🙂' },
  { label: '很好', value: 5, emoji: '😄' },
]

async function handleSave() {
  saving.value = true
  try {
    await dashboardStore.updateCheckin({
      planText: planText.value || undefined,
      reflectionText: reflectionText.value || undefined,
      mood: mood.value ?? undefined,
    })
    message.success('已保存')
  } catch {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleComplete() {
  if (!reflectionText.value.trim()) {
    message.warning('请先写一些复盘内容')
    return
  }
  saving.value = true
  try {
    await dashboardStore.updateCheckin({
      planText: planText.value || undefined,
      reflectionText: reflectionText.value,
      mood: mood.value ?? undefined,
    })
    await dashboardStore.completeCheckin()
    message.success('打卡成功！')
  } catch {
    message.error('打卡失败')
  } finally {
    saving.value = false
  }
}

async function loadData() {
  await dashboardStore.fetchTodayCheckin()
  const checkin = dashboardStore.todayCheckin
  if (checkin) {
    planText.value = checkin.planText ?? ''
    reflectionText.value = checkin.reflectionText ?? ''
    mood.value = checkin.mood
  }
}

onMounted(() => load(loadData))
</script>

<template>
  <div class="checkin-view">
    <!-- Loading -->
    <div v-if="loading" class="loading-center">
      <NSpin size="large" />
    </div>

    <!-- Error -->
    <StateError
      v-else-if="error"
      :title="error"
      @retry="retry(loadData)"
    />

    <template v-else>
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-left">
          <h3 class="page-title">每日复盘</h3>
          <span class="page-date">{{ today }}</span>
        </div>
        <div class="header-right">
          <div class="stat-item">
            <span class="stat-value">{{ todayFocusMin }}</span>
            <span class="stat-label">分钟专注</span>
          </div>
          <NTag v-if="isCompleted" type="success" size="small" round :bordered="false">已打卡</NTag>
          <NTag v-else size="small" round :bordered="false">未打卡</NTag>
        </div>
      </div>

      <!-- Plan Section -->
      <div class="section-card">
        <div class="section-label">今日计划</div>
        <NInput
          v-model:value="planText"
          type="textarea"
          placeholder="今天打算学什么？（选填）"
          :autosize="{ minRows: 2, maxRows: 5 }"
          :disabled="isCompleted"
        />
      </div>

      <!-- Reflection Section -->
      <div class="section-card">
        <div class="section-label">学习复盘</div>
        <NInput
          v-model:value="reflectionText"
          type="textarea"
          placeholder="今天学得怎么样？有什么收获和不足？"
          :autosize="{ minRows: 4, maxRows: 8 }"
          :disabled="isCompleted"
        />

        <div class="mood-section">
          <span class="mood-label">今日心情</span>
          <NRadioGroup v-model:value="mood" :disabled="isCompleted">
            <div class="mood-options">
              <NRadio v-for="opt in moodOptions" :key="opt.value" :value="opt.value">
                {{ opt.emoji }} {{ opt.label }}
              </NRadio>
            </div>
          </NRadioGroup>
        </div>
      </div>

      <!-- Actions -->
      <div class="actions-bar">
        <NButton :loading="saving" :disabled="isCompleted" @click="handleSave">
          保存
        </NButton>
        <NButton
          type="primary"
          :loading="saving"
          :disabled="isCompleted"
          @click="handleComplete"
        >
          {{ isCompleted ? '已打卡' : '完成打卡' }}
        </NButton>
      </div>
    </template>
  </div>
</template>

<style scoped>
.checkin-view {
  max-width: var(--component-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: var(--sp-12) 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-4);
}

.header-left {
  display: flex;
  align-items: baseline;
  gap: var(--sp-3);
}

.page-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.page-date {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--sp-4);
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: var(--sp-1);
}

.stat-value {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.section-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--sp-4);
  margin-bottom: var(--sp-3);
}

.section-label {
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  color: var(--text-color);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
  margin-bottom: var(--sp-2);
}

.mood-section {
  margin-top: var(--sp-3);
  padding-top: var(--sp-3);
  border-top: 1px solid var(--divider);
}

.mood-label {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
  display: block;
  margin-bottom: var(--sp-2);
}

.mood-options {
  display: flex;
  gap: var(--sp-4);
  flex-wrap: wrap;
}

.actions-bar {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-2);
}
</style>
