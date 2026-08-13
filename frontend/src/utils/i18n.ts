/**
 * Internationalization utility.
 */

export type Locale = 'zh-CN' | 'en'

export interface I18nMessages {
  [key: string]: string | I18nMessages
}

const messages: Record<Locale, I18nMessages> = {
  'zh-CN': {
    common: {
      ok: '确定',
      cancel: '取消',
      save: '保存',
      delete: '删除',
      edit: '编辑',
      create: '创建',
      update: '更新',
      search: '搜索',
      reset: '重置',
      confirm: '确认',
      back: '返回',
      next: '下一步',
      prev: '上一步',
      loading: '加载中...',
      noData: '暂无数据',
      success: '成功',
      error: '错误',
      warning: '警告',
      info: '信息',
    },
    auth: {
      login: '登录',
      register: '注册',
      logout: '退出登录',
      email: '邮箱',
      password: '密码',
      confirmPassword: '确认密码',
      forgotPassword: '忘记密码',
      resetPassword: '重置密码',
      verifyEmail: '验证邮箱',
      displayName: '显示名称',
      rememberMe: '记住我',
      noAccount: '没有账号？',
      hasAccount: '已有账号？',
      loginSuccess: '登录成功',
      registerSuccess: '注册成功',
      logoutSuccess: '退出成功',
    },
    study: {
      goals: '考试目标',
      subjects: '科目管理',
      tasks: '任务清单',
      stats: '学习统计',
      checkin: '每日复盘',
      focus: '专注计时',
      goalName: '目标名称',
      examDate: '考试日期',
      targetScore: '目标分数',
      subjectName: '科目名称',
      taskTitle: '任务标题',
      taskDescription: '任务描述',
      dueDate: '截止日期',
      priority: '优先级',
      status: '状态',
    },
    gamification: {
      pet: '宠物',
      achievements: '成就',
      dailyTasks: '每日任务',
      leaderboard: '排行榜',
      level: '等级',
      exp: '经验值',
      happiness: '幸福度',
      reward: '奖励',
    },
    room: {
      rooms: '陪伴房',
      createRoom: '创建房间',
      joinRoom: '加入房间',
      roomName: '房间名称',
      roomDescription: '房间描述',
      members: '成员',
      messages: '消息',
    },
    journal: {
      journals: '学习日志',
      createJournal: '写日志',
      journalTitle: '日志标题',
      journalContent: '日志内容',
      mood: '心情',
      public: '公开',
      private: '私密',
    },
    user: {
      profile: '个人资料',
      settings: '设置',
      notifications: '通知',
      displayName: '显示名称',
      avatar: '头像',
      bio: '个人简介',
    },
    validation: {
      required: '此字段为必填项',
      email: '请输入有效的邮箱地址',
      minLength: '最少输入{min}个字符',
      maxLength: '最多输入{max}个字符',
      passwordMismatch: '两次输入的密码不一致',
    },
    error: {
      network: '网络错误，请稍后重试',
      unauthorized: '登录已过期，请重新登录',
      forbidden: '没有权限执行此操作',
      notFound: '请求的资源不存在',
      server: '服务器错误，请稍后重试',
      unknown: '发生未知错误',
    },
  },
  'en': {
    common: {
      ok: 'OK',
      cancel: 'Cancel',
      save: 'Save',
      delete: 'Delete',
      edit: 'Edit',
      create: 'Create',
      update: 'Update',
      search: 'Search',
      reset: 'Reset',
      confirm: 'Confirm',
      back: 'Back',
      next: 'Next',
      prev: 'Previous',
      loading: 'Loading...',
      noData: 'No Data',
      success: 'Success',
      error: 'Error',
      warning: 'Warning',
      info: 'Info',
    },
    auth: {
      login: 'Login',
      register: 'Register',
      logout: 'Logout',
      email: 'Email',
      password: 'Password',
      confirmPassword: 'Confirm Password',
      forgotPassword: 'Forgot Password',
      resetPassword: 'Reset Password',
      verifyEmail: 'Verify Email',
      displayName: 'Display Name',
      rememberMe: 'Remember Me',
      noAccount: "Don't have an account?",
      hasAccount: 'Already have an account?',
      loginSuccess: 'Login successful',
      registerSuccess: 'Registration successful',
      logoutSuccess: 'Logout successful',
    },
    study: {
      goals: 'Exam Goals',
      subjects: 'Subjects',
      tasks: 'Tasks',
      stats: 'Statistics',
      checkin: 'Check-in',
      focus: 'Focus Timer',
      goalName: 'Goal Name',
      examDate: 'Exam Date',
      targetScore: 'Target Score',
      subjectName: 'Subject Name',
      taskTitle: 'Task Title',
      taskDescription: 'Task Description',
      dueDate: 'Due Date',
      priority: 'Priority',
      status: 'Status',
    },
    gamification: {
      pet: 'Pet',
      achievements: 'Achievements',
      dailyTasks: 'Daily Tasks',
      leaderboard: 'Leaderboard',
      level: 'Level',
      exp: 'Experience',
      happiness: 'Happiness',
      reward: 'Reward',
    },
    room: {
      rooms: 'Rooms',
      createRoom: 'Create Room',
      joinRoom: 'Join Room',
      roomName: 'Room Name',
      roomDescription: 'Room Description',
      members: 'Members',
      messages: 'Messages',
    },
    journal: {
      journals: 'Journals',
      createJournal: 'Write Journal',
      journalTitle: 'Journal Title',
      journalContent: 'Journal Content',
      mood: 'Mood',
      public: 'Public',
      private: 'Private',
    },
    user: {
      profile: 'Profile',
      settings: 'Settings',
      notifications: 'Notifications',
      displayName: 'Display Name',
      avatar: 'Avatar',
      bio: 'Bio',
    },
    validation: {
      required: 'This field is required',
      email: 'Please enter a valid email address',
      minLength: 'Minimum {min} characters',
      maxLength: 'Maximum {max} characters',
      passwordMismatch: 'Passwords do not match',
    },
    error: {
      network: 'Network error, please try again later',
      unauthorized: 'Session expired, please login again',
      forbidden: 'You do not have permission to perform this action',
      notFound: 'The requested resource was not found',
      server: 'Server error, please try again later',
      unknown: 'An unknown error occurred',
    },
  },
}

