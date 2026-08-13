/**
 * Theme configuration - 5种特色主题
 */

export type ThemeName = 'light' | 'dark' | 'cyberpunk' | 'newspaper' | 'pixel' | 'ocean'

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
  label: '默认浅色',
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
  label: '默认深色',
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

// 赛博朋克主题 - 霓虹色、深色背景、亮色点缀
const cyberpunkTheme: Theme = {
  name: 'cyberpunk',
  label: '赛博朋克',
  isDark: true,
  colors: {
    accent50: '#0d0221',
    accent100: '#150734',
    accent200: '#2d1b69',
    accent300: '#7b2ff7',
    accent400: '#ff2a6d',
    accent500: '#05d9e8',
    accent600: '#d1f7ff',
    accent700: '#ffffff',
  },
}

// 复古报纸主题 - 黑白灰、怀旧风格
const newspaperTheme: Theme = {
  name: 'newspaper',
  label: '复古报纸',
  isDark: false,
  colors: {
    accent50: '#f5f0e8',
    accent100: '#e8dfd0',
    accent200: '#d4c8b0',
    accent300: '#8b7355',
    accent400: '#5c4a32',
    accent500: '#2c1810',
    accent600: '#1a0f09',
    accent700: '#0d0705',
  },
}

// 像素艺术主题 - 8-bit风格、鲜艳色彩
const pixelTheme: Theme = {
  name: 'pixel',
  label: '像素艺术',
  isDark: false,
  colors: {
    accent50: '#fff9c4',
    accent100: '#ff8a80',
    accent200: '#ea80fc',
    accent300: '#82b1ff',
    accent400: '#69f0ae',
    accent500: '#ff6e40',
    accent600: '#ff3d00',
    accent700: '#dd2c00',
  },
}

// 海洋深蓝主题 - 蓝色系、深邃宁静
const oceanTheme: Theme = {
  name: 'ocean',
  label: '海洋深蓝',
  isDark: true,
  colors: {
    accent50: '#001220',
    accent100: '#001f3f',
    accent200: '#003366',
    accent300: '#00509e',
    accent400: '#0077b6',
    accent500: '#00b4d8',
    accent600: '#90e0ef',
    accent700: '#caf0f8',
  },
}

// 主题注册表
export const themes: Record<ThemeName, Theme> = {
  light: lightTheme,
  dark: darkTheme,
  cyberpunk: cyberpunkTheme,
  newspaper: newspaperTheme,
  pixel: pixelTheme,
  ocean: oceanTheme,
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