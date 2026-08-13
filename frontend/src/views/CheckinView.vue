<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import {
  NButton,
  NInput,
  NRadioGroup,
  NRadio,
  NTag,
  NSpin,
  NUpload,
  NEmpty,
  NTabs,
  NTabPane,
  NImage,
  NImageGroup,
  NSpace,
  useMessage,
} from 'naive-ui'
import type { UploadFileInfo } from 'naive-ui'
import { useDashboardStore } from '@/stores/dashboardStore'
import { usePageLoad } from '@/composables/usePageLoad'
import { storageApi } from '@/api/storage'
import { checkinApi, type Checkin } from '@/api/checkin'
import StateError from '@/components/common/StateError.vue'
import dayjs from 'dayjs'

const dashboardStore = useDashboardStore()
const message = useMessage()
const { loading, error, load, retry } = usePageLoad()

const planText = ref('')
const reflectionText = ref('')
const mood = ref<number | null>(null)
const saving = ref(false)
const imageUrls = ref<string[]>([])

// History
const activeTab = ref<string>('today')
const historyRecords = ref<Checkin[]>([])
const historyLoading = ref(false)

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

async function handleImageUpload(options: { file: UploadFileInfo; onFinish: () => void; onError: () => void }) {
  const rawFile = options.file.file
  if (!rawFile) {
    options.onError()
    return
  }
  if (imageUrls.value.length >= 6) {
    message.warning('最多上传 6 张图片')
    options.onError()
    return
  }
  try {
    const res = await storageApi.upload(rawFile)
    const url = res.data.data.url
    imageUrls.value.push(url)
    options.onFinish()
  } catch {
    message.error('图片上传失败')
    options.onError()
  }
}

