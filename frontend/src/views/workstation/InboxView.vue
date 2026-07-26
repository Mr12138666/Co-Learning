<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { NInput, NSelect, NButton, NTooltip, useMessage } from 'naive-ui'
import { Inbox, CalendarPlus } from 'lucide-vue-next'
import dayjs from 'dayjs'
import TaskRow from '@/components/task/TaskRow.vue'
import TaskEditorDrawer from '@/components/task/TaskEditorDrawer.vue'
import StateError from '@/components/common/StateError.vue'
import StateLoading from '@/components/common/StateLoading.vue'
import StateEmpty from '@/components/common/StateEmpty.vue'
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
const quickTitle = ref('')
const quickSubject = ref<number | null>(null)

const editorShow = ref(false)
const editingTask = ref<StudyTask | null>(null)
const today = dayjs().format('YYYY-MM-DD')

const subjectOptions = ref<{ label: string; value: number }[]>([])

async function fetchAll() {
  if (studyStore.subjects.length === 0) await studyStore.fetchSubjects().catch(() => {})
  subjectOptions.value = studyStore.subjects.map((s) => ({ label: s.name, value: s.id }))
  tasks.value = await taskStore.fetchInbox()
}
function reload() { load(fetchAll) }

async function addQuick() {
  const title = quickTitle.value.trim()
  if (!title) return
  try {
    const created = await taskStore.createTask({ title, subjectId: quickSubject.value ?? undefined })
    tasks.value.unshift(created)
    quickTitle.value = ''
  } catch (e) {
    message.error(getErrorMessage(e, '创建失败'))
  }
}

async function toggle(task: StudyTask) {
  const prev = task.status
  task.status = task.status === 'DONE' ? 'TODO' : 'DONE'
  try { await taskStore.toggleDone({ ...task, status: prev }) }
  catch (e) { task.status = prev; message.error(getErrorMessage(e, '更新失败')) }
}

async function planToday(task: StudyTask) {
  try {
    await taskStore.setPlannedDate(task.id, today)
    tasks.value = tasks.value.filter((t) => t.id !== task.id)
    message.success('已加入今天')
  } catch (e) { message.error(getErrorMessage(e, '操作失败')) }
}

async function planAllToday() {
  const ids = tasks.value.map((t) => t.id)
  if (!ids.length) return
  try {
    await taskStore.bulkSetPlannedDate(ids, today)
    tasks.value = []
    message.success('已全部加入今天')
  } catch (e) { message.error(getErrorMessage(e, '操作失败')) }
}

function openEditor(task: StudyTask | null) { editingTask.value = task; editorShow.value = true }
async function remove(task: StudyTask) {
  try { await taskStore.deleteTask(task.id); tasks.value = tasks.value.filter((t) => t.id !== task.id) }
  catch (e) { message.error(getErrorMessage(e, '删除失败')) }
}
function onSaved(saved: StudyTask) {
  // If it got a planned date, it leaves the inbox.
  if (saved.plannedDate) tasks.value = tasks.value.filter((t) => t.id !== saved.id)
  else reload()
}

onMounted(reload)
</script>

<template>
  <div class="inbox-view">
    <header class="inbox-head">
      <h2 class="inbox-head__title"><Inbox :size="16" /> Inbox <span class="inbox-head__count">{{ tasks.length }}</span></h2>
      <NButton v-if="tasks.length" size="small" @click="planAllToday">
        <template #icon><CalendarPlus :size="15" /></template>
        全部加入今天
      </NButton>
    </header>

    <div class="quick-line">
      <NInput v-model:value="quickTitle" placeholder="快速添加任务，回车创建…" @keyup.enter="addQuick" />
      <NSelect v-model:value="quickSubject" :options="subjectOptions" placeholder="科目" clearable size="medium" style="width: 140px" />
    </div>

    <StateLoading v-if="loading" text="加载收件箱…" />
    <StateError v-else-if="error" :description="error" @retry="reload" />
    <StateEmpty v-else-if="!tasks.length" title="收件箱为空" description="所有任务都已规划，或在上方快速添加新任务" />
    <TransitionGroup v-else tag="div" name="task-fade" class="task-list">
      <div v-for="task in tasks" :key="task.id" class="inbox-row">
        <TaskRow
          :task="task"
          class="inbox-row__task"
          @toggle="toggle"
          @open="openEditor"
          @edit="openEditor"
          @delete="remove"
        />
        <NTooltip>
          <template #trigger>
            <NButton class="inbox-row__plan" size="tiny" quaternary aria-label="加入今天" @click="planToday(task)">
              <template #icon><CalendarPlus :size="15" /></template>
            </NButton>
          </template>
          加入今天
        </NTooltip>
      </div>
    </TransitionGroup>

    <TaskEditorDrawer v-model:show="editorShow" :task="editingTask" @saved="onSaved" @deleted="(id) => tasks = tasks.filter(t => t.id !== id)" />
  </div>
</template>

<style scoped>
.inbox-view { max-width: var(--content-max-width); margin: 0 auto; padding: var(--sp-4); display: flex; flex-direction: column; gap: var(--sp-2); }
.inbox-head { display: flex; align-items: center; justify-content: space-between; gap: var(--sp-2); flex-wrap: wrap; }
.inbox-head__title { display: flex; align-items: center; gap: var(--sp-1); font-size: var(--text-lg); margin: 0; line-height: var(--leading-tight); }
.inbox-head__count { font-size: var(--text-sm); color: var(--text-color-muted); font-weight: var(--weight-normal); }
.quick-line { display: flex; gap: var(--sp-2); }
.task-list { position: relative; display: flex; flex-direction: column; gap: 2px; }
.inbox-row { display: flex; align-items: center; gap: var(--sp-1); }
.inbox-row__task { flex: 1; min-width: 0; }
.inbox-row__plan { flex-shrink: 0; }

.task-fade-enter-active { transition: opacity var(--duration-sm) var(--ease-enter), transform var(--duration-sm) var(--ease-enter); }
.task-fade-leave-active { transition: opacity var(--duration-sm) var(--ease-leave), transform var(--duration-sm) var(--ease-leave); position: absolute; width: 100%; }
.task-fade-enter-from { opacity: 0; transform: translateY(-4px); }
.task-fade-leave-to { opacity: 0; transform: translateY(4px); }
.task-fade-move { transition: transform var(--duration-md) var(--ease-standard); }

@media (max-width: 768px) {
  .inbox-view { padding: var(--sp-3); }
  .quick-line { flex-wrap: wrap; }
}
</style>
