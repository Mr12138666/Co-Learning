import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { mount } from '@vue/test-utils'

// Mock stores before importing composable
vi.mock('@/stores/studyStore', () => ({
  useStudyStore: () => ({
    subjectMap: new Map(),
    tasks: [],
  }),
}))

vi.mock('@/stores/dashboardStore', () => ({
  useDashboardStore: () => ({
    fetchStats: vi.fn().mockResolvedValue(undefined),
    fetchTodayCheckin: vi.fn().mockResolvedValue(undefined),
  }),
}))

vi.mock('@/stores/gamificationStore', () => ({
  useGamificationStore: () => ({
    loadProfile: vi.fn().mockResolvedValue(undefined),
    loadPet: vi.fn().mockResolvedValue(undefined),
  }),
}))

// We need to mock focusStore at a deeper level to control activeSession
const mockFocusState = {
  activeSession: null as any,
  loading: false,
}

vi.mock('@/stores/focusStore', () => ({
  useFocusStore: () => ({
    get activeSession() { return mockFocusState.activeSession },
    get loading() { return mockFocusState.loading },
    get isActive() { return mockFocusState.activeSession?.status === 'ACTIVE' },
    get isPaused() { return mockFocusState.activeSession?.status === 'PAUSED' },
    get hasSession() { return !!mockFocusState.activeSession },
    get sessionId() { return mockFocusState.activeSession?.sessionId ?? null },
    fetchActive: vi.fn().mockResolvedValue(undefined),
    start: vi.fn().mockResolvedValue({}),
    pause: vi.fn().mockResolvedValue(undefined),
    resume: vi.fn().mockResolvedValue(undefined),
    finish: vi.fn().mockResolvedValue(null),
    abort: vi.fn().mockResolvedValue(undefined),
    clear: vi.fn(),
  }),
}))

import { useFocusTimer } from '../useFocusTimer'

/**
 * Helper: mount a minimal component so the composable runs inside a valid
 * Vue component setup (onMounted / onUnmounted register correctly).
 */
function withSetup(composable: () => ReturnType<typeof useFocusTimer>) {
  let result: ReturnType<typeof useFocusTimer>
  const wrapper = mount({
    setup() {
      result = composable()
      // expose every returned key so the outer scope can read/write refs
      return result
    },
    template: '<div />',
  })
  return { result: result!, wrapper }
}

