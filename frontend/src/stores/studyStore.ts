import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  studyApi,
  type ExamGoal,
  type Subject,
  type StudyTask,
  type CreateExamGoalRequest,
  type UpdateExamGoalRequest,
  type CreateSubjectRequest,
  type UpdateSubjectRequest,
  type CreateTaskRequest,
  type UpdateTaskRequest,
} from '@/api/study'

export const useStudyStore = defineStore('study', () => {
  // State
  const goals = ref<ExamGoal[]>([])
  const subjects = ref<Subject[]>([])
  const tasks = ref<StudyTask[]>([])
  const loading = ref(false)

  // Getters
  const activeGoals = computed(() => goals.value.filter((g) => g.status === 'ACTIVE'))
  const subjectMap = computed(() => {
    const map = new Map<number, Subject>()
    subjects.value.forEach((s) => map.set(s.id, s))
    return map
  })
  const todoTasks = computed(() => tasks.value.filter((t) => t.status === 'TODO'))
  const inProgressTasks = computed(() => tasks.value.filter((t) => t.status === 'IN_PROGRESS'))
  const doneTasks = computed(() => tasks.value.filter((t) => t.status === 'DONE'))

  // Actions: Goals
  async function fetchGoals() {
    const res = await studyApi.listGoals()
    goals.value = res.data.data
  }

  async function createGoal(data: CreateExamGoalRequest) {
    const res = await studyApi.createGoal(data)
    goals.value.push(res.data.data)
    return res.data.data
  }

  async function updateGoal(id: number, data: UpdateExamGoalRequest) {
    const res = await studyApi.updateGoal(id, data)
    const index = goals.value.findIndex((g) => g.id === id)
    if (index !== -1) goals.value[index] = res.data.data
    return res.data.data
  }

  async function deleteGoal(id: number) {
    await studyApi.deleteGoal(id)
    goals.value = goals.value.filter((g) => g.id !== id)
  }

  // Actions: Subjects
  async function fetchSubjects() {
    const res = await studyApi.listSubjects()
    subjects.value = res.data.data
  }

  async function createSubject(data: CreateSubjectRequest) {
    const res = await studyApi.createSubject(data)
    subjects.value.push(res.data.data)
    return res.data.data
  }

  async function updateSubject(id: number, data: UpdateSubjectRequest) {
    const res = await studyApi.updateSubject(id, data)
    const index = subjects.value.findIndex((s) => s.id === id)
    if (index !== -1) subjects.value[index] = res.data.data
    return res.data.data
  }

  async function deleteSubject(id: number) {
    await studyApi.deleteSubject(id)
    subjects.value = subjects.value.filter((s) => s.id !== id)
    tasks.value = tasks.value.filter((t) => t.subjectId !== id)
  }

  // Actions: Tasks
  async function fetchTasks(params?: { status?: string; subjectId?: number }) {
    const res = await studyApi.listTasks(params)
    tasks.value = res.data.data
  }

  async function createTask(data: CreateTaskRequest) {
    const res = await studyApi.createTask(data)
    tasks.value.unshift(res.data.data)
    return res.data.data
  }

  async function updateTask(id: number, data: UpdateTaskRequest) {
    const res = await studyApi.updateTask(id, data)
    const index = tasks.value.findIndex((t) => t.id === id)
    if (index !== -1) tasks.value[index] = res.data.data
    return res.data.data
  }

  async function deleteTask(id: number) {
    await studyApi.deleteTask(id)
    tasks.value = tasks.value.filter((t) => t.id !== id)
  }

  // Fetch all study data at once
  async function fetchAll() {
    loading.value = true
    try {
      await Promise.all([fetchGoals(), fetchSubjects(), fetchTasks()])
    } finally {
      loading.value = false
    }
  }

  return {
    goals,
    subjects,
    tasks,
    loading,
    activeGoals,
    subjectMap,
    todoTasks,
    inProgressTasks,
    doneTasks,
    fetchGoals,
    createGoal,
    updateGoal,
    deleteGoal,
    fetchSubjects,
    createSubject,
    updateSubject,
    deleteSubject,
    fetchTasks,
    createTask,
    updateTask,
    deleteTask,
    fetchAll,
  }
})
