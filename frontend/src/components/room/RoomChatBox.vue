<script setup lang="ts">
import { ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { useMessage } from 'naive-ui'
import RoomMessageItem from './RoomMessageItem.vue'
import { useRoomStore } from '@/stores/roomStore'
import type { RoomMessageResponse } from '@/api/room'
import { storageApi } from '@/api/storage'

const props = defineProps<{
  messages: RoomMessageResponse[]
}>()

const emit = defineEmits<{
  send: [content: string]
  sendImage: [imageUrl: string]
}>()

const message = useMessage()
const inputText = ref('')
const scrollContainer = ref<HTMLElement | null>(null)
const fileInput = ref<HTMLInputElement | null>(null)
const roomStore = useRoomStore()

const loadingMore = ref(false)

const showEmojiPicker = ref(false)
const uploading = ref(false)

// Common emojis
const emojis = [
  '😀', '😃', '😄', '😁', '😆', '😅', '🤣', '😂',
  '🙂', '😊', '😇', '🥰', '😍', '🤩', '😘', '😗',
  '😚', '😙', '🥲', '😋', '😛', '😜', '🤪', '😝',
  '🤑', '🤗', '🤭', '🤫', '🤔', '🤐', '🤨', '😐',
  '😑', '😶', '😏', '😒', '🙄', '😬', '🤥', '😌',
  '😔', '😪', '🤤', '😴', '😷', '🤒', '🤕', '🤢',
  '🤮', '🥵', '🥶', '🥴', '😵', '🤯', '🤠', '🥳',
  '🥸', '😎', '🤓', '🧐', '😕', '😟', '🙁', '☹️',
  '😮', '😯', '😲', '😳', '🥺', '😦', '😧', '😨',
]

async function scrollToBottom() {
  await nextTick()
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

// Auto scroll only when new messages arrive (not when loading history)
watch(() => props.messages.length, (newLen, oldLen) => {
  if (newLen > oldLen) {
    scrollToBottom()
  }
})

async function loadMoreMessages() {
  if (loadingMore.value || !roomStore.hasMoreMessages || !roomStore.currentRoom) return

  const container = scrollContainer.value
  const prevHeight = container?.scrollHeight ?? 0
  const prevTop = container?.scrollTop ?? 0

  loadingMore.value = true
  try {
    const added = await roomStore.loadOlderMessages(roomStore.currentRoom.id)
    if (added > 0 && container) {
      // Keep the currently-visible messages anchored: offset scrollTop by the
      // height gained from the newly prepended rows (no magic per-row constant).
      await nextTick()
      container.scrollTop = prevTop + (container.scrollHeight - prevHeight)
    }
  } finally {
    loadingMore.value = false
  }
}

function handleScroll(e: Event) {
  const target = e.target as HTMLElement
  if (target.scrollTop < 50 && !loadingMore.value && roomStore.hasMoreMessages) {
    loadMoreMessages()
  }
}

function handleSend() {
  const content = inputText.value.trim()
  if (!content) return
  emit('send', content)
  inputText.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function insertEmoji(emoji: string) {
  inputText.value += emoji
  showEmojiPicker.value = false
}

function triggerImageUpload() {
  fileInput.value?.click()
}

async function handleImageUpload(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true
  try {
    const res = await storageApi.upload(file)
    emit('sendImage', res.data.data.url)
  } catch {
    message.error('图片上传失败，请重试')
  } finally {
    uploading.value = false
    target.value = ''
  }
}

function toggleEmojiPicker() {
  showEmojiPicker.value = !showEmojiPicker.value
}

onMounted(() => {
  if (scrollContainer.value) {
    scrollContainer.value.addEventListener('scroll', handleScroll)
  }
})

onUnmounted(() => {
  if (scrollContainer.value) {
    scrollContainer.value.removeEventListener('scroll', handleScroll)
  }
})
</script>

<template>
  <div class="chat-box">
    <div class="chat-messages" ref="scrollContainer">
      <!-- Load more indicator -->
      <div v-if="loadingMore" class="loading-more">
        <n-spin size="small" />
      </div>

      <!-- Load more button -->
      <div v-if="!loadingMore && roomStore.hasMoreMessages && messages.length > 0" class="load-more-btn">
        <button class="load-more-link" @click="loadMoreMessages">加载更多消息</button>
      </div>

      <div v-if="messages.length === 0" class="empty-state">
        <span class="empty-text">还没有消息，发送第一条吧！</span>
      </div>
      <RoomMessageItem
        v-for="message in messages"
        :key="message.id"
        :message="message"
      />
    </div>

    <div class="chat-input">
      <div class="input-toolbar">
        <button class="toolbar-btn" @click="toggleEmojiPicker" title="表情">😀</button>
        <button class="toolbar-btn" @click="triggerImageUpload" title="图片">📷</button>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="file-input"
          @change="handleImageUpload"
          :disabled="uploading"
        />
      </div>

      <div class="input-area">
        <textarea
          v-model="inputText"
          class="input-textarea"
          placeholder="输入消息，按 Enter 发送..."
          rows="1"
          @keydown="handleKeydown"
        />
      </div>

      <button
        class="send-btn"
        :disabled="!inputText.trim() || uploading"
        @click="handleSend"
      >
        {{ uploading ? '...' : '发送' }}
      </button>

      <!-- Emoji picker -->
      <div v-if="showEmojiPicker" class="emoji-picker">
        <div
          v-for="emoji in emojis"
          :key="emoji"
          class="emoji-item"
          @click="insertEmoji(emoji)"
        >
          {{ emoji }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-box {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: var(--sp-3);
  min-height: 0;
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: var(--sp-2) 0;
}

.load-more-btn {
  display: flex;
  justify-content: center;
  padding: var(--sp-1) 0;
}

.load-more-link {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--text-sm);
  color: var(--brand);
  padding: var(--sp-1) var(--sp-2);
  border-radius: var(--radius-sm);
  transition: background-color var(--transition-fast);
}

.load-more-link:hover {
  background: var(--state-hover);
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.empty-text {
  font-size: var(--text-sm);
  color: var(--text-color-muted);
}

.chat-input {
  display: flex;
  gap: var(--sp-2);
  padding: var(--sp-2) var(--sp-3);
  border-top: 1px solid var(--separator);
  align-items: flex-end;
  position: relative;
}

.input-toolbar {
  display: flex;
  gap: var(--sp-1);
  flex-shrink: 0;
}

.toolbar-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: var(--text-base);
  padding: var(--sp-1);
  border-radius: var(--radius-sm);
  transition: background-color var(--transition-fast);
  line-height: 1;
}

.toolbar-btn:hover {
  background: var(--state-hover);
}

.file-input {
  display: none;
}

.input-area {
  flex: 1;
  min-width: 0;
}

.input-textarea {
  width: 100%;
  resize: none;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  padding: var(--sp-2);
  font-size: var(--text-base);
  font-family: var(--font-family);
  line-height: var(--leading-normal);
  color: var(--text-color);
  background: var(--bg-card);
  outline: none;
  transition: border-color var(--transition-fast);
  max-height: 100px;
}

.input-textarea:focus {
  border-color: var(--brand);
}

.input-textarea::placeholder {
  color: var(--text-color-muted);
}

.send-btn {
  flex-shrink: 0;
  padding: var(--sp-2) var(--sp-3);
  background: var(--brand);
  color: var(--ink-on-accent);
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--text-sm);
  font-weight: var(--weight-medium);
  cursor: pointer;
  transition: background-color var(--transition-fast), opacity var(--transition-fast);
  white-space: nowrap;
}

.send-btn:hover:not(:disabled) {
  background: var(--brand-hover);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: var(--sp-3);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: var(--sp-2);
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: var(--sp-1);
  max-height: 200px;
  overflow-y: auto;
  box-shadow: var(--shadow-2);
  z-index: var(--z-popover);
}

.emoji-item {
  font-size: var(--text-lg);
  cursor: pointer;
  padding: var(--sp-1);
  border-radius: var(--radius-xs);
  text-align: center;
  transition: background-color var(--transition-fast);
}

.emoji-item:hover {
  background: var(--state-hover);
}
</style>
