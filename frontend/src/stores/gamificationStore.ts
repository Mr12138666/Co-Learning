import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  gamificationApi,
  type GamificationProfileResponse,
  type PetResponse,
  type PetItemResponse,
  type UserItemResponse,
  type AchievementResponse,
  type DailyTaskResponse,
} from '@/api/gamification'

export const useGamificationStore = defineStore('gamification', () => {
  // ===== State =====
  const profile = ref<GamificationProfileResponse | null>(null)
  const pet = ref<PetResponse | null>(null)
  const shopItems = ref<PetItemResponse[]>([])
  const inventory = ref<UserItemResponse[]>([])
  const achievements = ref<AchievementResponse[]>([])
  const dailyTasks = ref<DailyTaskResponse[]>([])
  const loading = ref(false)

  // ===== Getters =====
  const unlockedCount = computed(() => achievements.value.filter((a) => a.unlocked).length)
  const totalCount = computed(() => achievements.value.length)
  const expPercent = computed(() => {
    if (!profile.value) return 0
    const total = profile.value.expIntoCurrentLevel + profile.value.expToNextLevel
    if (total === 0) return 0
    return Math.round((profile.value.expIntoCurrentLevel / total) * 100)
  })

  // ===== Actions =====

  async function loadProfile() {
    const res = await gamificationApi.getProfile()
    profile.value = res.data.data
  }

  async function loadPet() {
    const res = await gamificationApi.getPet()
    pet.value = res.data.data
  }

  async function renamePet(name: string) {
    const res = await gamificationApi.renamePet(name)
    pet.value = res.data.data
  }

  async function feedPet(itemId: number) {
    const res = await gamificationApi.feedPet(itemId)
    pet.value = res.data.data
    await loadInventory()
  }

  async function interactPet(itemId: number) {
    const res = await gamificationApi.interactPet(itemId)
    pet.value = res.data.data
    await loadInventory()
  }

  async function loadShop() {
    const res = await gamificationApi.getShopItems()
    shopItems.value = res.data.data
  }

  async function loadInventory() {
    const res = await gamificationApi.getInventory()
    inventory.value = res.data.data
  }

  async function purchaseItem(itemId: number) {
    await gamificationApi.purchaseItem(itemId)
    // Refresh profile to update token balance and inventory
    await Promise.all([loadProfile(), loadInventory()])
  }

  async function loadAchievements() {
    const res = await gamificationApi.getAchievements()
    achievements.value = res.data.data
  }

  async function loadDailyTasks() {
    const res = await gamificationApi.getDailyTasks()
    dailyTasks.value = res.data.data
  }

  async function claimTaskReward(taskId: number) {
    const res = await gamificationApi.claimTaskReward(taskId)
    dailyTasks.value = dailyTasks.value.map(t => 
      t.id === taskId ? res.data.data : t
    )
    await loadProfile()
  }

  /**
   * Load all gamification data at once.
   */
  async function loadAll() {
    loading.value = true
    try {
      await Promise.all([loadProfile(), loadPet(), loadAchievements()])
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    profile,
    pet,
    shopItems,
    inventory,
    achievements,
    dailyTasks,
    loading,
    // Getters
    unlockedCount,
    totalCount,
    expPercent,
    // Actions
    loadProfile,
    loadPet,
    renamePet,
    feedPet,
    interactPet,
    loadShop,
    loadInventory,
    purchaseItem,
    loadAchievements,
    loadDailyTasks,
    claimTaskReward,
    loadAll,
  }
})
