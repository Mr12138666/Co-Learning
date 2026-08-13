<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { NInput, NButton, NTooltip, useMessage } from 'naive-ui'
import { ChevronRight, Sun, CheckCheck } from 'lucide-vue-next'
import dayjs from 'dayjs'
import TaskRow from '@/components/task/TaskRow.vue'
import TaskEditorDrawer from '@/components/task/TaskEditorDrawer.vue'
import StateError from '@/components/common/StateError.vue'
import StateLoading from '@/components/common/StateLoading.vue'
import StateEmpty from '@/components/common/StateEmpty.vue'
import { useTaskStore } from '@/stores/taskStore'
import { useDashboardStore } from '@/stores/dashboardStore'
import { usePageLoad } from '@/composables/usePageLoad'
import { getErrorMessage } from '@/utils/http-error'
import { formatDuration } from '@/utils/format'
import type { StudyTask } from '@/api/study'

const taskStore = useTaskStore()
const dashboardStore = useDashboardStore()
const message = useMessage()
const { loading, error, load } = usePageLoad()

const todayTasks = ref<StudyTask[]>([])
const overdueTasks = ref<StudyTask[]>([])
const overdueOpen = ref(true)
const quickTitle = ref('')
const today = dayjs().format('YYYY-MM-DD')

const editorShow = ref(false)
const editingTask = ref<StudyTask | null>(null)

const dateLabel = computed(() => dayjs().format('M月D日 ddd'))
const remaining = computed(() => todayTasks.value.filter((t) => t.status !== 'DONE').length)

const summary = computed(() => {
  const s = dashboardStore.stats
  return [
    { label: '今日专注', value: formatDuration(s?.todayFocusSeconds ?? 0) },
    { label: '本周专注', value: formatDuration(s?.weekFocusSeconds ?? 0) },
    { label: '连续天数', value: `${s?.streakDays ?? 0} 天` },
    { label: '待完成', value: `${remaining.value} 项` },
  ]
})

async function fetchAll() {
  const [t, o] = await Promise.all([
    taskStore.fetchToday(),
    taskStore.fetchOverdue(),
    dashboardStore.fetchStats().catch(() => {}),
  ])
  todayTasks.value = t
  overdueTasks.value = o
}

function reload() {
  load(fetchAll)
}

async function toggle(task: StudyTask) {
  const prev = task.status
  task.status = task.status === 'DONE' ? 'TODO' : 'DONE'
  try {
    await taskStore.toggleDone({ ...task, status: prev })
  } catch (e) {
    task.status = prev
    message.error(getErrorMessage(e, '更新失败'))
  }
}

async function addQuick() {
  const title = quickTitle.value.trim()
  if (!title) return
  try {
    const created = await taskStore.createTask({ title, plannedDate: today })
    todayTasks.value.unshift(created)
    quickTitle.value = ''
  } catch (e) {
    message.error(getErrorMessage(e, '创建失败'))
  }
}

async function addAllOverdue() {
  const ids = overdueTasks.value.map((t) => t.id)
  if (!ids.length) return
  try {
    await taskStore.bulkSetPlannedDate(ids, today)
    todayTasks.value.push(...overdueTasks.value.map((t) => ({ ...t, plannedDate: today })))
    overdueTasks.value = []
    message.success('逾期任务已加入今天')
  } catch (e) {
    message.error(getErrorMessage(e, '操作失败'))
  }
}

function openEditor(task: StudyTask | null) {
  editingTask.value = task
  editorShow.value = true
}

function onSaved() {
  reload()
}
function onDeleted(id: number) {
  todayTasks.value = todayTasks.value.filter((t) => t.id !== id)
  overdueTasks.value = overdueTasks.value.filter((t) => t.id !== id)
}
async function remove(task: StudyTask) {
  try {
    await taskStore.deleteTask(task.id)
    onDeleted(task.id)
  } catch (e) {
    message.error(getErrorMessage(e, '删除失败'))
  }
}

onMounted(reload)
</script>

