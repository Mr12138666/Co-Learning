import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTaskStore } from '../taskStore'
import type { StudyTask, Tag } from '@/api/study'

vi.mock('@/api/study', () => ({
  studyApi: {
    listTags: vi.fn(),
    createTag: vi.fn(),
    deleteTag: vi.fn(),
    listInboxTasks: vi.fn(),
    listTodayTasks: vi.fn(),
    listOverdueTasks: vi.fn(),
    listPlannerTasks: vi.fn(),
    listQuadrant: vi.fn(),
    listTasks: vi.fn(),
    createTask: vi.fn(),
    updateTask: vi.fn(),
    deleteTask: vi.fn(),
    bulkPlannedDate: vi.fn(),
  },
}))

import { studyApi } from '@/api/study'

const now = '2024-01-01T00:00:00Z'
function makeTask(over: Partial<StudyTask> = {}): StudyTask {
  return {
    id: 1, subjectId: null, subjectName: null, subjectColor: null, examGoalId: null,
    title: 'T', description: null, status: 'TODO', dueDate: null, sortOrder: 0,
    plannedDate: null, scheduledStart: null, scheduledEnd: null, estimatedMinutes: null,
    urgent: false, important: false, tags: [], totalFocusSeconds: 0,
    createdAt: now, updatedAt: now, ...over,
  }
}
const wrap = <T>(data: T) => ({ data: { data } })

describe('taskStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    vi.mocked(studyApi.updateTask).mockImplementation((id: number, patch) =>
      Promise.resolve(wrap(makeTask({ id, ...patch } as Partial<StudyTask>))) as never,
    )
  })

  it('toggleDone flips DONE -> TODO and TODO -> DONE', async () => {
    const store = useTaskStore()
    await store.toggleDone(makeTask({ id: 5, status: 'TODO' }))
    expect(studyApi.updateTask).toHaveBeenCalledWith(5, { status: 'DONE' })

    await store.toggleDone(makeTask({ id: 6, status: 'DONE' }))
    expect(studyApi.updateTask).toHaveBeenCalledWith(6, { status: 'TODO' })
  })

  it('setPlannedDate persists the planned date (Planner drag)', async () => {
    const store = useTaskStore()
    await store.setPlannedDate(3, '2026-07-27')
    expect(studyApi.updateTask).toHaveBeenCalledWith(3, { plannedDate: '2026-07-27' })
  })

  it('setQuadrant persists urgent/important (Eisenhower drag)', async () => {
    const store = useTaskStore()
    await store.setQuadrant(9, true, false)
    expect(studyApi.updateTask).toHaveBeenCalledWith(9, { urgent: true, important: false })
  })

  it('setStatus persists status (Kanban drag)', async () => {
    const store = useTaskStore()
    await store.setStatus(2, 'IN_PROGRESS')
    expect(studyApi.updateTask).toHaveBeenCalledWith(2, { status: 'IN_PROGRESS' })
  })

  it('bulkSetPlannedDate calls the bulk endpoint, and is a no-op for empty ids', async () => {
    const store = useTaskStore()
    vi.mocked(studyApi.bulkPlannedDate).mockResolvedValue(wrap(undefined) as never)
    await store.bulkSetPlannedDate([1, 2, 3], '2026-07-26')
    expect(studyApi.bulkPlannedDate).toHaveBeenCalledWith([1, 2, 3], '2026-07-26')

    vi.mocked(studyApi.bulkPlannedDate).mockClear()
    await store.bulkSetPlannedDate([], '2026-07-26')
    expect(studyApi.bulkPlannedDate).not.toHaveBeenCalled()
  })

  it('fetchQuadrant returns the four Eisenhower buckets', async () => {
    const store = useTaskStore()
    const map = {
      'urgent-important': [makeTask({ id: 1, urgent: true, important: true })],
      'not-urgent-important': [],
      'urgent-not-important': [],
      'not-urgent-not-important': [],
    }
    vi.mocked(studyApi.listQuadrant).mockResolvedValue(wrap(map) as never)
    const result = await store.fetchQuadrant()
    expect(Object.keys(result)).toHaveLength(4)
    expect(result['urgent-important']).toHaveLength(1)
  })

  it('manages tags: load, create, delete', async () => {
    const store = useTaskStore()
    const tags: Tag[] = [{ id: 1, name: '重要', color: '#f00', createdAt: now }]
    vi.mocked(studyApi.listTags).mockResolvedValue(wrap(tags) as never)
    await store.loadTags()
    expect(store.tags).toHaveLength(1)

    vi.mocked(studyApi.createTag).mockResolvedValue(wrap({ id: 2, name: '紧急', color: '#0f0', createdAt: now }) as never)
    await store.createTag('紧急', '#0f0')
    expect(store.tags).toHaveLength(2)
    expect(store.tagMap.get(2)?.name).toBe('紧急')

    vi.mocked(studyApi.deleteTag).mockResolvedValue(wrap(undefined) as never)
    await store.deleteTag(1)
    expect(store.tags).toHaveLength(1)
    expect(store.tags[0].id).toBe(2)
  })

  it('fetchInbox returns unplanned tasks', async () => {
    const store = useTaskStore()
    vi.mocked(studyApi.listInboxTasks).mockResolvedValue(wrap([makeTask({ id: 1 }), makeTask({ id: 2 })]) as never)
    const inbox = await store.fetchInbox()
    expect(inbox).toHaveLength(2)
  })
})
