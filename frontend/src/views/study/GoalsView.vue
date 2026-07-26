<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NButton,
  NSpace,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NDatePicker,
  NInputNumber,
  NTag,
  NEmpty,
  NPopconfirm,
  useMessage,
} from 'naive-ui'
import { useStudyStore } from '@/stores/studyStore'
import type { ExamGoal } from '@/api/study'
import dayjs from 'dayjs'

const studyStore = useStudyStore()
const message = useMessage()

const showModal = ref(false)
const editingId = ref<number | null>(null)
const formData = ref({
  examName: '',
  examDate: Date.now(),
  targetScore: null as number | null,
})

function openCreate() {
  editingId.value = null
  formData.value = {
    examName: '',
    examDate: Date.now(),
    targetScore: null,
  }
  showModal.value = true
}

function openEdit(goal: ExamGoal) {
  editingId.value = goal.id
  formData.value = {
    examName: goal.examName,
    examDate: dayjs(goal.examDate).valueOf(),
    targetScore: goal.targetScore ? Number(goal.targetScore) : null,
  }
  showModal.value = true
}

async function handleSave() {
  if (!formData.value.examName.trim()) {
    message.warning('请输入考试名称')
    return
  }
  const examDate = dayjs(formData.value.examDate).format('YYYY-MM-DD')
  const data = {
    examName: formData.value.examName,
    examDate,
    targetScore: formData.value.targetScore ?? undefined,
  }
  try {
    if (editingId.value) {
      await studyStore.updateGoal(editingId.value, data)
      message.success('更新成功')
    } else {
      await studyStore.createGoal(data)
      message.success('创建成功')
    }
    showModal.value = false
  } catch {
    message.error('操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await studyStore.deleteGoal(id)
    message.success('已删除')
  } catch {
    message.error('删除失败')
  }
}

async function toggleStatus(goal: ExamGoal) {
  const newStatus = goal.status === 'ACTIVE' ? 'ARCHIVED' : 'ACTIVE'
  await studyStore.updateGoal(goal.id, { status: newStatus })
}

onMounted(() => {
  studyStore.fetchGoals()
})
</script>

<template>
  <div class="page-container">
    <!-- Page Header -->
    <div class="page-header">
      <h3 class="page-title">考试目标</h3>
      <NButton type="primary" size="small" @click="openCreate">+ 新增目标</NButton>
    </div>

    <div class="page-divider" />

    <!-- Empty State -->
    <div v-if="studyStore.goals.length === 0" class="empty-state">
      <NEmpty description="还没有设定考试目标">
        <template #extra>
          <NButton type="primary" size="small" @click="openCreate">设定第一个目标</NButton>
        </template>
      </NEmpty>
    </div>

    <!-- Goal List -->
    <div v-else class="goal-list">
      <div v-for="goal in studyStore.goals" :key="goal.id" class="goal-row">
        <div class="goal-row__main">
          <div class="goal-row__title">
            <span class="goal-row__name">{{ goal.examName }}</span>
            <NTag
              size="small"
              :type="goal.status === 'ACTIVE' ? 'success' : goal.status === 'COMPLETED' ? 'info' : 'default'"
            >
              {{ goal.status === 'ACTIVE' ? '进行中' : goal.status === 'COMPLETED' ? '已完成' : '已归档' }}
            </NTag>
            <NTag
              v-if="goal.status === 'ACTIVE'"
              size="small"
              :type="goal.daysRemaining <= 30 ? 'error' : goal.daysRemaining <= 60 ? 'warning' : 'info'"
            >
              还剩 {{ goal.daysRemaining }} 天
            </NTag>
          </div>
          <div class="goal-row__meta">
            <span>考试日期: {{ goal.examDate }}</span>
            <span v-if="goal.targetScore">目标分数: {{ goal.targetScore }}</span>
          </div>
        </div>
        <div class="goal-row__actions">
          <NButton size="tiny" quaternary @click="openEdit(goal)">编辑</NButton>
          <NButton v-if="goal.status === 'ACTIVE'" size="tiny" quaternary @click="toggleStatus(goal)">
            归档
          </NButton>
          <NButton v-if="goal.status === 'ARCHIVED'" size="tiny" quaternary @click="toggleStatus(goal)">
            恢复
          </NButton>
          <NPopconfirm @positive-click="handleDelete(goal.id)">
            <template #trigger>
              <NButton size="tiny" quaternary type="error">删除</NButton>
            </template>
            确定删除该目标吗？
          </NPopconfirm>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      preset="card"
      :title="editingId ? '编辑目标' : '新增目标'"
      class="goal-modal"
    >
      <NForm label-placement="top">
        <NFormItem label="考试名称">
          <NInput v-model:value="formData.examName" placeholder="如: 研究生入学考试" />
        </NFormItem>
        <NFormItem label="考试日期">
          <NDatePicker
            v-model:value="formData.examDate"
            type="date"
            class="full-width"
            :is-date-disabled="(ts: number) => ts < Date.now() - 86400000"
          />
        </NFormItem>
        <NFormItem label="目标分数（可选）">
          <NInputNumber v-model:value="formData.targetScore" placeholder="如: 380" :min="0" :max="999" class="full-width" />
        </NFormItem>
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

.page-divider {
  border-bottom: 1px solid var(--separator);
  margin-bottom: var(--sp-3);
}

.empty-state {
  padding: var(--sp-8) 0;
  display: flex;
  justify-content: center;
}

.goal-list {
  display: flex;
  flex-direction: column;
}

.goal-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-3) var(--sp-4);
  min-height: 52px;
  border-bottom: 1px solid var(--separator);
  transition: background-color var(--transition-fast);
  gap: var(--sp-3);
}

.goal-row:last-child {
  border-bottom: none;
}

.goal-row:hover {
  background: var(--state-hover);
}

.goal-row__main {
  flex: 1;
  min-width: 0;
}

.goal-row__title {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  flex-wrap: wrap;
}

.goal-row__name {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.goal-row__meta {
  display: flex;
  gap: var(--sp-4);
  margin-top: var(--sp-1);
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.goal-row__actions {
  display: flex;
  gap: var(--sp-1);
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.goal-row:hover .goal-row__actions {
  opacity: 1;
}

.goal-modal {
  max-width: 480px;
  width: 90vw;
}
</style>
