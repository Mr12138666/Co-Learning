<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NCard,
  NButton,
  NSpace,
  NList,
  NListItem,
  NThing,
  NTag,
  NEmpty,
  NPopconfirm,
  NText,
  useMessage,
} from 'naive-ui'
import { useJournalStore } from '@/stores/journalStore'
import dayjs from 'dayjs'

const router = useRouter()
const journalStore = useJournalStore()
const message = useMessage()

const visibilityConfig = {
  PRIVATE: { label: '私密', type: 'default' as const },
  FRIENDS: { label: '好友可见', type: 'info' as const },
  ROOM: { label: '房间可见', type: 'info' as const },
  PUBLIC: { label: '公开', type: 'success' as const },
}

const statusConfig = {
  DRAFT: { label: '草稿', type: 'warning' as const },
  PUBLISHED: { label: '已发布', type: 'success' as const },
}

function formatDate(date: string): string {
  return dayjs(date).format('MM-DD HH:mm')
}

async function handleDelete(id: number) {
  try {
    await journalStore.remove(id)
    message.success('已删除')
  } catch {
    message.error('删除失败')
  }
}

async function handlePublish(id: number) {
  try {
    await journalStore.publish(id)
    message.success('已发布')
  } catch {
    message.error('发布失败')
  }
}

onMounted(() => {
  journalStore.fetchMyJournals({ page: 0, size: 50 })
})
</script>

<template>
  <div>
    <NCard :bordered="false">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>学习日志</span>
          <NButton type="primary" @click="router.push('/journals/new')">
            + 写日志
          </NButton>
        </div>
      </template>

      <NEmpty v-if="journalStore.journals.length === 0" description="还没有写过日志">
        <template #extra>
          <NButton type="primary" @click="router.push('/journals/new')">写第一篇日志</NButton>
        </template>
      </NEmpty>

      <NList v-else hoverable clickable>
        <NListItem
          v-for="journal in journalStore.journals"
          :key="journal.id"
          @click="router.push(`/journals/${journal.id}/edit`)"
        >
          <NThing>
            <template #header>
              <NSpace align="center" :size="8">
                <NText strong>{{ journal.title }}</NText>
                <NTag size="small" round :type="statusConfig[journal.status].type">
                  {{ statusConfig[journal.status].label }}
                </NTag>
                <NTag size="small" round :type="visibilityConfig[journal.visibility].type">
                  {{ visibilityConfig[journal.visibility].label }}
                </NTag>
              </NSpace>
            </template>
            <template #description>
              <NText depth="3" style="font-size: 13px;">
                {{ formatDate(journal.createdAt) }}
                <span v-if="journal.publishedAt"> · 发布于 {{ formatDate(journal.publishedAt) }}</span>
              </NText>
            </template>
            <template #action>
              <NSpace :size="4" @click.stop>
                <NButton
                  v-if="journal.status === 'DRAFT'"
                  size="tiny"
                  quaternary
                  type="success"
                  @click="handlePublish(journal.id)"
                >
                  发布
                </NButton>
                <NButton
                  size="tiny"
                  quaternary
                  @click="router.push(`/journals/${journal.id}/edit`)"
                >
                  编辑
                </NButton>
                <NPopconfirm @positive-click="handleDelete(journal.id)">
                  <template #trigger>
                    <NButton size="tiny" quaternary type="error">删除</NButton>
                  </template>
                  确定删除该日志吗？
                </NPopconfirm>
              </NSpace>
            </template>
          </NThing>
        </NListItem>
      </NList>
    </NCard>
  </div>
</template>
