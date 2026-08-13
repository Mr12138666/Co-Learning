import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { themes, getTheme } from '@/config/theme'
import type { ThemeName, Theme } from '@/config/theme'

export const useThemeStore = defineStore('theme', () => {
  // State
  const themeName = ref<ThemeName>('light')
  
  // Getters
  const currentTheme = computed<Theme>(() => getTheme(themeName.value))
  const isDark = computed(() => currentTheme.value.isDark)
  
  // Actions
  function setTheme(name: ThemeName) {
    themeName.value = name
    localStorage.setItem('theme', name)
    applyTheme(name)
  }
  
  function toggleDarkMode() {
    const newMode = isDark.value ? 'light' : 'dark'
    setTheme(newMode)
  }
  
  function applyTheme(name: ThemeName) {
    const theme = getTheme(name)
    const root = document.documentElement
    
    // 1. 移除所有主题类
    const allThemeNames: ThemeName[] = ['light', 'dark', 'cyberpunk', 'newspaper', 'pixel', 'ocean']
    allThemeNames.forEach(t => root.classList.remove(t))
    
    // 2. 添加当前主题类
    root.classList.add(name)
    
    // 3. 处理深色/浅色模式切换
    if (theme.isDark) {
      root.classList.add('dark')
    } else {
      root.classList.add('light')
    }
    
    // 4. 应用主题颜色（修改 accent 变量）
    const { colors } = theme
    root.style.setProperty('--accent-50', colors.accent50)
    root.style.setProperty('--accent-100', colors.accent100)
    root.style.setProperty('--accent-200', colors.accent200)
    root.style.setProperty('--accent-300', colors.accent300)
    root.style.setProperty('--accent-400', colors.accent400)
    root.style.setProperty('--accent-500', colors.accent500)
    root.style.setProperty('--accent-600', colors.accent600)
    root.style.setProperty('--accent-700', colors.accent700)
  }
  
  function initTheme() {
    const stored = localStorage.getItem('theme') as ThemeName | null
    
    if (stored && themes[stored]) {
      setTheme(stored)
    } else {
      // 检查系统偏好
      const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      setTheme(systemDark ? 'dark' : 'light')
    }
    
    // 监听系统主题变化
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
      if (!localStorage.getItem('theme')) {
        setTheme(e.matches ? 'dark' : 'light')
      }
    })
  }
  
  // 初始化主题
  initTheme()
  
  return {
    // State
    themeName,
    theme: themeName, // 向后兼容别名
    // Getters
    currentTheme,
    isDark,
    // Actions
    setTheme,
    toggleDarkMode,
    initTheme,
  }
})