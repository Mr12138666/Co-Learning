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
  NDatePicker,
  NInputNumber,
  NList,
  NListItem,
  NThing,
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
  <div>
    <NCard :bordered="false">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>考试目标</span>
          <NButton type="primary" @click="openCreate">
            + 新增目标
          </NButton>
        </div>
      </template>

      <NEmpty v-if="studyStore.goals.length === 0" description="还没有设定考试目标">
        <template #extra>
          <NButton type="primary" @click="openCreate">设定第一个目标</NButton>
        </template>
      </NEmpty>

      <NList v-else hoverable>
        <NListItem v-for="goal in studyStore.goals" :key="goal.id">
          <NThing>
            <template #header>
              <NSpace align="center" :size="8">
                <span style="font-size: 16px; font-weight: 600;">{{ goal.examName }}</span>
                <NTag
                  size="small"
                  round
                  :type="goal.status === 'ACTIVE' ? 'success' : goal.status === 'COMPLETED' ? 'info' : 'default'"
                >
                  {{ goal.status === 'ACTIVE' ? '进行中' : goal.status === 'COMPLETED' ? '已完成' : '已归档' }}
                </NTag>
              </NSpace>
            </template>
            <template #header-extra>
              <NTag
                v-if="goal.status === 'ACTIVE'"
                size="large"
                round
                :type="goal.daysRemaining <= 30 ? 'error' : goal.daysRemaining <= 60 ? 'warning' : 'info'"
              >
                还剩 {{ goal.daysRemaining }} 天
              </NTag>
            </template>
            <template #description>
              <NSpace :size="16">
                <span>考试日期: {{ goal.examDate }}</span>
                <span v-if="goal.targetScore">目标分数: {{ goal.targetScore }}</span>
              </NSpace>
            </template>
            <template #action>
              <NSpace :size="4">
                <NButton size="small" quaternary @click="openEdit(goal)">编辑</NButton>
                <NButton v-if="goal.status === 'ACTIVE'" size="small" quaternary @click="toggleStatus(goal)">
                  归档
                </NButton>
                <NButton v-if="goal.status === 'ARCHIVED'" size="small" quaternary @click="toggleStatus(goal)">
                  恢复
                </NButton>
                <NPopconfirm @positive-click="handleDelete(goal.id)">
                  <template #trigger>
                    <NButton size="small" quaternary type="error">删除</NButton>
                  </template>
                  确定删除该目标吗？
                </NPopconfirm>
              </NSpace>
            </template>
          </NThing>
        </NListItem>
      </NList>
    </NCard>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      preset="card"
      :title="editingId ? '编辑目标' : '新增目标'"
      style="max-width: 480px; width: 90vw;"
    >
      <NForm label-placement="top">
        <NFormItem label="考试名称">
          <NInput v-model:value="formData.examName" placeholder="如: 研究生入学考试" />
        </NFormItem>
        <NFormItem label="考试日期">
          <NDatePicker
            v-model:value="formData.examDate"
            type="date"
            style="width: 100%;"
            :is-date-disabled="(ts: number) => ts < Date.now() - 86400000"
          />
        </NFormItem>
        <NFormItem label="目标分数（可选）">
          <NInputNumber v-model:value="formData.targetScore" placeholder="如: 380" :min="0" :max="999" style="width: 100%;" />
        </NFormItem>
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
