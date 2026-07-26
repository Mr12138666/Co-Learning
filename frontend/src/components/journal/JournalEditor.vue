<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import {
  NInput,
  NButton,
  NSpace,
  NSelect,
  NGrid,
  NGridItem,
  NCard,
  NDivider,
  useMessage,
} from 'naive-ui'
import type { Journal } from '@/api/journal'
import { storageApi } from '@/api/storage'

const props = defineProps<{
  modelValue?: Partial<Journal>
  mode?: 'create' | 'edit'
}>()

const emit = defineEmits<{
  'update:modelValue': [value: { title: string; contentMarkdown: string; visibility: string }]
  save: [data: { title: string; contentMarkdown: string; visibility: string }]
  publish: [data: { title: string; contentMarkdown: string; visibility: string }]
}>()

const message = useMessage()

// Local state
const title = ref(props.modelValue?.title ?? '')
const content = ref(props.modelValue?.contentMarkdown ?? '')
const visibility = ref(props.modelValue?.visibility ?? 'PRIVATE')
const previewMode = ref<'split' | 'preview' | 'edit'>('split')

// Watch for modelValue changes (for async loading in edit mode)
watch(
  () => props.modelValue,
  (newVal) => {
    if (newVal) {
      title.value = newVal.title ?? ''
      content.value = newVal.contentMarkdown ?? ''
      visibility.value = newVal.visibility ?? 'PRIVATE'
    }
  },
  { deep: true },
)

// Auto-save to localStorage (debounced)
const STORAGE_KEY = 'journal-draft'
let saveTimer: ReturnType<typeof setTimeout> | null = null

const visibilityOptions = [
  { label: '私密', value: 'PRIVATE' },
  { label: '好友可见', value: 'FRIENDS' },
  { label: '公开', value: 'PUBLIC' },
]

const previewModeOptions = [
  { label: '编辑', value: 'edit' },
  { label: '分屏', value: 'split' },
  { label: '预览', value: 'preview' },
]

// Simple markdown to HTML for preview (client-side)
function renderMarkdown(md: string): string {
  // Basic markdown rendering
  let html = md
    // Escape HTML
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // Headings
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/^# (.+)$/gm, '<h1>$1</h1>')
    // Bold and italic
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // Code blocks
    .replace(/```[\s\S]*?```/g, (match) => {
      const code = match.replace(/```\w*\n?/g, '').replace(/```$/g, '')
      return `<pre><code>${code}</code></pre>`
    })
    // Inline code
    .replace(/`(.+?)`/g, '<code>$1</code>')
    // Unordered lists
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    // Images
    .replace(/!\[(.+?)\]\((.+?)\)/g, '<img src="$2" alt="$1" class="preview-image" />')
    // Line breaks (paragraphs)
    .replace(/\n\n/g, '</p><p>')
    .replace(/\n/g, '<br>')

  // Wrap in paragraphs
  if (!html.startsWith('<h') && !html.startsWith('<pre')) {
    html = `<p>${html}</p>`
  }

  // Wrap consecutive <li> in <ul>
  html = html.replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')

  return html
}

const previewHtml = computed(() => renderMarkdown(content.value))

// Auto-save draft
function saveDraft() {
  if (props.mode === 'edit') return // Only for create mode
  const draft = { title: title.value, content: content.value, visibility: visibility.value }
  localStorage.setItem(STORAGE_KEY, JSON.stringify(draft))
}

function loadDraft() {
  if (props.mode === 'edit') return
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      const draft = JSON.parse(saved)
      if (!title.value && !content.value) {
        title.value = draft.title ?? ''
        content.value = draft.content ?? ''
        visibility.value = draft.visibility ?? 'PRIVATE'
        message.info('已恢复上次未保存的草稿')
      }
    } catch {
      // ignore
    }
  }
}

function clearDraft() {
  localStorage.removeItem(STORAGE_KEY)
}

// Debounced auto-save
watch([title, content, visibility], () => {
  if (saveTimer) clearTimeout(saveTimer)
  saveTimer = setTimeout(saveDraft, 2000)
})

onMounted(() => {
  loadDraft()
})

// Emit data
function getData() {
  return {
    title: title.value || '无标题',
    contentMarkdown: content.value,
    visibility: visibility.value,
  }
}

function handleSave() {
  if (!content.value.trim()) {
    message.warning('请输入日志内容')
    return
  }
  const data = getData()
  emit('save', data)
  clearDraft()
}

function handlePublish() {
  if (!content.value.trim()) {
    message.warning('请输入日志内容')
    return
  }
  const data = getData()
  emit('publish', data)
  clearDraft()
}

// Toolbar actions
function insertText(before: string, after: string = '') {
  const textarea = document.querySelector('.editor-textarea textarea') as HTMLTextAreaElement
  if (!textarea) return
  const start = textarea.selectionStart
  const end = textarea.selectionEnd
  const selected = content.value.substring(start, end)
  const newText = content.value.substring(0, start) + before + selected + after + content.value.substring(end)
  content.value = newText
  // Restore cursor
  setTimeout(() => {
    textarea.focus()
    textarea.selectionStart = start + before.length
    textarea.selectionEnd = end + before.length
  }, 0)
}

