<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { NTabs, NTabPane, NInput, useMessage } from 'naive-ui'
import draggable from 'vuedraggable'
import TaskRow from '@/components/task/TaskRow.vue'
import TaskEditorDrawer from '@/components/task/TaskEditorDrawer.vue'
import StateError from '@/components/common/StateError.vue'
import StateLoading from '@/components/common/StateLoading.vue'
import { useTaskStore } from '@/stores/taskStore'
import { usePageLoad } from '@/composables/usePageLoad'
import { getErrorMessage } from '@/utils/http-error'
import type { StudyTask, TaskStatus, QuadrantKey } from '@/api/study'

const taskStore = useTaskStore()
const message = useMessage()
const { loading, error, load } = usePageLoad()

const tab = ref<'eisenhower' | 'kanban'>('eisenhower')

// ===== Eisenhower =====
const quadrantDefs: { key: QuadrantKey; title: string; urgent: boolean; important: boolean; accent: string }[] = [
  { key: 'urgent-important', title: '紧急 且 重要', urgent: true, important: true, accent: 'var(--danger)' },
  { key: 'not-urgent-important', title: '不紧急 但 重要', urgent: false, important: true, accent: 'var(--brand)' },
  { key: 'urgent-not-important', title: '紧急 但 不重要', urgent: true, important: false, accent: 'var(--warning)' },
  { key: 'not-urgent-not-important', title: '不紧急 且 不重要', urgent: false, important: false, accent: 'var(--text-color-muted)' },
]
const quadrants = reactive<Record<QuadrantKey, StudyTask[]>>({
  'urgent-important': [],
  'not-urgent-important': [],
  'urgent-not-important': [],
  'not-urgent-not-important': [],
})
const quadInputs = reactive<Record<string, string>>({})

// ===== Kanban =====
const kanbanDefs: { key: TaskStatus; title: string }[] = [
  { key: 'TODO', title: '待办' },
  { key: 'IN_PROGRESS', title: '进行中' },
  { key: 'DONE', title: '已完成' },
  { key: 'ARCHIVED', title: '已归档' },
]
const kanban = reactive<Record<TaskStatus, StudyTask[]>>({
  TODO: [], IN_PROGRESS: [], DONE: [], ARCHIVED: [],
})
const kanbanInputs = reactive<Record<string, string>>({})

const editorShow = ref(false)
const editingTask = ref<StudyTask | null>(null)

async function fetchAll() {
  const [q, all] = await Promise.all([taskStore.fetchQuadrant(), taskStore.fetchAllTasks()])
  for (const def of quadrantDefs) quadrants[def.key] = q[def.key] ?? []
  for (const def of kanbanDefs) kanban[def.key] = all.filter((t) => t.status === def.key)
}
function reload() { load(fetchAll) }

async function onQuadrantAdd(def: (typeof quadrantDefs)[number], evt: { added?: { element: StudyTask } }) {
  const el = evt.added?.element
  if (!el) return
  const prevU = el.urgent, prevI = el.important
  el.urgent = def.urgent; el.important = def.important
  try {
    await taskStore.setQuadrant(el.id, def.urgent, def.important)
  } catch (e) {
    el.urgent = prevU; el.important = prevI
    message.error(getErrorMessage(e, '移动失败'))
    reload()
  }
}

async function onKanbanAdd(status: TaskStatus, evt: { added?: { element: StudyTask } }) {
  const el = evt.added?.element
  if (!el) return
  const prev = el.status
  el.status = status
  try {
    await taskStore.setStatus(el.id, status)
  } catch (e) {
    el.status = prev
    message.error(getErrorMessage(e, '移动失败'))
    reload()
  }
}

async function addQuadrantTask(def: (typeof quadrantDefs)[number]) {
  const title = (quadInputs[def.key] ?? '').trim()
  if (!title) return
  try {
    const created = await taskStore.createTask({ title, urgent: def.urgent, important: def.important })
    quadrants[def.key].push(created)
    quadInputs[def.key] = ''
  } catch (e) { message.error(getErrorMessage(e, '创建失败')) }
}

async function addKanbanTask(status: TaskStatus) {
  const title = (kanbanInputs[status] ?? '').trim()
  if (!title) return
  try {
    const created = await taskStore.createTask({ title })
    // Newly created tasks are TODO; move to target column if needed.
    if (status !== 'TODO') { await taskStore.setStatus(created.id, status); created.status = status }
    kanban[status].push(created)
    kanbanInputs[status] = ''
  } catch (e) { message.error(getErrorMessage(e, '创建失败')) }
}

async function toggle(task: StudyTask) {
  const prev = task.status
  task.status = task.status === 'DONE' ? 'TODO' : 'DONE'
  try { await taskStore.toggleDone({ ...task, status: prev }) }
  catch (e) { task.status = prev; message.error(getErrorMessage(e, '更新失败')) }
}
function openEditor(task: StudyTask | null) { editingTask.value = task; editorShow.value = true }
async function remove(task: StudyTask) {
  try { await taskStore.deleteTask(task.id); reload() }
  catch (e) { message.error(getErrorMessage(e, '删除失败')) }
}

