import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  studyApi,
  type StudyTask,
  type Tag,
  type TaskStatus,
  type QuadrantMap,
  type CreateTaskRequest,
  type UpdateTaskRequest,
} from '@/api/study'

/**
 * Single source of truth for the productivity workstation's tasks and tags.
 * Every view (Today / Inbox / Planner / Schedule / Boards) shares this store
 * and the same StudyTask entity — no parallel task data.
 */
export const useTaskStore = defineStore('task', () => {
  // ===== Tags (shared across sidebar + task editing) =====
  const tags = ref<Tag[]>([])
  const tagMap = computed(() => {
    const m = new Map<number, Tag>()
    tags.value.forEach((t) => m.set(t.id, t))
    return m
  })

  async function loadTags() {
    const res = await studyApi.listTags()
    tags.value = res.data.data
  }

  async function createTag(name: string, color?: string) {
    const res = await studyApi.createTag({ name, color })
    tags.value.push(res.data.data)
    return res.data.data
  }

  async function deleteTag(id: number) {
    await studyApi.deleteTag(id)
    tags.value = tags.value.filter((t) => t.id !== id)
  }

  // ===== Task view fetchers (each view owns its own list) =====
  async function fetchInbox() {
    return (await studyApi.listInboxTasks()).data.data
  }
  async function fetchToday() {
    return (await studyApi.listTodayTasks()).data.data
  }
  async function fetchOverdue() {
    return (await studyApi.listOverdueTasks()).data.data
  }
  async function fetchPlanner(startDate: string, endDate: string) {
    return (await studyApi.listPlannerTasks(startDate, endDate)).data.data
  }
  async function fetchQuadrant(): Promise<QuadrantMap> {
    return (await studyApi.listQuadrant()).data.data
  }
  async function fetchAllTasks(params?: { status?: string; subjectId?: number }) {
    return (await studyApi.listTasks(params)).data.data
  }
  async function fetchScheduled(startDate: string, endDate: string) {
    // Scheduled tasks live within the planner date range and carry scheduledStart.
    const tasks = await fetchPlanner(startDate, endDate)
    return tasks.filter((t) => t.scheduledStart)
  }

  // ===== Mutations =====
  async function createTask(data: CreateTaskRequest) {
    return (await studyApi.createTask(data)).data.data
  }

  async function updateTask(id: number, data: UpdateTaskRequest) {
    return (await studyApi.updateTask(id, data)).data.data
  }

  async function deleteTask(id: number) {
    await studyApi.deleteTask(id)
  }

  async function setStatus(id: number, status: TaskStatus) {
    return updateTask(id, { status })
  }

  /** Toggle between DONE and TODO. */
  async function toggleDone(task: StudyTask) {
    return setStatus(task.id, task.status === 'DONE' ? 'TODO' : 'DONE')
  }

  async function setPlannedDate(id: number, plannedDate: string | null) {
    return updateTask(id, { plannedDate })
  }

  async function bulkSetPlannedDate(taskIds: number[], plannedDate: string | null) {
    if (taskIds.length === 0) return
    await studyApi.bulkPlannedDate(taskIds, plannedDate)
  }

  async function setQuadrant(id: number, urgent: boolean, important: boolean) {
    return updateTask(id, { urgent, important })
  }

  async function setSchedule(id: number, scheduledStart: string | null, scheduledEnd: string | null) {
    return updateTask(id, { scheduledStart, scheduledEnd })
  }

  return {
    tags,
    tagMap,
    loadTags,
    createTag,
    deleteTag,
    fetchInbox,
    fetchToday,
    fetchOverdue,
    fetchPlanner,
    fetchQuadrant,
    fetchAllTasks,
    fetchScheduled,
    createTask,
    updateTask,
    deleteTask,
    setStatus,
    toggleDone,
    setPlannedDate,
    bulkSetPlannedDate,
    setQuadrant,
    setSchedule,
  }
})
