import http from './http'
import type { ApiResponse } from '@/types/api'

// ===== Types =====

export interface FocusSession {
  id: number
  subjectId: number | null
  taskId: number | null
  status: 'ACTIVE' | 'PAUSED' | 'FINISHED' | 'ABORTED'
  startedAt: string
  pausedAt: string | null
  resumedAt: string | null
  endedAt: string | null
  pausedSeconds: number
  effectiveSeconds: number
  elapsedSeconds: number
  createdAt: string
  updatedAt: string
}

export interface ActiveFocusSession {
  sessionId: number
  status: 'ACTIVE' | 'PAUSED'
  startedAt: string
  pausedAt: string | null
  resumedAt: string | null
  pausedSeconds: number
  elapsedSeconds: number
  subjectId: number | null
  taskId: number | null
  graceDeadline: string | null
  graceReason: 'LEARNING_LIMIT' | 'PAUSE_LIMIT' | null
}

export interface StartFocusRequest {
  subjectId?: number
  taskId?: number
  clientRequestId?: string
}

// ===== API =====

export const focusApi = {
  start: (data: StartFocusRequest) => http.post<ApiResponse<FocusSession>>('/focus-sessions', data),
  getActive: () => http.get<ApiResponse<ActiveFocusSession | null>>('/focus-sessions/active'),
  getSession: (id: number) => http.get<ApiResponse<FocusSession>>(`/focus-sessions/${id}`),
  pause: (id: number) => http.post<ApiResponse<FocusSession>>(`/focus-sessions/${id}/pause`),
  resume: (id: number) => http.post<ApiResponse<FocusSession>>(`/focus-sessions/${id}/resume`),
  finish: (id: number) => http.post<ApiResponse<FocusSession>>(`/focus-sessions/${id}/finish`),
  abort: (id: number) => http.post<ApiResponse<FocusSession>>(`/focus-sessions/${id}/abort`),
}
