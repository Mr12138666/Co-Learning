<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NText, NButton, NAvatar } from 'naive-ui'
import { journalApi, type Journal } from '@/api/journal'
import { userApi } from '@/api/user'
import { sanitizeHtml } from '@/utils/markdown'
import { formatDate } from '@/utils/format'

const router = useRouter()
const route = useRoute()

const journal = ref<Journal | null>(null)
const loading = ref(true)
const authorInfo = ref<{ nickname: string; avatarUrl: string } | null>(null)

const journalId = computed(() => Number(route.params.id))

// Render the server-provided HTML through DOMPurify (defense in depth) instead
// of re-parsing markdown client-side with an XSS-prone hand-rolled regex.
const contentHtml = computed(() => sanitizeHtml(journal.value?.contentHtml))

async function loadJournal() {
  loading.value = true
  try {
    const res = await journalApi.getPublicById(journalId.value)
    journal.value = res.data.data

    // Load author info
    if (journal.value && journal.value.userId) {
      const userRes = await userApi.getProfile(journal.value.userId)
      authorInfo.value = {
        nickname: userRes.data.data.displayName || userRes.data.data.email,
        avatarUrl: userRes.data.data.avatarUrl || '',
      }
    }
  } catch {
    router.push('/journals/square')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadJournal()
})
</script>

<template>
  <div class="journal-detail-view">
    <!-- Loading -->
    <div v-if="loading" class="loading-center">
      <NText depth="3">加载中...</NText>
    </div>

    <!-- Content -->
    <template v-else-if="journal">
      <!-- Toolbar -->
      <div class="detail-toolbar">
        <NButton quaternary size="small" @click="router.push('/journals/square')">
          ← 返回广场
        </NButton>
      </div>

      <!-- Article -->
      <article class="article">
        <h1 class="article-title">{{ journal.title }}</h1>

        <div class="article-meta">
          <NAvatar round size="small" :src="authorInfo?.avatarUrl || undefined">
            {{ authorInfo?.nickname?.charAt(0) || '?' }}
          </NAvatar>
          <span class="meta-author">{{ authorInfo?.nickname || '未知用户' }}</span>
          <span class="meta-dot">·</span>
          <span class="meta-date">{{ formatDate(journal.publishedAt || journal.createdAt) }}</span>
        </div>

        <div class="content-preview" v-html="contentHtml" />
      </article>
    </template>
  </div>
</template>

<style scoped>
.journal-detail-view {
  max-width: var(--component-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.loading-center {
  display: flex;
  justify-content: center;
  padding: var(--sp-12) 0;
}

.detail-toolbar {
  margin-bottom: var(--sp-3);
}

.article {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--sp-6);
}

.article-title {
  font-size: var(--text-2xl);
  font-weight: var(--weight-bold);
  color: var(--text-color-strong);
  margin: 0 0 var(--sp-3) 0;
  line-height: var(--leading-tight);
}

.article-meta {
  display: flex;
  align-items: center;
  gap: var(--sp-2);
  padding-bottom: var(--sp-4);
  margin-bottom: var(--sp-4);
  border-bottom: 1px solid var(--divider);
}

.meta-author {
  font-size: var(--text-md);
  font-weight: var(--weight-medium);
  color: var(--text-color);
}

.meta-dot {
  color: var(--text-color-muted);
}

.meta-date {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.content-preview {
  font-size: var(--text-base);
  line-height: var(--leading-relaxed);
  color: var(--text-color);
}

.content-preview :deep(h1) {
  font-size: var(--text-xl);
  font-weight: var(--weight-bold);
  margin: var(--sp-5) 0 var(--sp-3);
  color: var(--text-color-strong);
}

.content-preview :deep(h2) {
  font-size: var(--text-lg);
  font-weight: var(--weight-semibold);
  margin: var(--sp-4) 0 var(--sp-2);
  color: var(--text-color-strong);
}

.content-preview :deep(h3) {
  font-size: var(--text-base);
  font-weight: var(--weight-semibold);
  margin: var(--sp-3) 0 var(--sp-2);
  color: var(--text-color-strong);
}

.content-preview :deep(p) {
  margin: var(--sp-2) 0;
}

.content-preview :deep(ul) {
  padding-left: var(--sp-6);
  margin: var(--sp-2) 0;
}

.content-preview :deep(li) {
  margin: var(--sp-1) 0;
}

.content-preview :deep(code) {
  background: var(--bg-sunken);
  padding: 2px var(--sp-1);
  border-radius: var(--radius-xs);
  font-size: var(--text-sm);
  font-family: var(--font-mono);
}

.content-preview :deep(pre) {
  background: var(--bg-sunken);
  padding: var(--sp-4);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin: var(--sp-3) 0;
}

.content-preview :deep(pre code) {
  background: none;
  padding: 0;
}

.content-preview :deep(strong) {
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.content-preview :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-sm);
  margin: var(--sp-2) 0;
}
</style>
