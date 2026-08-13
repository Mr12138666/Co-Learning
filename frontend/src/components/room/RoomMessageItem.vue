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
    <n-avatar round size="small" :src="message.avatarUrl || undefined" class="message-avatar">
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
  margin: var(--sp-2) 0;
}

.system-text {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  background: var(--brand-subtle);
  border: 1px solid var(--brand-subtle);
  padding: var(--sp-1) var(--sp-3);
  border-radius: var(--radius-pill);
}

.message-focus-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--sp-1);
  padding: var(--sp-2) 0;
  font-size: var(--text-xs);
  color: var(--text-color-muted);
}

.focus-icon {
  font-size: var(--text-sm);
}

.message-item {
  display: flex;
  gap: var(--sp-2);
  margin: var(--sp-3) 0;
  align-items: flex-start;
  animation: msg-pop 0.3s var(--ease-enter);
}

@keyframes msg-pop {
  from {
    opacity: 0;
    transform: translateY(8px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
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
  gap: 2px;
}

.message-item.own .message-content-wrapper {
  align-items: flex-end;
}

.message-author {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  font-weight: var(--weight-medium);
  padding: 0 var(--sp-1);
}

.message-bubble {
  padding: var(--sp-2) var(--sp-3);
  word-break: break-word;
  border-radius: var(--radius-sm) var(--radius-md) var(--radius-md) var(--radius-md);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
}

.message-bubble.own {
  border-radius: var(--radius-md) var(--radius-sm) var(--radius-md) var(--radius-md);
  background: linear-gradient(135deg, var(--brand), var(--accent-600));
  border-color: transparent;
  box-shadow: 0 4px 14px rgba(79, 140, 255, 0.25);
}

.message-text {
  font-size: var(--text-sm);
  line-height: var(--leading-normal);
  color: var(--text-color);
}

.message-bubble.own .message-text {
  color: var(--ink-on-accent);
}

.message-image {
  max-width: 200px;
  max-height: 200px;
  border-radius: var(--radius-xs);
  object-fit: cover;
}

.message-time {
  font-size: var(--text-xs);
  color: var(--text-color-muted);
  padding: 0 var(--sp-1);
}
</style>
