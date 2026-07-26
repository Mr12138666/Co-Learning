<script setup lang="ts">
import { NResult, NButton } from 'naive-ui'

defineProps<{
  title?: string
  description?: string
}>()

const emit = defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="state-error">
    <NResult
      status="error"
      :title="title || '加载失败'"
      :description="description || '请检查网络连接后重试'"
    >
      <template #footer>
        <NButton type="primary" @click="emit('retry')">
          重新加载
        </NButton>
      </template>
    </NResult>
  </div>
</template>

<style scoped>
.state-error {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--sp-10) var(--sp-6);
  min-height: 200px;
}
</style>
