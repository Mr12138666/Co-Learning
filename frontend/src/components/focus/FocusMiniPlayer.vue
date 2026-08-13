<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { Play, Pause, Maximize2, Timer } from 'lucide-vue-next'
import { useFocusStore } from '@/stores/focusStore'
import { useStudyStore } from '@/stores/studyStore'
import { getErrorMessage } from '@/utils/http-error'

/**
 * Compact always-on focus indicator for the top toolbar.
 * Reflects the server-authoritative session; clicking expands to Today.
 */
const router = useRouter()
const focusStore = useFocusStore()
const studyStore = useStudyStore()

const message = useMessage()
const now = ref(Date.now())
const starting = ref(false)
let timer: ReturnType<typeof setInterval> | null = null

const elapsed = computed(() => {
  const s = focusStore.activeSession
  if (!s) return 0
  const started = new Date(s.startedAt).getTime()
  if (focusStore.isActive) return Math.max(0, Math.floor((now.value - started) / 1000) - s.pausedSeconds)
  if (focusStore.isPaused && s.pausedAt) {
    return Math.max(0, Math.floor((new Date(s.pausedAt).getTime() - started) / 1000) - s.pausedSeconds)
  }
  return 0
})

const clock = computed(() => {
  const total = elapsed.value
  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  const sec = total % 60
  const mm = String(m).padStart(2, '0')
  const ss = String(sec).padStart(2, '0')
  return h > 0 ? `${h}:${mm}:${ss}` : `${mm}:${ss}`
})

const subjectName = computed(() => {
  const id = focusStore.activeSession?.subjectId
  return id ? studyStore.subjectMap.get(id)?.name ?? null : null
})

async function togglePause() {
  if (focusStore.isActive) await focusStore.pause()
  else if (focusStore.isPaused) await focusStore.resume()
  now.value = Date.now()
}

async function startFocus() {
  starting.value = true
  try {
    await focusStore.start({})
    now.value = Date.now()
  } catch (e: unknown) {
    // Another session may already be active — reflect the server truth.
    await focusStore.fetchActive().catch(() => {})
    if (!focusStore.hasSession) message.error(getErrorMessage(e, '无法开始专注'))
  } finally {
    starting.value = false
  }
}

function expand() {
  router.push('/dashboard')
}

onMounted(() => {
  focusStore.fetchActive().catch(() => {})
  timer = setInterval(() => (now.value = Date.now()), 1000)
})
onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div v-if="focusStore.hasSession" class="mini-player glass--subtle" :class="{ 'is-paused': focusStore.isPaused, 'glow-brand': focusStore.isActive }" role="timer" aria-live="off">
    <button class="mini-player__toggle" type="button" :aria-label="focusStore.isActive ? '暂停专注' : '继续专注'" @click="togglePause">
      <Pause v-if="focusStore.isActive" :size="14" />
      <Play v-else :size="14" />
    </button>
    <button class="mini-player__body" type="button" aria-label="打开专注模式" @click="expand">
      <span class="mini-player__time tabular-nums">{{ clock }}</span>
      <span v-if="subjectName" class="mini-player__subject">{{ subjectName }}</span>
    </button>
    <button class="mini-player__expand" type="button" aria-label="展开为专注模式" @click="expand">
      <Maximize2 :size="13" />
    </button>
  </div>
  <button v-else class="mini-start" type="button" :disabled="starting" aria-label="开始专注" @click="startFocus">
    <Timer :size="15" />
    <span class="mini-start__label">专注</span>
  </button>
</template>

<style scoped>
.mini-player {
  display: flex;
  align-items: center;
  gap: 2px;
  height: 32px;
  padding: 0 4px 0 2px;
  border-radius: var(--radius-pill);
}
.mini-player.is-paused {
  border-color: var(--warning);
  box-shadow: 0 0 12px rgba(245, 158, 11, 0.12);
}
.mini-player__toggle,
.mini-player__expand {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px;
  height: 26px;
  border: none;
  background: transparent;
  border-radius: var(--radius-full);
  color: var(--text-color);
  cursor: pointer;
  transition: background-color var(--transition-fast);
}
.mini-player__toggle:hover,
.mini-player__expand:hover {
  background: var(--state-hover);
}
.mini-player__body {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 0 var(--sp-1);
  color: var(--text-color);
}
.mini-player__time {
  font-size: var(--text-sm);
  font-weight: var(--weight-semibold);
}
.mini-player__subject {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  max-width: 90px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.mini-start {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 32px;
  padding: 0 var(--sp-3);
  border-radius: var(--radius-pill);
  border: 1px solid var(--border-color);
  background: var(--surface-2);
  color: var(--text-color);
  font-size: var(--text-sm);
  cursor: pointer;
  transition: background-color var(--transition-fast), border-color var(--transition-fast);
}
.mini-start:hover:not(:disabled) { background: var(--state-hover); border-color: var(--brand); color: var(--brand); }
.mini-start:disabled { opacity: 0.5; cursor: default; }
.mini-start__label { font-weight: var(--weight-medium); }
</style>
