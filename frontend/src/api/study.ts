import http from './http'

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

export interface StudyTask {
  id: number
  subjectId: number | null
  subjectName: string | null
  subjectColor: string | null
  examGoalId: number | null
  title: string
  description: string | null
  status: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'ARCHIVED'
  dueDate: string | null
  sortOrder: number
  createdAt: string
  updatedAt: string
}

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
}

export interface UpdateTaskRequest {
  title?: string
  description?: string
  status?: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'ARCHIVED'
  dueDate?: string
  subjectId?: number
  sortOrder?: number
}

// ===== API =====

export const studyApi = {
  // Stats
  getStats: () => http.get('/stats'),

  // Goals
  listGoals: () => http.get('/study/goals'),
  createGoal: (data: CreateExamGoalRequest) => http.post('/study/goals', data),
  updateGoal: (id: number, data: UpdateExamGoalRequest) => http.put(`/study/goals/${id}`, data),
  deleteGoal: (id: number) => http.delete(`/study/goals/${id}`),

  // Subjects
  listSubjects: () => http.get('/study/subjects'),
  createSubject: (data: CreateSubjectRequest) => http.post('/study/subjects', data),
  updateSubject: (id: number, data: UpdateSubjectRequest) => http.put(`/study/subjects/${id}`, data),
  deleteSubject: (id: number) => http.delete(`/study/subjects/${id}`),

  // Tasks
  listTasks: (params?: { status?: string; subjectId?: number }) =>
    http.get('/study/tasks', { params }),
  createTask: (data: CreateTaskRequest) => http.post('/study/tasks', data),
  updateTask: (id: number, data: UpdateTaskRequest) => http.put(`/study/tasks/${id}`, data),
  deleteTask: (id: number) => http.delete(`/study/tasks/${id}`),
}
