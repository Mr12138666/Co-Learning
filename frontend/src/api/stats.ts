import http from './http'

// ===== Types =====

export interface DailyStat {
  date: string
  focusSeconds: number
  sessionCount: number
  checkedIn: boolean
}

export interface SubjectStat {
  subjectId: number
  subjectName: string
  subjectColor: string
  focusSeconds: number
  sessionCount: number
}

export interface Stats {
  todayFocusSeconds: number
  weekFocusSeconds: number
  totalFocusSeconds: number
  streakDays: number
  weekCheckinCount: number
  weekCompletedCount: number
  lastCheckinDate: string | null
  dailyStats: DailyStat[]
  subjectStats: SubjectStat[]
}

// ===== API =====

export const statsApi = {
  getStats: () => http.get('/stats'),
}
