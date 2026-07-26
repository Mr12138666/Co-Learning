import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useFocusStore } from '@/stores/focusStore'
import { useStudyStore } from '@/stores/studyStore'
import { useDashboardStore } from '@/stores/dashboardStore'
import { useGamificationStore } from '@/stores/gamificationStore'

export type TimerMode = 'flowtime' | 'pomodoro' | 'countdown'
export type PomodoroPhase = 'work' | 'shortBreak' | 'longBreak'

/**
 * Server-authoritative focus timer composable with three modes:
 * - Flowtime: count up (stopwatch), no fixed duration
 * - Pomodoro: work/break cycles with configurable durations
 * - Countdown: count down from a target duration
 *
 * The server tracks the actual focus session (startedAt, pausedSeconds).
 * Pomodoro phases and countdown target are client-side only.
 */
export function useFocusTimer() {
  const focusStore = useFocusStore()
  const studyStore = useStudyStore()
  const dashboardStore = useDashboardStore()
  const gamificationStore = useGamificationStore()

  const now = ref(Date.now())
  let timerId: ReturnType<typeof setInterval> | null = null

  // ===== Timer Mode =====
  const timerMode = ref<TimerMode>('flowtime')

  // Pomodoro config
  const pomodoroWorkMinutes = ref(25)
  const pomodoroShortBreakMinutes = ref(5)
  const pomodoroLongBreakMinutes = ref(15)
  const pomodoroCyclesBeforeLongBreak = ref(4)

  // Countdown config
  const countdownMinutes = ref(30)

  // Elapsed seconds computed from server timestamps
  const elapsedSeconds = computed(() => {
    const session = focusStore.activeSession
    if (!session) return 0

    const startedAt = new Date(session.startedAt).getTime()
    if (focusStore.isActive) {
      return Math.max(0, Math.floor((now.value - startedAt) / 1000) - session.pausedSeconds)
    } else if (focusStore.isPaused) {
      if (session.pausedAt) {
        const pausedAt = new Date(session.pausedAt).getTime()
        return Math.max(0, Math.floor((pausedAt - startedAt) / 1000) - session.pausedSeconds)
      }
      return 0
    }
    return 0
  })

  // ===== Flowtime Mode =====
  const flowtimeFormatted = computed(() => {
    const total = elapsedSeconds.value
    const hours = Math.floor(total / 3600)
    const minutes = Math.floor((total % 3600) / 60)
    const seconds = total % 60
    if (hours > 0) {
      return `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    }
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  })

  const flowtimeProgress = computed(() => {
    const MAX_SESSION_HOURS = 8
    const maxSeconds = MAX_SESSION_HOURS * 3600
    return Math.min(100, Math.round((elapsedSeconds.value / maxSeconds) * 100))
  })

  // ===== Pomodoro Mode =====
  const pomodoroPhase = computed<PomodoroPhase>(() => {
    if (!focusStore.hasSession) return 'work'
    const elapsed = elapsedSeconds.value
    const workSec = pomodoroWorkMinutes.value * 60
    const shortBreakSec = pomodoroShortBreakMinutes.value * 60
    const longBreakSec = pomodoroLongBreakMinutes.value * 60
    const N = pomodoroCyclesBeforeLongBreak.value

    // Full set: N work + (N-1) short breaks + 1 long break
    const fullSetDuration = N * workSec + (N - 1) * shortBreakSec + longBreakSec
    const positionInSet = elapsed % fullSetDuration

    // Walk through the phases in order
    let accumulated = 0
    for (let i = 0; i < N; i++) {
      // Work phase
      if (positionInSet < accumulated + workSec) {
        return 'work'
      }
      accumulated += workSec

      // Break phase
      if (i < N - 1) {
        // Short break
        if (positionInSet < accumulated + shortBreakSec) {
          return 'shortBreak'
        }
        accumulated += shortBreakSec
      } else {
        // Long break (last cycle)
        if (positionInSet < accumulated + longBreakSec) {
          return 'longBreak'
        }
        accumulated += longBreakSec
      }
    }
    return 'work' // fallback
  })

  const pomodoroCycleCount = computed(() => {
    if (!focusStore.hasSession) return 0
    const elapsed = elapsedSeconds.value
    const workSec = pomodoroWorkMinutes.value * 60
    const shortBreakSec = pomodoroShortBreakMinutes.value * 60
    const longBreakSec = pomodoroLongBreakMinutes.value * 60
    const N = pomodoroCyclesBeforeLongBreak.value
    const fullSetDuration = N * workSec + (N - 1) * shortBreakSec + longBreakSec
    const positionInSet = elapsed % fullSetDuration

    let accumulated = 0
    for (let i = 0; i < N; i++) {
      if (positionInSet < accumulated + workSec) {
        return i + 1
      }
      accumulated += workSec
      if (i < N - 1) {
        accumulated += shortBreakSec
      } else {
        accumulated += longBreakSec
      }
    }
    return N
  })

  const pomodoroPhaseRemaining = computed(() => {
    if (!focusStore.hasSession) return 0
    const elapsed = elapsedSeconds.value
    const workSec = pomodoroWorkMinutes.value * 60
    const shortBreakSec = pomodoroShortBreakMinutes.value * 60
    const longBreakSec = pomodoroLongBreakMinutes.value * 60
    const N = pomodoroCyclesBeforeLongBreak.value
    const fullSetDuration = N * workSec + (N - 1) * shortBreakSec + longBreakSec
    const positionInSet = elapsed % fullSetDuration

    let accumulated = 0
    for (let i = 0; i < N; i++) {
      if (positionInSet < accumulated + workSec) {
        return workSec - (positionInSet - accumulated)
      }
      accumulated += workSec
      if (i < N - 1) {
        if (positionInSet < accumulated + shortBreakSec) {
          return shortBreakSec - (positionInSet - accumulated)
        }
        accumulated += shortBreakSec
      } else {
        if (positionInSet < accumulated + longBreakSec) {
          return longBreakSec - (positionInSet - accumulated)
        }
        accumulated += longBreakSec
      }
    }
    return workSec
  })

  const pomodoroFormatted = computed(() => {
    const remaining = pomodoroPhaseRemaining.value
    const minutes = Math.floor(remaining / 60)
    const seconds = remaining % 60
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  })

  const pomodoroProgress = computed(() => {
    if (!focusStore.hasSession) return 0
    const workSec = pomodoroWorkMinutes.value * 60
    const shortBreakSec = pomodoroShortBreakMinutes.value * 60
    const longBreakSec = pomodoroLongBreakMinutes.value * 60
    const N = pomodoroCyclesBeforeLongBreak.value
    const fullSetDuration = N * workSec + (N - 1) * shortBreakSec + longBreakSec
    const positionInSet = elapsedSeconds.value % fullSetDuration

    let accumulated = 0
    for (let i = 0; i < N; i++) {
      if (positionInSet < accumulated + workSec) {
        return Math.round(((positionInSet - accumulated) / workSec) * 100)
      }
      accumulated += workSec
      if (i < N - 1) {
        if (positionInSet < accumulated + shortBreakSec) {
          return Math.round(((positionInSet - accumulated) / shortBreakSec) * 100)
        }
        accumulated += shortBreakSec
      } else {
        if (positionInSet < accumulated + longBreakSec) {
          return Math.round(((positionInSet - accumulated) / longBreakSec) * 100)
        }
        accumulated += longBreakSec
      }
    }
    return 0
  })

  const pomodoroPhaseLabel = computed(() => {
    switch (pomodoroPhase.value) {
      case 'work': return '专注中'
      case 'shortBreak': return '短休息'
      case 'longBreak': return '长休息'
      default: return '专注中'
    }
  })

  // ===== Countdown Mode =====
  const countdownTargetSeconds = computed(() => countdownMinutes.value * 60)
  const countdownRemaining = computed(() => {
    if (!focusStore.hasSession) return countdownTargetSeconds.value
    return Math.max(0, countdownTargetSeconds.value - elapsedSeconds.value)
  })
  const countdownFinished = computed(() => {
    return focusStore.hasSession && countdownRemaining.value <= 0
  })

  const countdownFormatted = computed(() => {
    const remaining = countdownRemaining.value
    const minutes = Math.floor(remaining / 60)
    const seconds = remaining % 60
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  })

  const countdownProgress = computed(() => {
    if (!focusStore.hasSession) return 0
    const target = countdownTargetSeconds.value
    if (target <= 0) return 0
    return Math.min(100, Math.round((elapsedSeconds.value / target) * 100))
  })

  // ===== Unified Display =====
  const formattedTime = computed(() => {
    switch (timerMode.value) {
      case 'flowtime': return flowtimeFormatted.value
      case 'pomodoro': return pomodoroFormatted.value
      case 'countdown': return countdownFormatted.value
      default: return flowtimeFormatted.value
    }
  })

  const progressPercent = computed(() => {
    switch (timerMode.value) {
      case 'flowtime': return flowtimeProgress.value
      case 'pomodoro': return pomodoroProgress.value
      case 'countdown': return countdownProgress.value
      default: return flowtimeProgress.value
    }
  })

  // ===== Grace Period =====
  const MAX_SESSION_HOURS = 8
  const graceDeadline = computed(() => focusStore.activeSession?.graceDeadline ?? null)
  const graceReason = computed(() => focusStore.activeSession?.graceReason ?? null)
  const isInGracePeriod = computed(() => graceDeadline.value !== null)
  const isLearningLimit = computed(() => graceReason.value === 'LEARNING_LIMIT')

  const graceRemainingSeconds = computed(() => {
    if (!graceDeadline.value) return 0
    const deadline = new Date(graceDeadline.value).getTime()
    return Math.max(0, Math.floor((deadline - now.value) / 1000))
  })

  const formattedGraceTime = computed(() => {
    const total = graceRemainingSeconds.value
    const minutes = Math.floor(total / 60)
    const seconds = total % 60
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  })

  // ===== Subject/Task Info =====
  const currentSubject = computed(() => {
    const subjectId = focusStore.activeSession?.subjectId
    if (!subjectId) return null
    return studyStore.subjectMap.get(subjectId) ?? null
  })

  const currentTask = computed(() => {
    const taskId = focusStore.activeSession?.taskId
    if (!taskId) return null
    return studyStore.tasks.find((t) => t.id === taskId) ?? null
  })

  // ===== Timer Tick =====
  function startTick() {
    stopTick()
    timerId = setInterval(() => {
      now.value = Date.now()
      // Grace period expiry
      if (isInGracePeriod.value && graceRemainingSeconds.value <= 0) {
        focusStore.fetchActive()
      }
      // Countdown auto-finish
      if (timerMode.value === 'countdown' && countdownFinished.value) {
        finishFocus()
      }
    }, 1000)
  }

  function stopTick() {
    if (timerId) {
      clearInterval(timerId)
      timerId = null
    }
  }

  watch(
    () => [focusStore.isActive, isInGracePeriod.value],
    ([active, inGrace]) => {
      if (active || inGrace) {
        startTick()
      } else {
        stopTick()
      }
    },
    { immediate: true },
  )

  // ===== Actions =====
  async function startFocus(subjectId?: number, taskId?: number) {
    const clientRequestId = `focus-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`

    try {
      await focusStore.start({ subjectId, taskId, clientRequestId })
    } catch (error: any) {
      if (error.response?.status === 409) {
        // Server says another session is active - restore it instead of aborting
        await focusStore.fetchActive()
        throw new Error('EXISTING_SESSION_RESTORED')
      } else {
        throw error
      }
    }

    now.value = Date.now()
    startTick()
  }

  async function pauseFocus() {
    try {
      await focusStore.pause()
    } finally {
      stopTick()
    }
  }

  async function resumeFocus() {
    await focusStore.resume()
    now.value = Date.now()
    startTick()
  }

  async function finishFocus() {
    try {
      const result = await focusStore.finish()
      return result
    } finally {
      stopTick()
      dashboardStore.fetchStats().catch(() => {})
      dashboardStore.fetchTodayCheckin().catch(() => {})
      gamificationStore.loadProfile().catch(() => {})
      gamificationStore.loadPet().catch(() => {})
    }
  }

  async function abortFocus() {
    try {
      await focusStore.abort()
    } finally {
      stopTick()
    }
  }

  function handleVisibilityChange() {
    if (document.visibilityState === 'visible') {
      focusStore.fetchActive()
      now.value = Date.now()
      if (focusStore.isActive) {
        startTick()
      }
    }
  }

  async function init() {
    await focusStore.fetchActive()
    if (focusStore.isActive) {
      now.value = Date.now()
      startTick()
    }
  }

  onMounted(() => {
    init()
    document.addEventListener('visibilitychange', handleVisibilityChange)
  })

  onUnmounted(() => {
    stopTick()
    document.removeEventListener('visibilitychange', handleVisibilityChange)
  })

  return {
    // Mode
    timerMode,
    // Pomodoro config
    pomodoroWorkMinutes,
    pomodoroShortBreakMinutes,
    pomodoroLongBreakMinutes,
    pomodoroCyclesBeforeLongBreak,
    pomodoroPhase,
    pomodoroCycleCount,
    pomodoroPhaseLabel,
    // Countdown config
    countdownMinutes,
    countdownFinished,
    // Unified display
    elapsedSeconds,
    formattedTime,
    progressPercent,
    currentSubject,
    currentTask,
    // Grace period
    MAX_SESSION_HOURS,
    isInGracePeriod,
    isLearningLimit,
    graceRemainingSeconds,
    formattedGraceTime,
    // Store passthrough
    hasSession: computed(() => focusStore.hasSession),
    isActive: computed(() => focusStore.isActive),
    isPaused: computed(() => focusStore.isPaused),
    loading: computed(() => focusStore.loading),
    // Actions
    startFocus,
    pauseFocus,
    resumeFocus,
    finishFocus,
    abortFocus,
  }
}
