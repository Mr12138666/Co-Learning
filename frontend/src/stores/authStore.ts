import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginRequest, type RegisterRequest, type TokenResponse } from '@/api/auth'
import { useRoomStore } from '@/stores/roomStore'

export const useAuthStore = defineStore('auth', () => {
  // State
  const accessToken = ref<string | null>(null)
  const user = ref<TokenResponse | null>(null)

  // Getters
  const isAuthenticated = computed(() => !!accessToken.value)
  const isAdmin = computed(() => user.value?.role === 'ADMIN')
  const emailVerified = computed(() => user.value?.emailVerified ?? false)
  /** True when the access token is known to be within 10s of expiry (or already expired). */
  const isAccessTokenExpired = computed(() => {
    const expiresAt = user.value?.accessTokenExpiresAt
    if (!expiresAt) return false
    return new Date(expiresAt).getTime() - Date.now() < 10_000
  })

  // Actions
  async function register(data: RegisterRequest) {
    await authApi.register(data)
  }

  async function login(data: LoginRequest) {
    const response = await authApi.login(data)
    setToken(response.data.data)
  }

  async function refresh() {
    const response = await authApi.refresh()
    setToken(response.data.data)
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
    // Tear down any live realtime connection so STOMP stops reconnecting with a
    // stale/empty token (otherwise it loops forever after logout / refresh failure).
    try {
      useRoomStore().teardown()
    } catch {
      // roomStore not initialized yet (e.g. during boot) — nothing to disconnect.
    }
  }

  function updateAvatar(url: string) {
    if (user.value) {
      user.value.avatarUrl = url
    }
  }

  return {
    accessToken,
    user,
    isAuthenticated,
    isAdmin,
    emailVerified,
    isAccessTokenExpired,
    register,
    login,
    refresh,
    logout,
    setToken,
    clearAuth,
    updateAvatar,
  }
}, {
  persist: {
    pick: ['accessToken', 'user'],
  },
})
