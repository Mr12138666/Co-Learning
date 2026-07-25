import http from './http'

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
}

export interface StartFocusRequest {
  subjectId?: number
  taskId?: number
  clientRequestId?: string
}

// ===== API =====

export const focusApi = {
  start: (data: StartFocusRequest) => http.post('/focus-sessions', data),
  getActive: () => http.get('/focus-sessions/active'),
  pause: (id: number) => http.post(`/focus-sessions/${id}/pause`),
  resume: (id: number) => http.post(`/focus-sessions/${id}/resume`),
  finish: (id: number) => http.post(`/focus-sessions/${id}/finish`),
  abort: (id: number) => http.post(`/focus-sessions/${id}/abort`),
}
