<script setup lang="ts">
import { ref, computed } from 'vue'
import { NPopover, NInput, NSelect, NButton, useMessage } from 'naive-ui'
import { Plus } from 'lucide-vue-next'
import { useTaskStore } from '@/stores/taskStore'
import { useStudyStore } from '@/stores/studyStore'
import { getErrorMessage } from '@/utils/http-error'
import dayjs from 'dayjs'

const emit = defineEmits<{ created: [] }>()

const taskStore = useTaskStore()
const studyStore = useStudyStore()
const message = useMessage()

const show = ref(false)
const title = ref('')
const subjectId = ref<number | null>(null)
const saving = ref(false)

const subjectOptions = computed(() =>
  studyStore.subjects.map((s) => ({ label: s.name, value: s.id })),
)

async function ensureSubjects() {
  if (studyStore.subjects.length === 0) await studyStore.fetchSubjects().catch(() => {})
}

async function submit(planToday: boolean) {
  const t = title.value.trim()
  if (!t) return
  saving.value = true
  try {
    await taskStore.createTask({
      title: t,
      subjectId: subjectId.value ?? undefined,
      plannedDate: planToday ? dayjs().format('YYYY-MM-DD') : null,
    })
    title.value = ''
    subjectId.value = null
    message.success(planToday ? '已加入今天' : '已加入收件箱')
    emit('created')
    if (!planToday) show.value = false
  } catch (e) {
    message.error(getErrorMessage(e, '创建任务失败'))
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <NPopover v-model:show="show" trigger="click" placement="bottom-end" :width="320" @update:show="(v) => v && ensureSubjects()">
    <template #trigger>
      <button class="quick-add__trigger" type="button" aria-label="快速创建任务" title="快速创建 (Inbox)">
        <Plus :size="18" />
      </button>
    </template>
    <div class="quick-add">
      <NInput
        v-model:value="title"
        placeholder="输入任务标题，回车加入收件箱…"
        autofocus
        @keyup.enter="submit(false)"
      />
      <NSelect
        v-model:value="subjectId"
        :options="subjectOptions"
        placeholder="科目（可选）"
        clearable
        size="small"
      />
      <div class="quick-add__actions">
        <NButton size="small" :loading="saving" :disabled="!title.trim()" @click="submit(false)">加入收件箱</NButton>
        <NButton size="small" type="primary" :loading="saving" :disabled="!title.trim()" @click="submit(true)">加入今天</NButton>
      </div>
    </div>
  </NPopover>
</template>

<style scoped>
.quick-add__trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border: none;
  background: transparent;
  border-radius: var(--radius-sm);
  color: var(--text-color-muted);
  cursor: pointer;
  transition: background-color var(--transition-fast), color var(--transition-fast);
}
.quick-add__trigger:hover {
  background: var(--state-hover);
  color: var(--text-color);
}

.quick-add {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
  padding: var(--sp-1) 0;
}
.quick-add__actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--sp-2);
}
</style>
