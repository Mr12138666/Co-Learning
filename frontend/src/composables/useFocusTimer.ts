import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useFocusStore } from '@/stores/focusStore'
import { useStudyStore } from '@/stores/studyStore'
import { useDashboardStore } from '@/stores/dashboardStore'
import dayjs from 'dayjs'

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

  const now = ref(Date.now())
  let timerId: ReturnType<typeof setInterval> | null = null

  // Elapsed seconds computed from server timestamps
  const elapsedSeconds = computed(() => {
    const session = focusStore.activeSession
    if (!session) return 0

    const startedAt = dayjs(session.startedAt)
    if (focusStore.isActive) {
      // Active: now - started - pausedSeconds
      return Math.max(0, Math.floor((now.value - startedAt.valueOf()) / 1000) - session.pausedSeconds)
    } else if (focusStore.isPaused) {
      // Paused: pausedAt - started - pausedSeconds
      if (session.pausedAt) {
        return Math.max(0, Math.floor((dayjs(session.pausedAt).valueOf() - startedAt.valueOf()) / 1000) - session.pausedSeconds)
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

  // Timer tick - update `now` every second when active
  function startTick() {
    stopTick()
    timerId = setInterval(() => {
      now.value = Date.now()
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
    () => focusStore.isActive,
    (active) => {
      if (active) {
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
    await focusStore.start({ subjectId, taskId, clientRequestId })
    now.value = Date.now()
    startTick()
  }

  async function pauseFocus() {
    await focusStore.pause()
    stopTick()
  }

  async function resumeFocus() {
    await focusStore.resume()
    now.value = Date.now()
    startTick()
  }

  async function finishFocus() {
    const result = await focusStore.finish()
    stopTick()
    // Refresh stats after finishing
    await dashboardStore.fetchStats()
    await dashboardStore.fetchTodayCheckin()
    return result
  }

  async function abortFocus() {
    await focusStore.abort()
    stopTick()
  }

  // Tab visibility handling - just update `now` when returning
  function handleVisibilityChange() {
    if (document.visibilityState === 'visible') {
      now.value = Date.now()
      // If session is active, restart ticking
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
    // Store passthrough
    hasSession: focusStore.hasSession,
    isActive: focusStore.isActive,
    isPaused: focusStore.isPaused,
    loading: focusStore.loading,
    // Actions
    startFocus,
    pauseFocus,
    resumeFocus,
    finishFocus,
    abortFocus,
  }
}
