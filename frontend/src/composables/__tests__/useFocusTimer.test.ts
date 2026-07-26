import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

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

import { useFocusTimer, type TimerMode } from '../useFocusTimer'

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
    const { timerMode } = useFocusTimer()
    expect(timerMode.value).toBe('flowtime')
  })

  it('allows switching to pomodoro mode', () => {
    const { timerMode } = useFocusTimer()
    timerMode.value = 'pomodoro'
    expect(timerMode.value).toBe('pomodoro')
  })

  it('allows switching to countdown mode', () => {
    const { timerMode } = useFocusTimer()
    timerMode.value = 'countdown'
    expect(timerMode.value).toBe('countdown')
  })

  // ===== No Session State =====
  it('shows 00:00 when no session', () => {
    const { formattedTime } = useFocusTimer()
    expect(formattedTime.value).toBe('00:00')
  })

  it('shows 0 progress when no session', () => {
    const { progressPercent } = useFocusTimer()
    expect(progressPercent.value).toBe(0)
  })

  it('hasSession is false when no session', () => {
    const { hasSession } = useFocusTimer()
    expect(hasSession.value).toBe(false)
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

    const { formattedTime, elapsedSeconds } = useFocusTimer()
    expect(elapsedSeconds.value).toBe(125)
    expect(formattedTime.value).toBe('02:05')
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

    const { formattedTime } = useFocusTimer()
    expect(formattedTime.value).toBe('1:01:01')
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

    const { elapsedSeconds } = useFocusTimer()
    expect(elapsedSeconds.value).toBe(180) // 300 - 120 = 180
  })

  // ===== Pomodoro Mode =====
  it('pomodoro defaults: 25min work, 5min short break, 15min long break', () => {
    const { pomodoroWorkMinutes, pomodoroShortBreakMinutes, pomodoroLongBreakMinutes, pomodoroCyclesBeforeLongBreak } = useFocusTimer()
    expect(pomodoroWorkMinutes.value).toBe(25)
    expect(pomodoroShortBreakMinutes.value).toBe(5)
    expect(pomodoroLongBreakMinutes.value).toBe(15)
    expect(pomodoroCyclesBeforeLongBreak.value).toBe(4)
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

    const { pomodoroPhase } = useFocusTimer()
    expect(pomodoroPhase.value).toBe('work')
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

    const { pomodoroPhase } = useFocusTimer()
    expect(pomodoroPhase.value).toBe('shortBreak')
  })

  it('pomodoro phase label returns correct Chinese text', () => {
    const { pomodoroPhaseLabel } = useFocusTimer()
    expect(pomodoroPhaseLabel.value).toBe('专注中')
  })

  // ===== Countdown Mode =====
  it('countdown defaults to 30 minutes', () => {
    const { countdownMinutes, countdownFinished } = useFocusTimer()
    expect(countdownMinutes.value).toBe(30)
    expect(countdownFinished.value).toBe(false)
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

    const { formattedTime, timerMode, countdownMinutes } = useFocusTimer()
    timerMode.value = 'countdown'
    countdownMinutes.value = 30 // 30 min total, 10 elapsed = 20 remaining
    expect(formattedTime.value).toBe('20:00')
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

    const { countdownFinished, timerMode, countdownMinutes } = useFocusTimer()
    timerMode.value = 'countdown'
    countdownMinutes.value = 30
    expect(countdownFinished.value).toBe(true)
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

    const { formattedTime } = useFocusTimer()
    expect(formattedTime.value).toBe('01:05')
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

    const { formattedTime, timerMode, pomodoroWorkMinutes } = useFocusTimer()
    timerMode.value = 'pomodoro'
    pomodoroWorkMinutes.value = 25
    // 25 min work - 2 min elapsed = 23 min remaining
    expect(formattedTime.value).toBe('23:00')
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

    const { isInGracePeriod } = useFocusTimer()
    expect(isInGracePeriod.value).toBe(false)
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

    const { isInGracePeriod, isLearningLimit } = useFocusTimer()
    expect(isInGracePeriod.value).toBe(true)
    expect(isLearningLimit.value).toBe(true)
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

    const { currentSubject, currentTask } = useFocusTimer()
    expect(currentSubject.value).toBeNull()
    expect(currentTask.value).toBeNull()
  })

  // ===== MAX_SESSION_HOURS =====
  it('MAX_SESSION_HOURS is 8', () => {
    const { MAX_SESSION_HOURS } = useFocusTimer()
    expect(MAX_SESSION_HOURS).toBe(8)
  })
})
