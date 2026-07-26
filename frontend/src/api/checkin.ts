import http from './http'
import type { ApiResponse } from '@/types/api'

// ===== Types =====

export interface Checkin {
  id: number
  checkinDate: string
  planText: string | null
  reflectionText: string | null
  mood: number | null
  focusTotalSec: number
  completed: boolean
  createdAt: string
  updatedAt: string
}

export interface UpdateCheckinRequest {
  checkinDate?: string
  planText?: string
  reflectionText?: string
  mood?: number
}

// ===== API =====

export const checkinApi = {
  getToday: () => http.get<ApiResponse<Checkin>>('/checkins/today'),
  update: (data: UpdateCheckinRequest) => http.put<ApiResponse<Checkin>>('/checkins', data),
  complete: () => http.post<ApiResponse<Checkin>>('/checkins/complete'),
  getByDate: (date: string) => http.get<ApiResponse<Checkin>>('/checkins', { params: { date } }),
}
