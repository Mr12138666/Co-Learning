import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '../authStore'

// Mock the auth API
vi.mock('@/api/auth', () => ({
  authApi: {
    register: vi.fn().mockResolvedValue({}),
    login: vi.fn().mockResolvedValue({
      data: {
        data: {
          accessToken: 'test-token-123',
          userId: 1,
          email: 'test@example.com',
          role: 'USER',
          emailVerified: true,
          displayName: 'Test User',
        },
      },
    }),
    refresh: vi.fn().mockResolvedValue({
      data: {
        data: {
          accessToken: 'refreshed-token-456',
          userId: 1,
          email: 'test@example.com',
          role: 'USER',
          emailVerified: true,
          displayName: 'Test User',
        },
      },
    }),
    logout: vi.fn().mockResolvedValue({}),
  },
}))

describe('authStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('starts unauthenticated', () => {
    const store = useAuthStore()
    expect(store.isAuthenticated).toBe(false)
    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
  })

  it('login sets token and user', async () => {
    const store = useAuthStore()
    await store.login({ email: 'test@example.com', password: 'password123' })

    expect(store.isAuthenticated).toBe(true)
    expect(store.accessToken).toBe('test-token-123')
    expect(store.user?.email).toBe('test@example.com')
    expect(store.user?.role).toBe('USER')
  })

  it('isAdmin returns true for ADMIN role', () => {
    const store = useAuthStore()
    store.setToken({
      accessToken: 'admin-token',
      accessTokenExpiresIn: 3600,
      accessTokenExpiresAt: new Date(Date.now() + 3600000).toISOString(),
      userId: 1,
      email: 'admin@example.com',
      role: 'ADMIN',
      emailVerified: true,
      displayName: 'Admin',
      avatarUrl: null,
    })

    expect(store.isAdmin).toBe(true)
  })

  it('isAdmin returns false for USER role', async () => {
    const store = useAuthStore()
    await store.login({ email: 'test@example.com', password: 'password123' })

    expect(store.isAdmin).toBe(false)
  })

  it('emailVerified reflects user state', async () => {
    const store = useAuthStore()
    await store.login({ email: 'test@example.com', password: 'password123' })

    expect(store.emailVerified).toBe(true)
  })

  it('clearAuth resets state', async () => {
    const store = useAuthStore()
    await store.login({ email: 'test@example.com', password: 'password123' })
    expect(store.isAuthenticated).toBe(true)

    store.clearAuth()
    expect(store.isAuthenticated).toBe(false)
    expect(store.accessToken).toBeNull()
    expect(store.user).toBeNull()
  })

  it('refresh updates token', async () => {
    const store = useAuthStore()
    await store.login({ email: 'test@example.com', password: 'password123' })
    expect(store.accessToken).toBe('test-token-123')

    await store.refresh()
    expect(store.accessToken).toBe('refreshed-token-456')
  })

  it('logout clears auth state', async () => {
    const store = useAuthStore()
    await store.login({ email: 'test@example.com', password: 'password123' })
    expect(store.isAuthenticated).toBe(true)

    await store.logout()
    expect(store.isAuthenticated).toBe(false)
    expect(store.accessToken).toBeNull()
  })
})