let currentLocale: Locale = 'zh-CN'

/**
 * Set current locale.
 *
 * @param locale Locale to set
 */
export function setLocale(locale: Locale): void {
  currentLocale = locale
  localStorage.setItem('locale', locale)
}

/**
 * Get current locale.
 *
 * @returns Current locale
 */
export function getLocale(): Locale {
  return currentLocale
}

/**
 * Initialize locale from localStorage.
 */
export function initLocale(): void {
  const stored = localStorage.getItem('locale') as Locale | null
  if (stored && messages[stored]) {
    currentLocale = stored
  }
}

/**
 * Translate key to current locale.
 *
 * @param key Message key (dot notation)
 * @param params Parameters to replace
 * @returns Translated message
 */
export function t(key: string, params?: Record<string, string | number>): string {
  const keys = key.split('.')
  let value: unknown = messages[currentLocale]
  
  for (const k of keys) {
    if (typeof value === 'object' && value !== null && k in value) {
      value = (value as Record<string, unknown>)[k]
    } else {
      return key // Return key if not found
    }
  }
  
  if (typeof value !== 'string') {
    return key
  }
  
  // Replace parameters
  if (params) {
    return Object.entries(params).reduce(
      (str, [key, val]) => str.replace(`{${key}}`, String(val)),
      value
    )
  }
  
  return value
}

/**
 * Composable for internationalization.
 */
export function useI18n() {
  return {
    t,
    locale: currentLocale,
    setLocale,
    getLocale,
  }
}