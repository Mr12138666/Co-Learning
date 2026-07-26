<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import {
  NButton,
  NPopconfirm,
  NEmpty,
  NSelect,
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
  TODO: { label: '待办', class: 'status--todo' },
  IN_PROGRESS: { label: '进行中', class: 'status--progress' },
  DONE: { label: '已完成', class: 'status--done' },
  ARCHIVED: { label: '已归档', class: 'status--archived' },
}

const filteredTasks = computed(() => {
  if (props.filterStatus === 'ALL') return studyStore.tasks
  return studyStore.tasks.filter((t) => t.status === props.filterStatus)
})

// Subject filter
const route = useRoute()
const subjectFilter = ref<number | null>(null)

function syncRouteSubject() {
  const q = route.query.subjectId
  if (q) {
    const n = Number(q)
    if (!isNaN(n) && n > 0) subjectFilter.value = n
  }
}
syncRouteSubject()
watch(() => route.query.subjectId, syncRouteSubject)
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
    <!-- Filter bar -->
    <div v-if="studyStore.subjects.length > 0" class="filter-bar">
      <NSelect
        v-model:value="subjectFilter"
        :options="subjectOptions"
        size="small"
        class="filter-bar__select"
      />
    </div>

    <!-- Empty -->
    <div v-if="displayTasks.length === 0" class="empty-state">
      <NEmpty description="暂无任务" />
    </div>

    <!-- Task rows -->
    <div v-else class="task-rows">
      <div v-for="task in displayTasks" :key="task.id" class="task-row">
        <!-- Checkbox -->
        <button
          class="task-row__check"
          :class="{ 'task-row__check--done': task.status === 'DONE' }"
          @click="toggleStatus(task)"
        >
          <span v-if="task.status === 'DONE'" class="task-row__check-icon">&#10003;</span>
        </button>

        <!-- Content -->
        <div class="task-row__content">
          <span
            class="task-row__title"
            :class="{ 'task-row__title--done': task.status === 'DONE' }"
          >
            {{ task.title }}
          </span>
          <span v-if="showSubject && task.subjectName" class="task-row__subject-tag" :style="{ '--tag-color': task.subjectColor ?? '' }">
            {{ task.subjectName }}
          </span>
        </div>

        <!-- Due date -->
        <span v-if="task.dueDate" class="task-row__due">{{ task.dueDate }}</span>

        <!-- Status badge -->
        <span class="task-row__status" :class="statusConfig[task.status].class">
          {{ statusConfig[task.status].label }}
        </span>

        <!-- Hover actions -->
        <div class="task-row__actions">
          <NButton
            v-if="task.status === 'TODO'"
            size="tiny"
            quaternary
            type="info"
            @click="setInProgress(task)"
          >
            开始
          </NButton>
          <NButton size="tiny" quaternary @click="emit('edit', task)">编辑</NButton>
          <NPopconfirm @positive-click="handleDelete(task)">
            <template #trigger>
              <NButton size="tiny" quaternary type="error">删除</NButton>
            </template>
            确定删除该任务吗？
          </NPopconfirm>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.task-list {
  width: 100%;
}

.filter-bar {
  margin-bottom: var(--sp-3);
}

.filter-bar__select {
  width: 160px;
}

.empty-state {
  padding: var(--sp-6) 0;
  display: flex;
  justify-content: center;
}

.task-rows {
  display: flex;
  flex-direction: column;
}

.task-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-2) var(--sp-3);
  min-height: 36px;
  border-bottom: 1px solid var(--separator);
  transition: background-color var(--transition-fast);
}

.task-row:last-child {
  border-bottom: none;
}

.task-row:hover {
  background: var(--state-hover);
}

/* Checkbox */
.task-row__check {
  width: 16px;
  height: 16px;
  border: 1.5px solid var(--border-color);
  border-radius: var(--radius-xs);
  background: transparent;
  cursor: pointer;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: border-color var(--transition-fast), background-color var(--transition-fast);
  padding: 0;
  color: var(--bg-card);
  font-size: 10px;
  line-height: 1;
}

.task-row__check:hover {
  border-color: var(--brand);
}

.task-row__check--done {
  background: var(--brand);
  border-color: var(--brand);
}

.task-row__check-icon {
  color: var(--ink-on-accent);
  font-size: 10px;
}

/* Content */
.task-row__content {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.task-row__title {
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
  color: var(--text-color);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-row__title--done {
  text-decoration: line-through;
  color: var(--text-color-muted);
}

.task-row__subject-tag {
  display: inline-flex;
  align-items: center;
  gap: var(--sp-1);
  padding: 0 var(--sp-2);
  height: 20px;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  background: var(--bg-page);
  border-radius: var(--radius-xs);
  flex-shrink: 0;
  white-space: nowrap;
}

.task-row__subject-tag::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: var(--radius-full);
  background: var(--tag-color, var(--border-color));
  flex-shrink: 0;
}

.task-row__due {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
  flex-shrink: 0;
  white-space: nowrap;
}

/* Status badge */
.task-row__status {
  font-size: var(--text-xs);
  padding: 0 var(--sp-2);
  height: 18px;
  line-height: 18px;
  border-radius: var(--radius-xs);
  flex-shrink: 0;
  white-space: nowrap;
}

.status--todo {
  color: var(--text-color-muted);
  background: var(--bg-page);
}

.status--progress {
  color: var(--info);
  background: var(--info-muted);
}

.status--done {
  color: var(--success);
  background: var(--success-muted);
}

.status--archived {
  color: var(--warning);
  background: var(--warning-muted);
}

/* Hover actions */
.task-row__actions {
  display: flex;
  gap: var(--sp-1);
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.task-row:hover .task-row__actions {
  opacity: 1;
}
</style>
