<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NButton, NInput, NTooltip, useMessage } from 'naive-ui'
import { ChevronLeft, ChevronRight, CalendarDays } from 'lucide-vue-next'
import draggable from 'vuedraggable'
import dayjs, { type Dayjs } from 'dayjs'
import TaskRow from '@/components/task/TaskRow.vue'
import TaskEditorDrawer from '@/components/task/TaskEditorDrawer.vue'
import StateError from '@/components/common/StateError.vue'
import StateLoading from '@/components/common/StateLoading.vue'
import { useTaskStore } from '@/stores/taskStore'
import { usePageLoad } from '@/composables/usePageLoad'
import { getErrorMessage } from '@/utils/http-error'
import type { StudyTask } from '@/api/study'

const taskStore = useTaskStore()
const message = useMessage()
const { loading, error, load } = usePageLoad()

const DAY_COUNT = 7
const DAILY_CAPACITY_MIN = 8 * 60

const weekStart = ref<Dayjs>(dayjs().startOf('day'))
const overdue = ref<StudyTask[]>([])
const dayTasks = ref<Record<string, StudyTask[]>>({})

const quickInputs = ref<Record<string, string>>({})

const editorShow = ref(false)
const editingTask = ref<StudyTask | null>(null)
const editorDate = ref<string | null>(null)

const days = computed(() =>
  Array.from({ length: DAY_COUNT }, (_, i) => {
    const d = weekStart.value.add(i, 'day')
    return { key: d.format('YYYY-MM-DD'), date: d, isToday: d.isSame(dayjs(), 'day') }
  }),
)

const rangeLabel = computed(() => {
  const end = weekStart.value.add(DAY_COUNT - 1, 'day')
  return `${weekStart.value.format('M月D日')} – ${end.format('M月D日')}`
})

function plannedMinutes(tasks: StudyTask[]): number {
  return tasks.reduce((sum, t) => sum + (t.estimatedMinutes ?? 0), 0)
}
function capacityLabel(tasks: StudyTask[]): string {
  const planned = plannedMinutes(tasks)
  const available = Math.max(0, DAILY_CAPACITY_MIN - planned)
  const fmt = (m: number) => (m >= 60 ? `${Math.floor(m / 60)}h${m % 60 ? m % 60 + 'm' : ''}` : `${m}m`)
  return `已规划 ${fmt(planned)} / 剩余 ${fmt(available)}`
}

async function fetchAll() {
  const start = weekStart.value.format('YYYY-MM-DD')
  const end = weekStart.value.add(DAY_COUNT - 1, 'day').format('YYYY-MM-DD')
  const [planner, over] = await Promise.all([taskStore.fetchPlanner(start, end), taskStore.fetchOverdue()])
  const map: Record<string, StudyTask[]> = {}
  for (const day of days.value) map[day.key] = []
  for (const task of planner) {
    if (task.plannedDate && map[task.plannedDate]) map[task.plannedDate].push(task)
  }
  dayTasks.value = map
  overdue.value = over
}
function reload() { load(fetchAll) }

function shift(dir: number) {
  weekStart.value = weekStart.value.add(dir * DAY_COUNT, 'day')
  reload()
}
function goToday() {
  weekStart.value = dayjs().startOf('day')
  reload()
}

// Drag persistence: when a task is added to a day column, persist its planned date.
async function onDayAdd(dateKey: string, evt: { added?: { element: StudyTask } }) {
  const el = evt.added?.element
  if (!el) return
  const prev = el.plannedDate
  el.plannedDate = dateKey
  try {
    await taskStore.setPlannedDate(el.id, dateKey)
  } catch (e) {
    el.plannedDate = prev
    message.error(getErrorMessage(e, '移动失败'))
    reload()
  }
}

async function addQuick(dateKey: string) {
  const title = (quickInputs.value[dateKey] ?? '').trim()
  if (!title) return
  try {
    const created = await taskStore.createTask({ title, plannedDate: dateKey })
    dayTasks.value[dateKey].push(created)
    quickInputs.value[dateKey] = ''
  } catch (e) { message.error(getErrorMessage(e, '创建失败')) }
}

async function toggle(task: StudyTask) {
  const prev = task.status
  task.status = task.status === 'DONE' ? 'TODO' : 'DONE'
  try { await taskStore.toggleDone({ ...task, status: prev }) }
  catch (e) { task.status = prev; message.error(getErrorMessage(e, '更新失败')) }
}
function openEditor(task: StudyTask | null, dateKey: string | null = null) {
  editingTask.value = task
  editorDate.value = dateKey
  editorShow.value = true
}
async function remove(task: StudyTask) {
  try {
    await taskStore.deleteTask(task.id)
    reload()
  } catch (e) { message.error(getErrorMessage(e, '删除失败')) }
}

onMounted(reload)
</script>