function insertHeading() { insertText('## ') }
function insertBold() { insertText('**', '**') }
function insertItalic() { insertText('*', '*') }
function insertCode() { insertText('`', '`') }
function insertList() { insertText('- ') }

async function insertImage(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  
  try {
    const res = await storageApi.upload(file)
    const url = res.data.data.url
    insertText(`![图片](${url})`)
  } catch {
    message.error('图片上传失败')
  } finally {
    target.value = ''
  }
}
</script>

<template>
  <div class="journal-editor">
    <!-- Title & Visibility -->
    <NSpace vertical :size="12">
      <NInput
        v-model:value="title"
        placeholder="日志标题"
        size="large"
        :maxlength="200"
      />
      <NSpace :size="12" align="center">
        <NSelect
          v-model:value="visibility"
          :options="visibilityOptions"
          size="small"
          style="width: 140px;"
        />
        <NSelect
          v-model:value="previewMode"
          :options="previewModeOptions"
          size="small"
          style="width: 100px;"
        />
      </NSpace>
    </NSpace>

    <NDivider />

    <!-- Editor / Preview -->
    <div class="editor-area" :class="previewMode">
      <!-- Editor -->
      <div v-if="previewMode !== 'preview'" class="editor-pane">
        <!-- Toolbar -->
        <div class="editor-toolbar">
          <NButton size="tiny" quaternary @click="insertHeading">标题</NButton>
          <NButton size="tiny" quaternary @click="insertBold">加粗</NButton>
          <NButton size="tiny" quaternary @click="insertItalic">斜体</NButton>
          <NButton size="tiny" quaternary @click="insertCode">代码</NButton>
          <NButton size="tiny" quaternary @click="insertList">列表</NButton>
          <label class="image-upload-btn">
            <input type="file" accept="image/*" class="file-input" @change="insertImage" />
            <NButton size="tiny" quaternary>📷 图片</NButton>
          </label>
        </div>
        <NInput
          v-model:value="content"
          type="textarea"
          placeholder="用 Markdown 记录你的学习心得..."
          :autosize="{ minRows: 15, maxRows: 30 }"
          class="editor-textarea"
        />
      </div>

      <!-- Preview -->
      <div v-if="previewMode !== 'edit'" class="preview-pane">
        <div class="preview-content" v-html="previewHtml" />
      </div>
    </div>

    <NDivider />

    <!-- Actions -->
    <NSpace justify="end" :size="12">
      <NButton @click="handleSave">
        保存草稿
      </NButton>
      <NButton type="primary" @click="handlePublish">
        发布
      </NButton>
    </NSpace>
  </div>
</template>

<style scoped>
.journal-editor {
  width: 100%;
}

.editor-area {
  display: flex;
  gap: 16px;
  min-height: 400px;
}

.editor-area.split .editor-pane,
.editor-area.split .preview-pane {
  flex: 1;
}

.editor-area.edit .editor-pane,
.editor-area.preview .preview-pane {
  flex: 1;
  width: 100%;
}

.editor-pane {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.editor-toolbar {
  display: flex;
  gap: 4px;
  padding: 4px 0;
}

.image-upload-btn {
  cursor: pointer;
}

.file-input {
  display: none;
}

.preview-pane {
  background: var(--bg-card);
  border: 1px solid var(--border-default);
  border-radius: var(--radius-sm);
  padding: var(--sp-4);
  overflow-y: auto;
}

.preview-content {
  font-size: 14px;
  line-height: 1.7;
}

.preview-content :deep(h1) { font-size: 24px; font-weight: 700; margin: 12px 0 8px; }
.preview-content :deep(h2) { font-size: 20px; font-weight: 600; margin: 12px 0 8px; }
.preview-content :deep(h3) { font-size: 16px; font-weight: 600; margin: 10px 0 6px; }
.preview-content :deep(p) { margin: 8px 0; }
.preview-content :deep(ul) { padding-left: 20px; margin: 8px 0; }
.preview-content :deep(li) { margin: 4px 0; }
.preview-content :deep(code) {
  background: var(--bg-sunken);
  padding: 2px 6px;
  border-radius: var(--radius-xs);
  font-size: 13px;
}
.preview-content :deep(pre) {
  background: var(--bg-sunken);
  padding: var(--sp-3);
  border-radius: var(--radius-sm);
  overflow-x: auto;
}
.preview-content :deep(pre code) {
  background: none;
  padding: 0;
}
.preview-content :deep(strong) { font-weight: var(--weight-semibold); }
.preview-content :deep(.preview-image) { max-width: 100%; border-radius: var(--radius-xs); }

@media (max-width: 768px) {
  .editor-area.split {
    flex-direction: column;
  }

  .editor-area.split .editor-pane,
  .editor-area.split .preview-pane {
    flex: none;
    width: 100%;
  }
}
</style>
