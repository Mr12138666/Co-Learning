<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, NTag, NText, NButton, NSpace, NAvatar, useMessage } from 'naive-ui'
import { journalApi, type Journal } from '@/api/journal'
import { userApi } from '@/api/user'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const journal = ref<Journal | null>(null)
const loading = ref(true)
const authorInfo = ref<{ nickname: string; avatarUrl: string } | null>(null)

const journalId = computed(() => Number(route.params.id))

function formatDate(date: string): string {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// Simple markdown to HTML for preview
function renderMarkdown(md: string): string {
  let html = md
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/```[\s\S]*?```/g, (match) => {
      const code = match.replace(/```\w*\n?/g, '').replace(/```$/g, '')
      return `<pre><code>${code}</code></pre>`
    })
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .replace(/!\[(.+?)\]\((.+?)\)/g, '<img src="$2" alt="$1" class="preview-image" />')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')

  if (!html.startsWith('<h') && !html.startsWith('<pre')) {
    html = `<p>${html}</p>`
  }
  html = html.replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
  return html
}

const contentHtml = computed(() => journal.value ? renderMarkdown(journal.value.contentMarkdown) : '')

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
    message.error('加载日志失败')
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
  <div v-if="!loading && journal">
    <NCard :bordered="false">
      <template #header>
        <NSpace align="center" justify="space-between">
          <NSpace align="center">
            <NAvatar round :src="authorInfo?.avatarUrl || undefined">
              {{ authorInfo?.nickname?.charAt(0) || '?' }}
            </NAvatar>
            <div>
              <NText depth="3" style="font-size: 13px;">{{ authorInfo?.nickname || '未知用户' }}</NText>
              <NText depth="3" style="font-size: 12px; margin-left: 8px;">{{ formatDate(journal.publishedAt || journal.createdAt) }}</NText>
            </div>
          </NSpace>
          <NButton quaternary @click="router.push('/journals/square')">
            返回广场
          </NButton>
        </NSpace>
      </template>
      
      <h1 style="font-size: 24px; font-weight: 700; margin: 16px 0;">{{ journal.title }}</h1>
      
      <div class="content-preview" v-html="contentHtml" />
    </NCard>
  </div>
  
  <div v-else-if="loading" class="loading-state">
    <NCard :bordered="false">
      <div style="display: flex; justify-content: center; padding: 40px;">
        <NText>加载中...</NText>
      </div>
    </NCard>
  </div>
</template>

<style scoped>
.content-preview {
  font-size: 15px;
  line-height: 1.8;
  padding: 8px 0;
}

.content-preview :deep(h1) { font-size: 24px; font-weight: 700; margin: 16px 0 12px; }
.content-preview :deep(h2) { font-size: 20px; font-weight: 600; margin: 14px 0 10px; }
.content-preview :deep(h3) { font-size: 16px; font-weight: 600; margin: 12px 0 8px; }
.content-preview :deep(p) { margin: 10px 0; }
.content-preview :deep(ul) { padding-left: 24px; margin: 10px 0; }
.content-preview :deep(li) { margin: 6px 0; }
.content-preview :deep(code) {
  background: var(--bg-tertiary);
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 14px;
}
.content-preview :deep(pre) {
  background: var(--bg-tertiary);
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}
.content-preview :deep(pre code) {
  background: none;
  padding: 0;
}
.content-preview :deep(strong) { font-weight: 600; }
.content-preview :deep(.preview-image) { max-width: 100%; border-radius: 8px; margin: 8px 0; }
</style>
