<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NButton,
  NSpace,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NDatePicker,
  useMessage,
} from 'naive-ui'
import { useStudyStore } from '@/stores/studyStore'
import TaskList from '@/components/study/TaskList.vue'
import type { StudyTask } from '@/api/study'
import dayjs from 'dayjs'

const studyStore = useStudyStore()
const message = useMessage()

const showModal = ref(false)
const editingTask = ref<StudyTask | null>(null)
const formData = ref({
  title: '',
  description: '',
  subjectId: null as number | null,
  dueDate: null as number | null,
})

const subjectOptions = ref<{ label: string; value: number }[]>([])

function updateSubjectOptions() {
  subjectOptions.value = studyStore.subjects.map((s) => ({
    label: s.name,
    value: s.id,
  }))
}

const activeTab = ref('all')
const tabOptions = [
  { key: 'all', label: '全部', filter: 'ALL' as const },
  { key: 'todo', label: '待办', filter: 'TODO' as const },
  { key: 'in_progress', label: '进行中', filter: 'IN_PROGRESS' as const },
  { key: 'done', label: '已完成', filter: 'DONE' as const },
]

function openCreate() {
  editingTask.value = null
  formData.value = { title: '', description: '', subjectId: null, dueDate: null }
  updateSubjectOptions()
  showModal.value = true
}

function openEdit(task: StudyTask) {
  editingTask.value = task
  formData.value = {
    title: task.title,
    description: task.description ?? '',
    subjectId: task.subjectId,
    dueDate: task.dueDate ? dayjs(task.dueDate).valueOf() : null,
  }
  updateSubjectOptions()
  showModal.value = true
}

async function handleSave() {
  if (!formData.value.title.trim()) {
    message.warning('请输入任务标题')
    return
  }
  const data = {
    title: formData.value.title,
    description: formData.value.description || undefined,
    subjectId: formData.value.subjectId ?? undefined,
    dueDate: formData.value.dueDate ? dayjs(formData.value.dueDate).format('YYYY-MM-DD') : undefined,
  }
  try {
    if (editingTask.value) {
      await studyStore.updateTask(editingTask.value.id, data)
      message.success('更新成功')
    } else {
      await studyStore.createTask(data)
      message.success('创建成功')
    }
    showModal.value = false
  } catch {
    message.error('操作失败')
  }
}

onMounted(() => {
  studyStore.fetchAll()
})
</script>

<template>
  <div class="page-container">
    <!-- Page Header -->
    <div class="page-header">
      <h3 class="page-title">任务清单</h3>
      <NButton type="primary" size="small" @click="openCreate">+ 新增任务</NButton>
    </div>

    <!-- Tab bar (segmented) -->
    <div class="tab-bar">
      <button
        v-for="tab in tabOptions"
        :key="tab.key"
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="page-divider" />

    <!-- Task Lists (only render active tab) -->
    <div v-for="tab in tabOptions" :key="tab.key">
      <TaskList v-if="activeTab === tab.key" :filter-status="tab.filter" @edit="openEdit" />
    </div>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      preset="card"
      :title="editingTask ? '编辑任务' : '新增任务'"
      class="task-modal"
    >
      <NForm label-placement="top">
        <NFormItem label="任务标题">
          <NInput v-model:value="formData.title" placeholder="如: 复习高数第一章" :maxlength="200" />
        </NFormItem>
        <NFormItem label="描述（可选）">
          <NInput
            v-model:value="formData.description"
            type="textarea"
            placeholder="任务详情..."
            :autosize="{ minRows: 2, maxRows: 4 }"
          />
        </NFormItem>
        <div class="form-row">
          <NFormItem label="科目" class="form-row__item">
            <NSelect
              v-model:value="formData.subjectId"
              :options="subjectOptions"
              placeholder="选择科目"
              clearable
            />
          </NFormItem>
          <NFormItem label="截止日期" class="form-row__item">
            <NDatePicker
              v-model:value="formData.dueDate"
              type="date"
              clearable
              class="full-width"
            />
          </NFormItem>
        </div>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton size="small" @click="showModal = false">取消</NButton>
          <NButton type="primary" size="small" @click="handleSave">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.page-container {
  max-width: var(--content-max-width);
  padding-bottom: var(--sp-4);
}

.full-width {
  width: 100%;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-3) 0;
}

.page-title {
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

/* Segmented tab bar */
.tab-bar {
  display: inline-flex;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  overflow: hidden;
  margin-bottom: var(--sp-3);
}

.tab-bar__item {
  padding: var(--sp-1) var(--sp-3);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  color: var(--text-color-muted);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast);
  font-family: inherit;
  line-height: var(--leading-snug);
}

.tab-bar__item:hover {
  background: var(--state-hover);
  color: var(--text-color);
}

.tab-bar__item--active {
  background: var(--state-selected);
  color: var(--text-color-strong);
}

.tab-bar__item + .tab-bar__item {
  border-left: 1px solid var(--border-color);
}

.page-divider {
  border-bottom: 1px solid var(--separator);
  margin-bottom: var(--sp-3);
}

.form-row {
  display: flex;
  gap: var(--sp-3);
}

.form-row__item {
  flex: 1;
}

.task-modal {
  max-width: 480px;
  width: 90vw;
}
</style>
