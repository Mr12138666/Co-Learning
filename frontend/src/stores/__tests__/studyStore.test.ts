import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useStudyStore } from '../studyStore'
import type { ExamGoal, Subject, StudyTask } from '@/api/study'

// 模拟 study API
vi.mock('@/api/study', () => ({
  studyApi: {
    getStats: vi.fn(),
    listGoals: vi.fn(),
    createGoal: vi.fn(),
    updateGoal: vi.fn(),
    deleteGoal: vi.fn(),
    listSubjects: vi.fn(),
    createSubject: vi.fn(),
    updateSubject: vi.fn(),
    deleteSubject: vi.fn(),
    listTasks: vi.fn(),
    createTask: vi.fn(),
    updateTask: vi.fn(),
    deleteTask: vi.fn(),
  },
}))

import { studyApi } from '@/api/study'

// 测试数据
const now = '2024-01-01T00:00:00Z'

const mockGoals: ExamGoal[] = [
  {
    id: 1,
    examName: '高考',
    examDate: '2025-06-07',
    targetScore: '650',
    status: 'ACTIVE',
    daysRemaining: 365,
    createdAt: now,
    updatedAt: now,
  },
  {
    id: 2,
    examName: '期末考试',
    examDate: '2024-01-15',
    targetScore: null,
    status: 'COMPLETED',
    daysRemaining: 0,
    createdAt: now,
    updatedAt: now,
  },
  {
    id: 3,
    examName: '模拟考试',
    examDate: '2025-03-01',
    targetScore: '600',
    status: 'ACTIVE',
    daysRemaining: 200,
    createdAt: now,
    updatedAt: now,
  },
]

const mockSubjects: Subject[] = [
  {
    id: 1,
    name: '数学',
    color: '#FF5722',
    sortOrder: 1,
    createdAt: now,
    updatedAt: now,
  },
  {
    id: 2,
    name: '英语',
    color: '#4CAF50',
    sortOrder: 2,
    createdAt: now,
    updatedAt: now,
  },
]

// Shared defaults for the V13/V14 scheduling + tag fields.
const taskExtra = {
  plannedDate: null,
  scheduledStart: null,
  scheduledEnd: null,
  estimatedMinutes: null,
  urgent: false,
  important: false,
  tags: [],
  totalFocusSeconds: 0,
}

const mockTasks: StudyTask[] = [
  {
    id: 1,
    subjectId: 1,
    subjectName: '数学',
    subjectColor: '#FF5722',
    examGoalId: 1,
    title: '完成高数作业',
    description: '第三章习题',
    status: 'TODO',
    dueDate: '2024-06-01',
    sortOrder: 1,
    ...taskExtra,
    createdAt: now,
    updatedAt: now,
  },
  {
    id: 2,
    subjectId: 1,
    subjectName: '数学',
    subjectColor: '#FF5722',
    examGoalId: 1,
    title: '复习线性代数',
    description: null,
    status: 'IN_PROGRESS',
    dueDate: null,
    sortOrder: 2,
    ...taskExtra,
    createdAt: now,
    updatedAt: now,
  },
  {
    id: 3,
    subjectId: 2,
    subjectName: '英语',
    subjectColor: '#4CAF50',
    examGoalId: null,
    title: '背单词',
    description: '每天50个',
    status: 'DONE',
    dueDate: null,
    sortOrder: 3,
    ...taskExtra,
    createdAt: now,
    updatedAt: now,
  },
  {
    id: 4,
    subjectId: 2,
    subjectName: '英语',
    subjectColor: '#4CAF50',
    examGoalId: 1,
    title: '阅读理解练习',
    description: null,
    status: 'TODO',
    dueDate: '2024-07-01',
    sortOrder: 4,
    ...taskExtra,
    createdAt: now,
    updatedAt: now,
  },
]

