import { describe, it, expect, beforeEach, afterEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useThemeStore } from '../themeStore'

// Shared localStorage store
let lsStore: Record<string, string> = {}

const localStorageMock = {
  getItem: vi.fn((key: string) => lsStore[key] ?? null),
  setItem: vi.fn((key: string, value: string) => { lsStore[key] = value }),
  removeItem: vi.fn((key: string) => { delete lsStore[key] }),
  clear: vi.fn(() => { lsStore = {} }),
}

Object.defineProperty(window, 'localStorage', { value: localStorageMock })

// Store original matchMedia
const originalMatchMedia = window.matchMedia

function setupMatchMediaMock(matchesDark = false) {
  window.matchMedia = vi.fn().mockImplementation((query: string) => ({
    matches: query === '(prefers-color-scheme: dark)' ? matchesDark : false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })) as any
}

describe('themeStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    // Reset mock implementations (important: previous test's mockReturnValue persists otherwise)
    localStorageMock.getItem.mockReset()
    localStorageMock.getItem.mockImplementation((key: string) => lsStore[key] ?? null)
    localStorageMock.setItem.mockReset()
    localStorageMock.setItem.mockImplementation((key: string, value: string) => { lsStore[key] = value })
    localStorageMock.removeItem.mockReset()
    localStorageMock.removeItem.mockImplementation((key: string) => { delete lsStore[key] })
    localStorageMock.clear.mockReset()
    localStorageMock.clear.mockImplementation(() => { lsStore = {} })
    lsStore = {}
    setupMatchMediaMock(false)
    document.documentElement.className = ''
  })

  afterEach(() => {
    window.matchMedia = originalMatchMedia
  })

  it('defaults to light theme', () => {
    const store = useThemeStore()
    expect(store.theme).toBe('light')
  })

  it('toggleTheme switches between light and dark', () => {
    const store = useThemeStore()
    expect(store.theme).toBe('light')

    store.toggleTheme()
    expect(store.theme).toBe('dark')

    store.toggleTheme()
    expect(store.theme).toBe('light')
  })

  it('setTheme saves to localStorage', () => {
    const store = useThemeStore()
    store.setTheme('dark')

    expect(localStorageMock.setItem).toHaveBeenCalledWith('theme', 'dark')
  })

  it('initTheme reads from localStorage', () => {
    localStorageMock.getItem.mockReturnValue('dark')
    const store = useThemeStore()
    store.initTheme()

    expect(store.theme).toBe('dark')
  })

  it('initTheme falls back to system preference when no stored value', () => {
    const store = useThemeStore()
    store.initTheme()

    // matchMedia mock returns false for dark, so theme stays light
    expect(store.theme).toBe('light')
  })

  it('initTheme sets dark when system prefers dark', () => {
    setupMatchMediaMock(true)
    const store = useThemeStore()
    store.initTheme()

    expect(store.theme).toBe('dark')
  })
})
