/**
 * Theme configuration.
 * Provides theme colors, typography, and spacing.
 */

export interface ThemeColors {
  // Primary colors
  primary: string
  primaryHover: string
  primaryPressed: string
  primarySuppl: string
  
  // Secondary colors
  secondary: string
  secondaryHover: string
  secondaryPressed: string
  
  // Success colors
  success: string
  successHover: string
  successPressed: string
  
  // Warning colors
  warning: string
  warningHover: string
  warningPressed: string
  
  // Error colors
  error: string
  errorHover: string
  errorPressed: string
  
  // Info colors
  info: string
  infoHover: string
  infoPressed: string
  
  // Neutral colors
  text: string
  textSecondary: string
  textTertiary: string
  textQuaternary: string
  
  // Background colors
  background: string
  backgroundSecondary: string
  backgroundTertiary: string
  
  // Surface colors
  surface: string
  surfaceSecondary: string
  surfaceTertiary: string
  
  // Border colors
  border: string
  borderSecondary: string
  borderTertiary: string
  
  // Shadow colors
  shadow: string
  shadowSecondary: string
}

export interface ThemeTypography {
  fontFamily: string
  fontFamilyMono: string
  
  fontSize: {
    xs: string
    sm: string
    base: string
    lg: string
    xl: string
    '2xl': string
    '3xl': string
    '4xl': string
  }
  
  fontWeight: {
    light: number
    normal: number
    medium: number
    semibold: number
    bold: number
  }
  
  lineHeight: {
    none: number
    tight: number
    snug: number
    normal: number
    relaxed: number
    loose: number
  }
}

export interface ThemeSpacing {
  px: string
  0: string
  1: string
  2: string
  3: string
  4: string
  5: string
  6: string
  8: string
  10: string
  12: string
  16: string
  20: string
  24: string
  32: string
  40: string
  48: string
  56: string
  64: string
}

export interface ThemeRadius {
  none: string
  sm: string
  base: string
  md: string
  lg: string
  xl: string
  '2xl': string
  '3xl': string
  full: string
}

export interface ThemeShadow {
  sm: string
  base: string
  md: string
  lg: string
  xl: string
  '2xl': string
  inner: string
}

export interface Theme {
  name: string
  colors: ThemeColors
  typography: ThemeTypography
  spacing: ThemeSpacing
  radius: ThemeRadius
  shadow: ThemeShadow
}

// Light theme
export const lightTheme: Theme = {
  name: 'light',
  colors: {
    primary: '#2080F0',
    primaryHover: '#4098FC',
    primaryPressed: '#1060C9',
    primarySuppl: 'rgba(32, 128, 240, 0.1)',
    
    secondary: '#6B7280',
    secondaryHover: '#9CA3AF',
    secondaryPressed: '#4B5563',
    
    success: '#18A058',
    successHover: '#36AD6A',
    successPressed: '#0C7A43',
    
    warning: '#F0A020',
    warningHover: '#FCB040',
    warningPressed: '#C97C10',
    
    error: '#D03050',
    errorHover: '#DE576D',
    errorPressed: '#AB1F3B',
    
    info: '#2080F0',
    infoHover: '#4098FC',
    infoPressed: '#1060C9',
    
    text: '#1F2937',
    textSecondary: '#6B7280',
    textTertiary: '#9CA3AF',
    textQuaternary: '#D1D5DB',
    
    background: '#F9FAFB',
    backgroundSecondary: '#F3F4F6',
    backgroundTertiary: '#E5E7EB',
    
    surface: '#FFFFFF',
    surfaceSecondary: '#F9FAFB',
    surfaceTertiary: '#F3F4F6',
    
    border: '#E5E7EB',
    borderSecondary: '#D1D5DB',
    borderTertiary: '#9CA3AF',
    
    shadow: 'rgba(0, 0, 0, 0.1)',
    shadowSecondary: 'rgba(0, 0, 0, 0.05)',
  },
  typography: {
    fontFamily: 'Inter, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif',
    fontFamilyMono: 'JetBrains Mono, Fira Code, monospace',
    
    fontSize: {
      xs: '0.75rem',
      sm: '0.875rem',
      base: '1rem',
      lg: '1.125rem',
      xl: '1.25rem',
      '2xl': '1.5rem',
      '3xl': '1.875rem',
      '4xl': '2.25rem',
    },
    
    fontWeight: {
      light: 300,
      normal: 400,
      medium: 500,
      semibold: 600,
      bold: 700,
    },
    
    lineHeight: {
      none: 1,
      tight: 1.25,
      snug: 1.375,
      normal: 1.5,
      relaxed: 1.625,
      loose: 2,
    },
  },
  spacing: {
    px: '1px',
    0: '0',
    1: '0.25rem',
    2: '0.5rem',
    3: '0.75rem',
    4: '1rem',
    5: '1.25rem',
    6: '1.5rem',
    8: '2rem',
    10: '2.5rem',
    12: '3rem',
    16: '4rem',
    20: '5rem',
    24: '6rem',
    32: '8rem',
    40: '10rem',
    48: '12rem',
    56: '14rem',
    64: '16rem',
  },
  radius: {
    none: '0',
    sm: '0.125rem',
    base: '0.25rem',
    md: '0.375rem',
    lg: '0.5rem',
    xl: '0.75rem',
    '2xl': '1rem',
    '3xl': '1.5rem',
    full: '9999px',
  },
  shadow: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    base: '0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1)',
    '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
    inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.05)',
  },
}