describe('studyStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('初始状态：所有数组为空', () => {
    const store = useStudyStore()
    expect(store.goals).toEqual([])
    expect(store.subjects).toEqual([])
    expect(store.tasks).toEqual([])
    expect(store.loading).toBe(false)
  })

  it('fetchGoals 填充 goals 列表', async () => {
    vi.mocked(studyApi.listGoals).mockResolvedValue({
      data: { data: mockGoals },
    } as any)

    const store = useStudyStore()
    await store.fetchGoals()

    expect(store.goals).toHaveLength(3)
    expect(store.goals[0].examName).toBe('高考')
    expect(store.goals[0].status).toBe('ACTIVE')
    expect(studyApi.listGoals).toHaveBeenCalledOnce()
  })

  it('fetchSubjects 填充 subjects 列表和 subjectMap', async () => {
    vi.mocked(studyApi.listSubjects).mockResolvedValue({
      data: { data: mockSubjects },
    } as any)

    const store = useStudyStore()
    await store.fetchSubjects()

    expect(store.subjects).toHaveLength(2)
    expect(store.subjects[0].name).toBe('数学')
    expect(store.subjects[1].name).toBe('英语')
    expect(studyApi.listSubjects).toHaveBeenCalledOnce()

    // 验证 subjectMap 计算属性
    expect(store.subjectMap.size).toBe(2)
    expect(store.subjectMap.get(1)?.name).toBe('数学')
    expect(store.subjectMap.get(2)?.name).toBe('英语')
  })

  it('fetchTasks 填充 tasks 列表', async () => {
    vi.mocked(studyApi.listTasks).mockResolvedValue({
      data: { data: mockTasks },
    } as any)

    const store = useStudyStore()
    await store.fetchTasks()

    expect(store.tasks).toHaveLength(4)
    expect(store.tasks[0].title).toBe('完成高数作业')
    expect(studyApi.listTasks).toHaveBeenCalledOnce()
  })

  it('fetchTasks 可传入筛选参数', async () => {
    vi.mocked(studyApi.listTasks).mockResolvedValue({
      data: { data: [mockTasks[0]] },
    } as any)

    const store = useStudyStore()
    await store.fetchTasks({ status: 'TODO', subjectId: 1 })

    expect(studyApi.listTasks).toHaveBeenCalledWith({ status: 'TODO', subjectId: 1 })
    expect(store.tasks).toHaveLength(1)
  })

  it('计算属性 activeGoals 只返回 ACTIVE 状态的目标', async () => {
    vi.mocked(studyApi.listGoals).mockResolvedValue({
      data: { data: mockGoals },
    } as any)

    const store = useStudyStore()
    await store.fetchGoals()

    expect(store.activeGoals).toHaveLength(2)
    expect(store.activeGoals.every((g) => g.status === 'ACTIVE')).toBe(true)
    expect(store.activeGoals[0].examName).toBe('高考')
    expect(store.activeGoals[1].examName).toBe('模拟考试')
  })

  it('计算属性 todoTasks 只返回 TODO 状态的任务', async () => {
    vi.mocked(studyApi.listTasks).mockResolvedValue({
      data: { data: mockTasks },
    } as any)

    const store = useStudyStore()
    await store.fetchTasks()

    expect(store.todoTasks).toHaveLength(2)
    expect(store.todoTasks.every((t) => t.status === 'TODO')).toBe(true)
    expect(store.todoTasks[0].title).toBe('完成高数作业')
    expect(store.todoTasks[1].title).toBe('阅读理解练习')
  })

  it('计算属性 inProgressTasks 只返回 IN_PROGRESS 状态的任务', async () => {
    vi.mocked(studyApi.listTasks).mockResolvedValue({
      data: { data: mockTasks },
    } as any)

    const store = useStudyStore()
    await store.fetchTasks()

    expect(store.inProgressTasks).toHaveLength(1)
    expect(store.inProgressTasks[0].status).toBe('IN_PROGRESS')
    expect(store.inProgressTasks[0].title).toBe('复习线性代数')
  })

  it('计算属性 doneTasks 只返回 DONE 状态的任务', async () => {
    vi.mocked(studyApi.listTasks).mockResolvedValue({
      data: { data: mockTasks },
    } as any)

    const store = useStudyStore()
    await store.fetchTasks()

    expect(store.doneTasks).toHaveLength(1)
    expect(store.doneTasks[0].status).toBe('DONE')
    expect(store.doneTasks[0].title).toBe('背单词')
  })

  it('subjectMap 在 subjects 为空时返回空 Map', () => {
    const store = useStudyStore()
    expect(store.subjectMap.size).toBe(0)
  })
})
