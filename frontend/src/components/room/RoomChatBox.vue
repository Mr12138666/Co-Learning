<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import RoomMessageItem from './RoomMessageItem.vue'

const props = defineProps<{
  messages: any[]
}>()

const emit = defineEmits<{
  send: [content: string]
}>()

const inputText = ref('')
const scrollContainer = ref<HTMLElement | null>(null)

async function scrollToBottom() {
  await nextTick()
  if (scrollContainer.value) {
    scrollContainer.value.scrollTop = scrollContainer.value.scrollHeight
  }
}

watch(() => props.messages.length, () => {
  scrollToBottom()
})

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
</script>

<template>
  <div class="chat-box">
    <div class="chat-messages" ref="scrollContainer">
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
      <n-input
        v-model:value="inputText"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 4 }"
        placeholder="输入消息，按 Enter 发送..."
        @keydown="handleKeydown"
      />
      <n-button type="primary" @click="handleSend" :disabled="!inputText.trim()">
        发送
      </n-button>
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
}

.chat-input :deep(.n-input) {
  flex: 1;
}
</style>
