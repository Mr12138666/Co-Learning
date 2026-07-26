import http from './http'
import type { ApiResponse } from '@/types/api'

// ===== Types =====

export interface LeaderboardEntryResponse {
  userId: number
  displayName: string
  avatarUrl: string | null
  rank: number
  score: number
}

export interface LeaderboardResponse {
  type: string
  entries: LeaderboardEntryResponse[]
  myRank: LeaderboardEntryResponse | null
}

export type LeaderboardType = 'daily' | 'weekly' | 'alltime'

// ===== API =====

export const leaderboardApi = {
  getLeaderboard(type: LeaderboardType = 'daily', limit = 20) {
    return http.get<ApiResponse<LeaderboardResponse>>('/leaderboard', { params: { type, limit } })
  },

  getMyRank(type: LeaderboardType = 'daily') {
    return http.get<ApiResponse<LeaderboardEntryResponse>>('/leaderboard/me', { params: { type } })
  },

  syncLeaderboard: () => http.post<ApiResponse<void>>('/leaderboard/sync'),
}
