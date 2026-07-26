import http from './http'
import type { ApiResponse } from '@/types/api'

// ===== Types =====

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

export interface Tag {
  id: number
  name: string
  color: string
  createdAt: string
}

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'ARCHIVED'

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
  // Scheduling / planning (V13)
  plannedDate: string | null
  scheduledStart: string | null
  scheduledEnd: string | null
  estimatedMinutes: number | null
  urgent: boolean
  important: boolean
  // Tags (V14)
  tags: Tag[]
  totalFocusSeconds: number
  createdAt: string
  updatedAt: string
}

/** Eisenhower quadrant keys, matching the backend LinkedHashMap ordering. */
export type QuadrantKey =
  | 'urgent-important'
  | 'not-urgent-important'
  | 'urgent-not-important'
  | 'not-urgent-not-important'

export type QuadrantMap = Record<QuadrantKey, StudyTask[]>

export interface CreateExamGoalRequest {
  examName: string
  examDate: string
  targetScore?: number
}

export interface UpdateExamGoalRequest {
  examName?: string
  examDate?: string
  targetScore?: number
  status?: 'ACTIVE' | 'COMPLETED' | 'ARCHIVED'
}

export interface CreateSubjectRequest {
  name: string
  color?: string
}

export interface UpdateSubjectRequest {
  name?: string
  color?: string
  sortOrder?: number
}

export interface CreateTaskRequest {
  subjectId?: number
  examGoalId?: number
  title: string
  description?: string
  dueDate?: string
  plannedDate?: string | null
  scheduledStart?: string | null
  scheduledEnd?: string | null
  estimatedMinutes?: number | null
  urgent?: boolean
  important?: boolean
  tagIds?: number[]
}

export interface UpdateTaskRequest {
  title?: string
  description?: string
  status?: TaskStatus
  dueDate?: string | null
  subjectId?: number | null
  sortOrder?: number
  plannedDate?: string | null
  scheduledStart?: string | null
  scheduledEnd?: string | null
  estimatedMinutes?: number | null
  urgent?: boolean
  important?: boolean
  tagIds?: number[]
}

export interface CreateTagRequest {
  name: string
  color?: string
}

// ===== API =====

export const studyApi = {
  // Goals
  listGoals: () => http.get<ApiResponse<ExamGoal[]>>('/study/goals'),
  createGoal: (data: CreateExamGoalRequest) => http.post<ApiResponse<ExamGoal>>('/study/goals', data),
  updateGoal: (id: number, data: UpdateExamGoalRequest) =>
    http.put<ApiResponse<ExamGoal>>(`/study/goals/${id}`, data),
  deleteGoal: (id: number) => http.delete<ApiResponse<void>>(`/study/goals/${id}`),

  // Subjects
  listSubjects: () => http.get<ApiResponse<Subject[]>>('/study/subjects'),
  createSubject: (data: CreateSubjectRequest) => http.post<ApiResponse<Subject>>('/study/subjects', data),
  updateSubject: (id: number, data: UpdateSubjectRequest) =>
    http.put<ApiResponse<Subject>>(`/study/subjects/${id}`, data),
  deleteSubject: (id: number) => http.delete<ApiResponse<void>>(`/study/subjects/${id}`),

  // Tasks — CRUD
  listTasks: (params?: { status?: string; subjectId?: number }) =>
    http.get<ApiResponse<StudyTask[]>>('/study/tasks', { params }),
  createTask: (data: CreateTaskRequest) => http.post<ApiResponse<StudyTask>>('/study/tasks', data),
  updateTask: (id: number, data: UpdateTaskRequest) =>
    http.put<ApiResponse<StudyTask>>(`/study/tasks/${id}`, data),
  deleteTask: (id: number) => http.delete<ApiResponse<void>>(`/study/tasks/${id}`),

  // Tasks — workstation views
  listInboxTasks: () => http.get<ApiResponse<StudyTask[]>>('/study/tasks/inbox'),
  listTodayTasks: () => http.get<ApiResponse<StudyTask[]>>('/study/tasks/today'),
  listOverdueTasks: () => http.get<ApiResponse<StudyTask[]>>('/study/tasks/overdue'),
  listPlannerTasks: (startDate: string, endDate: string) =>
    http.get<ApiResponse<StudyTask[]>>('/study/tasks/planner', { params: { startDate, endDate } }),
  listQuadrant: () => http.get<ApiResponse<QuadrantMap>>('/study/tasks/quadrant'),
  bulkPlannedDate: (taskIds: number[], plannedDate: string | null) =>
    http.post<ApiResponse<void>>('/study/tasks/bulk-planned-date', { taskIds, plannedDate }),

  // Tags
  listTags: () => http.get<ApiResponse<Tag[]>>('/study/tags'),
  createTag: (data: CreateTagRequest) => http.post<ApiResponse<Tag>>('/study/tags', data),
  deleteTag: (id: number) => http.delete<ApiResponse<void>>(`/study/tags/${id}`),
}
