<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  NTag,
  NEmpty,
  NButton,
} from 'naive-ui'
import { journalApi, type Journal } from '@/api/journal'
import { userApi } from '@/api/user'
import dayjs from 'dayjs'

const router = useRouter()
const journals = ref<Journal[]>([])
const loading = ref(false)
const userCache = ref<Map<number, { nickname: string; avatarUrl: string }>>(new Map())

function formatDate(date: string): string {
  return dayjs(date).format('MM-DD HH:mm')
}

async function _fetchUserInfo(userId: number) {
  if (userCache.value.has(userId)) {
    return userCache.value.get(userId)!
  }
  try {
    const res = await userApi.getProfile(userId)
    const info = {
      nickname: res.data.data.displayName || '未知用户',
      avatarUrl: res.data.data.avatarUrl || '',
    }
    userCache.value.set(userId, info)
    return info
  } catch {
    return { nickname: '未知用户', avatarUrl: '' }
  }
}

async function loadPublicJournals() {
  loading.value = true
  try {
    const res = await journalApi.listPublic({ page: 0, size: 20 })
    journals.value = res.data.data.items
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPublicJournals()
})
</script>

<template>
  <div class="journal-square-view">
    <!-- Page Header -->
    <div class="page-header">
      <h3 class="page-title">日志广场</h3>
      <NButton type="primary" size="small" @click="router.push('/journals/new')">
        + 写日志
      </NButton>
    </div>

    <!-- Empty -->
    <div v-if="!loading && journals.length === 0" class="empty-container">
      <NEmpty description="暂无公开日志，发布日志时选择'公开'即可展示在这里">
        <template #extra>
          <NButton type="primary" size="small" @click="router.push('/journals/new')">发布第一篇日志</NButton>
        </template>
      </NEmpty>
    </div>

    <!-- Journal List -->
    <div v-else class="journal-list">
      <div
        v-for="journal in journals"
        :key="journal.id"
        class="journal-row"
        @click="router.push(`/journals/${journal.id}`)"
      >
        <div class="journal-row-main">
          <span class="journal-title">{{ journal.title }}</span>
          <NTag size="tiny" round type="success" :bordered="false">公开</NTag>
        </div>
        <span class="journal-date">{{ formatDate(journal.publishedAt || journal.createdAt) }}</span>
        <NButton size="tiny" quaternary @click.stop="router.push(`/journals/${journal.id}`)">
          阅读
        </NButton>
      </div>
    </div>
  </div>
</template>

<style scoped>
.journal-square-view {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-4);
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
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  background: var(--bg-card);
}

.journal-row {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  min-height: 44px;
  cursor: pointer;
  transition: background-color var(--transition-fast);
  border-bottom: 1px solid var(--divider);
}

.journal-row:last-child {
  border-bottom: none;
}

.journal-row:hover {
  background: var(--state-hover);
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

.journal-date {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
  white-space: nowrap;
  flex-shrink: 0;
}
</style>
