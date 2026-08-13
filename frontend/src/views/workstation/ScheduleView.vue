<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useMessage } from 'naive-ui'
import FullCalendar from '@fullcalendar/vue3'
import type { CalendarOptions, EventDropArg, EventClickArg, DateSelectArg } from '@fullcalendar/core'
import type { EventResizeDoneArg } from '@fullcalendar/interaction'
import zhCn from '@fullcalendar/core/locales/zh-cn'
import dayGridPlugin from '@fullcalendar/daygrid'
import timeGridPlugin from '@fullcalendar/timegrid'
import interactionPlugin from '@fullcalendar/interaction'
import dayjs from 'dayjs'
import TaskEditorDrawer from '@/components/task/TaskEditorDrawer.vue'
import StateError from '@/components/common/StateError.vue'
import StateLoading from '@/components/common/StateLoading.vue'
import { useTaskStore } from '@/stores/taskStore'
import { useStudyStore } from '@/stores/studyStore'
import { usePageLoad } from '@/composables/usePageLoad'
import { getErrorMessage } from '@/utils/http-error'
import type { StudyTask } from '@/api/study'

const taskStore = useTaskStore()
const studyStore = useStudyStore()
const message = useMessage()
const { loading, error, load } = usePageLoad()

const tasks = ref<StudyTask[]>([])
const editorShow = ref(false)
const editingTask = ref<StudyTask | null>(null)

const events = computed(() =>
  tasks.value
    .filter((t) => t.scheduledStart)
    .map((t) => ({
      id: String(t.id),
      title: t.title,
      start: t.scheduledStart as string,
      end: t.scheduledEnd ?? undefined,
      backgroundColor: t.subjectColor ?? 'var(--brand)',
      borderColor: t.subjectColor ?? 'var(--brand)',
      classNames: t.status === 'DONE' ? ['fc-done'] : [],
    })),
)

async function fetchAll() {
  if (studyStore.subjects.length === 0) await studyStore.fetchSubjects().catch(() => {})
  tasks.value = await taskStore.fetchAllTasks()
}
function reload() { load(fetchAll) }

async function persistSchedule(id: number, start: Date | null, end: Date | null) {
  const task = tasks.value.find((t) => t.id === id)
  const prevStart = task?.scheduledStart ?? null
  const prevEnd = task?.scheduledEnd ?? null
  const startIso = start ? start.toISOString() : null
  const endIso = end ? end.toISOString() : null
  if (task) {
    task.scheduledStart = startIso
    task.scheduledEnd = endIso
    if (start) task.plannedDate = dayjs(start).format('YYYY-MM-DD')
  }
  try {
    await taskStore.updateTask(id, {
      scheduledStart: startIso,
      scheduledEnd: endIso,
      plannedDate: start ? dayjs(start).format('YYYY-MM-DD') : task?.plannedDate ?? null,
    })
  } catch (e) {
    if (task) { task.scheduledStart = prevStart; task.scheduledEnd = prevEnd }
    message.error(getErrorMessage(e, '更新时间失败'))
    reload()
  }
}

function onEventDrop(arg: EventDropArg) {
  persistSchedule(Number(arg.event.id), arg.event.start, arg.event.end)
}
function onEventResize(arg: EventResizeDoneArg) {
  persistSchedule(Number(arg.event.id), arg.event.start, arg.event.end)
}
function onEventClick(arg: EventClickArg) {
  const task = tasks.value.find((t) => t.id === Number(arg.event.id))
  if (task) { editingTask.value = task; editorShow.value = true }
}
async function onSelect(arg: DateSelectArg) {
  try {
    const created = await taskStore.createTask({
      title: '新任务',
      scheduledStart: arg.start.toISOString(),
      scheduledEnd: arg.end.toISOString(),
      plannedDate: dayjs(arg.start).format('YYYY-MM-DD'),
    })
    tasks.value.push(created)
    editingTask.value = created
    editorShow.value = true
  } catch (e) {
    message.error(getErrorMessage(e, '创建任务失败'))
  }
}

const calendarOptions = computed<CalendarOptions>(() => ({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'timeGridWeek',
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    right: 'timeGridDay,timeGridWeek,dayGridMonth',
  },
  locale: zhCn,
  firstDay: 1,
  nowIndicator: true,
  editable: true,
  selectable: true,
  selectMirror: true,
  allDaySlot: false,
  slotMinTime: '06:00:00',
  slotMaxTime: '24:00:00',
  height: '100%',
  expandRows: true,
  events: events.value,
  eventDrop: onEventDrop,
  eventResize: onEventResize,
  eventClick: onEventClick,
  select: onSelect,
}))

onMounted(reload)
</script>

<template>
  <div class="schedule-view gradient-mesh">
    <StateLoading v-if="loading" text="加载日程…" />
    <StateError v-else-if="error" :description="error" @retry="reload" />
    <div v-else class="schedule-calendar glass">
      <FullCalendar :options="calendarOptions" />
    </div>
    <TaskEditorDrawer v-model:show="editorShow" :task="editingTask" @saved="reload" @deleted="reload" />
  </div>
</template>

<style scoped>
.schedule-view { height: 100%; padding: var(--sp-4); display: flex; flex-direction: column; overflow: hidden; }
.schedule-calendar { flex: 1; min-height: 0; padding: var(--sp-3); border-radius: var(--radius-lg); overflow: hidden; }

/* Map FullCalendar to the design system */
.schedule-calendar :deep(.fc) {
  --fc-border-color: var(--divider);
  --fc-page-bg-color: transparent;
  --fc-neutral-bg-color: var(--surface-1);
  --fc-today-bg-color: var(--state-selected);
  --fc-now-indicator-color: var(--danger);
  --fc-event-border-color: transparent;
  font-family: var(--font-family);
  font-size: var(--text-sm);
  color: var(--text-color);
}
.schedule-calendar :deep(.fc .fc-toolbar-title) { font-size: var(--text-lg); font-weight: var(--weight-semibold); color: var(--text-color-strong); }
.schedule-calendar :deep(.fc .fc-button) {
  background: var(--surface-2); border: 1px solid var(--border-color); color: var(--text-color);
  box-shadow: none; text-transform: none; font-size: var(--text-sm); padding: 4px 10px;
}
.schedule-calendar :deep(.fc .fc-button:hover) { background: var(--state-hover); }
.schedule-calendar :deep(.fc .fc-button-primary:not(:disabled).fc-button-active) { background: var(--brand); border-color: var(--brand); color: var(--ink-on-accent); }
.schedule-calendar :deep(.fc .fc-col-header-cell-cushion),
.schedule-calendar :deep(.fc .fc-timegrid-slot-label-cushion) { color: var(--text-color-muted); }
.schedule-calendar :deep(.fc-event) { border-radius: var(--radius-xs); padding: 1px 4px; cursor: pointer; font-size: var(--text-xs); line-height: var(--leading-snug); }
.schedule-calendar :deep(.fc-event .fc-event-title) { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-height: 2.6em; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.schedule-calendar :deep(.fc-event.fc-done) { opacity: 0.5; text-decoration: line-through; }
</style>
