/**
 * Common type definitions.
 */

// API Response types
export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  traceId?: string
  timestamp?: string
}

export interface PageResponse<T> {
  items: T[]
  page: number
  size: number
  total: number
  totalPages: number
}

// User types
export interface User {
  id: number
  email: string
  displayName: string
  avatarUrl: string | null
  role: string
  emailVerified: boolean
  createdAt: string
  updatedAt: string
}

export interface UserProfile {
  userId: number
  displayName: string
  avatarUrl: string | null
  bio: string | null
  dailyFocusGoalMinutes: number
  createdAt: string
  updatedAt: string
}

// Study types
export interface ExamGoal {
  id: number
  examName: string
  examDate: string
  targetScore: string | null
  status: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED'
  daysRemaining: number
  createdAt: string
  updatedAt: string
}

export interface Subject {
  id: number
  name: string
  color: string
  sortOrder: number
  createdAt: string
  updatedAt: string
}

export interface StudyTask {
  id: number
  subjectId: number | null
  subjectName: string | null
  subjectColor: string | null
  examGoalId: number | null
  title: string
  description: string | null
  status: TaskStatus
  dueDate: string | null
  sortOrder: number
  plannedDate: string | null
  scheduledStart: string | null
  scheduledEnd: string | null
  estimatedMinutes: number | null
  urgent: boolean
  important: boolean
  tags: Tag[]
  totalFocusSeconds: number
  createdAt: string
  updatedAt: string
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'ARCHIVED'

export interface Tag {
  id: number
  name: string
  color: string
  createdAt: string
}

// Gamification types
export interface Pet {
  id: number
  name: string
  level: number
  exp: number
  happiness: number
  createdAt: string
  updatedAt: string
}

export interface Achievement {
  id: number
  name: string
  description: string
  icon: string
  unlockedAt: string | null
}

export interface DailyTask {
  id: number
  title: string
  description: string
  reward: number
  completed: boolean
  completedAt: string | null
}

// Room types
export interface Room {
  id: number
  name: string
  description: string | null
  maxMembers: number
  memberCount: number
  isPrivate: boolean
  createdAt: string
  updatedAt: string
}

export interface RoomMember {
  userId: number
  displayName: string
  avatarUrl: string | null
  role: 'OWNER' | 'ADMIN' | 'MEMBER'
  joinedAt: string
}

export interface RoomMessage {
  id: number
  userId: number
  displayName: string
  avatarUrl: string | null
  content: string
  createdAt: string
}

// Journal types
export interface Journal {
  id: number
  title: string
  content: string
  mood: string | null
  isPublic: boolean
  likes: number
  comments: number
  createdAt: string
  updatedAt: string
}

// Focus types
export interface FocusSession {
  id: number
  taskId: number | null
  startTime: string
  endTime: string | null
  durationSeconds: number
  completed: boolean
  createdAt: string
}

// Stats types
export interface StudyStats {
  todayFocusSeconds: number
  weekFocusSeconds: number
  monthFocusSeconds: number
  streakDays: number
  totalTasks: number
  completedTasks: number
  totalSubjects: number
}

// Leaderboard types
export interface LeaderboardEntry {
  rank: number
  userId: number
  displayName: string
  avatarUrl: string | null
  exp: number
  level: number
}

// Common types
export interface SelectOption {
  label: string
  value: string | number
  disabled?: boolean
}

export interface TreeNode {
  id: string | number
  label: string
  children?: TreeNode[]
  disabled?: boolean
}

export interface BreadcrumbItem {
  label: string
  path?: string
  icon?: Component
}

export interface MenuItem {
  key: string
  label: string
  icon?: Component
  path?: string
  children?: MenuItem[]
  disabled?: boolean
}