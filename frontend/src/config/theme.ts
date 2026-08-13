/**
 * Theme configuration - 配合项目实际的CSS变量系统
 */

export type ThemeName = 'light' | 'dark' | 'blue' | 'green' | 'purple'

export interface ThemeColors {
  // 主要颜色 (accent)
  accent50: string
  accent100: string
  accent200: string
  accent300: string
  accent400: string
  accent500: string
  accent600: string
  accent700: string
}

export interface Theme {
  name: ThemeName
  label: string
  isDark: boolean
  colors: ThemeColors
}

// 浅色主题（默认）
const lightTheme: Theme = {
  name: 'light',
  label: '浅色模式',
  isDark: false,
  colors: {
    accent50: '#eff6ff',
    accent100: '#dbeafe',
    accent200: '#bfdbfe',
    accent300: '#93c5fd',
    accent400: '#60a5fa',
    accent500: '#3b82f6',
    accent600: '#2563eb',
    accent700: '#1d4ed8',
  },
}

// 深色主题
const darkTheme: Theme = {
  name: 'dark',
  label: '深色模式',
  isDark: true,
  colors: {
    accent50: '#1e3a5f',
    accent100: '#1e40af',
    accent200: '#1d4ed8',
    accent300: '#2563eb',
    accent400: '#3b82f6',
    accent500: '#60a5fa',
    accent600: '#93c5fd',
    accent700: '#bfdbfe',
  },
}

// 蓝色主题
const blueTheme: Theme = {
  name: 'blue',
  label: '蓝色主题',
  isDark: false,
  colors: {
    accent50: '#e0f2fe',
    accent100: '#bae6fd',
    accent200: '#7dd3fc',
    accent300: '#38bdf8',
    accent400: '#0ea5e9',
    accent500: '#0284c7',
    accent600: '#0369a1',
    accent700: '#075985',
  },
}

// 绿色主题
const greenTheme: Theme = {
  name: 'green',
  label: '绿色主题',
  isDark: false,
  colors: {
    accent50: '#ecfdf5',
    accent100: '#d1fae5',
    accent200: '#a7f3d0',
    accent300: '#6ee7b7',
    accent400: '#34d399',
    accent500: '#10b981',
    accent600: '#059669',
    accent700: '#047857',
  },
}

// 紫色主题
const purpleTheme: Theme = {
  name: 'purple',
  label: '紫色主题',
  isDark: false,
  colors: {
    accent50: '#faf5ff',
    accent100: '#f3e8ff',
    accent200: '#e9d5ff',
    accent300: '#d8b4fe',
    accent400: '#c084fc',
    accent500: '#a855f7',
    accent600: '#9333ea',
    accent700: '#7e22ce',
  },
}

// 主题注册表
export const themes: Record<ThemeName, Theme> = {
  light: lightTheme,
  dark: darkTheme,
  blue: blueTheme,
  green: greenTheme,
  purple: purpleTheme,
}

/**
 * 获取主题配置
 */
export function getTheme(name: ThemeName): Theme {
  return themes[name] || lightTheme
}

/**
 * 获取所有主题列表
 */
export function getThemeList(): Theme[] {
  return Object.values(themes)
}