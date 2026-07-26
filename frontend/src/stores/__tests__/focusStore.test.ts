import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useFocusStore } from '../focusStore'
import type { FocusSession } from '@/api/focus'

// 模拟 focus API
vi.mock('@/api/focus', () => ({
  focusApi: {
    getActive: vi.fn(),
    start: vi.fn(),
    pause: vi.fn(),
    resume: vi.fn(),
    finish: vi.fn(),
    abort: vi.fn(),
  },
}))

import { focusApi } from '@/api/focus'

// 测试数据
const now = '2024-01-01T00:00:00Z'

const mockFocusSession: FocusSession = {
  id: 1,
  subjectId: 1,
  taskId: null,
  status: 'ACTIVE',
  startedAt: now,
  pausedAt: null,
  resumedAt: null,
  endedAt: null,
  pausedSeconds: 0,
  effectiveSeconds: 0,
  elapsedSeconds: 0,
  createdAt: now,
  updatedAt: now,
}

describe('focusStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('初始状态：无活跃会话且未加载', () => {
    const store = useFocusStore()
    expect(store.activeSession).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.isActive).toBe(false)
    expect(store.isPaused).toBe(false)
    expect(store.hasSession).toBe(false)
    expect(store.sessionId).toBeNull()
  })

  it('start() 设置活跃会话', async () => {
    vi.mocked(focusApi.start).mockResolvedValue({
      data: { data: mockFocusSession },
    } as any)

    const store = useFocusStore()
    const result = await store.start({ subjectId: 1 })

    expect(store.hasSession).toBe(true)
    expect(store.isActive).toBe(true)
    expect(store.isPaused).toBe(false)
    expect(store.activeSession?.sessionId).toBe(1)
    expect(store.activeSession?.status).toBe('ACTIVE')
    expect(store.activeSession?.subjectId).toBe(1)
    expect(store.activeSession?.taskId).toBeNull()
    expect(store.activeSession?.graceDeadline).toBeNull()
    expect(store.activeSession?.graceReason).toBeNull()
    expect(result).toEqual(mockFocusSession)
  })

  it('start() 在加载期间设置 loading 状态', async () => {
    let resolveStart: (value: any) => void
    vi.mocked(focusApi.start).mockReturnValue(
      new Promise((resolve) => {
        resolveStart = resolve
      }) as any,
    )

    const store = useFocusStore()
    const promise = store.start({ subjectId: 1 })

    expect(store.loading).toBe(true)

    resolveStart!({ data: { data: mockFocusSession } })
    await promise

    expect(store.loading).toBe(false)
  })

  it('pause() 更新会话状态为 PAUSED', async () => {
    // 先启动会话
    vi.mocked(focusApi.start).mockResolvedValue({
      data: { data: mockFocusSession },
    } as any)
    const store = useFocusStore()
    await store.start({ subjectId: 1 })

    // 模拟暂停响应
    const pausedSession: FocusSession = {
      ...mockFocusSession,
      status: 'PAUSED',
      pausedAt: '2024-01-01T00:05:00Z',
      pausedSeconds: 0,
      elapsedSeconds: 300,
    }
    vi.mocked(focusApi.pause).mockResolvedValue({
      data: { data: pausedSession },
    } as any)

    await store.pause()

    expect(store.activeSession?.status).toBe('PAUSED')
    expect(store.activeSession?.pausedAt).toBe('2024-01-01T00:05:00Z')
    expect(store.activeSession?.pausedSeconds).toBe(0)
    expect(store.activeSession?.elapsedSeconds).toBe(300)
    expect(store.activeSession?.graceDeadline).toBeNull()
    expect(store.activeSession?.graceReason).toBeNull()
    expect(store.isPaused).toBe(true)
    expect(store.isActive).toBe(false)
  })

  it('pause() 无活跃会话时直接返回', async () => {
    const store = useFocusStore()
    await store.pause()

    expect(focusApi.pause).not.toHaveBeenCalled()
  })

  it('resume() 更新会话状态为 ACTIVE', async () => {
    // 启动会话
    vi.mocked(focusApi.start).mockResolvedValue({
      data: { data: mockFocusSession },
    } as any)
    const store = useFocusStore()
    await store.start({ subjectId: 1 })

    // 暂停会话
    const pausedSession: FocusSession = {
      ...mockFocusSession,
      status: 'PAUSED',
      pausedAt: '2024-01-01T00:05:00Z',
      pausedSeconds: 0,
      elapsedSeconds: 300,
    }
    vi.mocked(focusApi.pause).mockResolvedValue({
      data: { data: pausedSession },
    } as any)
    await store.pause()

    // 模拟恢复响应
    const resumedSession: FocusSession = {
      ...mockFocusSession,
      status: 'ACTIVE',
      resumedAt: '2024-01-01T00:10:00Z',
      pausedSeconds: 300,
      elapsedSeconds: 600,
    }
    vi.mocked(focusApi.resume).mockResolvedValue({
      data: { data: resumedSession },
    } as any)

    await store.resume()

    expect(store.activeSession?.status).toBe('ACTIVE')
    expect(store.activeSession?.resumedAt).toBe('2024-01-01T00:10:00Z')
    expect(store.activeSession?.pausedSeconds).toBe(300)
    expect(store.activeSession?.elapsedSeconds).toBe(600)
    expect(store.activeSession?.graceDeadline).toBeNull()
    expect(store.activeSession?.graceReason).toBeNull()
    expect(store.isActive).toBe(true)
    expect(store.isPaused).toBe(false)
  })

  it('resume() 无活跃会话时直接返回', async () => {
    const store = useFocusStore()
    await store.resume()

    expect(focusApi.resume).not.toHaveBeenCalled()
  })

  it('finish() 清除活跃会话', async () => {
    // 启动会话
    vi.mocked(focusApi.start).mockResolvedValue({
      data: { data: mockFocusSession },
    } as any)
    const store = useFocusStore()
    await store.start({ subjectId: 1 })
    expect(store.hasSession).toBe(true)

    // 模拟结束响应
    const finishedSession: FocusSession = {
      ...mockFocusSession,
      status: 'FINISHED',
      endedAt: '2024-01-01T01:00:00Z',
      effectiveSeconds: 3600,
      elapsedSeconds: 3600,
    }
    vi.mocked(focusApi.finish).mockResolvedValue({
      data: { data: finishedSession },
    } as any)

    const result = await store.finish()

    expect(store.activeSession).toBeNull()
    expect(store.hasSession).toBe(false)
    expect(result).toEqual(finishedSession)
  })

  it('finish() 无活跃会话时返回 null', async () => {
    const store = useFocusStore()
    const result = await store.finish()

    expect(result).toBeNull()
    expect(focusApi.finish).not.toHaveBeenCalled()
  })

  it('abort() 清除活跃会话', async () => {
    // 启动会话
    vi.mocked(focusApi.start).mockResolvedValue({
      data: { data: mockFocusSession },
    } as any)
    const store = useFocusStore()
    await store.start({ subjectId: 1 })
    expect(store.hasSession).toBe(true)

    vi.mocked(focusApi.abort).mockResolvedValue(undefined as any)

    await store.abort()

    expect(store.activeSession).toBeNull()
    expect(store.hasSession).toBe(false)
  })

  it('abort() 无活跃会话时直接返回', async () => {
    const store = useFocusStore()
    await store.abort()

    expect(focusApi.abort).not.toHaveBeenCalled()
  })

  it('计算属性：isActive、isPaused、hasSession', () => {
    const store = useFocusStore()

    // 无会话时
    expect(store.isActive).toBe(false)
    expect(store.isPaused).toBe(false)
    expect(store.hasSession).toBe(false)

    // 直接设置活跃会话
    store.activeSession = {
      sessionId: 1,
      status: 'ACTIVE',
      startedAt: now,
      pausedAt: null,
      resumedAt: null,
      pausedSeconds: 0,
      elapsedSeconds: 0,
      subjectId: 1,
      taskId: null,
      graceDeadline: null,
      graceReason: null,
    }
    expect(store.isActive).toBe(true)
    expect(store.isPaused).toBe(false)
    expect(store.hasSession).toBe(true)

    // 切换为暂停状态
    store.activeSession = {
      ...store.activeSession!,
      status: 'PAUSED',
    }
    expect(store.isActive).toBe(false)
    expect(store.isPaused).toBe(true)
    expect(store.hasSession).toBe(true)

    // 清除会话
    store.clear()
    expect(store.isActive).toBe(false)
    expect(store.isPaused).toBe(false)
    expect(store.hasSession).toBe(false)
  })

  it('sessionId 计算属性返回会话 ID 或 null', () => {
    const store = useFocusStore()

    // 无会话
    expect(store.sessionId).toBeNull()

    // 有会话
    store.activeSession = {
      sessionId: 42,
      status: 'ACTIVE',
      startedAt: now,
      pausedAt: null,
      resumedAt: null,
      pausedSeconds: 0,
      elapsedSeconds: 0,
      subjectId: null,
      taskId: null,
      graceDeadline: null,
      graceReason: null,
    }
    expect(store.sessionId).toBe(42)
  })
})
