<script setup lang="ts">
import { ref, computed } from 'vue'
import {
  NList,
  NListItem,
  NThing,
  NTag,
  NSpace,
  NButton,
  NPopconfirm,
  NEmpty,
  NSelect,
  NText,
  NIcon,
} from 'naive-ui'
import { useStudyStore } from '@/stores/studyStore'
import type { StudyTask } from '@/api/study'

const props = withDefaults(defineProps<{
  filterStatus?: 'TODO' | 'IN_PROGRESS' | 'DONE' | 'ARCHIVED' | 'ALL'
  showSubject?: boolean
}>(), {
  filterStatus: 'ALL',
  showSubject: true,
})

const emit = defineEmits<{
  edit: [task: StudyTask]
}>()

const studyStore = useStudyStore()

const statusConfig = {
  TODO: { label: '待办', type: 'default' as const },
  IN_PROGRESS: { label: '进行中', type: 'info' as const },
  DONE: { label: '已完成', type: 'success' as const },
  ARCHIVED: { label: '已归档', type: 'warning' as const },
}

const filteredTasks = computed(() => {
  if (props.filterStatus === 'ALL') return studyStore.tasks
  return studyStore.tasks.filter((t) => t.status === props.filterStatus)
})

// Subject filter
const subjectFilter = ref<number | null>(null)
const subjectOptions = computed(() => [
  { label: '全部科目', value: 0 },
  ...studyStore.subjects.map((s) => ({ label: s.name, value: s.id })),
])

const displayTasks = computed(() => {
  if (!subjectFilter.value || subjectFilter.value === 0) return filteredTasks.value
  return filteredTasks.value.filter((t) => t.subjectId === subjectFilter.value)
})

async function toggleStatus(task: StudyTask) {
  const newStatus = task.status === 'DONE' ? 'TODO' : 'DONE'
  await studyStore.updateTask(task.id, { status: newStatus })
}

async function setInProgress(task: StudyTask) {
  await studyStore.updateTask(task.id, { status: 'IN_PROGRESS' })
}

async function handleDelete(task: StudyTask) {
  await studyStore.deleteTask(task.id)
}
</script>

<template>
  <div class="task-list">
    <div v-if="studyStore.subjects.length > 0" class="filter-bar">
      <NSelect
        v-model:value="subjectFilter"
        :options="subjectOptions"
        size="small"
        style="width: 160px;"
      />
    </div>

    <NEmpty v-if="displayTasks.length === 0" description="暂无任务" />

    <NList v-else hoverable clickable>
      <NListItem v-for="task in displayTasks" :key="task.id">
        <NThing>
          <template #header>
            <NSpace align="center" :size="6">
              <span
                v-if="showSubject && task.subjectColor"
                class="subject-dot"
                :style="{ background: task.subjectColor }"
              />
              <NText
                :class="{ 'task-done': task.status === 'DONE' }"
                class="task-title"
              >
                {{ task.title }}
              </NText>
            </NSpace>
          </template>
          <template #header-extra>
            <NSpace :size="4">
              <NTag
                size="small"
                :type="statusConfig[task.status].type"
                round
              >
                {{ statusConfig[task.status].label }}
              </NTag>
            </NSpace>
          </template>
          <template #description>
            <NSpace :size="8" align="center">
              <NText v-if="showSubject && task.subjectName" depth="3" style="font-size: 13px;">
                {{ task.subjectName }}
              </NText>
              <NText v-if="task.dueDate" depth="3" style="font-size: 13px;">
                截止: {{ task.dueDate }}
              </NText>
            </NSpace>
          </template>
          <template #action>
            <NSpace :size="4">
              <NButton
                v-if="task.status !== 'DONE'"
                size="tiny"
                quaternary
                type="success"
                @click="toggleStatus(task)"
              >
                完成
              </NButton>
              <NButton
                v-if="task.status === 'TODO'"
                size="tiny"
                quaternary
                type="info"
                @click="setInProgress(task)"
              >
                开始
              </NButton>
              <NButton size="tiny" quaternary @click="emit('edit', task)">
                编辑
              </NButton>
              <NPopconfirm @positive-click="handleDelete(task)">
                <template #trigger>
                  <NButton size="tiny" quaternary type="error">
                    删除
                  </NButton>
                </template>
                确定删除该任务吗？
              </NPopconfirm>
            </NSpace>
          </template>
        </NThing>
      </NListItem>
    </NList>
  </div>
</template>

<style scoped>
.task-list {
  width: 100%;
}

.filter-bar {
  margin-bottom: 12px;
}

.subject-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.task-title {
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
}

.task-done {
  text-decoration: line-through;
  opacity: 0.6;
}
</style>
