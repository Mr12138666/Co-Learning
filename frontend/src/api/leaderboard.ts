import http from './http'

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

// ===== API =====

export const leaderboardApi = {
  getLeaderboard(type: 'daily' | 'weekly' | 'alltime' = 'daily', limit = 20) {
    return http.get('/leaderboard', { params: { type, limit } })
  },

  getMyRank(type: 'daily' | 'weekly' | 'alltime' = 'daily') {
    return http.get('/leaderboard/me', { params: { type } })
  },

  syncLeaderboard: () => http.get('/leaderboard/sync'),
}
