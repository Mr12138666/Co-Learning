<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NSpace, NSelect, NTag, NText, NAlert, useMessage } from 'naive-ui'
import { useFocusTimer } from '@/composables/useFocusTimer'
import { useStudyStore } from '@/stores/studyStore'
import { useGamificationStore } from '@/stores/gamificationStore'

const props = withDefaults(defineProps<{
  compact?: boolean
}>(), {
  compact: false,
})

const emit = defineEmits<{
  finished: [session: unknown]
}>()

const studyStore = useStudyStore()
const gamificationStore = useGamificationStore()
const message = useMessage()

const {
  elapsedSeconds,
  formattedTime,
  progressPercent,
  currentSubject,
  currentTask,
  MAX_SESSION_HOURS,
  isInGracePeriod,
  isLearningLimit,
  formattedGraceTime,
  graceRemainingSeconds,
  hasSession,
  isActive,
  isPaused,
  loading,
  startFocus,
  pauseFocus,
  resumeFocus,
  finishFocus,
  abortFocus,
} = useFocusTimer()

// Subject options for starting
const subjectOptions = computed(() =>
  studyStore.subjects.map((s) => ({
    label: s.name,
    value: s.id,
  })),
)

// Selected subject for new session
const selectedSubjectId = ref<number | null>(null)

async function handleStart() {
  await startFocus(selectedSubjectId.value ?? undefined)
}

async function handleFinish() {
  const result = await finishFocus()
  emit('finished', result)

  // Show reward notification
  if (result && (result as any).effectiveSeconds) {
    const effectiveSeconds = (result as any).effectiveSeconds
    // EXP and Tokens: every 10 minutes = 1 (600 seconds)
    const exp = Math.max(1, Math.floor(effectiveSeconds / 600))
    const tokens = Math.max(1, Math.floor(effectiveSeconds / 600))

    message.success(`专注完成！获得 ${exp} 经验 + ${tokens} 代币 🪙`)
  }
}

// Ring size
const ringSize = computed(() => (props.compact ? 160 : 220))
const ringStroke = computed(() => (props.compact ? 8 : 12))
const ringRadius = computed(() => (ringSize.value - ringStroke.value) / 2)
const ringCircumference = computed(() => 2 * Math.PI * ringRadius.value)
const ringDashOffset = computed(() =>
  ringCircumference.value - (progressPercent.value / 100) * ringCircumference.value,
)
</script>

<template>
  <div class="focus-timer" :class="{ compact }">
    <!-- Timer Ring -->
    <div class="timer-ring" :style="{ width: `${ringSize}px`, height: `${ringSize}px` }">
      <svg :width="ringSize" :height="ringSize" class="ring-svg">
        <circle
          :cx="ringSize / 2"
          :cy="ringSize / 2"
          :r="ringRadius"
          fill="none"
          stroke="var(--border-color, #e0e0e6)"
          :stroke-width="ringStroke"
        />
        <circle
          :cx="ringSize / 2"
          :cy="ringSize / 2"
          :r="ringRadius"
          fill="none"
          stroke="var(--accent-primary, #2080F0)"
          :stroke-width="ringStroke"
          stroke-linecap="round"
          :stroke-dasharray="ringCircumference"
          :stroke-dashoffset="ringDashOffset"
          transform="rotate(-90)"
          :transform-origin="`${ringSize / 2} ${ringSize / 2}`"
          class="progress-circle"
          :class="{ active: isActive }"
        />
      </svg>
      <div class="timer-display">
        <div class="time-text">{{ formattedTime }}</div>
        <div v-if="hasSession" class="session-status">
          <NTag v-if="isInGracePeriod" type="error" size="small" round>
            宽限期 {{ formattedGraceTime }}
          </NTag>
          <NTag v-else :type="isActive ? 'success' : 'warning'" size="small" round>
            {{ isActive ? '专注中' : '已暂停' }}
          </NTag>
        </div>
        <div v-else class="session-status">
          <NText depth="3" style="font-size: 13px;">准备开始</NText>
        </div>
      </div>
    </div>

    <!-- Grace period warning -->
    <NAlert
      v-if="isInGracePeriod"
      :type="isLearningLimit ? 'warning' : 'info'"
      :title="isLearningLimit ? '已达到学习时长上限' : '暂停时间过长'"
      style="max-width: 360px; text-align: left;"
      :show-icon="true"
    >
      <template v-if="isLearningLimit">
        连续学习已达 {{ MAX_SESSION_HOURS }} 小时上限，请在 {{ formattedGraceTime }} 内结束会话，
        否则本次专注时长将不计入统计。
      </template>
      <template v-else>
        暂停已超过 1 小时，请在 {{ formattedGraceTime }} 内继续或结束会话，
        否则本次专注时长将不计入统计。
      </template>
    </NAlert>

    <!-- Current session info -->
    <div v-if="hasSession" class="session-info">
      <NSpace vertical :size="4" align="center">
        <NText v-if="currentSubject" strong>
          <span
            class="subject-dot"
            :style="{ background: currentSubject.color }"
          />
          {{ currentSubject.name }}
        </NText>
        <NText v-if="currentTask" depth="3" style="font-size: 13px;">
          {{ currentTask.title }}
        </NText>
      </NSpace>
    </div>

    <!-- Max session hint (only when no session) -->
    <div v-if="!hasSession && !compact" class="max-session-hint">
      <NText depth="3" style="font-size: 12px;">
        单次专注最多 {{ MAX_SESSION_HOURS }} 小时，超过将自动暂停
      </NText>
    </div>

    <!-- Controls -->
    <div class="controls">
      <template v-if="!hasSession">
        <NSelect
          v-model:value="selectedSubjectId"
          :options="subjectOptions"
          placeholder="选择科目（可选）"
          clearable
          size="medium"
          style="width: 200px;"
        />
        <NButton
          type="primary"
          size="large"
          :loading="loading"
          @click="handleStart"
        >
          开始专注
        </NButton>
      </template>
      <template v-else>
        <NSpace justify="center" :size="12">
          <!-- ACTIVE 状态：可暂停 -->
          <NButton
            v-if="isActive"
            size="large"
            @click="pauseFocus"
          >
            暂停
          </NButton>
          <!-- PAUSED 且非8h超限：可继续 -->
          <NButton
            v-if="isPaused && !isLearningLimit"
            type="primary"
            size="large"
            @click="resumeFocus"
          >
            继续
          </NButton>
          <!-- 始终可结束 -->
          <NButton
            type="success"
            size="large"
            @click="handleFinish"
          >
            结束
          </NButton>
          <!-- 始终可放弃 -->
          <NButton
            size="large"
            quaternary
            type="error"
            @click="abortFocus"
          >
            放弃
          </NButton>
        </NSpace>
      </template>
    </div>
  </div>
</template>

<style scoped>
.focus-timer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.timer-ring {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-svg {
  position: absolute;
  top: 0;
  left: 0;
}

.progress-circle {
  transition: stroke-dashoffset 1s linear;
}

.progress-circle.active {
  filter: drop-shadow(0 0 6px rgba(32, 128, 240, 0.3));
}

.timer-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  z-index: 1;
}

.time-text {
  font-size: 36px;
  font-weight: 700;
  font-variant-numeric: tabular-nums;
  color: #2080F0;
  line-height: 1;
}

.compact .time-text {
  font-size: 28px;
}

.session-status {
  display: flex;
  align-items: center;
  gap: 4px;
}

.session-info {
  text-align: center;
}

.subject-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 4px;
}

.controls {
  display: flex;
  align-items: center;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}

.max-session-hint {
  text-align: center;
  opacity: 0.7;
}
</style>