describe('useFocusTimer', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    mockFocusState.activeSession = null
    vi.useFakeTimers()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  // ===== Mode Selection =====
  it('defaults to flowtime mode', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.timerMode.value).toBe('flowtime')
    wrapper.unmount()
  })

  it('allows switching to pomodoro mode', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    result.timerMode.value = 'pomodoro'
    expect(result.timerMode.value).toBe('pomodoro')
    wrapper.unmount()
  })

  it('allows switching to countdown mode', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    result.timerMode.value = 'countdown'
    expect(result.timerMode.value).toBe('countdown')
    wrapper.unmount()
  })

  // ===== No Session State =====
  it('shows 00:00 when no session', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.formattedTime.value).toBe('00:00')
    wrapper.unmount()
  })

  it('shows 0 progress when no session', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.progressPercent.value).toBe(0)
    wrapper.unmount()
  })

  it('hasSession is false when no session', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.hasSession.value).toBe(false)
    wrapper.unmount()
  })

  // ===== Flowtime Mode =====
  it('flowtime shows elapsed time in MM:SS format', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 125000).toISOString(), // 2 min 5 sec ago
      pausedSeconds: 0,
      elapsedSeconds: 125,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.elapsedSeconds.value).toBe(125)
    expect(result.formattedTime.value).toBe('02:05')
    wrapper.unmount()
  })

  it('flowtime shows HH:MM:SS when over 1 hour', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 3661000).toISOString(), // 1h 1min 1sec ago
      pausedSeconds: 0,
      elapsedSeconds: 3661,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.formattedTime.value).toBe('1:01:01')
    wrapper.unmount()
  })

  it('flowtime accounts for paused seconds', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 300000).toISOString(), // 5 min ago
      pausedSeconds: 120, // 2 min paused
      elapsedSeconds: 180,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.elapsedSeconds.value).toBe(180) // 300 - 120 = 180
    wrapper.unmount()
  })

  // ===== Pomodoro Mode =====
  it('pomodoro defaults: 25min work, 5min short break, 15min long break', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.pomodoroWorkMinutes.value).toBe(25)
    expect(result.pomodoroShortBreakMinutes.value).toBe(5)
    expect(result.pomodoroLongBreakMinutes.value).toBe(15)
    expect(result.pomodoroCyclesBeforeLongBreak.value).toBe(4)
    wrapper.unmount()
  })

  it('pomodoro shows work phase initially', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 600000).toISOString(), // 10 min ago
      pausedSeconds: 0,
      elapsedSeconds: 600,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.pomodoroPhase.value).toBe('work')
    wrapper.unmount()
  })

  it('pomodoro shows short break after work phase', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 1560000).toISOString(), // 26 min ago (past 25 min work)
      pausedSeconds: 0,
      elapsedSeconds: 1560,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.pomodoroPhase.value).toBe('shortBreak')
    wrapper.unmount()
  })

  it('pomodoro phase label returns correct Chinese text', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.pomodoroPhaseLabel.value).toBe('专注中')
    wrapper.unmount()
  })

  // ===== Countdown Mode =====
  it('countdown defaults to 30 minutes', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.countdownMinutes.value).toBe(30)
    expect(result.countdownFinished.value).toBe(false)
    wrapper.unmount()
  })

  it('countdown shows remaining time', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 600000).toISOString(), // 10 min ago
      pausedSeconds: 0,
      elapsedSeconds: 600,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    result.timerMode.value = 'countdown'
    result.countdownMinutes.value = 30 // 30 min total, 10 elapsed = 20 remaining
    expect(result.formattedTime.value).toBe('20:00')
    wrapper.unmount()
  })

  it('countdown finished when remaining <= 0', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 1800000).toISOString(), // 30 min ago
      pausedSeconds: 0,
      elapsedSeconds: 1800,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    result.timerMode.value = 'countdown'
    result.countdownMinutes.value = 30
    expect(result.countdownFinished.value).toBe(true)
    wrapper.unmount()
  })

  // ===== Unified Display =====
  it('formattedTime delegates to flowtime by default', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 65000).toISOString(),
      pausedSeconds: 0,
      elapsedSeconds: 65,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.formattedTime.value).toBe('01:05')
    wrapper.unmount()
  })

  it('formattedTime delegates to pomodoro when mode set', () => {
    const now = Date.now()
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date(now - 120000).toISOString(), // 2 min
      pausedSeconds: 0,
      elapsedSeconds: 120,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    result.timerMode.value = 'pomodoro'
    result.pomodoroWorkMinutes.value = 25
    // 25 min work - 2 min elapsed = 23 min remaining
    expect(result.formattedTime.value).toBe('23:00')
    wrapper.unmount()
  })

  // ===== Grace Period =====
  it('isInGracePeriod is false when no grace deadline', () => {
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date().toISOString(),
      pausedSeconds: 0,
      graceDeadline: null,
      graceReason: null,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.isInGracePeriod.value).toBe(false)
    wrapper.unmount()
  })

  it('isInGracePeriod is true when grace deadline set', () => {
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date().toISOString(),
      pausedSeconds: 0,
      graceDeadline: new Date(Date.now() + 300000).toISOString(),
      graceReason: 'LEARNING_LIMIT',
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.isInGracePeriod.value).toBe(true)
    expect(result.isLearningLimit.value).toBe(true)
    wrapper.unmount()
  })

  // ===== Subject/Task =====
  it('currentSubject is null when no subjectId', () => {
    mockFocusState.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: new Date().toISOString(),
      pausedSeconds: 0,
      subjectId: null,
      taskId: null,
    }

    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.currentSubject.value).toBeNull()
    expect(result.currentTask.value).toBeNull()
    wrapper.unmount()
  })

  // ===== MAX_SESSION_HOURS =====
  it('MAX_SESSION_HOURS is 8', () => {
    const { result, wrapper } = withSetup(() => useFocusTimer())
    expect(result.MAX_SESSION_HOURS).toBe(8)
    wrapper.unmount()
  })
})
