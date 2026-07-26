import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { focusApi, type ActiveFocusSession, type StartFocusRequest } from '@/api/focus'

export const useFocusStore = defineStore('focus', () => {
  // State
  const activeSession = ref<ActiveFocusSession | null>(null)
  const loading = ref(false)

  // Getters
  const isActive = computed(() => activeSession.value?.status === 'ACTIVE')
  const isPaused = computed(() => activeSession.value?.status === 'PAUSED')
  const hasSession = computed(() => !!activeSession.value)
  const sessionId = computed(() => activeSession.value?.sessionId ?? null)

  // Actions
  async function fetchActive() {
    try {
      const res = await focusApi.getActive()
      activeSession.value = res.data.data ?? null
    } catch {
      activeSession.value = null
    }
  }

  async function start(data: StartFocusRequest) {
    loading.value = true
    try {
      const res = await focusApi.start(data)
      const session = res.data.data
      activeSession.value = {
        sessionId: session.id,
        status: session.status,
        startedAt: session.startedAt,
        pausedAt: session.pausedAt,
        resumedAt: session.resumedAt,
        pausedSeconds: session.pausedSeconds,
        elapsedSeconds: session.elapsedSeconds,
        subjectId: session.subjectId ?? null,
        taskId: session.taskId ?? null,
        graceDeadline: session.graceDeadline ?? null,
        graceReason: session.graceReason ?? null,
      }
      return session
    } finally {
      loading.value = false
    }
  }

  async function pause() {
    if (!activeSession.value) return
    const res = await focusApi.pause(activeSession.value.sessionId)
    const session = res.data.data
    activeSession.value = {
      ...activeSession.value,
      status: session.status,
      pausedAt: session.pausedAt,
      pausedSeconds: session.pausedSeconds,
      elapsedSeconds: session.elapsedSeconds,
      graceDeadline: null,
      graceReason: null,
    }
  }

  async function resume() {
    if (!activeSession.value) return
    const res = await focusApi.resume(activeSession.value.sessionId)
    const session = res.data.data
    activeSession.value = {
      ...activeSession.value,
      status: session.status,
      resumedAt: session.resumedAt,
      pausedSeconds: session.pausedSeconds,
      elapsedSeconds: session.elapsedSeconds,
      graceDeadline: null,
      graceReason: null,
    }
  }

  async function finish() {
    if (!activeSession.value) return null
    const res = await focusApi.finish(activeSession.value.sessionId)
    activeSession.value = null
    return res.data.data
  }

  async function abort() {
    if (!activeSession.value) return
    try {
      await focusApi.abort(activeSession.value.sessionId)
    } finally {
      activeSession.value = null
    }
  }

  function clear() {
    activeSession.value = null
  }

  return {
    activeSession,
    loading,
    isActive,
    isPaused,
    hasSession,
    sessionId,
    fetchActive,
    start,
    pause,
    resume,
    finish,
    abort,
    clear,
  }
})
