import http from './http'

// ===== Types =====

export interface GamificationProfileResponse {
  userId: number
  totalExp: number
  level: number
  tokens: number
  expToNextLevel: number
  expIntoCurrentLevel: number
}

export interface PetResponse {
  id: number
  userId: number
  name: string
  species: string
  level: number
  exp: number
  mood: number
  hunger: number
  lastFedAt: string | null
  lastInteractedAt: string | null
}

export interface PetItemResponse {
  id: number
  name: string
  description: string | null
  itemType: string
  effectType: string
  effectValue: number
  price: number
  icon: string | null
}

export interface AchievementResponse {
  id: number
  code: string
  name: string
  description: string
  category: string
  conditionType: string
  conditionValue: number
  icon: string | null
  expReward: number
  tokenReward: number
  unlocked: boolean
  unlockedAt: string | null
}

// ===== API =====

export const gamificationApi = {
  getProfile() {
    return http.get('/gamification/profile')
  },

  getPet() {
    return http.get('/gamification/pet')
  },

  renamePet(name: string) {
    return http.put('/gamification/pet', { name })
  },

  feedPet(itemId: number) {
    return http.post(`/gamification/pet/feed/${itemId}`)
  },

  interactPet(itemId: number) {
    return http.post(`/gamification/pet/interact/${itemId}`)
  },

  getShopItems() {
    return http.get('/gamification/shop')
  },

  purchaseItem(itemId: number) {
    return http.post(`/gamification/shop/buy/${itemId}`)
  },

  getAchievements() {
    return http.get('/gamification/achievements')
  },
}