// Dark theme
export const darkTheme: Theme = {
  name: 'dark',
  colors: {
    primary: '#4098FC',
    primaryHover: '#6AB0FF',
    primaryPressed: '#2080F0',
    primarySuppl: 'rgba(64, 152, 252, 0.1)',
    
    secondary: '#9CA3AF',
    secondaryHover: '#D1D5DB',
    secondaryPressed: '#6B7280',
    
    success: '#36AD6A',
    successHover: '#63E2A0',
    successPressed: '#18A058',
    
    warning: '#FCB040',
    warningHover: '#F5D58C',
    warningPressed: '#F0A020',
    
    error: '#DE576D',
    errorHover: '#F098A8',
    errorPressed: '#D03050',
    
    info: '#4098FC',
    infoHover: '#6AB0FF',
    infoPressed: '#2080F0',
    
    text: '#F9FAFB',
    textSecondary: '#D1D5DB',
    textTertiary: '#9CA3AF',
    textQuaternary: '#6B7280',
    
    background: '#111827',
    backgroundSecondary: '#1F2937',
    backgroundTertiary: '#374151',
    
    surface: '#1F2937',
    surfaceSecondary: '#374151',
    surfaceTertiary: '#4B5563',
    
    border: '#374151',
    borderSecondary: '#4B5563',
    borderTertiary: '#6B7280',
    
    shadow: 'rgba(0, 0, 0, 0.3)',
    shadowSecondary: 'rgba(0, 0, 0, 0.2)',
  },
  typography: lightTheme.typography,
  spacing: lightTheme.spacing,
  radius: lightTheme.radius,
  shadow: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.2)',
    base: '0 1px 3px 0 rgba(0, 0, 0, 0.3), 0 1px 2px -1px rgba(0, 0, 0, 0.3)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.3), 0 2px 4px -2px rgba(0, 0, 0, 0.3)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.3), 0 4px 6px -4px rgba(0, 0, 0, 0.3)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 8px 10px -6px rgba(0, 0, 0, 0.3)',
    '2xl': '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
    inner: 'inset 0 2px 4px 0 rgba(0, 0, 0, 0.2)',
  },
}

// Blue theme (calm, focused)
export const blueTheme: Theme = {
  name: 'blue',
  colors: {
    primary: '#3B82F6',
    primaryHover: '#60A5FA',
    primaryPressed: '#2563EB',
    primarySuppl: 'rgba(59, 130, 246, 0.1)',
    
    secondary: '#64748B',
    secondaryHover: '#94A3B8',
    secondaryPressed: '#475569',
    
    success: '#10B981',
    successHover: '#34D399',
    successPressed: '#059669',
    
    warning: '#F59E0B',
    warningHover: '#FBBF24',
    warningPressed: '#D97706',
    
    error: '#EF4444',
    errorHover: '#F87171',
    errorPressed: '#DC2626',
    
    info: '#3B82F6',
    infoHover: '#60A5FA',
    infoPressed: '#2563EB',
    
    text: '#1E293B',
    textSecondary: '#64748B',
    textTertiary: '#94A3B8',
    textQuaternary: '#CBD5E1',
    
    background: '#F8FAFC',
    backgroundSecondary: '#F1F5F9',
    backgroundTertiary: '#E2E8F0',
    
    surface: '#FFFFFF',
    surfaceSecondary: '#F8FAFC',
    surfaceTertiary: '#F1F5F9',
    
    border: '#E2E8F0',
    borderSecondary: '#CBD5E1',
    borderTertiary: '#94A3B8',
    
    shadow: 'rgba(59, 130, 246, 0.1)',
    shadowSecondary: 'rgba(59, 130, 246, 0.05)',
  },
  typography: lightTheme.typography,
  spacing: lightTheme.spacing,
  radius: lightTheme.radius,
  shadow: lightTheme.shadow,
}

