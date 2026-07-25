<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NCard,
  NButton,
  NSpace,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NSelect,
  NDatePicker,
  NTabs,
  NTabPane,
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
  <div>
    <NCard :bordered="false">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>任务清单</span>
          <NButton type="primary" @click="openCreate">+ 新增任务</NButton>
        </div>
      </template>

      <NTabs type="line" animated>
        <NTabPane name="all" tab="全部">
          <TaskList filter-status="ALL" @edit="openEdit" />
        </NTabPane>
        <NTabPane name="todo" tab="待办">
          <TaskList filter-status="TODO" @edit="openEdit" />
        </NTabPane>
        <NTabPane name="in_progress" tab="进行中">
          <TaskList filter-status="IN_PROGRESS" @edit="openEdit" />
        </NTabPane>
        <NTabPane name="done" tab="已完成">
          <TaskList filter-status="DONE" @edit="openEdit" />
        </NTabPane>
      </NTabs>
    </NCard>

    <NModal
      v-model:show="showModal"
      preset="card"
      :title="editingTask ? '编辑任务' : '新增任务'"
      style="width: 480px;"
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
        <NSpace>
          <NFormItem label="科目">
            <NSelect
              v-model:value="formData.subjectId"
              :options="subjectOptions"
              placeholder="选择科目"
              clearable
              style="width: 200px;"
            />
          </NFormItem>
          <NFormItem label="截止日期">
            <NDatePicker
              v-model:value="formData.dueDate"
              type="date"
              clearable
              style="width: 200px;"
            />
          </NFormItem>
        </NSpace>
      </NForm>
      <template #footer>
        <NSpace justify="end">
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSave">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>
