import { defineStore } from 'pinia'
import { ref } from 'vue'
import { statsApi, type Stats } from '@/api/stats'
import { checkinApi, type Checkin, type UpdateCheckinRequest } from '@/api/checkin'

export const useDashboardStore = defineStore('dashboard', () => {
  // State
  const stats = ref<Stats | null>(null)
  const todayCheckin = ref<Checkin | null>(null)
  const loading = ref(false)

  // Actions
  async function fetchStats() {
    const res = await statsApi.getStats()
    stats.value = res.data.data
  }

  async function fetchTodayCheckin() {
    const res = await checkinApi.getToday()
    todayCheckin.value = res.data.data
  }

  async function updateCheckin(data: UpdateCheckinRequest) {
    const res = await checkinApi.update(data)
    todayCheckin.value = res.data.data
    return res.data.data
  }

  async function completeCheckin() {
    const res = await checkinApi.complete()
    todayCheckin.value = res.data.data
    // Refresh stats after completing checkin
    await fetchStats()
    return res.data.data
  }

  async function fetchCheckinByDate(date: string) {
    const res = await checkinApi.getByDate(date)
    return res.data.data as Checkin
  }

  async function refreshAll() {
    loading.value = true
    try {
      await Promise.all([fetchStats(), fetchTodayCheckin()])
    } finally {
      loading.value = false
    }
  }

  return {
    stats,
    todayCheckin,
    loading,
    fetchStats,
    fetchTodayCheckin,
    updateCheckin,
    completeCheckin,
    fetchCheckinByDate,
    refreshAll,
  }
})
