import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export type ThemeMode = 'light' | 'dark'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<ThemeMode>('light')

  function setTheme(mode: ThemeMode) {
    theme.value = mode
    localStorage.setItem('theme', mode)
    updateDocumentTheme(mode)
  }

  function toggleTheme() {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  function updateDocumentTheme(mode: ThemeMode) {
    const html = document.documentElement
    html.classList.remove('light', 'dark')
    html.classList.add(mode)
  }

  // Initialize theme from localStorage or system preference
  function initTheme() {
    const stored = localStorage.getItem('theme') as ThemeMode | null
    const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    
    if (stored) {
      setTheme(stored)
    } else if (systemDark) {
      setTheme('dark')
    }
  }

  // Watch for system theme changes
  watch(theme, (newTheme) => {
    updateDocumentTheme(newTheme)
  })

  return {
    theme,
    setTheme,
    toggleTheme,
    initTheme,
  }
})
