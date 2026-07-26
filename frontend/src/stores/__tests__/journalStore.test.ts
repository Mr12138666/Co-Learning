import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useJournalStore } from '../journalStore'
import type { Journal } from '@/api/journal'

// 模拟 journal API
vi.mock('@/api/journal', () => ({
  journalApi: {
    create: vi.fn(),
    getById: vi.fn(),
    getPublicById: vi.fn(),
    update: vi.fn(),
    delete: vi.fn(),
    publish: vi.fn(),
    listMy: vi.fn(),
    listPublic: vi.fn(),
    listByUser: vi.fn(),
  },
}))

import { journalApi } from '@/api/journal'

// 测试数据
const now = '2024-01-01T00:00:00Z'

const mockJournals: Journal[] = [
  {
    id: 1,
    userId: 1,
    title: '第一天学习笔记',
    contentMarkdown: '# 今天学习了 Vue 3',
    contentHtml: '<h1>今天学习了 Vue 3</h1>',
    visibility: 'PRIVATE',
    roomId: null,
    status: 'DRAFT',
    publishedAt: null,
    aiSummary: null,
    createdAt: now,
    updatedAt: now,
  },
  {
    id: 2,
    userId: 1,
    title: 'Pinia 状态管理总结',
    contentMarkdown: '# Pinia 使用心得',
    contentHtml: '<h1>Pinia 使用心得</h1>',
    visibility: 'PUBLIC',
    roomId: null,
    status: 'PUBLISHED',
    publishedAt: '2024-01-02T00:00:00Z',
    aiSummary: 'Pinia 是 Vue 3 的状态管理库',
    createdAt: now,
    updatedAt: now,
  },
]

const mockNewJournal: Journal = {
  id: 3,
  userId: 1,
  title: '新日记',
  contentMarkdown: '# 新内容',
  contentHtml: '<h1>新内容</h1>',
  visibility: 'PRIVATE',
  roomId: null,
  status: 'DRAFT',
  publishedAt: null,
  aiSummary: null,
  createdAt: now,
  updatedAt: now,
}