<template>
  <div class="planner-view gradient-mesh">
    <header class="planner-head glass section-card">
      <h2 class="planner-head__title section-card-title gradient-brand-text"><CalendarDays :size="16" /> Planner</h2>
      <div class="planner-head__nav">
        <NTooltip placement="top">
          <template #trigger>
            <NButton size="small" quaternary class="pill" @click="shift(-1)"><template #icon><ChevronLeft :size="16" /></template></NButton>
          </template>
          上一周
        </NTooltip>
        <NButton size="small" class="pill" @click="goToday">今天</NButton>
        <NTooltip placement="top">
          <template #trigger>
            <NButton size="small" quaternary class="pill" @click="shift(1)"><template #icon><ChevronRight :size="16" /></template></NButton>
          </template>
          下一周
        </NTooltip>
        <span class="planner-head__range">{{ rangeLabel }}</span>
      </div>
    </header>

    <StateLoading v-if="loading" text="加载规划…" />
    <StateError v-else-if="error" :description="error" @retry="reload" />

    <div v-else class="planner-board">
      <!-- Overdue column (drag out only) -->
      <section class="planner-col planner-col--overdue glass stagger-in">
        <div class="planner-col__head">
          <span class="planner-col__title">逾期</span>
          <span class="planner-col__cap">{{ overdue.length }} 项</span>
        </div>
        <draggable
          v-model="overdue"
          :group="{ name: 'planner', pull: true, put: false }"
          item-key="id"
          class="planner-col__list"
          :animation="150"
        >
          <template #item="{ element }">
            <TaskRow :task="element" draggable card @toggle="toggle" @open="(t) => openEditor(t)" @edit="(t) => openEditor(t)" @delete="remove" />
          </template>
        </draggable>
        <p v-if="!overdue.length" class="planner-col__empty">无逾期任务</p>
      </section>

      <!-- Day columns -->
      <section v-for="day in days" :key="day.key" class="planner-col glass stagger-in" :class="{ 'planner-col--today': day.isToday, 'glow-brand': day.isToday }">
        <div class="planner-col__head">
          <div class="planner-col__daylabel badge badge--brand">
            <span class="planner-col__weekday">{{ day.date.format('ddd') }}</span>
            <span class="planner-col__date">{{ day.date.format('M/D') }}</span>
          </div>
          <span class="planner-col__cap">{{ capacityLabel(dayTasks[day.key] || []) }}</span>
        </div>
        <draggable
          :list="dayTasks[day.key]"
          group="planner"
          item-key="id"
          class="planner-col__list"
          :animation="150"
          @change="(e: any) => onDayAdd(day.key, e)"
        >
          <template #item="{ element }">
            <TaskRow :task="element" draggable card :show-planned-date="false" @toggle="toggle" @open="(t) => openEditor(t, day.key)" @edit="(t) => openEditor(t, day.key)" @delete="remove" />
          </template>
        </draggable>
        <div class="planner-col__add">
          <NInput
            v-model:value="quickInputs[day.key]"
            size="tiny"
            placeholder="+ 添加任务"
            @keyup.enter="addQuick(day.key)"
          />
        </div>
      </section>
    </div>

    <TaskEditorDrawer v-model:show="editorShow" :task="editingTask" :default-planned-date="editorDate" @saved="reload" @deleted="reload" />
  </div>
</template>

<style scoped>
.planner-view { display: flex; flex-direction: column; gap: var(--sp-2); height: 100%; padding: var(--sp-4); }
.planner-head { display: flex; align-items: center; justify-content: space-between; flex-shrink: 0; padding: var(--sp-2) var(--sp-3); border-radius: var(--radius-lg); }
.planner-head__title { display: flex; align-items: center; gap: var(--sp-1); font-size: var(--text-lg); margin: 0; }
.planner-head__nav { display: flex; align-items: center; gap: var(--sp-1); }
.planner-head__nav :deep(.n-button) {
  height: 30px;
  padding: 0 var(--sp-3);
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: var(--text-color);
}
.dark .planner-head__nav :deep(.n-button) {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.08);
}
.planner-head__nav :deep(.n-button:hover) {
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  border-color: rgba(59, 130, 246, 0.3);
}
.planner-head__range { font-size: var(--text-sm); color: var(--text-color-muted); margin-left: var(--sp-1); }

.planner-board { display: flex; gap: var(--sp-2); overflow-x: auto; flex: 1; min-height: 0; padding-bottom: var(--sp-1); }
.planner-col {
  display: flex; flex-direction: column; gap: var(--sp-1);
  min-width: 240px; width: 240px; flex-shrink: 0;
  border-radius: var(--radius-lg);
  padding: var(--sp-2) var(--sp-3);
}
.planner-col--overdue { border-color: rgba(244, 67, 54, 0.4); }
.planner-col--today { border-color: rgba(59, 130, 246, 0.45); }
.planner-col__head { display: flex; flex-direction: column; gap: 1px; padding: var(--sp-1) 0 var(--sp-1); border-bottom: 1px solid var(--divider); }
.planner-col__daylabel { display: flex; align-items: center; gap: var(--sp-1); }
.planner-col__weekday { font-size: var(--text-sm); font-weight: var(--weight-semibold); color: var(--text-color-strong); }
.planner-col__date { font-size: var(--text-xs); color: var(--text-color-muted); }
.planner-col__title { font-size: var(--text-sm); font-weight: var(--weight-semibold); }
.planner-col__cap { font-size: var(--text-xs); color: var(--text-color-muted); }
.planner-col__list { display: flex; flex-direction: column; gap: 2px; flex: 1; min-height: 40px; overflow-y: auto; }
.planner-col__empty { font-size: var(--text-xs); color: var(--text-color-muted); text-align: center; padding: var(--sp-2) 0; }
.planner-col__add { flex-shrink: 0; }

@media (max-width: 768px) {
  .planner-col { min-width: 82vw; width: 82vw; scroll-snap-align: start; }
  .planner-board { scroll-snap-type: x mandatory; }
}
</style>
