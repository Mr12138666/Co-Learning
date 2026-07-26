import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useFocusStore } from '@/stores/focusStore'
import { useStudyStore } from '@/stores/studyStore'
import { useDashboardStore } from '@/stores/dashboardStore'
import { useGamificationStore } from '@/stores/gamificationStore'

/**
 * Server-authoritative focus timer composable.
 *
 * Key design decisions:
 * - Elapsed time is calculated from server's `startedAt` timestamp, not local accumulation.
 * - On mount, checks for active session via GET /api/focus-sessions/active for refresh recovery.
 * - Tab visibility changes don't affect server timing, only local display updates.
 */
export function useFocusTimer() {
  const focusStore = useFocusStore()
  const studyStore = useStudyStore()
  const dashboardStore = useDashboardStore()
  const gamificationStore = useGamificationStore()

  const now = ref(Date.now())
  let timerId: ReturnType<typeof setInterval> | null = null

  // Elapsed seconds computed from server timestamps
  const elapsedSeconds = computed(() => {
    const session = focusStore.activeSession
    if (!session) return 0

    const startedAt = new Date(session.startedAt).getTime()
    if (focusStore.isActive) {
      // Active: now - started - pausedSeconds
      return Math.max(0, Math.floor((now.value - startedAt) / 1000) - session.pausedSeconds)
    } else if (focusStore.isPaused) {
      // Paused: pausedAt - started - pausedSeconds
      if (session.pausedAt) {
        const pausedAt = new Date(session.pausedAt).getTime()
        return Math.max(0, Math.floor((pausedAt - startedAt) / 1000) - session.pausedSeconds)
      }
      return 0
    }
    return 0
  })

  // Formatted display strings
  const formattedTime = computed(() => {
    const total = elapsedSeconds.value
    const hours = Math.floor(total / 3600)
    const minutes = Math.floor((total % 3600) / 60)
    const seconds = total % 60
    if (hours > 0) {
      return `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    }
    return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
  })

  const progressPercent = computed(() => {
    // 25-minute pomodoro cycle, circular progress
    const target = 25 * 60
    const pct = (elapsedSeconds.value % target) / target
    return Math.round(pct * 100)
  })

  // 宽限期相关
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

  // Current subject info
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

  // Timer tick - update `now` every second when active or in grace period
  function startTick() {
    stopTick()
    timerId = setInterval(() => {
      now.value = Date.now()
      // 宽限期过期后，后端会 abort 会话，刷新前端状态
      if (isInGracePeriod.value && graceRemainingSeconds.value <= 0) {
        focusStore.fetchActive()
      }
    }, 1000)
  }

  function stopTick() {
    if (timerId) {
      clearInterval(timerId)
      timerId = null
    }
  }

  // Watch session status to start/stop ticking
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

  // Actions
  async function startFocus(subjectId?: number, taskId?: number) {
    const clientRequestId = `focus-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
    
    try {
      await focusStore.start({ subjectId, taskId, clientRequestId })
    } catch (error: any) {
      // If conflict (session already exists), abort it and retry
      if (error.response?.status === 409) {
        try {
          await focusStore.fetchActive()
          if (focusStore.hasSession) {
            await focusStore.abort()
          }
        } catch {
          // Ignore abort errors
        }
        await focusStore.start({ subjectId, taskId, clientRequestId })
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
      // Refresh stats after finishing (best effort)
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

  // Tab visibility handling - re-fetch session state when returning
  function handleVisibilityChange() {
    if (document.visibilityState === 'visible') {
      // Re-fetch active session to sync with server (may have been auto-paused)
      focusStore.fetchActive()
      now.value = Date.now()
      if (focusStore.isActive) {
        startTick()
      }
    }
  }

  // Initialize: check for active session on mount (refresh recovery)
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
    // State
    elapsedSeconds,
    formattedTime,
    progressPercent,
    currentSubject,
    currentTask,
    // 宽限期
    MAX_SESSION_HOURS,
    isInGracePeriod,
    isLearningLimit,
    graceRemainingSeconds,
    formattedGraceTime,
    // Store passthrough - use computed to maintain reactivity
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
