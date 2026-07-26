<script setup lang="ts">
import { ref, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { useMessage } from 'naive-ui'
import RoomMessageItem from './RoomMessageItem.vue'
import { useRoomStore } from '@/stores/roomStore'
import { storageApi } from '@/api/storage'

const props = defineProps<{
  messages: any[]
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

const MAX_MESSAGES = 50
const LOAD_SIZE = 20

const loadingMore = ref(false)
const hasMore = ref(true)
const currentPage = ref(0)

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
  if (loadingMore.value || !hasMore.value || !roomStore.currentRoom) return
  
  loadingMore.value = true
  try {
    currentPage.value++
    await roomStore.loadMoreMessages(roomStore.currentRoom.id, currentPage.value, LOAD_SIZE)
    
    // Check if we've loaded all available messages
    if (roomStore.messages.length >= roomStore.totalMessages) {
      hasMore.value = false
    }
    
    // Keep only MAX_MESSAGES most recent messages
    if (roomStore.messages.length > MAX_MESSAGES) {
      roomStore.messages = roomStore.messages.slice(-MAX_MESSAGES)
    }
    
    // Scroll down a bit to show the newly loaded messages
    await nextTick()
    if (scrollContainer.value) {
      scrollContainer.value.scrollTop = LOAD_SIZE * 60 // Approximate height per message
    }
  } finally {
    loadingMore.value = false
  }
}

function handleScroll(e: Event) {
  const target = e.target as HTMLElement
  // Load more when scrolled to top (within 50px)
  if (target.scrollTop < 50 && !loadingMore.value && hasMore.value) {
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
      <div v-if="!loadingMore && hasMore && messages.length > 0" class="load-more-btn">
        <n-button text size="small" @click="loadMoreMessages">
          加载更多消息
        </n-button>
      </div>
      
      <div v-if="messages.length === 0" class="empty-state">
        <n-empty description="还没有消息，发送第一条吧！" />
      </div>
      <RoomMessageItem
        v-for="message in messages"
        :key="message.id"
        :message="message"
      />
    </div>

    <div class="chat-input">
      <!-- Toolbar -->
      <div class="input-toolbar">
        <n-button text size="small" @click="toggleEmojiPicker">
          😀
        </n-button>
        <n-button text size="small" @click="triggerImageUpload">
          📷
        </n-button>
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="file-input"
          @change="handleImageUpload"
          :disabled="uploading"
        />
      </div>
      
      <!-- Input area -->
      <n-input
        v-model:value="inputText"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 4 }"
        placeholder="输入消息，按 Enter 发送..."
        @keydown="handleKeydown"
      />
      
      <n-button type="primary" @click="handleSend" :disabled="!inputText.trim()" :loading="uploading">
        发送
      </n-button>
      
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
  padding: 12px;
  min-height: 0;
}

.loading-more {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}

.load-more-btn {
  display: flex;
  justify-content: center;
  padding: 4px 0;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.chat-input {
  display: flex;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid var(--divider-color);
  align-items: flex-end;
  position: relative;
}

.input-toolbar {
  display: flex;
  gap: 4px;
}

.upload-btn {
  cursor: pointer;
}

.file-input {
  display: none;
}

.chat-input :deep(.n-input) {
  flex: 1;
}

.emoji-picker {
  position: absolute;
  bottom: 100%;
  left: 12px;
  background-color: var(--bg-card);
  border: 1px solid var(--divider-color);
  border-radius: 8px;
  padding: 8px;
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
  max-height: 200px;
  overflow-y: auto;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.emoji-item {
  font-size: 18px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  text-align: center;
}

.emoji-item:hover {
  background-color: var(--bg-primary);
}
</style>
