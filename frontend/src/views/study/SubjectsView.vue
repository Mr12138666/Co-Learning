<script setup lang="ts">
import { ref, onMounted } from 'vue'
import {
  NButton,
  NSpace,
  NModal,
  NForm,
  NFormItem,
  NInput,
  NColorPicker,
  NGrid,
  NGridItem,
  NPopconfirm,
  NEmpty,
  useMessage,
} from 'naive-ui'
import { useStudyStore } from '@/stores/studyStore'
import type { Subject } from '@/api/study'

const studyStore = useStudyStore()
const message = useMessage()

const showModal = ref(false)
const editingId = ref<number | null>(null)
const formData = ref({
  name: '',
  color: '#2080F0',
})

function openCreate() {
  editingId.value = null
  formData.value = { name: '', color: '#2080F0' }
  showModal.value = true
}

function openEdit(subject: Subject) {
  editingId.value = subject.id
  formData.value = { name: subject.name, color: subject.color }
  showModal.value = true
}

async function handleSave() {
  if (!formData.value.name.trim()) {
    message.warning('请输入科目名称')
    return
  }
  try {
    if (editingId.value) {
      await studyStore.updateSubject(editingId.value, formData.value)
      message.success('更新成功')
    } else {
      await studyStore.createSubject(formData.value)
      message.success('创建成功')
    }
    showModal.value = false
  } catch {
    message.error('操作失败')
  }
}

async function handleDelete(id: number) {
  try {
    await studyStore.deleteSubject(id)
    message.success('已删除')
  } catch {
    message.error('删除失败')
  }
}

onMounted(() => {
  studyStore.fetchSubjects()
})
</script>

<template>
  <div class="page-container gradient-mesh">
    <!-- Page Header -->
    <div class="page-header">
      <h3 class="page-title">科目管理</h3>
      <NButton type="primary" size="small" @click="openCreate">+ 新增科目</NButton>
    </div>

    <div class="page-divider" />

    <!-- Empty State -->
    <div v-if="studyStore.subjects.length === 0" class="empty-state">
      <NEmpty description="还没有创建科目">
        <template #extra>
          <NButton type="primary" size="small" @click="openCreate">创建第一个科目</NButton>
        </template>
      </NEmpty>
    </div>

    <!-- Subject Grid -->
    <NGrid v-else :cols="4" :x-gap="8" :y-gap="8" responsive="screen" item-responsive>
      <NGridItem v-for="subject in studyStore.subjects" :key="subject.id" span="4 s:2 m:1" class="stagger-in">
        <div class="subject-card glass stagger-in">
          <div class="subject-card__body">
            <span class="subject-card__dot" :style="{ background: subject.color }" />
            <span class="subject-card__name">{{ subject.name }}</span>
          </div>
          <div class="subject-card__actions">
            <NButton size="tiny" quaternary @click="openEdit(subject)">编辑</NButton>
            <NPopconfirm @positive-click="handleDelete(subject.id)">
              <template #trigger>
                <NButton size="tiny" quaternary type="error">删除</NButton>
              </template>
              删除科目会同时删除该科目下的所有任务，确定吗？
            </NPopconfirm>
          </div>
        </div>
      </NGridItem>
    </NGrid>

    <!-- Create/Edit Modal -->
    <NModal
      v-model:show="showModal"
      preset="card"
      :title="editingId ? '编辑科目' : '新增科目'"
      style="width: 400px; max-width: 90vw"
    >
      <NForm label-placement="top">
        <NFormItem label="科目名称">
          <NInput v-model:value="formData.name" placeholder="如: 数学" :maxlength="30" />
        </NFormItem>
        <NFormItem label="颜色">
          <NColorPicker v-model:value="formData.color" :show-alpha="false" />
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

.subject-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--sp-3) var(--sp-4);
  min-height: 48px;
  border-radius: var(--radius-sm);
}

.subject-card__body {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  flex: 1;
  min-width: 0;
}

.subject-card__dot {
  width: 10px;
  height: 10px;
  border-radius: var(--radius-full);
  flex-shrink: 0;
}

.subject-card__name {
  font-weight: var(--weight-medium);
  font-size: var(--text-base);
  color: var(--text-color-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.subject-card__actions {
  display: flex;
  gap: var(--sp-1);
  opacity: 0;
  transition: opacity var(--transition-fast);
  flex-shrink: 0;
}

.subject-card:hover .subject-card__actions {
  opacity: 1;
}

</style>