describe('journalStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('初始状态：列表为空，未加载', () => {
    const store = useJournalStore()
    expect(store.journals).toEqual([])
    expect(store.publicJournals).toEqual([])
    expect(store.currentJournal).toBeNull()
    expect(store.total).toBe(0)
    expect(store.loading).toBe(false)
  })

  it('fetchMyJournals 填充分页数据', async () => {
    vi.mocked(journalApi.listMy).mockResolvedValue({
      data: {
        data: {
          items: [...mockJournals],
          total: 2,
        },
      },
    } as any)

    const store = useJournalStore()
    await store.fetchMyJournals()

    expect(store.journals).toHaveLength(2)
    expect(store.journals[0].title).toBe('第一天学习笔记')
    expect(store.journals[1].title).toBe('Pinia 状态管理总结')
    expect(store.total).toBe(2)
    expect(store.loading).toBe(false)
    expect(journalApi.listMy).toHaveBeenCalledOnce()
  })

  it('fetchMyJournals 处理非分页数组响应', async () => {
    vi.mocked(journalApi.listMy).mockResolvedValue({
      data: {
        data: mockJournals,
      },
    } as any)

    const store = useJournalStore()
    await store.fetchMyJournals()

    expect(store.journals).toHaveLength(2)
    expect(store.total).toBe(2)
  })

  it('fetchMyJournals 可传入分页参数', async () => {
    vi.mocked(journalApi.listMy).mockResolvedValue({
      data: {
        data: { items: [mockJournals[0]], total: 10 },
      },
    } as any)

    const store = useJournalStore()
    await store.fetchMyJournals({ page: 2, size: 1 })

    expect(journalApi.listMy).toHaveBeenCalledWith({ page: 2, size: 1 })
    expect(store.journals).toHaveLength(1)
    expect(store.total).toBe(10)
  })

  it('fetchMyJournals 在加载期间设置 loading 状态', async () => {
    let resolveList: (value: any) => void
    vi.mocked(journalApi.listMy).mockReturnValue(
      new Promise((resolve) => {
        resolveList = resolve
      }) as any,
    )

    const store = useJournalStore()
    const promise = store.fetchMyJournals()

    expect(store.loading).toBe(true)

    resolveList!({ data: { data: { items: [], total: 0 } } })
    await promise

    expect(store.loading).toBe(false)
  })

  it('create 将新日记添加到列表头部', async () => {
    // 先填充列表
    vi.mocked(journalApi.listMy).mockResolvedValue({
      data: { data: { items: [...mockJournals], total: 2 } },
    } as any)

    const store = useJournalStore()
    await store.fetchMyJournals()
    expect(store.journals).toHaveLength(2)

    // 创建新日记
    vi.mocked(journalApi.create).mockResolvedValue({
      data: { data: mockNewJournal },
    } as any)

    const result = await store.create({
      title: '新日记',
      contentMarkdown: '# 新内容',
    })

    expect(store.journals).toHaveLength(3)
    expect(store.journals[0].id).toBe(3)
    expect(store.journals[0].title).toBe('新日记')
    expect(result).toEqual(mockNewJournal)
    expect(journalApi.create).toHaveBeenCalledWith({
      title: '新日记',
      contentMarkdown: '# 新内容',
    })
  })

  it('remove 从列表中删除日记', async () => {
    // 先填充列表
    vi.mocked(journalApi.listMy).mockResolvedValue({
      data: { data: { items: [...mockJournals], total: 2 } },
    } as any)

    const store = useJournalStore()
    await store.fetchMyJournals()
    expect(store.journals).toHaveLength(2)

    // 删除日记
    vi.mocked(journalApi.delete).mockResolvedValue({} as any)

    await store.remove(1)

    expect(store.journals).toHaveLength(1)
    expect(store.journals[0].id).toBe(2)
    expect(store.journals[0].title).toBe('Pinia 状态管理总结')
    expect(journalApi.delete).toHaveBeenCalledWith(1)
  })

  it('fetchById 设置 currentJournal', async () => {
    vi.mocked(journalApi.getById).mockResolvedValue({
      data: { data: mockJournals[0] },
    } as any)

    const store = useJournalStore()
    const result = await store.fetchById(1)

    expect(store.currentJournal).toEqual(mockJournals[0])
    expect(result).toEqual(mockJournals[0])
    expect(journalApi.getById).toHaveBeenCalledWith(1)
  })

  it('update 更新列表中对应日记', async () => {
    // 先填充列表
    vi.mocked(journalApi.listMy).mockResolvedValue({
      data: { data: { items: [...mockJournals], total: 2 } },
    } as any)

    const store = useJournalStore()
    await store.fetchMyJournals()

    // 模拟更新响应
    const updatedJournal: Journal = {
      ...mockJournals[0],
      title: '更新后的标题',
      updatedAt: '2024-02-01T00:00:00Z',
    }
    vi.mocked(journalApi.update).mockResolvedValue({
      data: { data: updatedJournal },
    } as any)

    const result = await store.update(1, { title: '更新后的标题' })

    expect(store.journals[0].title).toBe('更新后的标题')
    expect(result.title).toBe('更新后的标题')
  })

  it('update 同时更新 currentJournal（如果 id 匹配）', async () => {
    vi.mocked(journalApi.getById).mockResolvedValue({
      data: { data: mockJournals[0] },
    } as any)

    const store = useJournalStore()
    await store.fetchById(1)
    expect(store.currentJournal?.title).toBe('第一天学习笔记')

    // 填充列表
    vi.mocked(journalApi.listMy).mockResolvedValue({
      data: { data: { items: [...mockJournals], total: 2 } },
    } as any)
    await store.fetchMyJournals()

    // 更新日记
    const updatedJournal: Journal = {
      ...mockJournals[0],
      title: '更新后的标题',
    }
    vi.mocked(journalApi.update).mockResolvedValue({
      data: { data: updatedJournal },
    } as any)

    await store.update(1, { title: '更新后的标题' })

    expect(store.currentJournal?.title).toBe('更新后的标题')
    expect(store.journals[0].title).toBe('更新后的标题')
  })
})
