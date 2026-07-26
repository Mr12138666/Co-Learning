<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NSpace, NSelect, NTag, NText, NAlert, NRadioGroup, NRadioButton, NInputNumber, useMessage } from 'naive-ui'
import { useFocusTimer } from '@/composables/useFocusTimer'
import { useStudyStore } from '@/stores/studyStore'

const props = withDefaults(defineProps<{
  compact?: boolean
}>(), {
  compact: false,
})

const emit = defineEmits<{
  finished: [session: unknown]
}>()

const studyStore = useStudyStore()
const message = useMessage()

const {
  timerMode,
  pomodoroWorkMinutes,
  pomodoroShortBreakMinutes,
  pomodoroLongBreakMinutes,
  pomodoroCyclesBeforeLongBreak,
  pomodoroPhase,
  pomodoroCycleCount,
  pomodoroPhaseLabel,
  countdownMinutes,
  countdownFinished,
  formattedTime,
  progressPercent,
  currentSubject,
  currentTask,
  MAX_SESSION_HOURS,
  isInGracePeriod,
  isLearningLimit,
  formattedGraceTime,
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

const subjectOptions = computed(() =>
  studyStore.subjects.map((s) => ({
    label: s.name,
    value: s.id,
  })),
)

const selectedSubjectId = ref<number | null>(null)

async function handleStart() {
  await startFocus(selectedSubjectId.value ?? undefined)
}

async function handleFinish() {
  const result = await finishFocus()
  emit('finished', result)

  if (result && (result as any).effectiveSeconds) {
    const effectiveSeconds = (result as any).effectiveSeconds
    const exp = Math.max(1, Math.floor(effectiveSeconds / 600))
    const tokens = Math.max(1, Math.floor(effectiveSeconds / 600))
    message.success(`专注完成！获得 ${exp} 经验 + ${tokens} 代币`)
  }
}

// Ring
const ringSize = computed(() => (props.compact ? 160 : 220))
const ringStroke = computed(() => (props.compact ? 8 : 12))
const ringRadius = computed(() => (ringSize.value - ringStroke.value) / 2)
const ringCircumference = computed(() => 2 * Math.PI * ringRadius.value)
const ringDashOffset = computed(() =>
  ringCircumference.value - (progressPercent.value / 100) * ringCircumference.value,
)

// Mode options for radio group
const modeOptions = [
  { label: '正计时', value: 'flowtime' },
  { label: '番茄钟', value: 'pomodoro' },
  { label: '倒计时', value: 'countdown' },
]

// Countdown preset options
const countdownPresets = [
  { label: '15分钟', value: 15 },
  { label: '25分钟', value: 25 },
  { label: '30分钟', value: 30 },
  { label: '45分钟', value: 45 },
  { label: '60分钟', value: 60 },
  { label: '90分钟', value: 90 },
]

// Status label based on mode
const statusLabel = computed(() => {
  if (!hasSession.value) return '准备开始'
  if (isInGracePeriod.value) return `宽限期 ${formattedGraceTime.value}`
  if (isPaused.value) return '已暂停'
  if (timerMode.value === 'pomodoro') return pomodoroPhaseLabel.value
  if (timerMode.value === 'countdown' && countdownFinished.value) return '倒计时结束'
  return '专注中'
})

const statusType = computed(() => {
  if (isInGracePeriod.value) return 'error'
  if (isPaused.value) return 'warning'
  if (timerMode.value === 'pomodoro' && pomodoroPhase.value !== 'work') return 'info'
  return 'success'
})
</script>

<template>
  <div class="focus-timer" :class="{ compact }">
    <!-- Mode Selector (only when no active session) -->
    <div v-if="!hasSession && !compact" class="mode-selector">
      <NRadioGroup v-model:value="timerMode" size="small">
        <NRadioButton
          v-for="opt in modeOptions"
          :key="opt.value"
          :value="opt.value"
          :label="opt.label"
        />
      </NRadioGroup>
    </div>

    <!-- Pomodoro Settings (only when pomodoro mode and no session) -->
    <div v-if="timerMode === 'pomodoro' && !hasSession && !compact" class="mode-settings">
      <NSpace align="center" :size="8">
        <NText depth="3" style="font-size: 12px;">专注</NText>
        <NInputNumber v-model:value="pomodoroWorkMinutes" :min="1" :max="120" size="small" style="width: 70px;" />
        <NText depth="3" style="font-size: 12px;">分钟</NText>
        <NText depth="3" style="font-size: 12px; margin-left: 8px;">短休</NText>
        <NInputNumber v-model:value="pomodoroShortBreakMinutes" :min="1" :max="30" size="small" style="width: 70px;" />
        <NText depth="3" style="font-size: 12px;">分钟</NText>
        <NText depth="3" style="font-size: 12px; margin-left: 8px;">长休</NText>
        <NInputNumber v-model:value="pomodoroLongBreakMinutes" :min="1" :max="60" size="small" style="width: 70px;" />
        <NText depth="3" style="font-size: 12px;">分钟</NText>
      </NSpace>
    </div>

    <!-- Countdown Settings (only when countdown mode and no session) -->
    <div v-if="timerMode === 'countdown' && !hasSession && !compact" class="mode-settings">
      <NSpace align="center" :size="8">
        <NText depth="3" style="font-size: 12px;">时长</NText>
        <NSelect
          v-model:value="countdownMinutes"
          :options="countdownPresets"
          size="small"
          style="width: 120px;"
        />
        <NText depth="3" style="font-size: 12px;">分钟</NText>
      </NSpace>
    </div>

    <!-- Timer Ring -->
    <div class="timer-ring" :style="{ width: `${ringSize}px`, height: `${ringSize}px` }">
      <svg :width="ringSize" :height="ringSize" class="ring-svg">
        <circle
          :cx="ringSize / 2"
          :cy="ringSize / 2"
          :r="ringRadius"
          fill="none"
          stroke="var(--border-default, #e0e0e6)"
          :stroke-width="ringStroke"
        />
        <circle
          :cx="ringSize / 2"
          :cy="ringSize / 2"
          :r="ringRadius"
          fill="none"
          :stroke="timerMode === 'pomodoro' && pomodoroPhase !== 'work' ? 'var(--info, #3b82f6)' : 'var(--accent, #2080F0)'"
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
          <NTag :type="statusType" size="small" round>
            {{ statusLabel }}
          </NTag>
        </div>
        <div v-else class="session-status">
          <NText depth="3" style="font-size: 13px;">准备开始</NText>
        </div>
      </div>
    </div>

    <!-- Pomodoro cycle indicator -->
    <div v-if="timerMode === 'pomodoro' && hasSession && !compact" class="pomodoro-cycles">
      <NText depth="3" style="font-size: 12px;">
        第 {{ pomodoroCycleCount }} / {{ pomodoroCyclesBeforeLongBreak }} 轮
      </NText>
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

    <!-- Max session hint -->
    <div v-if="!hasSession && !compact && timerMode === 'flowtime'" class="max-session-hint">
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
          <NButton
            v-if="isActive"
            size="large"
            @click="pauseFocus"
          >
            暂停
          </NButton>
          <NButton
            v-if="isPaused && !isLearningLimit"
            type="primary"
            size="large"
            @click="resumeFocus"
          >
            继续
          </NButton>
          <NButton
            type="success"
            size="large"
            @click="handleFinish"
          >
            结束
          </NButton>
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
  gap: var(--sp-4);
}

.mode-selector {
  display: flex;
  justify-content: center;
}

.mode-settings {
  display: flex;
  justify-content: center;
  padding: var(--sp-2) var(--sp-4);
  background: var(--bg-sunken);
  border-radius: var(--radius-md);
}

.pomodoro-cycles {
  display: flex;
  justify-content: center;
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
  gap: var(--sp-2);
  z-index: 1;
}

.time-text {
  font-size: 36px;
  font-weight: var(--weight-bold);
  font-variant-numeric: tabular-nums;
  color: var(--accent);
  line-height: 1;
}

.compact .time-text {
  font-size: 28px;
}

.session-status {
  display: flex;
  align-items: center;
  gap: var(--sp-1);
}

.session-info {
  text-align: center;
}

.subject-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  margin-right: var(--sp-1);
}

.controls {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  justify-content: center;
  flex-wrap: wrap;
}

.max-session-hint {
  text-align: center;
  opacity: 0.7;
}
</style>
