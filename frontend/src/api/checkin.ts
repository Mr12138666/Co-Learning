import http from './http'

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
  getToday: () => http.get('/checkins/today'),
  update: (data: UpdateCheckinRequest) => http.put('/checkins', data),
  complete: () => http.post('/checkins/complete'),
  getByDate: (date: string) => http.get('/checkins', { params: { date } }),
}