async function handleSave() {
  saving.value = true
  try {
    await dashboardStore.updateCheckin({
      planText: planText.value || undefined,
      reflectionText: reflectionText.value || undefined,
      mood: mood.value ?? undefined,
      images: imageUrls.value.length > 0 ? JSON.stringify(imageUrls.value) : undefined,
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
      images: imageUrls.value.length > 0 ? JSON.stringify(imageUrls.value) : undefined,
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
    imageUrls.value = checkin.images ? JSON.parse(checkin.images) : []
  }
}

async function loadHistory() {
  historyLoading.value = true
  try {
    const from = dayjs().subtract(30, 'day').format('YYYY-MM-DD')
    const to = today.value
    const res = await checkinApi.getHistory(from, to)
    historyRecords.value = res.data.data ?? []
  } catch {
    message.error('加载历史记录失败')
  } finally {
    historyLoading.value = false
  }
}

function parseImages(images: string | null): string[] {
  if (!images) return []
  try {
    return JSON.parse(images)
  } catch {
    return []
  }
}

function getMoodEmoji(moodVal: number | null): string {
  const opt = moodOptions.find((o) => o.value === moodVal)
  return opt?.emoji ?? ''
}

function getMoodLabel(moodVal: number | null): string {
  const opt = moodOptions.find((o) => o.value === moodVal)
  return opt?.label ?? ''
}

function handleTabChange(tab: string) {
  activeTab.value = tab
  if (tab === 'history' && historyRecords.value.length === 0) {
    loadHistory()
  }
}

onMounted(() => load(loadData))
</script>

<template>
  <div class="checkin-view gradient-mesh">
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

      <NTabs :value="activeTab" type="line" @update:value="handleTabChange">
        <!-- Today Tab -->
        <NTabPane name="today" tab="今日">
          <!-- Plan Section -->
          <div class="section-card glass">
            <div class="section-header">
              <span class="section-label">今日计划</span>
              <NTag size="tiny" :bordered="false" type="info">步骤 1</NTag>
            </div>
            <NInput
              v-model:value="planText"
              type="textarea"
              placeholder="今天打算学什么？（选填）"
              :autosize="{ minRows: 2, maxRows: 5 }"
            />
          </div>

          <!-- Reflection Section -->
          <div class="section-card glass">
            <div class="section-header">
              <span class="section-label">学习复盘</span>
              <NTag size="tiny" :bordered="false" type="info">步骤 2</NTag>
            </div>
            <NInput
              v-model:value="reflectionText"
              type="textarea"
              placeholder="今天学得怎么样？有什么收获和不足？"
              :autosize="{ minRows: 4, maxRows: 8 }"
            />

            <div class="mood-section">
              <span class="mood-label">今日心情</span>
              <NRadioGroup v-model:value="mood">
                <div class="mood-options glass glass--subtle">
                  <NRadio v-for="opt in moodOptions" :key="opt.value" :value="opt.value">
                    {{ opt.emoji }} {{ opt.label }}
                  </NRadio>
                </div>
              </NRadioGroup>
            </div>

            <!-- Image Upload -->
            <div class="image-section">
              <span class="mood-label">附图（最多 6 张）</span>
              <NImageGroup>
                <div v-if="imageUrls.length > 0" class="image-preview-list">
                  <div v-for="(url, idx) in imageUrls" :key="idx" class="image-preview-item">
                    <NImage
                      :src="url"
                      width="80"
                      height="80"
                      object-fit="cover"
                      :preview-src="url"
                      lazy
                    />
                    <NButton
                      size="tiny"
                      quaternary
                      type="error"
                      class="image-remove-btn"
                      @click="imageUrls.splice(idx, 1)"
                    >
                      ✕
                    </NButton>
                  </div>
                </div>
              </NImageGroup>
              <NUpload
                v-if="imageUrls.length < 6"
                :max="6"
                :default-upload="false"
                :custom-request="handleImageUpload"
                accept="image/*"
                list-type="image"
                class="image-upload"
              >
                <NButton size="small" quaternary>+ 添加图片</NButton>
              </NUpload>
            </div>
          </div>

          <!-- Actions -->
          <div class="actions-bar">
            <NButton :loading="saving" @click="handleSave">
              保存
            </NButton>
            <NButton
              type="primary"
              :loading="saving"
              @click="handleComplete"
            >
              {{ isCompleted ? '重新打卡' : '完成打卡' }}
            </NButton>
          </div>
        </NTabPane>

        <!-- History Tab -->
        <NTabPane name="history" tab="历史">
          <div class="history-section">
            <div v-if="historyLoading" class="loading-center">
              <NSpin size="medium" />
            </div>
            <template v-else-if="historyRecords.length > 0">
              <div
                v-for="record in historyRecords"
                :key="record.id"
                class="history-card glass glass--subtle stagger-in"
              >
                <div class="history-card-header">
                  <span class="history-date">{{ record.checkinDate }}</span>
                  <NSpace :size="4">
                    <NTag v-if="record.completed" type="success" size="tiny" :bordered="false">已打卡</NTag>
                    <NTag v-if="record.mood" size="tiny" :bordered="false">
                      {{ getMoodEmoji(record.mood) }} {{ getMoodLabel(record.mood) }}
                    </NTag>
                    <NTag v-if="record.focusTotalSec > 0" size="tiny" :bordered="false">
                      {{ Math.floor(record.focusTotalSec / 60) }} 分钟
                    </NTag>
                  </NSpace>
                </div>
                <div v-if="record.planText" class="history-field">
                  <span class="history-field-label">计划</span>
                  <span class="history-field-value">{{ record.planText }}</span>
                </div>
                <div v-if="record.reflectionText" class="history-field">
                  <span class="history-field-label">复盘</span>
                  <span class="history-field-value">{{ record.reflectionText }}</span>
                </div>
                <div v-if="record.images" class="history-images">
                  <NImageGroup>
                    <NSpace :size="4">
                      <NImage
                        v-for="(url, idx) in parseImages(record.images)"
                        :key="idx"
                        :src="url"
                        width="60"
                        height="60"
                        object-fit="cover"
                        :preview-src="url"
                        lazy
                      />
                    </NSpace>
                  </NImageGroup>
                </div>
              </div>
            </template>
            <NEmpty v-else description="暂无历史复盘记录" />
          </div>
        </NTabPane>
      </NTabs>
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
  border-radius: var(--radius-md);
  padding: var(--sp-4);
  margin-bottom: var(--sp-3);
}

.section-header {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  margin-bottom: var(--sp-2);
}

.section-label {
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
  color: var(--text-color);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
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
  padding: var(--sp-2) var(--sp-3);
  border-radius: var(--radius-md);
}

.image-section {
  margin-top: var(--sp-3);
  padding-top: var(--sp-3);
  border-top: 1px solid var(--divider);
}

.image-preview-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--sp-2);
  margin-bottom: var(--sp-2);
}

.image-preview-item {
  position: relative;
  border-radius: var(--radius-sm);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.image-remove-btn {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: auto;
  padding: 0 4px;
  font-size: 10px;
}

.actions-bar {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-2);
}

/* History */
.history-section {
  padding: var(--sp-2) 0;
}

.history-card {
  border-radius: var(--radius-md);
  padding: var(--sp-4);
  margin-bottom: var(--sp-3);
}

.history-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-2);
}

.history-date {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.history-field {
  margin-top: var(--sp-2);
}

.history-field-label {
  font-size: var(--text-xs);
  font-weight: var(--weight-semibold);
  color: var(--text-color-muted);
  text-transform: uppercase;
  letter-spacing: var(--tracking-wide);
  margin-right: var(--sp-1);
}

.history-field-value {
  font-size: var(--text-sm);
  color: var(--text-color);
  white-space: pre-wrap;
}

.history-images {
  margin-top: var(--sp-2);
}
</style>
