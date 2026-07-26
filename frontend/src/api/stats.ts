import http from './http'
import type { ApiResponse } from '@/types/api'

// ===== Types =====

export interface DailyStat {
  date: string
  focusSeconds: number
  sessionCount: number
  checkedIn: boolean
}

export interface WeeklyStat {
  weekOfYear: number
  weekLabel: string
  focusSeconds: number
  sessionCount: number
  checkinCount: number
}

export interface MonthlyStat {
  month: string
  monthLabel: string
  focusSeconds: number
  sessionCount: number
  focusDays: number
}

export interface SubjectStat {
  subjectId: number
  subjectName: string
  subjectColor: string
  focusSeconds: number
  sessionCount: number
}

/**
 * Canonical stats payload (superset previously duplicated inline in StatsView).
 */
export interface Stats {
  todayFocusSeconds: number
  weekFocusSeconds: number
  monthFocusSeconds: number
  yearFocusSeconds: number
  totalFocusSeconds: number
  streakDays: number
  focusDays: number
  totalCheckins: number
  weekCheckinCount: number
  weekCompletedCount: number
  lastCheckinDate: string | null
  dailyStats: DailyStat[]
  weeklyStats: WeeklyStat[]
  monthlyStats: MonthlyStat[]
  subjectStats: SubjectStat[]
}

// ===== API =====

export const statsApi = {
  getStats: () => http.get<ApiResponse<Stats>>('/stats'),
}