// Green theme (natural, growth)
export const greenTheme: Theme = {
  name: 'green',
  colors: {
    primary: '#22C55E',
    primaryHover: '#4ADE80',
    primaryPressed: '#16A34A',
    primarySuppl: 'rgba(34, 197, 94, 0.1)',
    
    secondary: '#6B7280',
    secondaryHover: '#9CA3AF',
    secondaryPressed: '#4B5563',
    
    success: '#22C55E',
    successHover: '#4ADE80',
    successPressed: '#16A34A',
    
    warning: '#EAB308',
    warningHover: '#FACC15',
    warningPressed: '#CA8A04',
    
    error: '#EF4444',
    errorHover: '#F87171',
    errorPressed: '#DC2626',
    
    info: '#3B82F6',
    infoHover: '#60A5FA',
    infoPressed: '#2563EB',
    
    text: '#1F2937',
    textSecondary: '#6B7280',
    textTertiary: '#9CA3AF',
    textQuaternary: '#D1D5DB',
    
    background: '#F0FDF4',
    backgroundSecondary: '#DCFCE7',
    backgroundTertiary: '#BBF7D0',
    
    surface: '#FFFFFF',
    surfaceSecondary: '#F0FDF4',
    surfaceTertiary: '#DCFCE7',
    
    border: '#BBF7D0',
    borderSecondary: '#86EFAC',
    borderTertiary: '#4ADE80',
    
    shadow: 'rgba(34, 197, 94, 0.1)',
    shadowSecondary: 'rgba(34, 197, 94, 0.05)',
  },
  typography: lightTheme.typography,
  spacing: lightTheme.spacing,
  radius: lightTheme.radius,
  shadow: lightTheme.shadow,
}

// Purple theme (creative, elegant)
export const purpleTheme: Theme = {
  name: 'purple',
  colors: {
    primary: '#A855F7',
    primaryHover: '#C084FC',
    primaryPressed: '#9333EA',
    primarySuppl: 'rgba(168, 85, 247, 0.1)',
    
    secondary: '#6B7280',
    secondaryHover: '#9CA3AF',
    secondaryPressed: '#4B5563',
    
    success: '#22C55E',
    successHover: '#4ADE80',
    successPressed: '#16A34A',
    
    warning: '#F59E0B',
    warningHover: '#FBBF24',
    warningPressed: '#D97706',
    
    error: '#EF4444',
    errorHover: '#F87171',
    errorPressed: '#DC2626',
    
    info: '#3B82F6',
    infoHover: '#60A5FA',
    infoPressed: '#2563EB',
    
    text: '#1F2937',
    textSecondary: '#6B7280',
    textTertiary: '#9CA3AF',
    textQuaternary: '#D1D5DB',
    
    background: '#FAF5FF',
    backgroundSecondary: '#F3E8FF',
    backgroundTertiary: '#E9D5FF',
    
    surface: '#FFFFFF',
    surfaceSecondary: '#FAF5FF',
    surfaceTertiary: '#F3E8FF',
    
    border: '#E9D5FF',
    borderSecondary: '#D8B4FE',
    borderTertiary: '#C084FC',
    
    shadow: 'rgba(168, 85, 247, 0.1)',
    shadowSecondary: 'rgba(168, 85, 247, 0.05)',
  },
  typography: lightTheme.typography,
  spacing: lightTheme.spacing,
  radius: lightTheme.radius,
  shadow: lightTheme.shadow,
}

// Theme registry
export const themes = {
  light: lightTheme,
  dark: darkTheme,
  blue: blueTheme,
  green: greenTheme,
  purple: purpleTheme,
}

export type ThemeName = keyof typeof themes

/**
 * Get theme by name.
 *
 * @param name Theme name
 * @returns Theme configuration
 */
export function getTheme(name: ThemeName): Theme {
  return themes[name] || lightTheme
}