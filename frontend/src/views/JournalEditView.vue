<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NButton, useMessage } from 'naive-ui'
import JournalEditor from '@/components/journal/JournalEditor.vue'
import { useJournalStore } from '@/stores/journalStore'

const router = useRouter()
const route = useRoute()
const journalStore = useJournalStore()
const message = useMessage()

const journalId = computed(() => {
  const id = route.params.id
  return id ? Number(id) : null
})

const isEdit = computed(() => !!journalId.value)
type JournalVisibility = 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC'
const loadedJournal = ref<{ title: string; contentMarkdown: string; visibility: JournalVisibility } | null>(null)

async function handleSave(data: { title: string; contentMarkdown: string; visibility: string }) {
  try {
    if (isEdit.value && journalId.value) {
      await journalStore.update(journalId.value, {
        title: data.title,
        contentMarkdown: data.contentMarkdown,
        visibility: data.visibility as 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC',
        status: 'DRAFT',
      })
      message.success('草稿已保存')
    } else {
      await journalStore.create({
        title: data.title,
        contentMarkdown: data.contentMarkdown,
        visibility: data.visibility as 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC',
      })
      message.success('已创建')
    }
    router.push('/journals')
  } catch {
    message.error('保存失败')
  }
}

async function handlePublish(data: { title: string; contentMarkdown: string; visibility: string }) {
  try {
    if (isEdit.value && journalId.value) {
      await journalStore.update(journalId.value, {
        title: data.title,
        contentMarkdown: data.contentMarkdown,
        visibility: data.visibility as 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC',
        status: 'PUBLISHED',
      })
    } else {
      const journal = await journalStore.create({
        title: data.title,
        contentMarkdown: data.contentMarkdown,
        visibility: data.visibility as 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC',
      })
      await journalStore.publish(journal.id)
    }
    message.success('已发布')
    router.push('/journals')
  } catch (e: any) {
    message.error(e?.response?.data?.message || '发布失败')
  }
}

onMounted(async () => {
  if (isEdit.value && journalId.value) {
    try {
      const journal = await journalStore.fetchById(journalId.value)
      loadedJournal.value = {
        title: journal.title,
        contentMarkdown: journal.contentMarkdown,
        visibility: journal.visibility,
      }
    } catch {
      message.error('加载失败')
      router.push('/journals')
    }
  }
})
</script>

<template>
  <div class="journal-edit-view gradient-mesh">
    <!-- Toolbar -->
    <div class="edit-toolbar glass">
      <h3 class="toolbar-title">{{ isEdit ? '编辑日志' : '写日志' }}</h3>
      <NButton quaternary size="small" @click="router.push('/journals')">
        ← 返回列表
      </NButton>
    </div>

    <!-- Editor -->
    <div class="editor-container glass">
      <JournalEditor
        :model-value="loadedJournal ?? undefined"
        :mode="isEdit ? 'edit' : 'create'"
        @save="handleSave"
        @publish="handlePublish"
      />
    </div>
  </div>
</template>

<style scoped>
.journal-edit-view {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: var(--sp-4);
}

.edit-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--sp-3);
  padding: var(--sp-3) var(--sp-4);
  border-radius: var(--radius-md);
}

.toolbar-title {
  margin: 0;
  font-size: var(--text-xl);
  font-weight: var(--weight-semibold);
  color: var(--text-color-strong);
}

.editor-container {
  border-radius: var(--radius-lg);
  padding: var(--sp-4);
}
</style>
