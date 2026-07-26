<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search, CornerDownLeft } from 'lucide-vue-next'
import dayjs from 'dayjs'
import { useTaskStore } from '@/stores/taskStore'
import type { StudyTask } from '@/api/study'

const props = defineProps<{ show: boolean }>()
const emit = defineEmits<{ 'update:show': [value: boolean] }>()

const router = useRouter()
const taskStore = useTaskStore()

const query = ref('')
const activeIndex = ref(0)
const inputRef = ref<HTMLInputElement | null>(null)
const allTasks = ref<StudyTask[]>([])

const navItems = [
  { type: 'nav' as const, label: 'Today', to: '/today', keywords: 'today 今日 首页' },
  { type: 'nav' as const, label: 'Inbox 收件箱', to: '/inbox', keywords: 'inbox 收件箱 未规划' },
  { type: 'nav' as const, label: 'Planner 规划器', to: '/planner', keywords: 'planner 规划 计划' },
  { type: 'nav' as const, label: 'Schedule 日程', to: '/schedule', keywords: 'schedule 日历 日程' },
  { type: 'nav' as const, label: 'Boards 看板', to: '/boards', keywords: 'boards 看板 矩阵 kanban' },
  { type: 'nav' as const, label: '每日复盘', to: '/checkin', keywords: 'checkin 复盘 打卡' },
  { type: 'nav' as const, label: '学习统计', to: '/stats', keywords: 'stats 统计 图表' },
  { type: 'nav' as const, label: '自习室', to: '/rooms', keywords: 'rooms 自习室 房间' },
  { type: 'nav' as const, label: '日志', to: '/journals', keywords: 'journal 日志' },
  { type: 'nav' as const, label: '排行榜', to: '/leaderboard', keywords: 'leaderboard 排行' },
  { type: 'nav' as const, label: '宠物', to: '/pet', keywords: 'pet 宠物' },
  { type: 'nav' as const, label: '成就', to: '/achievements', keywords: 'achievements 成就' },
]

type Result =
  | { type: 'nav'; label: string; to: string }
  | { type: 'task'; label: string; task: StudyTask }

const results = computed<Result[]>(() => {
  const q = query.value.trim().toLowerCase()
  const navMatches = navItems
    .filter((n) => !q || n.label.toLowerCase().includes(q) || n.keywords.includes(q))
    .map((n) => ({ type: 'nav' as const, label: n.label, to: n.to }))
  const taskMatches = q
    ? allTasks.value
        .filter((t) => t.title.toLowerCase().includes(q))
        .slice(0, 8)
        .map((t) => ({ type: 'task' as const, label: t.title, task: t }))
    : []
  return [...navMatches, ...taskMatches]
})

function taskRoute(task: StudyTask): string {
  if (task.plannedDate === dayjs().format('YYYY-MM-DD')) return '/today'
  if (!task.plannedDate) return '/inbox'
  return '/planner'
}

function select(item?: Result) {
  const target = item ?? results.value[activeIndex.value]
  if (!target) return
  router.push(target.type === 'nav' ? target.to : taskRoute(target.task))
  close()
}

function close() {
  emit('update:show', false)
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'ArrowDown') { e.preventDefault(); activeIndex.value = Math.min(activeIndex.value + 1, results.value.length - 1) }
  else if (e.key === 'ArrowUp') { e.preventDefault(); activeIndex.value = Math.max(activeIndex.value - 1, 0) }
  else if (e.key === 'Enter') { e.preventDefault(); select() }
  else if (e.key === 'Escape') { e.preventDefault(); close() }
}

watch(() => props.show, async (open) => {
  if (!open) return
  query.value = ''
  activeIndex.value = 0
  taskStore.fetchAllTasks().then((t) => (allTasks.value = t)).catch(() => {})
  await nextTick()
  inputRef.value?.focus()
})
watch(query, () => (activeIndex.value = 0))
</script>

<template>
  <Teleport to="body">
    <div v-if="show" class="cmdk" role="dialog" aria-label="命令面板" @click.self="close">
      <div class="cmdk__panel">
        <div class="cmdk__search">
          <Search :size="17" class="cmdk__search-icon" />
          <input
            ref="inputRef"
            v-model="query"
            class="cmdk__input"
            placeholder="搜索页面或任务…"
            aria-label="搜索"
            @keydown="onKeydown"
          />
          <kbd class="cmdk__kbd">Esc</kbd>
        </div>
        <ul class="cmdk__list">
          <li
            v-for="(r, i) in results"
            :key="r.type + '-' + (r.type === 'nav' ? r.to : r.task.id)"
            class="cmdk__item"
            :class="{ 'is-active': i === activeIndex }"
            @mouseenter="activeIndex = i"
            @click="select(r)"
          >
            <span class="cmdk__item-kind">{{ r.type === 'nav' ? '页面' : '任务' }}</span>
            <span class="cmdk__item-label">{{ r.label }}</span>
            <CornerDownLeft v-if="i === activeIndex" :size="13" class="cmdk__item-enter" />
          </li>
          <li v-if="!results.length" class="cmdk__empty">没有匹配结果</li>
        </ul>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.cmdk {
  position: fixed; inset: 0; z-index: var(--z-modal);
  background: var(--scrim);
  display: flex; align-items: flex-start; justify-content: center;
  padding-top: 12vh;
  animation: fadeIn var(--duration-sm) var(--ease-enter);
}
.cmdk__panel {
  width: min(560px, 92vw);
  background: var(--bg-elevated);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-4);
  overflow: hidden;
  animation: scaleIn var(--duration-sm) var(--ease-enter);
}
.cmdk__search { display: flex; align-items: center; gap: var(--sp-2); padding: var(--sp-3) var(--sp-4); border-bottom: 1px solid var(--divider); }
.cmdk__search-icon { color: var(--text-color-muted); flex-shrink: 0; }
.cmdk__input { flex: 1; border: none; background: transparent; outline: none; color: var(--text-color); font-size: var(--text-lg); font-family: inherit; }
.cmdk__input::placeholder { color: var(--text-color-muted); }
.cmdk__kbd { font-size: var(--text-xs); color: var(--text-color-muted); border: 1px solid var(--border-color); border-radius: var(--radius-xs); padding: 1px 5px; }
.cmdk__list { list-style: none; margin: 0; padding: var(--sp-1); max-height: 50vh; overflow-y: auto; }
.cmdk__item { display: flex; align-items: center; gap: var(--sp-2); padding: var(--sp-2) var(--sp-3); border-radius: var(--radius-sm); cursor: pointer; }
.cmdk__item.is-active { background: var(--state-selected); }
.cmdk__item-kind { font-size: var(--text-xs); color: var(--text-color-muted); background: var(--state-hover); border-radius: var(--radius-xs); padding: 1px 6px; flex-shrink: 0; }
.cmdk__item-label { flex: 1; font-size: var(--text-base); color: var(--text-color); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.cmdk__item-enter { color: var(--text-color-muted); }
.cmdk__empty { padding: var(--sp-4); text-align: center; color: var(--text-color-muted); font-size: var(--text-sm); }
</style>