<template>
  <div class="today-view gradient-mesh">
    <header class="today-head glass section-card">
      <div>
        <h2 class="today-head__title section-card-title"><Sun :size="16" /> Today</h2>
        <p class="today-head__date">{{ dateLabel }}</p>
      </div>
    </header>

    <!-- Compact summary bar -->
    <div class="summary-bar">
      <div v-for="item in summary" :key="item.label" class="summary-bar__item glass interactive stagger-in">
        <span class="summary-bar__label">{{ item.label }}</span>
        <span class="summary-bar__value tabular-nums">{{ item.value }}</span>
      </div>
    </div>

    <StateLoading v-if="loading" text="加载今日任务…" />
    <StateError v-else-if="error" :description="error" @retry="reload" />

    <template v-else>
      <!-- Quick add -->
      <div class="quick-line glass--subtle">
        <NInput v-model:value="quickTitle" placeholder="添加今日任务，回车创建…" @keyup.enter="addQuick">
          <template #prefix><span class="quick-line__plus">+</span></template>
        </NInput>
      </div>

      <!-- Today's tasks -->
      <section class="task-group glass--subtle stagger-in">
        <TransitionGroup v-if="todayTasks.length" tag="div" name="task-fade" class="task-list">
          <TaskRow
            v-for="task in todayTasks"
            :key="task.id"
            :task="task"
            :show-planned-date="false"
            @toggle="toggle"
            @open="openEditor"
            @edit="openEditor"
            @delete="remove"
          />
        </TransitionGroup>
        <StateEmpty v-else title="今天还没有安排任务" description="在上方输入框快速添加，或从收件箱规划任务" />
      </section>

      <!-- Overdue -->
      <section v-if="overdueTasks.length" class="task-group glass--subtle stagger-in">
        <div class="group-head">
          <button class="group-head__toggle" type="button" @click="overdueOpen = !overdueOpen">
            <ChevronRight :size="15" class="group-head__chev" :class="{ 'is-open': overdueOpen }" />
            <span class="group-head__title section-card-title">逾期 ({{ overdueTasks.length }})</span>
          </button>
          <NTooltip>
            <template #trigger>
              <NButton size="tiny" @click="addAllOverdue">
                <template #icon><CheckCheck :size="14" /></template>
                全部加入今天
              </NButton>
            </template>
            将所有逾期任务的计划日期设为今天
          </NTooltip>
        </div>
        <TransitionGroup v-show="overdueOpen" tag="div" name="task-fade" class="task-list">
          <TaskRow
            v-for="task in overdueTasks"
            :key="task.id"
            :task="task"
            @toggle="toggle"
            @open="openEditor"
            @edit="openEditor"
            @delete="remove"
          />
        </TransitionGroup>
        <div v-show="overdueOpen" class="overdue-actions">
          <span class="overdue-hint">拖延的任务，尽快规划或加入今天</span>
        </div>
      </section>
    </template>

    <TaskEditorDrawer
      v-model:show="editorShow"
      :task="editingTask"
      :default-planned-date="today"
      @saved="onSaved"
      @deleted="onDeleted"
    />
  </div>
</template>

<style scoped>
.today-view {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
  display: flex;
  flex-direction: column;
  gap: var(--sp-3);
}
.today-head__title {
  display: flex; align-items: center; gap: var(--sp-1);
  font-size: var(--text-lg); margin: 0; line-height: var(--leading-tight);
}
.today-head__date { color: var(--text-color-muted); font-size: var(--text-sm); margin: 2px 0 0; }

.summary-bar {
  display: flex; gap: var(--sp-2); flex-wrap: wrap;
}
.summary-bar__item { display: flex; flex-direction: column; gap: 1px; padding: var(--sp-2) var(--sp-3); flex: 1; min-width: 90px; border-radius: var(--radius-md); }
.summary-bar__label { font-size: var(--text-xs); color: var(--text-color-muted); }
.summary-bar__value { font-size: var(--text-md); font-weight: var(--weight-semibold); color: var(--text-color-strong); }

.quick-line { display: flex; align-items: center; gap: var(--sp-2); padding: var(--sp-2) var(--sp-3); border-radius: var(--radius-lg); }
.quick-line :deep(.n-input) { background: transparent; }
.quick-line :deep(.n-input__border) { border-color: transparent; }
.quick-line__plus { color: var(--text-color-muted); font-size: var(--text-lg); }

.task-group { display: flex; flex-direction: column; gap: var(--sp-1); padding: var(--sp-3); border-radius: var(--radius-lg); }
.task-list { display: flex; flex-direction: column; gap: 2px; }

/* List enter/leave transitions (restrained) */
.task-fade-enter-active { transition: opacity var(--duration-sm) var(--ease-enter), transform var(--duration-sm) var(--ease-enter); }
.task-fade-leave-active { transition: opacity var(--duration-sm) var(--ease-leave), transform var(--duration-sm) var(--ease-leave); position: absolute; width: 100%; }
.task-fade-enter-from { opacity: 0; transform: translateY(-4px); }
.task-fade-leave-to { opacity: 0; transform: translateY(4px); }
.task-fade-move { transition: transform var(--duration-md) var(--ease-standard); }
.task-list { position: relative; }

.group-head { display: flex; align-items: center; justify-content: space-between; }
.group-head__toggle { display: flex; align-items: center; gap: var(--sp-1); background: none; border: none; cursor: pointer; color: var(--text-color); font-family: inherit; padding: var(--sp-1) 0; }
.group-head__chev { transition: transform var(--transition-fast); color: var(--text-color-muted); }
.group-head__chev.is-open { transform: rotate(90deg); }
.group-head__title { font-size: var(--text-sm); font-weight: var(--weight-semibold); }

.overdue-actions { padding-left: var(--sp-3); }
.overdue-hint { font-size: var(--text-xs); color: var(--text-color-muted); }

@media (max-width: 768px) {
  .today-view { padding: var(--sp-3); }
  .summary-bar__item { min-width: calc(50% - var(--sp-2)); }
}
</style>
