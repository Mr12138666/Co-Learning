<script setup lang="ts">
import { computed } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import dayjs from 'dayjs'
import type { RoomMessageResponse } from '@/api/room'

const props = defineProps<{
  message: RoomMessageResponse
}>()

const authStore = useAuthStore()

const isOwn = computed(() => props.message.userId === authStore.user?.userId)
const isSystem = computed(() => props.message.messageType === 'SYSTEM')
const isFocusStatus = computed(() => props.message.messageType === 'FOCUS_STATUS')

const formattedTime = computed(() =>
  dayjs(props.message.createdAt).format('HH:mm'),
)

const focusStatusText = computed(() => {
  if (props.message.focusStatus === 'STUDYING') return '开始专注学习'
  if (props.message.focusStatus === 'PAUSED') return '暂停了学习'
  if (props.message.focusStatus === 'IDLE') return '休息中'
  return props.message.content
})

// Check if message is an image
const imageUrl = computed(() => {
  const match = props.message.content?.match(/!\[图片\]\((.+?)\)/)
  if (!match) return null
  
  let url = match[1]
  // Convert old format http://localhost:9000/bucket/key to /api/storage/proxy/bucket/key
  if (url.startsWith('http://localhost:9000/')) {
    url = url.replace('http://localhost:9000/', '/api/storage/proxy/')
  }
  // Convert old Vite proxy format /storage/bucket/key to /api/storage/proxy/bucket/key
  if (url.startsWith('/storage/')) {
    url = url.replace('/storage/', '/api/storage/proxy/')
  }
  return url
})
</script>

<template>
  <div v-if="isSystem" class="message-system">
    <span class="system-text">{{ message.displayName }} {{ message.content }}</span>
  </div>

  <div v-else-if="isFocusStatus" class="message-focus-status">
    <span class="focus-icon">{{ message.focusStatus === 'STUDYING' ? '📚' : '⏸️' }}</span>
    <span class="focus-text">{{ message.displayName }} {{ focusStatusText }}</span>
    <span class="focus-time">{{ formattedTime }}</span>
  </div>

  <div v-else class="message-item" :class="{ own: isOwn }">
    <!-- Avatar -->
    <n-avatar round size="large" :src="message.avatarUrl || undefined" class="message-avatar">
      {{ message.displayName?.charAt(0) }}
    </n-avatar>

    <!-- Content -->
    <div class="message-content-wrapper">
      <span class="message-author">{{ message.displayName }}</span>
      <div class="message-bubble" :class="{ own: isOwn }">
        <img v-if="imageUrl" :src="imageUrl" class="message-image" />
        <span v-else class="message-text">{{ message.content }}</span>
      </div>
      <span class="message-time">{{ formattedTime }}</span>
    </div>
  </div>
</template>

<style scoped>
.message-system {
  display: flex;
  justify-content: center;
  margin: 8px 0;
}

.system-text {
  font-size: 12px;
  color: var(--text-tertiary);
  background-color: var(--bg-sunken);
  padding: 4px 12px;
  border-radius: 12px;
}

.message-focus-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 0;
  font-size: 12px;
  color: var(--text-tertiary);
}

.focus-icon {
  font-size: 14px;
}

.message-item {
  display: flex;
  gap: 8px;
  margin: 16px 0;
  align-items: flex-start;
}

.message-item.own {
  flex-direction: row-reverse;
}

.message-avatar {
  flex-shrink: 0;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 70%;
  gap: 4px;
}

.message-item.own .message-content-wrapper {
  align-items: flex-end;
}

.message-author {
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.message-bubble {
  padding: 8px 12px;
  word-break: break-word;
  border-radius: 0 8px 8px 8px;
  background-color: var(--bg-card);
  box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-default);
}

.message-bubble.own {
  border-radius: 8px 0 8px 8px;
  background-color: var(--accent);
  border-color: var(--accent);
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
  color: var(--text-primary);
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: 4px;
  object-fit: cover;
}

.message-bubble.own .message-text {
  color: #fff;
}

.message-time {
  font-size: 10px;
  color: var(--text-tertiary);
}
</style>
