<script setup lang="ts">
import { ref, computed } from 'vue'
import { NButton, NSpace, NSelect, NTag, NText } from 'naive-ui'
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
const {
  elapsedSeconds,
  formattedTime,
  progressPercent,
  currentSubject,
  currentTask,
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
          stroke="var(--n-border-color, #e0e0e6)"
          :stroke-width="ringStroke"
        />
        <circle
          :cx="ringSize / 2"
          :cy="ringSize / 2"
          :r="ringRadius"
          fill="none"
          stroke="#2080F0"
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
          <NTag :type="isActive ? 'success' : 'warning'" size="small" round>
            {{ isActive ? '专注中' : '已暂停' }}
          </NTag>
        </div>
        <div v-else class="session-status">
          <NText depth="3" style="font-size: 13px;">准备开始</NText>
        </div>
      </div>
    </div>

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
            v-if="isPaused"
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
</style>
