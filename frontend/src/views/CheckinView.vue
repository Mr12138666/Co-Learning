<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  NCard,
  NButton,
  NSpace,
  NInput,
  NRadioGroup,
  NRadio,
  NStatistic,
  NTag,
  NText,
  NDivider,
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
  <div>
    <!-- Loading State -->
    <div v-if="loading" style="display: flex; justify-content: center; padding: 80px 0;">
      <NSpin size="large" />
    </div>

    <!-- Error State -->
    <StateError
      v-else-if="error"
      :title="error"
      @retry="retry(loadData)"
    />

    <template v-else>
    <!-- Header -->
    <NCard :bordered="false" style="margin-bottom: 16px;">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h2 style="margin: 0; font-size: 20px;">每日复盘</h2>
          <NText depth="3" style="font-size: 13px;">{{ today }}</NText>
        </div>
        <NSpace :size="24">
          <NStatistic label="今日专注" :value="todayFocusMin" suffix="分钟" />
          <NTag v-if="isCompleted" type="success" size="large" round>已打卡</NTag>
          <NTag v-else size="large" round>未打卡</NTag>
        </NSpace>
      </div>
    </NCard>

    <!-- Plan -->
    <NCard title="今日计划" :bordered="false" style="margin-bottom: 16px;">
      <NInput
        v-model:value="planText"
        type="textarea"
        placeholder="今天打算学什么？（选填）"
        :autosize="{ minRows: 3, maxRows: 6 }"
        :disabled="isCompleted"
      />
    </NCard>

    <!-- Reflection -->
    <NCard title="学习复盘" :bordered="false" style="margin-bottom: 16px;">
      <NInput
        v-model:value="reflectionText"
        type="textarea"
        placeholder="今天学得怎么样？有什么收获和不足？"
        :autosize="{ minRows: 5, maxRows: 10 }"
        :disabled="isCompleted"
      />
      <NDivider />
      <div>
        <NText depth="3" style="font-size: 13px; margin-bottom: 8px; display: block;">今日心情</NText>
        <NRadioGroup v-model:value="mood" :disabled="isCompleted">
          <NSpace>
            <NRadio v-for="opt in moodOptions" :key="opt.value" :value="opt.value">
              {{ opt.emoji }} {{ opt.label }}
            </NRadio>
          </NSpace>
        </NRadioGroup>
      </div>
    </NCard>

    <!-- Actions -->
    <NSpace justify="end" :size="12">
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
    </NSpace>
    </template>
  </div>
</template>
