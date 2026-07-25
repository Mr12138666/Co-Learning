import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginRequest, type RegisterRequest, type TokenResponse } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  // State
  const accessToken = ref<string | null>(null)
  const user = ref<TokenResponse | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const emailVerified = computed(() => user.value?.emailVerified ?? false)

  // Actions
  async function register(data: RegisterRequest) {
    await authApi.register(data)
  }

  async function login(data: LoginRequest) {
    const response = await authApi.login(data)
    const tokenData = response.data.data as TokenResponse
    setToken(tokenData)
  }

  async function refresh() {
    const response = await authApi.refresh()
    const tokenData = response.data.data as TokenResponse
    setToken(tokenData)
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      clearAuth()
    }
  }

  function setToken(tokenData: TokenResponse) {
    accessToken.value = tokenData.accessToken
    user.value = tokenData
  }

  function clearAuth() {
    accessToken.value = null
    user.value = null
  }

  return {
    accessToken,
    user,
    isAuthenticated,
    isAdmin,
    emailVerified,
    register,
    login,
    refresh,
    logout,
    setToken,
    clearAuth,
  }
}, {
  persist: {
    pick: ['accessToken', 'user'],
  },
})
