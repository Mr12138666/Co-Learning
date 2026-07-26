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
  <div>
    <NCard :bordered="false">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>科目管理</span>
          <NButton type="primary" @click="openCreate">+ 新增科目</NButton>
        </div>
      </template>

      <NEmpty v-if="studyStore.subjects.length === 0" description="还没有创建科目">
        <template #extra>
          <NButton type="primary" @click="openCreate">创建第一个科目</NButton>
        </template>
      </NEmpty>

      <NGrid v-else :cols="4" :x-gap="12" :y-gap="12" responsive="screen" item-responsive>
        <NGridItem v-for="subject in studyStore.subjects" :key="subject.id" span="4 s:2 m:1">
          <NCard size="small" hoverable>
            <div style="display: flex; align-items: center; gap: 8px;">
              <span
                class="color-dot"
                :style="{ background: subject.color }"
              />
              <span style="font-weight: 600; flex: 1;">{{ subject.name }}</span>
            </div>
            <div style="margin-top: 12px; display: flex; gap: 4px; justify-content: flex-end;">
              <NButton size="tiny" quaternary @click="openEdit(subject)">编辑</NButton>
              <NPopconfirm @positive-click="handleDelete(subject.id)">
                <template #trigger>
                  <NButton size="tiny" quaternary type="error">删除</NButton>
                </template>
                删除科目会同时删除该科目下的所有任务，确定吗？
              </NPopconfirm>
            </div>
          </NCard>
        </NGridItem>
      </NGrid>
    </NCard>

    <NModal
      v-model:show="showModal"
      preset="card"
      :title="editingId ? '编辑科目' : '新增科目'"
      style="max-width: 400px; width: 90vw;"
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
          <NButton @click="showModal = false">取消</NButton>
          <NButton type="primary" @click="handleSave">保存</NButton>
        </NSpace>
      </template>
    </NModal>
  </div>
</template>

<style scoped>
.color-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
}
</style>
