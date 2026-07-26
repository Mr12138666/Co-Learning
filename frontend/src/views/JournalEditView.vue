<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { NCard, useMessage } from 'naive-ui'
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
      // 编辑模式：直接更新内容并设置为已发布，不需要再调用publish
      await journalStore.update(journalId.value, {
        title: data.title,
        contentMarkdown: data.contentMarkdown,
        visibility: data.visibility as 'PRIVATE' | 'FRIENDS' | 'ROOM' | 'PUBLIC',
        status: 'PUBLISHED',
      })
    } else {
      // 新建模式：先创建再发布
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
  <NCard :bordered="false">
    <template #header>
      {{ isEdit ? '编辑日志' : '写日志' }}
    </template>
    <JournalEditor
      :model-value="loadedJournal ?? undefined"
      :mode="isEdit ? 'edit' : 'create'"
      @save="handleSave"
      @publish="handlePublish"
    />
  </NCard>
</template>
