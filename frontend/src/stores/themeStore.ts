import { defineStore } from 'pinia'
import { ref, watch, computed } from 'vue'
import { themes, getTheme } from '@/config/theme'
import type { ThemeName, Theme } from '@/config/theme'

export const useThemeStore = defineStore('theme', () => {
  // State
  const themeName = ref<ThemeName>('light')
  const isDark = ref(false)
  
  // Getters
  const currentTheme = computed<Theme>(() => getTheme(themeName.value))
  const themeMode = computed(() => isDark.value ? 'dark' : 'light')
  
  // Actions
  function setTheme(name: ThemeName) {
    themeName.value = name
    isDark.value = name === 'dark'
    localStorage.setItem('theme', name)
    applyTheme(name)
  }
  
  function toggleDarkMode() {
    const newMode = isDark.value ? 'light' : 'dark'
    setTheme(newMode)
  }
  
  function setDarkMode(dark: boolean) {
    setTheme(dark ? 'dark' : 'light')
  }
  
  function applyTheme(name: ThemeName) {
    const theme = getTheme(name)
    const root = document.documentElement
    
    // Apply CSS variables
    Object.entries(theme.colors).forEach(([key, value]) => {
      root.style.setProperty(`--${key.replace(/([A-Z])/g, '-$1').toLowerCase()}`, value)
    })
    
    // Apply dark mode class
    if (isDark.value) {
      root.classList.add('dark')
      root.classList.remove('light')
    } else {
      root.classList.add('light')
      root.classList.remove('dark')
    }
  }
  
  function initTheme() {
    const stored = localStorage.getItem('theme') as ThemeName | null
    const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
    
    if (stored && themes[stored]) {
      setTheme(stored)
    } else if (systemDark) {
      setTheme('dark')
    } else {
      setTheme('light')
    }
    
    // Watch for system theme changes
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem('theme')) {
        setTheme(e.matches ? 'dark' : 'light')
      }
    })
  }
  
  // Initialize theme
  initTheme()
  
  return {
    // State
    themeName,
    isDark,
    // Getters
    currentTheme,
    themeMode,
    // Actions
    setTheme,
    toggleDarkMode,
    setDarkMode,
    initTheme,
  }
})