onMounted(reload)
</script>

<template>
  <div class="boards-view">
    <NTabs v-model:value="tab" type="line" animated class="boards-tabs">
      <NTabPane name="eisenhower" tab="艾森豪威尔矩阵" />
      <NTabPane name="kanban" tab="Kanban" />
    </NTabs>

    <StateLoading v-if="loading" text="加载看板…" />
    <StateError v-else-if="error" :description="error" @retry="reload" />

    <!-- Eisenhower -->
    <div v-else-if="tab === 'eisenhower'" class="eisenhower">
      <section v-for="def in quadrantDefs" :key="def.key" class="quadrant" :style="{ '--accent': def.accent }">
        <div class="quadrant__head">
          <span class="quadrant__dot" />
          <span class="quadrant__title">{{ def.title }}</span>
          <span class="quadrant__count">{{ quadrants[def.key].length }}</span>
        </div>
        <draggable
          :list="quadrants[def.key]"
          group="eisenhower"
          item-key="id"
          class="quadrant__list"
          :animation="150"
          @change="(e: any) => onQuadrantAdd(def, e)"
        >
          <template #item="{ element }">
            <TaskRow :task="element" draggable card @toggle="toggle" @open="openEditor" @edit="openEditor" @delete="remove" />
          </template>
        </draggable>
        <NInput v-model:value="quadInputs[def.key]" size="tiny" placeholder="+ 添加任务" @keyup.enter="addQuadrantTask(def)" />
      </section>
    </div>

    <!-- Kanban -->
    <div v-else class="kanban">
      <section v-for="def in kanbanDefs" :key="def.key" class="kanban-col">
        <div class="kanban-col__head">
          <span class="kanban-col__title">{{ def.title }}</span>
          <span class="kanban-col__count">{{ kanban[def.key].length }}</span>
        </div>
        <draggable
          :list="kanban[def.key]"
          group="kanban"
          item-key="id"
          class="kanban-col__list"
          :animation="150"
          @change="(e: any) => onKanbanAdd(def.key, e)"
        >
          <template #item="{ element }">
            <TaskRow :task="element" draggable card @toggle="toggle" @open="openEditor" @edit="openEditor" @delete="remove" />
          </template>
        </draggable>
        <NInput v-model:value="kanbanInputs[def.key]" size="tiny" placeholder="+ 添加任务" @keyup.enter="addKanbanTask(def.key)" />
      </section>
    </div>

    <TaskEditorDrawer v-model:show="editorShow" :task="editingTask" @saved="reload" @deleted="reload" />
  </div>
</template>

<style scoped>
.boards-view { display: flex; flex-direction: column; height: 100%; padding: var(--sp-4); gap: var(--sp-2); }
.boards-tabs { flex-shrink: 0; }

.eisenhower {
  flex: 1; min-height: 0;
  display: grid; grid-template-columns: 1fr 1fr; grid-template-rows: 1fr 1fr; gap: var(--sp-2);
}
.quadrant {
  display: flex; flex-direction: column; gap: var(--sp-1);
  border-left: 2px solid var(--accent);
  border-radius: 0; padding: var(--sp-1) var(--sp-2); min-height: 0;
}
.quadrant__head { display: flex; align-items: center; gap: var(--sp-1); }
.quadrant__dot { width: 8px; height: 8px; border-radius: var(--radius-full); background: var(--accent); }
.quadrant__title { font-size: var(--text-sm); font-weight: var(--weight-semibold); flex: 1; }
.quadrant__count { font-size: var(--text-xs); color: var(--text-color-muted); }
.quadrant__list { flex: 1; min-height: 40px; overflow-y: auto; display: flex; flex-direction: column; gap: 2px; }

.kanban { flex: 1; min-height: 0; display: flex; gap: var(--sp-2); overflow-x: auto; }
.kanban-col {
  display: flex; flex-direction: column; gap: var(--sp-1);
  min-width: 260px; width: 260px; flex-shrink: 0;
  border-left: 2px solid var(--divider);
  border-radius: 0; padding: var(--sp-1) var(--sp-2);
}
.kanban-col__head { display: flex; align-items: center; justify-content: space-between; padding: var(--sp-1) 0; border-bottom: 1px solid var(--divider); }
.kanban-col__title { font-size: var(--text-sm); font-weight: var(--weight-semibold); }
.kanban-col__count { font-size: var(--text-xs); color: var(--text-color-muted); }
.kanban-col__list { flex: 1; min-height: 40px; overflow-y: auto; display: flex; flex-direction: column; gap: 2px; }

@media (max-width: 768px) {
  .eisenhower { grid-template-columns: 1fr; grid-template-rows: none; overflow-y: auto; }
  .quadrant { min-height: 200px; }
  .kanban-col { min-width: 82vw; width: 82vw; scroll-snap-align: start; }
  .kanban { scroll-snap-type: x mandatory; }
}
</style>
