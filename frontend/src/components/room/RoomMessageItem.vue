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
</script>

<template>
  <div v-if="isSystem" class="message-system">
    <n-divider>
      <span class="system-text">{{ message.displayName }} {{ message.content }}</span>
    </n-divider>
  </div>

  <div v-else-if="isFocusStatus" class="message-focus-status">
    <span class="focus-icon">{{ message.focusStatus === 'STUDYING' ? '📚' : '⏸️' }}</span>
    <span class="focus-text">{{ message.displayName }} {{ focusStatusText }}</span>
    <span class="focus-time">{{ formattedTime }}</span>
  </div>

  <div v-else class="message-item" :class="{ own: isOwn }">
    <n-avatar round size="small" :src="message.avatarUrl || undefined" v-if="!isOwn">
      {{ message.displayName?.charAt(0) }}
    </n-avatar>

    <div class="message-content-wrapper">
      <div class="message-meta" v-if="!isOwn">
        <span class="message-author">{{ message.displayName }}</span>
      </div>
      <div class="message-bubble" :class="{ own: isOwn }">
        <span class="message-text">{{ message.content }}</span>
      </div>
      <span class="message-time">{{ formattedTime }}</span>
    </div>

    <n-avatar round size="small" :src="message.avatarUrl || undefined" v-if="isOwn">
      {{ message.displayName?.charAt(0) }}
    </n-avatar>
  </div>
</template>

<style scoped>
.message-system {
  margin: 4px 0;
}

.system-text {
  font-size: 12px;
  color: var(--text-color-3);
}

.message-focus-status {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 4px 0;
  font-size: 12px;
  color: var(--text-color-3);
}

.focus-icon {
  font-size: 14px;
}

.message-item {
  display: flex;
  gap: 8px;
  margin: 8px 0;
  align-items: flex-start;
}

.message-item.own {
  flex-direction: row-reverse;
}

.message-content-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-item.own .message-content-wrapper {
  align-items: flex-end;
}

.message-meta {
  margin-bottom: 2px;
}

.message-author {
  font-size: 12px;
  color: var(--text-color-3);
}

.message-bubble {
  padding: 8px 12px;
  border-radius: 12px;
  background-color: var(--tag-color);
  word-break: break-word;
}

.message-bubble.own {
  background-color: var(--primary-color);
  color: white;
}

.message-text {
  font-size: 14px;
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  color: var(--text-color-3);
  margin-top: 2px;
}
</style>
