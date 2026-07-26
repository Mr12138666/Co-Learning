<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import {
  NDrawer, NDrawerContent, NInput, NSelect, NDatePicker, NInputNumber,
  NCheckbox, NButton, NSpace, useMessage,
} from 'naive-ui'
import type { StudyTask, TaskStatus, CreateTaskRequest, UpdateTaskRequest } from '@/api/study'
import { useTaskStore } from '@/stores/taskStore'
import { useStudyStore } from '@/stores/studyStore'
import { getErrorMessage } from '@/utils/http-error'

const props = defineProps<{
  show: boolean
  /** null = create mode. */
  task: StudyTask | null
  /** Optional default planned date (ISO yyyy-MM-dd) when creating. */
  defaultPlannedDate?: string | null
}>()

const emit = defineEmits<{
  'update:show': [value: boolean]
  saved: [task: StudyTask]
  deleted: [id: number]
}>()

const taskStore = useTaskStore()
const studyStore = useStudyStore()
const message = useMessage()

const isEdit = computed(() => !!props.task)
const saving = ref(false)

const form = ref({
  title: '',
  description: '',
  subjectId: null as number | null,
  tagIds: [] as number[],
  plannedDate: null as string | null,
  estimatedMinutes: null as number | null,
  urgent: false,
  important: false,
  status: 'TODO' as TaskStatus,
})

const statusOptions = [
  { label: '待办', value: 'TODO' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'DONE' },
  { label: '已归档', value: 'ARCHIVED' },
]
const subjectOptions = computed(() => studyStore.subjects.map((s) => ({ label: s.name, value: s.id })))
const tagOptions = computed(() => taskStore.tags.map((t) => ({ label: t.name, value: t.id })))

watch(
  () => props.show,
  (open) => {
    if (!open) return
    if (studyStore.subjects.length === 0) studyStore.fetchSubjects().catch(() => {})
    if (taskStore.tags.length === 0) taskStore.loadTags().catch(() => {})
    const t = props.task
    form.value = {
      title: t?.title ?? '',
      description: t?.description ?? '',
      subjectId: t?.subjectId ?? null,
      tagIds: t?.tags.map((tag) => tag.id) ?? [],
      plannedDate: t?.plannedDate ?? props.defaultPlannedDate ?? null,
      estimatedMinutes: t?.estimatedMinutes ?? null,
      urgent: t?.urgent ?? false,
      important: t?.important ?? false,
      status: t?.status ?? 'TODO',
    }
  },
)

function close() {
  emit('update:show', false)
}

async function save() {
  if (!form.value.title.trim()) {
    message.warning('请输入任务标题')
    return
  }
  saving.value = true
  try {
    let result: StudyTask
    if (isEdit.value && props.task) {
      const payload: UpdateTaskRequest = {
        title: form.value.title.trim(),
        description: form.value.description,
        subjectId: form.value.subjectId,
        tagIds: form.value.tagIds,
        plannedDate: form.value.plannedDate,
        estimatedMinutes: form.value.estimatedMinutes,
        urgent: form.value.urgent,
        important: form.value.important,
        status: form.value.status,
      }
      result = await taskStore.updateTask(props.task.id, payload)
    } else {
      const payload: CreateTaskRequest = {
        title: form.value.title.trim(),
        description: form.value.description || undefined,
        subjectId: form.value.subjectId ?? undefined,
        tagIds: form.value.tagIds,
        plannedDate: form.value.plannedDate,
        estimatedMinutes: form.value.estimatedMinutes,
        urgent: form.value.urgent,
        important: form.value.important,
      }
      result = await taskStore.createTask(payload)
    }
    message.success(isEdit.value ? '已保存' : '任务已创建')
    emit('saved', result)
    close()
  } catch (e) {
    message.error(getErrorMessage(e, '保存失败'))
  } finally {
    saving.value = false
  }
}

async function remove() {
  if (!props.task) return
  saving.value = true
  try {
    await taskStore.deleteTask(props.task.id)
    message.success('任务已删除')
    emit('deleted', props.task.id)
    close()
  } catch (e) {
    message.error(getErrorMessage(e, '删除失败'))
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <NDrawer :show="show" :width="400" placement="right" @update:show="close">
    <NDrawerContent :title="isEdit ? '编辑任务' : '新建任务'" closable :native-scrollbar="false">
      <NSpace vertical :size="16" class="editor-body">
        <div class="field">
          <label class="field__label">标题</label>
          <NInput v-model:value="form.title" placeholder="任务标题" @keyup.enter="save" />
        </div>

        <div class="field">
          <label class="field__label">描述</label>
          <NInput v-model:value="form.description" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" placeholder="补充说明（可选）" />
        </div>

        <div class="field-grid">
          <div class="field">
            <label class="field__label">科目</label>
            <NSelect v-model:value="form.subjectId" :options="subjectOptions" placeholder="无" clearable />
          </div>
          <div class="field">
            <label class="field__label">计划日期</label>
            <NDatePicker v-model:formatted-value="form.plannedDate" value-format="yyyy-MM-dd" type="date" clearable />
          </div>
        </div>

        <div class="field">
          <label class="field__label">标签</label>
          <NSelect v-model:value="form.tagIds" :options="tagOptions" multiple placeholder="选择标签" clearable />
        </div>

        <div class="field-grid">
          <div class="field">
            <label class="field__label">预计时长（分钟）</label>
            <NInputNumber v-model:value="form.estimatedMinutes" :min="0" :step="15" placeholder="0" style="width: 100%" />
          </div>
          <div v-if="isEdit" class="field">
            <label class="field__label">状态</label>
            <NSelect v-model:value="form.status" :options="statusOptions" />
          </div>
        </div>

        <div class="field">
          <label class="field__label">优先级</label>
          <NSpace>
            <NCheckbox v-model:checked="form.important">重要</NCheckbox>
            <NCheckbox v-model:checked="form.urgent">紧急</NCheckbox>
          </NSpace>
        </div>
      </NSpace>

      <template #footer>
        <NSpace justify="space-between" style="width: 100%">
          <NButton v-if="isEdit" quaternary type="error" :loading="saving" @click="remove">删除</NButton>
          <span v-else />
          <NSpace>
            <NButton :disabled="saving" @click="close">取消</NButton>
            <NButton type="primary" :loading="saving" @click="save">保存</NButton>
          </NSpace>
        </NSpace>
      </template>
    </NDrawerContent>
  </NDrawer>
</template>

<style scoped>
.editor-body { animation: fadeInUp var(--duration-md) var(--ease-enter); }
.field { display: flex; flex-direction: column; gap: var(--sp-1); }
.field__label { font-size: var(--text-xs); color: var(--text-color-muted); font-weight: var(--weight-medium); }
.field-grid { display: grid; grid-template-columns: 1fr 1fr; gap: var(--sp-3); }
</style>
