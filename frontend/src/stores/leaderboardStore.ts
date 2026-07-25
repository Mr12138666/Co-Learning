import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  leaderboardApi,
  type LeaderboardEntryResponse,
  type LeaderboardResponse,
} from '@/api/leaderboard'

export type LeaderboardType = 'daily' | 'weekly' | 'alltime'

export const useLeaderboardStore = defineStore('leaderboard', () => {
  // ===== State =====
  const entries = ref<LeaderboardEntryResponse[]>([])
  const myRank = ref<LeaderboardEntryResponse | null>(null)
  const currentType = ref<LeaderboardType>('daily')
  const loading = ref(false)

  // ===== Actions =====

  async function loadLeaderboard(type: LeaderboardType = currentType.value, limit = 20) {
    loading.value = true
    currentType.value = type
    try {
      const res = await leaderboardApi.getLeaderboard(type, limit)
      const data: LeaderboardResponse = res.data.data
      entries.value = data.entries
      myRank.value = data.myRank
    } finally {
      loading.value = false
    }
  }

  return {
    entries,
    myRank,
    currentType,
    loading,
    loadLeaderboard,
  }
})
