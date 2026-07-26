import http from './http'
import type { ApiResponse, Page } from '@/types/api'

// ===== Types =====

export type JournalVisibility = 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC'
export type JournalStatus = 'DRAFT' | 'PUBLISHED'

export interface Journal {
  id: number
  userId: number
  title: string
  contentMarkdown: string
  contentHtml: string
  visibility: JournalVisibility
  roomId: number | null
  status: JournalStatus
  publishedAt: string | null
  aiSummary: string | null
  createdAt: string
  updatedAt: string
}

export interface CreateJournalRequest {
  title: string
  contentMarkdown: string
  visibility?: JournalVisibility
  roomId?: number
}

export interface UpdateJournalRequest {
  title?: string
  contentMarkdown?: string
  visibility?: JournalVisibility
  status?: JournalStatus
  roomId?: number
}

export interface JournalListParams {
  page?: number
  size?: number
}

// ===== API =====

export const journalApi = {
  create: (data: CreateJournalRequest) => http.post<ApiResponse<Journal>>('/journals', data),
  getById: (id: number) => http.get<ApiResponse<Journal>>(`/journals/${id}`),
  getPublicById: (id: number) => http.get<ApiResponse<Journal>>(`/journals/public/${id}`),
  update: (id: number, data: UpdateJournalRequest) => http.put<ApiResponse<Journal>>(`/journals/${id}`, data),
  delete: (id: number) => http.delete<ApiResponse<void>>(`/journals/${id}`),
  publish: (id: number) => http.post<ApiResponse<Journal>>(`/journals/${id}/publish`),
  listMy: (params?: JournalListParams) => http.get<ApiResponse<Page<Journal>>>('/journals', { params }),
  listPublic: (params?: JournalListParams) =>
    http.get<ApiResponse<Page<Journal>>>('/journals/public', { params }),
  listByUser: (userId: number, params?: JournalListParams) =>
    http.get<ApiResponse<Page<Journal>>>(`/journals/users/${userId}`, { params }),
}
