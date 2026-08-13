<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  NButton,
  NTag,
  NEmpty,
  NPopconfirm,
  NSpin,
  useMessage,
} from 'naive-ui'
import { useJournalStore } from '@/stores/journalStore'
import { usePageLoad } from '@/composables/usePageLoad'
import StateError from '@/components/common/StateError.vue'
import dayjs from 'dayjs'

const router = useRouter()
const journalStore = useJournalStore()
const message = useMessage()
const { loading, error, load, retry } = usePageLoad()

const statusConfig = {
  DRAFT: { label: '草稿', type: 'warning' as const },
  PUBLISHED: { label: '已发布', type: 'success' as const },
}

const visibilityConfig = {
  PRIVATE: { label: '私密', type: 'default' as const },
  FRIENDS: { label: '好友可见', type: 'info' as const },
  ROOM: { label: '房间可见', type: 'info' as const },
  PUBLIC: { label: '公开', type: 'success' as const },
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

onMounted(() => load(async () => {
  await journalStore.fetchMyJournals({ page: 0, size: 50 })
}))
</script>

<template>
  <div class="journal-view gradient-mesh">
    <!-- Loading -->
    <div v-if="loading" class="loading-center">
      <NSpin size="large" />
    </div>

    <!-- Error -->
    <StateError
      v-else-if="error"
      :title="error"
      @retry="retry(async () => {
        await journalStore.fetchMyJournals({ page: 0, size: 50 })
      })"
    />

    <template v-else>
      <!-- Page Header -->
      <div class="page-header glass">
        <h3 class="page-title">学习日志</h3>
        <NButton type="primary" size="small" @click="router.push('/journals/new')">
          + 写日志
        </NButton>
      </div>

      <!-- Empty -->
      <div v-if="journalStore.journals.length === 0" class="empty-container">
        <NEmpty description="还没有写过日志">
          <template #extra>
            <NButton type="primary" size="small" @click="router.push('/journals/new')">写第一篇日志</NButton>
          </template>
        </NEmpty>
      </div>

      <!-- Journal List -->
      <div v-else class="journal-list">
        <div
          v-for="journal in journalStore.journals"
          :key="journal.id"
          class="journal-row glass-list-item"
          @click="router.push(`/journals/${journal.id}/edit`)"
        >
          <div class="journal-row-main">
            <span class="journal-title">{{ journal.title }}</span>
            <div class="journal-tags">
              <NTag size="small" round :type="statusConfig[journal.status].type" :bordered="false">
                {{ statusConfig[journal.status].label }}
              </NTag>
              <NTag size="tiny" round :type="visibilityConfig[journal.visibility].type" :bordered="false">
                {{ visibilityConfig[journal.visibility].label }}
              </NTag>
            </div>
          </div>
          <div class="journal-row-meta">
            <span class="journal-date">{{ formatDate(journal.createdAt) }}</span>
            <span v-if="journal.publishedAt" class="journal-date">发布于 {{ formatDate(journal.publishedAt) }}</span>
          </div>
          <div class="journal-row-actions" @click.stop>
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
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.journal-view {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: var(--sp-12) 0;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-4);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
}

.page-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.empty-container {
  padding: var(--sp-12) 0;
}

.journal-list {
  display: flex;
  flex-direction: column;
  gap: var(--sp-2);
}

.journal-row {
  min-height: 44px;
  cursor: pointer;
}

.journal-row-main {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: var(--sp-2);
}

.journal-title {
  font-size: var(--text-base);
  font-weight: var(--weight-medium);
  color: var(--text-color-strong);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.journal-tags {
  display: flex;
  gap: var(--sp-1);
  flex-shrink: 0;
}

.journal-row-meta {
  display: flex;
  gap: var(--sp-3);
  flex-shrink: 0;
}

.journal-date {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
  white-space: nowrap;
}

.journal-row-actions {
  display: flex;
  gap: var(--sp-1);
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition-fast);
}

.journal-row:hover .journal-row-actions {
  opacity: 1;
}

@media (max-width: 768px) {
  .journal-row {
    flex-wrap: wrap;
  }

  .journal-row-meta {
    width: 100%;
    order: 3;
  }

  .journal-row-actions {
    opacity: 1;
  }
}
</style>
