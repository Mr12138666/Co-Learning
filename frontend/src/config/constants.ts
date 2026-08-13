/**
 * Application constants.
 */

// API Constants
export const API_BASE_URL = '/api'
export const API_TIMEOUT = 15000

// Auth Constants
export const AUTH_TOKEN_KEY = 'access_token'
export const AUTH_REFRESH_TOKEN_KEY = 'refresh_token'
export const AUTH_USER_KEY = 'user'

// Theme Constants
export const THEME_KEY = 'theme'
export const THEME_DEFAULT = 'light'
export const THEMES = ['light', 'dark', 'blue', 'green', 'purple'] as const

// Pagination Constants
export const PAGE_SIZE_DEFAULT = 10
export const PAGE_SIZE_OPTIONS = [10, 20, 30, 50, 100]
export const PAGE_DEFAULT = 1

// Date Format Constants
export const DATE_FORMAT = 'YYYY-MM-DD'
export const DATETIME_FORMAT = 'YYYY-MM-DD HH:mm:ss'
export const TIME_FORMAT = 'HH:mm'

// Status Constants
export const TASK_STATUS = {
  TODO: 'TODO',
  IN_PROGRESS: 'IN_PROGRESS',
  DONE: 'DONE',
  ARCHIVED: 'ARCHIVED',
} as const

export const GOAL_STATUS = {
  ACTIVE: 'ACTIVE',
  COMPLETED: 'COMPLETED',
  ARCHIVED: 'ARCHIVED',
} as const

// Priority Constants
export const PRIORITY = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  URGENT: 'URGENT',
} as const

// Mood Constants
export const MOOD = {
  HAPPY: 'HAPPY',
  EXCITED: 'EXCITED',
  NEUTRAL: 'NEUTRAL',
  TIRED: 'TIRED',
  STRESSED: 'STRESSED',
  SAD: 'SAD',
} as const

export const MOOD_EMOJI = {
  HAPPY: '😊',
  EXCITED: '🎉',
  NEUTRAL: '😐',
  TIRED: '😴',
  STRESSED: '😰',
  SAD: '😢',
} as const

// Focus Timer Constants
export const FOCUS_DEFAULT_MINUTES = 25
export const FOCUS_BREAK_MINUTES = 5
export const FOCUS_LONG_BREAK_MINUTES = 15
export const FOCUS_SESSIONS_BEFORE_LONG_BREAK = 4

// Gamification Constants
export const EXP_PER_LEVEL = 1000
export const DAILY_TASK_REWARD = 10
export const ACHIEVEMENT_REWARD = 50

// Room Constants
export const ROOM_MAX_MEMBERS = 50
export const ROOM_MESSAGE_MAX_LENGTH = 500

// Journal Constants
export const JOURNAL_TITLE_MAX_LENGTH = 100
export const JOURNAL_CONTENT_MAX_LENGTH = 10000

// Validation Constants
export const EMAIL_REGEX = /^[^s@]+@[^s@]+.[^s@]+$/
export const PHONE_REGEX = /^1[3-9]d{9}$/
export const PASSWORD_MIN_LENGTH = 8
export const PASSWORD_MAX_LENGTH = 50

// Error Messages
export const ERROR_MESSAGES = {
  REQUIRED: '此字段为必填项',
  INVALID_EMAIL: '请输入有效的邮箱地址',
  INVALID_PHONE: '请输入有效的手机号码',
  PASSWORD_TOO_SHORT: `密码长度不能少于${PASSWORD_MIN_LENGTH}个字符`,
  PASSWORD_TOO_LONG: `密码长度不能超过${PASSWORD_MAX_LENGTH}个字符`,
  NETWORK_ERROR: '网络错误，请稍后重试',
  UNAUTHORIZED: '登录已过期，请重新登录',
  FORBIDDEN: '没有权限执行此操作',
  NOT_FOUND: '请求的资源不存在',
  SERVER_ERROR: '服务器错误，请稍后重试',
} as const

// Success Messages
export const SUCCESS_MESSAGES = {
  SAVED: '保存成功',
  DELETED: '删除成功',
  CREATED: '创建成功',
  UPDATED: '更新成功',
  SENT: '发送成功',
  COPIED: '复制成功',
} as const

// Loading Messages
export const LOADING_MESSAGES = {
  DEFAULT: '加载中...',
  SAVING: '保存中...',
  DELETING: '删除中...',
  UPLOADING: '上传中...',
  SUBMITTING: '提交中...',
} as const