import http from './http'

// ===== Types =====

export interface Journal {
  id: number
  title: string
  contentMarkdown: string
  contentHtml: string
  visibility: 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC'
  roomId: number | null
  status: 'DRAFT' | 'PUBLISHED'
  publishedAt: string | null
  aiSummary: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateJournalRequest {
  title: string
  contentMarkdown: string
  visibility?: 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC'
  roomId?: number
}

export interface UpdateJournalRequest {
  title?: string
  contentMarkdown?: string
  visibility?: 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC'
  status?: 'DRAFT' | 'PUBLISHED'
  roomId?: number
}

export interface JournalListParams {
  page?: number
  size?: number
}

// ===== API =====

export const journalApi = {
  create: (data: CreateJournalRequest) => http.post('/journals', data),
  getById: (id: number) => http.get(`/journals/${id}`),
  update: (id: number, data: UpdateJournalRequest) => http.put(`/journals/${id}`, data),
  delete: (id: number) => http.delete(`/journals/${id}`),
  publish: (id: number) => http.post(`/journals/${id}/publish`),
  listMy: (params?: JournalListParams) => http.get('/journals', { params }),
  listPublic: (params?: JournalListParams) => http.get('/journals/public', { params }),
  listByUser: (userId: number, params?: JournalListParams) =>
    http.get(`/journals/user/${userId}`, { params }),
}
